; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.workspace.viewport.hooks
  (:require
   [app.common.geom.shapes :as gsh]
   [app.common.pages :as cp]
   [app.common.pages.helpers :as cph]
   [app.common.uuid :as uuid]
   [app.main.data.shortcuts :as dsc]
   [app.main.data.workspace :as dw]
   [app.main.data.workspace.path.shortcuts :as psc]
   [app.main.data.workspace.shortcuts :as wsc]
   [app.main.store :as st]
   [app.main.streams :as ms]
   [app.main.ui.hooks :as hooks]
   [app.main.ui.workspace.shapes.frame.dynamic-modifiers :as sfd]
   [app.main.ui.workspace.viewport.actions :as actions]
   [app.main.ui.workspace.viewport.utils :as utils]
   [app.main.worker :as uw]
   [app.util.dom :as dom]
   [app.util.globals :as globals]
   [app.util.timers :as timers]
   [beicon.core :as rx]
   [goog.events :as events]
   [rumext.alpha :as mf])
  (:import goog.events.EventType))

(defn setup-dom-events [viewport-ref overlays-ref zoom disable-paste in-viewport?]
  (let [on-key-down       (actions/on-key-down)
        on-key-up         (actions/on-key-up)
        on-mouse-move     (actions/on-mouse-move viewport-ref zoom)
        on-mouse-wheel    (actions/on-mouse-wheel viewport-ref overlays-ref zoom)
        on-paste          (actions/on-paste disable-paste in-viewport?)]
    (mf/use-layout-effect
     (mf/deps on-key-down on-key-up on-mouse-move on-mouse-wheel on-paste)
     (fn []
       (let [node (mf/ref-val viewport-ref)
             keys [(events/listen js/document EventType.KEYDOWN on-key-down)
                   (events/listen js/document EventType.KEYUP on-key-up)
                   (events/listen node EventType.MOUSEMOVE on-mouse-move)
                   ;; bind with passive=false to allow the event to be cancelled
                   ;; https://stackoverflow.com/a/57582286/3219895
                   (events/listen js/window EventType.WHEEL on-mouse-wheel #js {:passive false})
                   (events/listen js/window EventType.PASTE on-paste)]]

         (fn []
           (doseq [key keys]
             (events/unlistenByKey key))))))))

(defn setup-viewport-size [viewport-ref]
  (mf/use-layout-effect
   (fn []
     (let [node (mf/ref-val viewport-ref)
           prnt (dom/get-parent node)
           size (dom/get-client-size prnt)]
       ;; We schedule the event so it fires after `initialize-page` event
       (timers/schedule #(st/emit! (dw/initialize-viewport size)))))))

(defn setup-cursor [cursor alt? mod? space? panning drawing-tool drawing-path? path-editing?]
  (mf/use-effect
   (mf/deps @cursor @alt? @mod? @space? panning drawing-tool drawing-path? path-editing?)
   (fn []
     (let [show-pen? (or (= drawing-tool :path)
                         (and drawing-path?
                              (not= drawing-tool :curve)))
           new-cursor
           (cond
             (and @mod? @space?)            (utils/get-cursor :zoom)
             (or panning @space?)            (utils/get-cursor :hand)
             (= drawing-tool :comments)      (utils/get-cursor :comments)
             (= drawing-tool :frame)         (utils/get-cursor :create-artboard)
             (= drawing-tool :rect)          (utils/get-cursor :create-rectangle)
             (= drawing-tool :circle)        (utils/get-cursor :create-ellipse)
             show-pen?                       (utils/get-cursor :pen)
             (= drawing-tool :curve)         (utils/get-cursor :pencil)
             drawing-tool                    (utils/get-cursor :create-shape)
             (and @alt? (not path-editing?)) (utils/get-cursor :duplicate)
             :else                           (utils/get-cursor :pointer-inner))]

       (when (not= @cursor new-cursor)
         (reset! cursor new-cursor))))))

(defn setup-keyboard [alt? mod? space?]
  (hooks/use-stream ms/keyboard-alt #(reset! alt? %))
  (hooks/use-stream ms/keyboard-mod #(reset! mod? %))
  (hooks/use-stream ms/keyboard-space #(reset! space? %)))

(defn group-empty-space?
  "Given a group `group-id` check if `hover-ids` contains any of its children. If it doesn't means
  we're hovering over empty space for the group "
  [group-id objects hover-ids]

  (and (contains? #{:group :bool} (get-in objects [group-id :type]))

       ;; If there are no children in the hover-ids we're in the empty side
       (->> hover-ids
            (remove #(contains? #{:group :bool} (get-in objects [% :type])))
            (some #(cph/is-parent? objects % group-id))
            (not))))

(defn setup-hover-shapes [page-id move-stream objects transform selected mod? hover hover-ids hover-disabled? focus zoom]
  (let [;; We use ref so we don't recreate the stream on a change
        zoom-ref (mf/use-ref zoom)
        mod-ref (mf/use-ref @mod?)
        transform-ref (mf/use-ref nil)
        selected-ref (mf/use-ref selected)
        hover-disabled-ref (mf/use-ref hover-disabled?)
        focus-ref (mf/use-ref focus)

        query-point
        (mf/use-callback
         (mf/deps page-id)
         (fn [point]
           (let [zoom (mf/ref-val zoom-ref)
                 mod? (mf/ref-val mod-ref)
                 rect (gsh/center->rect point (/ 5 zoom) (/ 5 zoom))]
             (if (mf/ref-val hover-disabled-ref)
               (rx/of nil)
               (uw/ask-buffered!
                 {:cmd :selection/query
                  :page-id page-id
                  :rect rect
                  :include-frames? true
                  :clip-children? (not mod?)
                  :reverse? true}))))) ;; we want the topmost shape to be selected first

        over-shapes-stream
        (mf/use-memo
          (fn []
            (rx/merge
             (->> move-stream
                  ;; When transforming shapes we stop querying the worker
                  (rx/filter #(not (some? (mf/ref-val transform-ref))))
                  (rx/merge-map query-point))

             (->> move-stream
                  ;; When transforming shapes we stop querying the worker
                  (rx/filter #(some? (mf/ref-val transform-ref)))
                  (rx/map (constantly nil))))))]

    ;; Refresh the refs on a value change
    (mf/use-effect
     (mf/deps transform)
     #(mf/set-ref-val! transform-ref transform))

    (mf/use-effect
     (mf/deps zoom)
     #(mf/set-ref-val! zoom-ref zoom))

    (mf/use-effect
     (mf/deps @mod?)
     #(mf/set-ref-val! mod-ref @mod?))

    (mf/use-effect
     (mf/deps selected)
     #(mf/set-ref-val! selected-ref selected))

    (mf/use-effect
     (mf/deps hover-disabled?)
     #(mf/set-ref-val! hover-disabled-ref hover-disabled?))

    (mf/use-effect
     (mf/deps focus)
     #(mf/set-ref-val! focus-ref focus))

    (hooks/use-stream
     over-shapes-stream
     (mf/deps page-id objects)
     (fn [ids]
       (let [is-group?
             (fn [id]
               (contains? #{:group :bool} (get-in objects [id :type])))

             selected (mf/ref-val selected-ref)
             focus (mf/ref-val focus-ref)

             mod? (mf/ref-val mod-ref)

             remove-xfm (mapcat #(cph/get-parent-ids objects %))
             remove-id? (cond-> (into #{} remove-xfm selected)
                          (not mod?)
                          (into (filter #(group-empty-space? % objects ids)) ids)

                          mod?
                          (into (filter is-group?) ids))

             hover-shape (->> ids
                              (filter (comp not remove-id?))
                              (filter #(or (empty? focus)
                                           (cp/is-in-focus? objects focus %)))
                              (first)
                              (get objects))]
         (reset! hover hover-shape)
         (reset! hover-ids ids))))))

(defn setup-viewport-modifiers
  [modifiers objects]
  (let [root-frame-ids
        (mf/use-memo
         (mf/deps objects)
         (fn []
           (let [frame? (into #{} (cph/get-frames-ids objects))
                 ;; Removes from zero/shapes attribute all the frames so we can ask only for
                 ;; the non-frame children
                 objects (-> objects
                             (update-in [uuid/zero :shapes] #(filterv (comp not frame?) %)))]
             (cph/get-children-ids objects uuid/zero))))
        modifiers (select-keys modifiers root-frame-ids)]
    (sfd/use-dynamic-modifiers objects globals/document modifiers)))

(defn inside-vbox [vbox objects frame-id]
  (let [frame (get objects frame-id)]
    (and (some? frame) (gsh/overlaps? frame vbox))))

(defn setup-active-frames
  [objects hover-ids selected active-frames zoom transform vbox]

  (let [frame?                 #(= :frame (get-in objects [% :type]))
        all-frames             (mf/use-memo (mf/deps objects) #(cph/get-frames-ids objects))
        selected-frames        (mf/use-memo (mf/deps selected) #(->> all-frames (filter selected)))
        xf-selected-frame      (comp (remove frame?) (map #(get-in objects [% :frame-id])))
        selected-shapes-frames (mf/use-memo (mf/deps selected) #(into #{} xf-selected-frame selected))

        active-selection       (when (and (not= transform :move) (= (count selected-frames) 1)) (first selected-frames))
        hover-frame            (last @hover-ids)
        last-hover-frame       (mf/use-var nil)]

    (mf/use-effect
     (mf/deps hover-frame)
     (fn []
       (when (some? hover-frame)
         (reset! last-hover-frame hover-frame))))

    (mf/use-effect
     (mf/deps objects @hover-ids selected zoom transform vbox)
     (fn []

       ;; Rules for active frame:
       ;; - If zoom < 25% displays thumbnail except when selecting a single frame or a child
       ;; - We always active the current hovering frame for zoom > 25%
       ;; - When zoom > 130% we activate the frames that are inside the vbox
       ;; - If no hovering over any frames we keep the previous active one
       ;; - Check always that the active frames are inside the vbox

       (let [is-active-frame?
             (fn [id]
               (or
                ;; Zoom > 130% shows every frame
                (> zoom 1.3)

                ;; Zoom >= 25% will show frames hovering
                (and
                 (>= zoom 0.25)
                 (or (= id hover-frame) (= id @last-hover-frame)))

                ;; Otherwise, if it's a selected frame
                (= id active-selection)

                ;; Or contains a selected shape
                (contains? selected-shapes-frames id)))

             new-active-frames
             (into #{}
                   (comp (filter is-active-frame?)

                         ;; We only allow active frames that are contained in the vbox
                         (filter (partial inside-vbox vbox objects)))
                   all-frames)]

         (when (not= @active-frames new-active-frames)
           (reset! active-frames new-active-frames)))))))

;; NOTE: this is executed on each page change, maybe we need to move
;; this shortcuts outside the viewport?

(defn setup-shortcuts
  [path-editing? drawing-path?]
  (hooks/use-shortcuts ::workspace wsc/shortcuts)
  (mf/use-effect
   (mf/deps path-editing? drawing-path?)
   (fn []
     (when (or drawing-path? path-editing?)
       (st/emit! (dsc/push-shortcuts ::path psc/shortcuts))
       #(st/emit! (dsc/pop-shortcuts ::path))))))
