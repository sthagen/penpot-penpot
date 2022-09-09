;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.common.types.components-list
  (:require
    [app.common.data :as d]))

(defn components-seq
  [file-data]
  (vals (:components file-data)))

(defn add-component
  [file-data id name path main-instance-id main-instance-page shapes]
  (let [components-v2 (get-in file-data [:options :components-v2])]
    (cond-> file-data
      :always
      (assoc-in [:components id]
                {:id id
                 :name name
                 :path path
                 :objects (d/index-by :id shapes)})

      components-v2
      (update-in [:components id] assoc :main-instance-id main-instance-id
                                        :main-instance-page main-instance-page))))

(defn get-component
  [file-data component-id]
  (get-in file-data [:components component-id]))

(defn update-component
  [file-data component-id f]
  (update-in file-data [:components component-id] f))

(defn delete-component
  [file-data component-id]
  (update file-data :components dissoc component-id))

