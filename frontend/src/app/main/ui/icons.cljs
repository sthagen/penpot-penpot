;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.icons
  (:refer-clojure :exclude [import mask])
  (:require-macros [app.main.ui.icons :refer [icon-xref collect-icons]])
  (:require
   [app.common.data :as d]
   [cuerdas.core :as str]
   [rumext.v2 :as mf]))

;; Keep the list of icons sorted
(def ^:icon icon-verify (icon-xref :icon-verify))
(def ^:icon loader (icon-xref :loader))
(def ^:icon logo (icon-xref :penpot-logo))
(def ^:icon logo-icon (icon-xref :penpot-logo-icon))
(def ^:icon logo-error-screen (icon-xref :logo-error-screen))
(def ^:icon login-illustration (icon-xref :login-illustration))

(def ^:icon brand-openid (icon-xref :brand-openid))
(def ^:icon brand-github (icon-xref :brand-github))
(def ^:icon brand-gitlab (icon-xref :brand-gitlab))
(def ^:icon brand-google (icon-xref :brand-google))

(def ^:icon absolute (icon-xref :absolute))
(def ^:icon add (icon-xref :add))
(def ^:icon align-bottom (icon-xref :align-bottom))
(def ^:icon align-content-column-around (icon-xref :align-content-column-around))
(def ^:icon align-content-column-between (icon-xref :align-content-column-between))
(def ^:icon align-content-column-center (icon-xref :align-content-column-center))
(def ^:icon align-content-column-end (icon-xref :align-content-column-end))
(def ^:icon align-content-column-evenly (icon-xref :align-content-column-evenly))
(def ^:icon align-content-column-start (icon-xref :align-content-column-start))
(def ^:icon align-content-column-stretch (icon-xref :align-content-column-stretch))
(def ^:icon align-content-row-around (icon-xref :align-content-row-around))
(def ^:icon align-content-row-between (icon-xref :align-content-row-between))
(def ^:icon align-content-row-center (icon-xref :align-content-row-center))
(def ^:icon align-content-row-end (icon-xref :align-content-row-end))
(def ^:icon align-content-row-evenly (icon-xref :align-content-row-evenly))
(def ^:icon align-content-row-start (icon-xref :align-content-row-start))
(def ^:icon align-content-row-stretch (icon-xref :align-content-row-stretch))
(def ^:icon align-horizontal-center (icon-xref :align-horizontal-center))
(def ^:icon align-items-column-center (icon-xref :align-items-column-center))
(def ^:icon align-items-column-end (icon-xref :align-items-column-end))
(def ^:icon align-items-column-start (icon-xref :align-items-column-start))
(def ^:icon align-items-row-center (icon-xref :align-items-row-center))
(def ^:icon align-items-row-end (icon-xref :align-items-row-end))
(def ^:icon align-items-row-start (icon-xref :align-items-row-start))
(def ^:icon align-left (icon-xref :align-left))
(def ^:icon align-right (icon-xref :align-right))
(def ^:icon align-self-column-bottom (icon-xref :align-self-column-bottom))
(def ^:icon align-self-column-center (icon-xref :align-self-column-center))
(def ^:icon align-self-column-stretch (icon-xref :align-self-column-stretch))
(def ^:icon align-self-column-top (icon-xref :align-self-column-top))
(def ^:icon align-self-row-center (icon-xref :align-self-row-center))
(def ^:icon align-self-row-left (icon-xref :align-self-row-left))
(def ^:icon align-self-row-right (icon-xref :align-self-row-right))
(def ^:icon align-self-row-stretch (icon-xref :align-self-row-stretch))
(def ^:icon align-top (icon-xref :align-top))
(def ^:icon align-vertical-center (icon-xref :align-vertical-center))
(def ^:icon arrow (icon-xref :arrow))
(def ^:icon asc-sort (icon-xref :asc-sort))
(def ^:icon board (icon-xref :board))
(def ^:icon boards-thumbnail (icon-xref :boards-thumbnail))
(def ^:icon boolean-difference (icon-xref :boolean-difference))
(def ^:icon boolean-exclude (icon-xref :boolean-exclude))
(def ^:icon boolean-flatten (icon-xref :boolean-flatten))
(def ^:icon boolean-intersection (icon-xref :boolean-intersection))
(def ^:icon boolean-union (icon-xref :boolean-union))
(def ^:icon bug (icon-xref :bug))
(def ^:icon clip-content (icon-xref :clip-content))
(def ^:icon clipboard (icon-xref :clipboard))
(def ^:icon close-small (icon-xref :close-small))
(def ^:icon close (icon-xref :close))
(def ^:icon code (icon-xref :code))
(def ^:icon column-reverse (icon-xref :column-reverse))
(def ^:icon column (icon-xref :column))
(def ^:icon comments (icon-xref :comments))
(def ^:icon component-copy (icon-xref :component-copy))
(def ^:icon component (icon-xref :component))
(def ^:icon constraint-horizontal (icon-xref :constraint-horizontal))
(def ^:icon constraint-vertical (icon-xref :constraint-vertical))
(def ^:icon corner-bottom-left (icon-xref :corner-bottom-left))
(def ^:icon corner-bottom-right (icon-xref :corner-bottom-right))
(def ^:icon corner-bottom (icon-xref :corner-bottom))
(def ^:icon corner-center (icon-xref :corner-center))
(def ^:icon corner-radius (icon-xref :corner-radius))
(def ^:icon corner-top (icon-xref :corner-top))
(def ^:icon corner-top-left (icon-xref :corner-top-left))
(def ^:icon corner-top-right (icon-xref :corner-top-right))
(def ^:icon curve (icon-xref :curve))
(def ^:icon delete-text (icon-xref :delete-text))
(def ^:icon delete (icon-xref :delete))
(def ^:icon desc-sort (icon-xref :desc-sort))
(def ^:icon detach (icon-xref :detach))
(def ^:icon detached (icon-xref :detached))
(def ^:icon distribute-horizontally (icon-xref :distribute-horizontally))
(def ^:icon distribute-vertical-spacing (icon-xref :distribute-vertical-spacing))
(def ^:icon document (icon-xref :document))
(def ^:icon download (icon-xref :download))
(def ^:icon drop-icon (icon-xref :drop))
(def ^:icon easing-ease-in-out (icon-xref :easing-ease-in-out))
(def ^:icon easing-ease-in (icon-xref :easing-ease-in))
(def ^:icon easing-ease-out (icon-xref :easing-ease-out))
(def ^:icon easing-ease (icon-xref :easing-ease))
(def ^:icon easing-linear (icon-xref :easing-linear))
(def ^:icon effects (icon-xref :effects))
(def ^:icon elipse (icon-xref :elipse))
(def ^:icon exit (icon-xref :exit))
(def ^:icon expand (icon-xref :expand))
(def ^:icon feedback (icon-xref :feedback))
(def ^:icon fill-content (icon-xref :fill-content))
(def ^:icon filter-icon (icon-xref :filter))
(def ^:icon fixed-width (icon-xref :fixed-width))
(def ^:icon flex-grid (icon-xref :flex-grid))
(def ^:icon flex-horizontal (icon-xref :flex-horizontal))
(def ^:icon flex-vertical (icon-xref :flex-vertical))
(def ^:icon flex (icon-xref :flex))
(def ^:icon flip-horizontal (icon-xref :flip-horizontal))
(def ^:icon flip-vertical (icon-xref :flip-vertical))
(def ^:icon gap-horizontal (icon-xref :gap-horizontal))
(def ^:icon gap-vertical (icon-xref :gap-vertical))
(def ^:icon graphics (icon-xref :graphics))
(def ^:icon grid-column (icon-xref :grid-column))
(def ^:icon grid-columns (icon-xref :grid-columns))
(def ^:icon grid-gutter (icon-xref :grid-gutter))
(def ^:icon grid-margin (icon-xref :grid-margin))
(def ^:icon grid (icon-xref :grid))
(def ^:icon grid-row (icon-xref :grid-row))
(def ^:icon grid-rows (icon-xref :grid-rows))
(def ^:icon grid-square (icon-xref :grid-square))
(def ^:icon group (icon-xref :group))
(def ^:icon gutter-horizontal (icon-xref :gutter-horizontal))
(def ^:icon gutter-vertical (icon-xref :gutter-vertical))
(def ^:icon help (icon-xref :help))
(def ^:icon hide (icon-xref :hide))
(def ^:icon history (icon-xref :history))
(def ^:icon hsva (icon-xref :hsva))
(def ^:icon hug-content (icon-xref :hug-content))
(def ^:icon icon (icon-xref :icon))
(def ^:icon img (icon-xref :img))
(def ^:icon interaction (icon-xref :interaction))
(def ^:icon join-nodes (icon-xref :join-nodes))
(def ^:icon external-link (icon-xref :external-link))
(def ^:icon justify-content-column-around (icon-xref :justify-content-column-around))
(def ^:icon justify-content-column-between (icon-xref :justify-content-column-between))
(def ^:icon justify-content-column-center (icon-xref :justify-content-column-center))
(def ^:icon justify-content-column-end (icon-xref :justify-content-column-end))
(def ^:icon justify-content-column-evenly (icon-xref :justify-content-column-evenly))
(def ^:icon justify-content-column-start (icon-xref :justify-content-column-start))
(def ^:icon justify-content-row-around (icon-xref :justify-content-row-around))
(def ^:icon justify-content-row-between (icon-xref :justify-content-row-between))
(def ^:icon justify-content-row-center (icon-xref :justify-content-row-center))
(def ^:icon justify-content-row-end (icon-xref :justify-content-row-end))
(def ^:icon justify-content-row-evenly (icon-xref :justify-content-row-evenly))
(def ^:icon justify-content-row-start (icon-xref :justify-content-row-start))
(def ^:icon layers (icon-xref :layers))
(def ^:icon library (icon-xref :library))
(def ^:icon locate (icon-xref :locate))
(def ^:icon lock (icon-xref :lock))
(def ^:icon margin (icon-xref :margin))
(def ^:icon margin-bottom (icon-xref :margin-bottom))
(def ^:icon margin-left (icon-xref :margin-left))
(def ^:icon margin-left-right (icon-xref :margin-left-right))
(def ^:icon margin-right (icon-xref :margin-right))
(def ^:icon margin-top (icon-xref :margin-top))
(def ^:icon margin-top-bottom (icon-xref :margin-top-bottom))
(def ^:icon mask (icon-xref :mask))
(def ^:icon masked (icon-xref :masked))
(def ^:icon menu (icon-xref :menu))
(def ^:icon merge-nodes (icon-xref :merge-nodes))
(def ^:icon move (icon-xref :move))
(def ^:icon msg-error (icon-xref :msg-error))
(def ^:icon msg-neutral (icon-xref :msg-neutral))
(def ^:icon msg-success (icon-xref :msg-success))
(def ^:icon msg-warning (icon-xref :msg-warning))
(def ^:icon open-link (icon-xref :open-link))
(def ^:icon oauth-1 (icon-xref :oauth-1))
(def ^:icon oauth-2 (icon-xref :oauth-2))
(def ^:icon oauth-3 (icon-xref :oauth-3))
(def ^:icon padding-bottom (icon-xref :padding-bottom))
(def ^:icon padding-extended (icon-xref :padding-extended))
(def ^:icon padding-left (icon-xref :padding-left))
(def ^:icon padding-left-right (icon-xref :padding-left-right))
(def ^:icon padding-right (icon-xref :padding-right))
(def ^:icon padding-top (icon-xref :padding-top))
(def ^:icon padding-top-bottom (icon-xref :padding-top-bottom))
(def ^:icon path (icon-xref :path))
(def ^:icon pentool (icon-xref :pentool))
(def ^:icon picker (icon-xref :picker))
(def ^:icon pin (icon-xref :pin))
(def ^:icon play (icon-xref :play))
(def ^:icon puzzle (icon-xref :puzzle))
(def ^:icon rectangle (icon-xref :rectangle))
(def ^:icon reload (icon-xref :reload))
(def ^:icon remove-icon (icon-xref :remove))
(def ^:icon rgba (icon-xref :rgba))
(def ^:icon rgba-complementary (icon-xref :rgba-complementary))
(def ^:icon rocket (icon-xref :rocket))
(def ^:icon rotation (icon-xref :rotation))
(def ^:icon row (icon-xref :row))
(def ^:icon row-reverse (icon-xref :row-reverse))
(def ^:icon search (icon-xref :search))
(def ^:icon separate-nodes (icon-xref :separate-nodes))
(def ^:icon shown (icon-xref :shown))
(def ^:icon size-horizontal (icon-xref :size-horizontal))
(def ^:icon size-vertical (icon-xref :size-vertical))
(def ^:icon snap-nodes (icon-xref :snap-nodes))
(def ^:icon status-alert (icon-xref :status-alert))
(def ^:icon status-tick (icon-xref :status-tick))
(def ^:icon status-update (icon-xref :status-update))
(def ^:icon status-wrong (icon-xref :status-wrong))
(def ^:icon stroke-arrow (icon-xref :stroke-arrow))
(def ^:icon stroke-circle (icon-xref :stroke-circle))
(def ^:icon stroke-diamond (icon-xref :stroke-diamond))
(def ^:icon stroke-rectangle (icon-xref :stroke-rectangle))
(def ^:icon stroke-rounded (icon-xref :stroke-rounded))
(def ^:icon stroke-size (icon-xref :stroke-size))
(def ^:icon stroke-squared (icon-xref :stroke-squared))
(def ^:icon stroke-triangle (icon-xref :stroke-triangle))
(def ^:icon svg (icon-xref :svg))
(def ^:icon swatches (icon-xref :swatches))
(def ^:icon switch (icon-xref :switch))
(def ^:icon text (icon-xref :text))
(def ^:icon text-align-center (icon-xref :text-align-center))
(def ^:icon text-align-left (icon-xref :text-align-left))
(def ^:icon text-align-right (icon-xref :text-align-right))
(def ^:icon text-auto-height (icon-xref :text-auto-height))
(def ^:icon text-auto-width (icon-xref :text-auto-width))
(def ^:icon text-bottom (icon-xref :text-bottom))
(def ^:icon text-fixed (icon-xref :text-fixed))
(def ^:icon text-justify (icon-xref :text-justify))
(def ^:icon text-letterspacing (icon-xref :text-letterspacing))
(def ^:icon text-lineheight (icon-xref :text-lineheight))
(def ^:icon text-lowercase (icon-xref :text-lowercase))
(def ^:icon text-ltr (icon-xref :text-ltr))
(def ^:icon text-middle (icon-xref :text-middle))
(def ^:icon text-mixed (icon-xref :text-mixed))
(def ^:icon text-palette (icon-xref :text-palette))
(def ^:icon text-paragraph (icon-xref :text-paragraph))
(def ^:icon text-rtl (icon-xref :text-rtl))
(def ^:icon text-stroked (icon-xref :text-stroked))
(def ^:icon text-top (icon-xref :text-top))
(def ^:icon text-underlined (icon-xref :text-underlined))
(def ^:icon text-uppercase (icon-xref :text-uppercase))
(def ^:icon thumbnail (icon-xref :thumbnail))
(def ^:icon tick (icon-xref :tick))
(def ^:icon to-corner (icon-xref :to-corner))
(def ^:icon to-curve (icon-xref :to-curve))
(def ^:icon tree (icon-xref :tree))
(def ^:icon unlock (icon-xref :unlock))
(def ^:icon user (icon-xref :user))
(def ^:icon v2-icon-1 (icon-xref :v2-icon-1))
(def ^:icon v2-icon-2 (icon-xref :v2-icon-2))
(def ^:icon v2-icon-3 (icon-xref :v2-icon-3))
(def ^:icon v2-icon-4 (icon-xref :v2-icon-4))
(def ^:icon vertical-align-items-center (icon-xref :vertical-align-items-center))
(def ^:icon vertical-align-items-end (icon-xref :vertical-align-items-end))
(def ^:icon vertical-align-items-start (icon-xref :vertical-align-items-start))
(def ^:icon view-as-icons (icon-xref :view-as-icons))
(def ^:icon view-as-list (icon-xref :view-as-list))
(def ^:icon wrap (icon-xref :wrap))

(def ^:icon loader-pencil
  (mf/html
   [:svg
    {:viewBox "0 0 677.34762 182.15429"
     :height "182"
     :width "667"
     :id "loader-pencil"}
    [:g
     [:path
      {:id "body-body"
       :d
       "M128.273 0l-3.9 2.77L0 91.078l128.273 91.076 549.075-.006V.008L128.273 0zm20.852 30l498.223.006V152.15l-498.223.007V30zm-25 9.74v102.678l-49.033-34.813-.578-32.64 49.61-35.225z"}]
     [:path
      {:id "loader-line"
       :d
       "M134.482 157.147v25l518.57.008.002-25-518.572-.008z"}]]]))

(def default
  "A collection of all icons"
  (collect-icons))

(mf/defc debug-icons-preview
  {::mf/wrap-props false}
  []
  (let [entries   (->> (seq (js/Object.entries default))
                       (sort-by first))]
    [:section.debug-icons-preview
     [:h2 "icons"]
     (for [[key val] entries]
       [:div.icon-item-old {:key key
                            :title key}
        val
        [:span key]])]))

(defn key->icon
  [icon-key]
  (when icon-key
    (unchecked-get default (-> icon-key d/name str/camel str/capital))))
