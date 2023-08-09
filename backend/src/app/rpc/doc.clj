;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.rpc.doc
  "API autogenerated documentation."
  (:require
   [app.common.data :as d]
   [app.common.exceptions :as ex]
   [app.common.pprint :as pp]
   [app.common.schema :as sm]
   [app.common.schema.desc-js-like :as smdj]
   [app.common.schema.desc-native :as smdn]
   [app.common.schema.openapi :as oapi]
   [app.common.schema.registry :as sr]
   [app.config :as cf]
   [app.loggers.webhooks :as-alias webhooks]
   [app.rpc :as-alias rpc]
   [app.util.json :as json]
   [app.util.services :as sv]
   [app.util.template :as tmpl]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [cuerdas.core :as str]
   [integrant.core :as ig]
   [malli.transform :as mt]
   [pretty-spec.core :as ps]
   [yetti.response :as yrs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DOC (human readable)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- prepare-doc-context
  [methods]
  (letfn [(fmt-spec [mdata]
            (when-let [spec (ex/ignoring (s/spec (::sv/spec mdata)))]
              (with-out-str
                (ps/pprint (s/form spec)
                           {:ns-aliases {"clojure.spec.alpha" "s"
                                         "clojure.core.specs.alpha" "score"
                                         "clojure.core" nil}}))))

          (fmt-schema [type mdata key]
            (when-let [schema (get mdata key)]
              (if (= type :js)
                (smdj/describe (sm/schema schema) {::smdj/max-level 4})
                (-> (smdn/describe (sm/schema schema))
                    (pp/pprint-str {:level 5 :width 70})))))

          (get-context [mdata]
            {:name (::sv/name mdata)
             :module (or (some-> (::module mdata) d/name)
                         (-> (:ns mdata) (str/split ".") last))
             :auth (::rpc/auth mdata true)
             :webhook (::webhooks/event? mdata false)
             :docs (::sv/docstring mdata)
             :deprecated (::deprecated mdata)
             :added (::added mdata)
             :changes (some->> (::changes mdata) (partition-all 2) (map vec))
             :spec (fmt-spec mdata)
             :entrypoint (str (cf/get :public-uri) "/api/rpc/command/" (::sv/name mdata))

             :params-schema-js   (fmt-schema :js mdata ::sm/params)
             :result-schema-js   (fmt-schema :js mdata ::sm/result)
             :webhook-schema-js  (fmt-schema :js mdata ::sm/webhook)
             :params-schema-clj  (fmt-schema :clj mdata ::sm/params)
             :result-schema-clj  (fmt-schema :clj mdata ::sm/result)
             :webhook-schema-clj (fmt-schema :clj mdata ::sm/webhook)})]

    {:version (:main cf/version)
     :methods
     (->> methods
          (map val)
          (map first)
          (remove ::skip)
          (map get-context)
          (sort-by (juxt :module :name)))}))

(defn- doc-handler
  [context]
  (if (contains? cf/flags :backend-api-doc)
    (fn [request]
      (let [params  (:query-params request)
            pstyle  (:type params "js")
            context (assoc context :param-style pstyle)]
        {::yrs/status 200
         ::yrs/body (-> (io/resource "app/templates/api-doc.tmpl")
                        (tmpl/render context))}))
    (fn [_]
      {::yrs/status 404})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OPENAPI / SWAGGER (v3.1)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def output-transformer
  (mt/transformer
   sm/default-transformer
   (mt/key-transformer {:encode str/camel
                        :decode (comp keyword str/kebab)})))

(defn prepare-openapi-context
  [methods]
  (letfn [(gen-response-doc [tsx schema]
            (let [schema  (sm/schema schema)
                  example (sm/generate schema)
                  example (sm/encode schema example output-transformer)]
              {:default
               {:description "A default response"
                :content
                {"application/json"
                 {:schema tsx
                  :example example}}}}))

          (gen-params-doc [tsx schema]
            (let [example (sm/generate schema)
                  example (sm/encode schema example output-transformer)]
              {:required true
               :content
               {"application/json"
                {:schema tsx
                 :example example}}}))

          (gen-method-doc [options mdata]
            (let [pschema (::sm/params mdata)
                  rschema (::sm/result mdata)

                  sparams (-> pschema (oapi/transform options) (gen-params-doc pschema))
                  sresp   (some-> rschema (oapi/transform options) (gen-response-doc rschema))

                  rpost   {:description (::sv/docstring mdata)
                           :deprecated (::deprecated mdata false)
                           :requestBody sparams}

                  rpost  (cond-> rpost
                           (some? sresp)
                           (assoc :responses sresp))]

              {:name (-> mdata ::sv/name d/name)
               :module (-> (:ns mdata) (str/split ".") last)
               :repr {:post rpost}}))
          ]

    (let [definitions (atom {})
          options {:registry sr/default-registry
                   ::oapi/definitions-path "#/components/schemas/"
                   ::oapi/definitions definitions}

          paths   (binding [oapi/*definitions* definitions]
                    (->> methods
                         (map (comp first val))
                         (filter ::sm/params)
                         (map (partial gen-method-doc options))
                         (sort-by (juxt :module :name))
                         (map (fn [doc]
                                [(str/ffmt "/command/%" (:name doc)) (:repr doc)]))
                         (into {})))]
    {:openapi "3.0.0"
     :info {:version (:main cf/version)}
     :servers [{:url (str/ffmt "%/api/rpc" (cf/get :public-uri))
                ;; :description "penpot backend"
                }]
     :security
     {:api_key []}

     :paths paths
     :components {:schemas @definitions}})))

(defn openapi-json-handler
  [context]
  (if (contains? cf/flags :backend-openapi-doc)
    (fn [_]
      {::yrs/status 200
       ::yrs/headers {"content-type" "application/json; charset=utf-8"}
       ::yrs/body (json/encode context)})

    (fn [_]
      {::yrs/status 404})))

(defn openapi-handler
  []
  (if (contains? cf/flags :backend-openapi-doc)
    (fn [_]
      (let [swagger-js (slurp (io/resource "app/assets/swagger-ui-4.18.3.js"))
            swagger-cs (slurp (io/resource "app/assets/swagger-ui-4.18.3.css"))
            context    {:public-uri (cf/get :public-uri)
                        :swagger-js swagger-js
                        :swagger-css swagger-cs}]
        {::yrs/status 200
         ::yrs/headers {"content-type" "text/html"}
         ::yrs/body (-> (io/resource "app/templates/openapi.tmpl")
                        (tmpl/render context))}))
    (fn [_]
      {::yrs/status 404})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MODULE INIT
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::routes vector?)

(defmethod ig/pre-init-spec ::routes [_]
  (s/keys :req-un [::rpc/methods]))

(defmethod ig/init-key ::routes
  [_ {:keys [methods] :as cfg}]
  [(let [context (prepare-doc-context methods)]
     [["/_doc"
       {:handler (doc-handler context)
        :allowed-methods #{:get}}]
      ["/doc"
       {:handler (doc-handler context)
        :allowed-methods #{:get}}]])

   (let [context (prepare-openapi-context methods)]
     [["/openapi"
       {:handler (openapi-handler)
        :allowed-methods #{:get}}]
      ["/openapi.json"
       {:handler (openapi-json-handler context)
        :allowed-methods #{:get}}]])])
