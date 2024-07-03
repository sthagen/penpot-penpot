
;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.ds.storybook
  (:require-macros [app.main.style :as stl])
  (:require
   [rumext.v2 :as mf]))

(mf/defc story-wrapper*
  {::mf/props :obj}
  [{:keys [theme children]}]
  [:article {:class (stl/css :story-wrapper)}
   (if (some? theme)
     [:section {:class theme} children]
     [*
      [:section {:class "default"} children]
      [:section {:class "light"} children]])])

(mf/defc icon-grid*
  {::mf/props :obj}
  [{:keys [children]}]
  [:article {:class (stl/css :icon-grid)} children])