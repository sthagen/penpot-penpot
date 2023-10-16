;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.workspace.sidebar.options.menus.frame-grid
  (:require-macros [app.main.style :as stl])
  (:require
   [app.common.geom.grid :as gg]
   [app.main.data.workspace.grid :as dw]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.components.editable-select :refer [editable-select]]
   [app.main.ui.components.numeric-input :refer [numeric-input*]]
   [app.main.ui.components.select :refer [select]]
   [app.main.ui.components.title-bar :refer [title-bar]]
   [app.main.ui.context :as ctx]
   [app.main.ui.icons :as i]
   [app.main.ui.workspace.sidebar.options.common :refer [advanced-options]]
   [app.main.ui.workspace.sidebar.options.rows.color-row :refer [color-row]]
   [app.main.ui.workspace.sidebar.options.rows.input-row :refer [input-row input-row-v2]]
   [app.util.i18n :as i18n :refer [tr]]
   [okulary.core :as l]
   [rumext.v2 :as mf]))

(def workspace-saved-grids
  (l/derived :saved-grids refs/workspace-page-options))

(defn- get-size-options []
  [{:value nil :label (tr "workspace.options.grid.auto")}
   :separator
   18 12 10 8 6 4 3 2])

(mf/defc grid-options
  {::mf/wrap [mf/memo]}
  [{:keys [shape-id index grid frame-width frame-height default-grid-params]}]
  (let [new-css-system      (mf/use-ctx ctx/new-css-system)
        on-change           (mf/use-fn (mf/deps shape-id index) #(st/emit! (dw/set-frame-grid shape-id index %)))
        on-remove           (mf/use-fn (mf/deps shape-id index) #(st/emit! (dw/remove-frame-grid shape-id index)))
        on-save-default     (mf/use-fn #(st/emit! (dw/set-default-grid (:type %) (:params %))))

        size-options        (mf/use-memo get-size-options)
        state*              (mf/use-state {:show-advanced-options false
                                           :show-more-options false})
        state               (deref state*)

        open?              (:show-advanced-options state)
        show-more-options? (:show-more-options state)

        is-hidden? (not (:display grid))

        {:keys [type display params]} grid

        toggle-advanced-options
        (mf/use-fn #(swap! state* update :show-advanced-options not))

        toggle-more-options
        (mf/use-fn #(swap! state* update :show-more-options not))

        handle-toggle-visibility
        (mf/use-fn
         (mf/deps grid)
         (fn [_]
           (on-change (update grid :display #(if (nil? %) false (not %))))))

        handle-change-type
        (mf/use-fn
         (mf/deps grid)
         (fn [grid-type]
           (let [defaults (grid-type default-grid-params)]
             (on-change (assoc grid
                               :type grid-type
                               :params defaults)))))
        handle-change
        (fn [& keys-path]
          (fn [value]
            (on-change (assoc-in grid keys-path value))))

        handle-change-size
        (mf/use-fn
         (mf/deps grid)
         (fn [size]
           (let [{:keys [margin gutter item-length]} (:params grid)
                 frame-length (if (= :column (:type grid)) frame-width frame-height)
                 item-length  (if (nil? size)
                                (gg/calculate-default-item-length frame-length margin gutter)
                                item-length)]
             (-> grid
                 (update :params assoc :size size :item-length item-length)
                 (on-change)))))

        handle-change-item-length
        (mf/use-fn
         (mf/deps grid)
         (fn [item-length]
           (let [item-length (if (zero? item-length) nil item-length)
                 size (get-in grid [:params :size])
                 size (if (and (nil? item-length) (nil? size)) 12 size)]
             (-> grid
                 (update :params assoc :size size :item-length item-length)
                 (on-change)))))

        handle-change-color
        (mf/use-fn
         (mf/deps grid)
         (fn [color]
           (-> grid
               (update :params assoc :color color)
               (on-change))))

        handle-detach-color
        (mf/use-fn
         (mf/deps grid)
         (fn []
           (-> grid
               (update-in [:params :color] dissoc :id :file-id)
               (on-change))))

        handle-use-default
        (mf/use-fn
         (mf/deps grid)
         (fn []
           (let [params ((:type grid) default-grid-params)
                 color (or (get-in params [:color :value]) (get-in params [:color :color]))
                 params (-> params
                            (assoc-in [:color :color] color)
                            (update :color dissoc :value))]
             (when on-change
               (on-change (assoc grid :params params))))))

        handle-set-as-default
        (mf/use-fn (mf/deps grid) #(on-save-default grid))

        is-default (= (->> grid :params)
                      (->> grid :type default-grid-params))]

    (if new-css-system
      [:div {:class (stl/css :grid-option)}
       [:div {:class (stl/css :grid-title)}
        [:div {:class (stl/css-case :option-row true
                                    :hidden is-hidden?)}
         [:button {:class (stl/css-case :show-options true
                                        :selected open?)
                   :on-click toggle-advanced-options}
          i/menu-refactor]
         [:div {:class (stl/css :type-select-wrapper)}
          [:& select
           {:class (stl/css :grid-type-select)
            :default-value type
            :options [{:value :square :label (tr "workspace.options.grid.square")}
                      {:value :column :label (tr "workspace.options.grid.column")}
                      {:value :row :label (tr "workspace.options.grid.row")}]
            :on-change handle-change-type}]]
         (if (= type :square)
           [:div {:class (stl/css :grid-size)
                  :title (tr "workspace.options.size")}
            [:> numeric-input* {:min 0.01
                                :value (or (:size params) "")
                                :no-validate true
                                :className (stl/css :numeric-input)
                                :on-change (handle-change :params :size)}]]

           [:div {:class (stl/css :editable-select-wrapper)}
            [:& editable-select {:value (:size params)
                                 :type  "number"
                                 :class (stl/css :column-select)
                                 :input-class (stl/css :numeric-input)
                                 :min 1
                                 :options size-options
                                 :placeholder "Auto"
                                 :on-change handle-change-size}]])]

        [:div {:class (stl/css :actions)}
         [:button {:class (stl/css :action-btn)
                   :on-click handle-toggle-visibility}
          (if display i/shown-refactor i/hide-refactor)]
         [:button {:class (stl/css :action-btn)
                   :on-click on-remove}
          i/remove-refactor]]]

       [:& advanced-options {:class (stl/css :grid-advanced-options)
                             :visible? open?
                             :on-close toggle-advanced-options}
        ;; square
        (when (= :square type)
          [:div {:class (stl/css :square-row)}
           [:div {:class (stl/css :advanced-row)}
            [:& color-row {:color (:color params)
                           :title (tr "workspace.options.grid.params.color")
                           :disable-gradient true
                           :on-change handle-change-color
                           :on-detach handle-detach-color}]
            [:button {:class (stl/css :show-more-options)
                      :on-click toggle-more-options}
             i/menu-refactor]]
           (when show-more-options?
             [:div {:class (stl/css :second-row)}
              [:button {:class (stl/css-case :btn-options true
                                             :disabled is-default)
                        :disabled is-default
                        :on-click handle-use-default}
               [:span (tr "workspace.options.grid.params.use-default")]]
              [:button {:class (stl/css-case :btn-options true
                                             :disabled is-default)
                        :disabled is-default
                        :on-click handle-set-as-default}
               [:span (tr "workspace.options.grid.params.set-default")]]])])

        (when (or (= :column type) (= :row type))
          [:div {:class (stl/css :column-row)}
           [:div {:class (stl/css :advanced-row)}
            [:div {:class (stl/css :select-wrapper)}
             [:& select {:data-mousetrap-dont-stop true ;; makes mousetrap to not stop at this element
                         :default-value (:type params)
                         :class (stl/css :orientation)
                         :options [{:value :stretch :label (tr "workspace.options.grid.params.type.stretch")}
                                   {:value :left :label (if (= type :row)
                                                          (tr "workspace.options.grid.params.type.top")
                                                          (tr "workspace.options.grid.params.type.left"))}
                                   {:value :center :label (tr "workspace.options.grid.params.type.center")}
                                   {:value :right :label (if (= type :row)
                                                           (tr "workspace.options.grid.params.type.bottom")
                                                           (tr "workspace.options.grid.params.type.right"))}]
                         :on-change (handle-change :params :type)}]]

            [:div {:class (stl/css :color-wrapper)}
             [:& color-row {:color (:color params)
                            :title (tr "workspace.options.grid.params.color")
                            :disable-gradient true
                            :on-change handle-change-color
                            :on-detach handle-detach-color}]]]

           [:div {:class (stl/css :advanced-row)}
            [:div {:class (stl/css :height)
                   :title (if (= :row type)
                            (tr "workspace.options.grid.params.height")
                            (tr "workspace.options.grid.params.width"))}
             [:span {:class (stl/css :icon-text)}
              (if (= :row type)
                "H"
                "W")]
             [:> numeric-input* {:placeholder "Auto"
                                 :on-change handle-change-item-length
                                 :nillable true
                                 :className (stl/css :numeric-input)
                                 :value (or (:item-length params) "")}]]

            [:div {:class (stl/css :gutter)
                   :title (tr "workspace.options.grid.params.gutter")}
             [:span {:class (stl/css-case :icon true
                                          :rotated (= type :row))} i/gap-horizontal-refactor]
             [:> numeric-input* {:placeholder "0"
                                 :on-change (handle-change :params :gutter)
                                 :nillable true
                                 :className (stl/css :numeric-input)
                                 :value (or (:gutter params) 0)}]]

            [:div {:class (stl/css :margin)
                   :title (tr "workspace.options.grid.params.margin")}
             [:span {:class (stl/css-case :icon true
                                          :rotated (= type :column))} i/grid-margin-refactor]
             [:> numeric-input* {:placeholder "0"
                                 :on-change (handle-change :params :margin)
                                 :nillable true
                                 :className (stl/css :numeric-input)
                                 :value (or (:margin params) 0)}]]

            [:button {:class (stl/css :show-more-options)
                      :on-click toggle-more-options}
             i/menu-refactor]
            (when show-more-options?
              [:div {:class (stl/css :more-options)}
               [:button {:disabled is-default
                         :class (stl/css :option-btn)
                         :on-click handle-use-default} (tr "workspace.options.grid.params.use-default")]
               [:button {:disabled is-default
                         :class (stl/css :option-btn)
                         :on-click handle-set-as-default} (tr "workspace.options.grid.params.set-default")]])]])]]

      [:div.grid-option
       [:div.grid-option-main {:style {:display (when open? "none")}}
        [:button.custom-button {:class (when open? "is-active")
                                :on-click toggle-advanced-options} i/actions]
        [:& select
         {:class "flex-grow"
          :default-value type
          :options [{:value :square :label (tr "workspace.options.grid.square")}
                    {:value :column :label (tr "workspace.options.grid.column")}
                    {:value :row :label (tr "workspace.options.grid.row")}]
          :on-change handle-change-type}]

        (if (= type :square)
          [:div.input-element.pixels {:title (tr "workspace.options.size")}
           [:> numeric-input* {:min 0.01
                               :value (or (:size params) "")
                               :no-validate true
                               :on-change (handle-change :params :size)}]]

          [:& editable-select {:value (:size params)
                               :type  "number"
                               :class "input-option"
                               :min 1
                               :options size-options
                               :placeholder "Auto"
                               :on-change handle-change-size}])

        [:div.grid-option-main-actions
         [:button.custom-button {:on-click handle-toggle-visibility} (if display i/eye i/eye-closed)]
         [:button.custom-button {:on-click on-remove} i/minus]]]

       [:& advanced-options {:visible? open? :on-close toggle-advanced-options}
        [:button.custom-button {:on-click toggle-advanced-options} i/actions]
        (when (= :square type)
          [:& input-row {:label (tr "workspace.options.grid.params.size")
                         :class "pixels"
                         :min 0.01
                         :value (:size params)
                         :on-change (handle-change :params :size)}])

        (when (= :row type)
          [:& input-row {:label (tr "workspace.options.grid.params.rows")
                         :type :editable-select
                         :options size-options
                         :value (:size params)
                         :min 1
                         :placeholder "Auto"
                         :on-change handle-change-size}])

        (when (= :column type)
          [:& input-row {:label (tr "workspace.options.grid.params.columns")
                         :type :editable-select
                         :options size-options
                         :value (:size params)
                         :min 1
                         :placeholder "Auto"
                         :on-change handle-change-size}])

        (when (#{:row :column} type)
          [:& input-row {:label (tr "workspace.options.grid.params.type")
                         :type :select
                         :options [{:value :stretch :label (tr "workspace.options.grid.params.type.stretch")}
                                   {:value :left :label (if (= type :row)
                                                          (tr "workspace.options.grid.params.type.top")
                                                          (tr "workspace.options.grid.params.type.left"))}
                                   {:value :center :label (tr "workspace.options.grid.params.type.center")}
                                   {:value :right :label (if (= type :row)
                                                           (tr "workspace.options.grid.params.type.bottom")
                                                           (tr "workspace.options.grid.params.type.right"))}]
                         :value (:type params)
                         :on-change (handle-change :params :type)}])

        (when (#{:row :column} type)
          [:& input-row-v2
           {:class "pixels"
            :label (if (= :row type)
                     (tr "workspace.options.grid.params.height")
                     (tr "workspace.options.grid.params.width"))}
           [:> numeric-input*
            {:placeholder "Auto"
             :value (or (:item-length params) "")
             :nillable true
             :on-change handle-change-item-length}]])

        (when (#{:row :column} type)
          [:*
           [:& input-row {:label (tr "workspace.options.grid.params.gutter")
                          :class "pixels"
                          :value (:gutter params)
                          :min 0
                          :nillable true
                          :default 0
                          :placeholder "0"
                          :on-change (handle-change :params :gutter)}]
           [:& input-row {:label (tr "workspace.options.grid.params.margin")
                          :class "pixels"
                          :min 0
                          :nillable true
                          :default 0
                          :placeholder "0"
                          :value (:margin params)
                          :on-change (handle-change :params :margin)}]])

        [:& color-row {:color (:color params)
                       :title (tr "workspace.options.grid.params.color")
                       :disable-gradient true
                       :on-change handle-change-color
                       :on-detach handle-detach-color}]
        [:div.row-flex
         [:button.btn-options {:disabled is-default
                               :on-click handle-use-default} (tr "workspace.options.grid.params.use-default")]
         [:button.btn-options {:disabled is-default
                               :on-click handle-set-as-default} (tr "workspace.options.grid.params.set-default")]]]])))

(mf/defc frame-grid
  [{:keys [shape]}]
  (let [new-css-system      (mf/use-ctx ctx/new-css-system)
        state*              (mf/use-state true)
        open?               (deref state*)
        frame-grids         (:grids shape)
        has-frame-grids?    (or (= :multiple frame-grids) (some? (seq frame-grids)))

        toggle-content      (mf/use-fn #(swap! state* not))
        id                  (:id shape)
        saved-grids         (mf/deref workspace-saved-grids)
        default-grid-params (mf/use-memo (mf/deps saved-grids) #(merge dw/default-grid-params saved-grids))
        handle-create-grid  (mf/use-fn (mf/deps id) #(st/emit! (dw/add-frame-grid id)))]

    (if new-css-system
      [:div {:class (stl/css :element-set)}
       [:& title-bar {:collapsable? has-frame-grids?
                      :collapsed?   (not open?)
                      :on-collapsed toggle-content
                      :class (stl/css-case :title-spacing-board-grid (not has-frame-grids?))
                      :title        (tr "workspace.options.grid.grid-title")}

        [:button  {:on-click handle-create-grid
                   :class (stl/css :add-grid)}
         i/add-refactor]]

       (when (and open? (seq frame-grids))
         [:div  {:class (stl/css :element-set-content)}
          (for [[index grid] (map-indexed vector frame-grids)]
            [:& grid-options {:key (str id "-" index)
                              :shape-id id
                              :grid grid
                              :index index
                              :frame-width (:width shape)
                              :frame-height (:height shape)
                              :default-grid-params default-grid-params}])])]


      [:div.element-set
       [:div.element-set-title
        [:span (tr "workspace.options.grid.grid-title")]
        [:div.add-page {:on-click handle-create-grid} i/close]]

       (when (seq frame-grids)
         [:div.element-set-content
          (for [[index grid] (map-indexed vector frame-grids)]
            [:& grid-options {:key (str id "-" index)
                              :shape-id id
                              :grid grid
                              :index index
                              :frame-width (:width shape)
                              :frame-height (:height shape)
                              :default-grid-params default-grid-params}])])])))


