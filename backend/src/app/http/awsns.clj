;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 Andrey Antukh <niwi@niwi.nz>

(ns app.http.awsns
  "AWS SNS webhook handler for bounces."
  (:require
   [app.common.exceptions :as ex]
   [app.common.spec :as us]
   [app.db :as db]
   [app.tasks :as tasks]
   [app.util.http :as http]
   [app.util.json :as json]
   [app.util.time :as dt]
   [clojure.pprint :refer [pprint]]
   [clojure.spec.alpha :as s]
   [clojure.tools.logging :as log]
   [cuerdas.core :as str]
   [integrant.core :as ig]
   [jsonista.core :as j]))

(declare parse-json)
(declare parse-notification)
(declare process-notification)

(defn- pprint-message
  [message]
  (binding [clojure.pprint/*print-right-margin* 120]
    (with-out-str (pprint message))))

(defmethod ig/pre-init-spec ::handler [_]
  (s/keys :req-un [::db/pool]))

(defmethod ig/init-key ::handler
  [_ {:keys [pool] :as cfg}]
  (fn [request]
    (let [body  (parse-json (slurp (:body request)))

          body  {"Type" "Notification"
                 "Message" (json/encode-str body)}

          mtype (get body "Type")]
      (cond
        (= mtype "SubscriptionConfirmation")
        (let [surl   (get body "SubscribeURL")
              stopic (get body "TopicArn")]
          ;; TODO: timeout
          (log/infof "Subscription received (topic=%s, url=%s)" stopic surl)
          (http/send! {:uri surl :method :post}))

        (= mtype "Notification")
        (when-let [message (parse-json (get body "Message"))]
          ;; (log/infof "Received: %s" (pr-str message))
          (let [notification (parse-notification cfg message)]
            (process-notification cfg notification)))

        :else
        (log/warn (str "Unexpected data received.\n"
                       (pprint-message body))))

      {:status 200 :body ""})))

(defn- parse-bounce
  [data]
  {:type        :bounce
   :kind        (str/lower (get data "bounceType"))
   :category    (str/lower (get data "bounceSubType"))
   :feedback-id (get data "feedbackId")
   :timestamp   (get data "timestamp")
   :recipients  (->> (get data "bouncedRecipients")
                     (mapv (fn [item]
                             {:email (get item "emailAddress")
                              :status (get item "status")
                              :action (get item "action")
                              :dcode  (get item "diagnosticCode")})))})

(defn- parse-complaint
  [data]
  {:type          :complaint
   :user-agent    (get data "userAgent")
   :category      (get data "complaintSubType")
   :feedback-type (get data "complaintFeedbackType")
   :arrival-date  (get data "arrivalDate")
   :feedback-id   (get data "feedbackId")
   :recipients    (->> (get data "complainedRecipients")
                       (mapv #(get % "emailAddress")))})

(defn- extract-headers
  [mail]
  (reduce (fn [acc item]
            (let [key (get item "name")
                  val (get item "value")]
              (assoc acc (str/lower key) val)))
          {}
          (get mail "headers")))

(defn- extract-identity
  [{:keys [tokens] :as cfg} headers]
  (when-let [tdata (get headers "x-penpot-data")]
    (let [result (tokens :verify {:token tdata :iss :profile-identity})]
      (:profile-id result))))

(defn- parse-notification
  [cfg message]
  (let [type (get message "notificationType")
        data (case type
               "Bounce" (parse-bounce (get message "bounce"))
               "Complaint" (parse-complaint (get message "complaint"))
               {:type (keyword (str/lower type))
                :message message})]
    (when data
      (let [mail (get message "mail")]
        (when-not mail
          (ex/raise :type :internal
                    :code :incomplete-notification
                    :hint "no email data received, please enable full headers report"))
        (let [headers (extract-headers mail)
              mail    {:destination (get mail "destination")
                       :source      (get mail "source")
                       :timestamp   (get mail "timestamp")
                       :subject     (get-in mail ["commonHeaders" "subject"])
                       :headers     headers}]
          (assoc data
                 :mail mail
                 :profile-id (extract-identity cfg headers)))))))

(defn- parse-json
  [v]
  (ex/ignoring
   (j/read-value v)))

(defn- register-complaint-for-profile
  [{:keys [pool]} {:keys [type profile-id] :as message}]
  (db/insert! pool :profile-complaint
              {:profile-id profile-id
               :type (name type)
               :content (db/tjson message)}))

(defn process-notification
  [{:keys [pool] :as cfg} {:keys [type profile-id] :as message}]
  (log/debug (str "Procesing message:\n" (pprint-message message)))
  (cond
    ;; In this case we receive a bounce/complaint notification without
    ;; confirmed identity, we just emit a warning but do nothing about
    ;; it because this is not a normal case. All notifications should
    ;; come with profile identity.
    (nil? profile-id)
    (log/warn (str "A notification without identity recevied from AWS\n"
                   (pprint-message message)))

    (or (= :bounce type)
        (= :complaint type))
    (register-complaint-for-profile cfg message)

    :else
    (log/warn (str "Unrecognized message received from AWS\n"
                   (pprint-message message)))))


