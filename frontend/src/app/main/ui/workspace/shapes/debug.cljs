;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.workspace.shapes.debug
  (:require
   [app.common.data :as d]
   [app.common.data.macros :as dm]
   [app.common.files.helpers :as cfh]
   [app.common.geom.shapes :as gsh]
   [app.common.geom.shapes.text :as gst]
   [app.common.math :as mth]
   [app.main.refs :as refs]
   [app.util.color :as uc]
   [app.util.debug :as dbg]
   [app.util.dom :as dom]
   [cuerdas.core :as str]
   [rumext.v2 :as mf]))

(mf/defc debug-bounding-boxes
  [{:keys [shape]}]
  (let [points (->> (:points shape)
                    (map #(dm/fmt "%,%" (dm/get-prop % :x) (dm/get-prop % :y)))
                    (str/join " "))
        color (mf/use-memo #(uc/random-color))
        sr (:selrect shape)]
    [:g.debug-bounding-boxes
     [:rect {:transform (gsh/transform-str shape)
             :x (:x sr)
             :y (:y sr)
             :width (:width sr)
             :height (:height sr)
             :fill color
             :opacity 0.2}]
     (for [p (:points shape)]
       [:circle {:cx (dm/get-prop p :x)
                 :cy (dm/get-prop p :y)
                 :r 2
                 :fill color}])
     [:polygon {:points points
                :stroke-width 1
                :stroke color}]]))

(mf/defc debug-text-bounds
  {::mf/wrap-props false}
  [props]
  (let [shape (unchecked-get props "shape")
        zoom (mf/deref refs/selected-zoom)
        bounding-box (gst/shape->rect shape)
        ctx (js* "document.createElement(\"canvas\").getContext(\"2d\")")]
    [:g {:transform (gsh/transform-str shape)}
     [:rect {:x (:x bounding-box)
             :y (:y bounding-box)
             :width (:width bounding-box)
             :height (:height bounding-box)
             :style {:fill "none"
                     :stroke "orange"
                     :stroke-width (/ 1 zoom)}}]

     (for [[index data] (d/enumerate (:position-data shape))]
       (let [{:keys [x y width height]} data
             res (dom/measure-text ctx (:font-size data) (:font-family data) (:text data))]
         [:g {:key (dm/str index)}
          ;; Text fragment bounding box
          [:rect {:x x
                  :y (- y height)
                  :width width
                  :height height
                  :style {:fill "none"
                          :stroke "red"
                          :stroke-width (/ 1 zoom)}}]

          ;; Text baseline
          [:line {:x1 (mth/round x)
                  :y1 (mth/round (- (:y data) (:height data)))
                  :x2 (mth/round (+ x width))
                  :y2 (mth/round (- (:y data) (:height data)))
                  :style {:stroke "blue"
                          :stroke-width (/ 1 zoom)}}]

          [:line {:x1 (:x data)
                  :y1 (- (:y data) (:descent res))
                  :x2 (+ (:x data) (:width data))
                  :y2 (- (:y data) (:descent res))
                  :style {:stroke "green"
                          :stroke-width (/ 2 zoom)}}]]))]))

(mf/defc shape-debug
  [{:keys [shape]}]
  [:*
   (when ^boolean (dbg/enabled? :bounding-boxes)
     [:& debug-bounding-boxes])

   (when (and ^boolean (cfh/text-shape? shape)
              ^boolean (dbg/enabled? :text-outline)
              ^boolean (seq (:position-data shape)))
     [:& debug-text-bounds {:shape shape}])])
