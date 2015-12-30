(ns myapp.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST PUT]]

            ;[cljsjs.jquery :as jq]
            [goog.net.cookies :as cks]
            [yarec.semantic :as sem]
            [clojure.walk :refer [keywordize-keys]]
            [myapp.pages.common :as common :refer [footer]]
            [myapp.routes.core :as route :refer [page]]
            [myapp.func :as func]

            [myapp.fn.star :refer [get-stars]]
            )
  (:import goog.History))

(defn about-page []
  [:div.ui.container
   [:div.ui.card.olive {:style {:margin-top "4em"}}
    [:div.image [:img {:src (:epic (:image_urls (session/get :avatar)))}]]
    [:div.content
     [:a.header (:username (session/get :avatar))]
     [:div.meta [:span.date "Joined in 2015"]]
     [:div.description
      "desc"]]
    [:div.extra.content
     [:a [:i.user.icon ] "22 Friends"]]]
   [:div "Helo"]]
  )

(defn syn-star []
  (GET (func/token-url "synstar"){:handler #(get-stars)})
  )

(defn header []
  (let [page (session/get :page)]
    [:div
     [:div.ui.orange.bottom.attached.progress {:id "load-progress" :style {:height "0.1rem"}} [:div.bar]]
     [:div.ui.menu.secondary.pointing
      {:style {:border-bottom "1px solid rgba(34, 36, 38, 0.05)" :margin-top "0px" }}
      [:div.ui.container
       [:a.item {:class (if (= :home page) "active")
                 :href (str (.-pathname (.-location js/window)) "#/") } "Home"]
       [:a.item {:class (if (= :about page) "active")
                 :href (str (.-pathname (.-location js/window)) "#/about")} "About"]
       [:div.right.menu
        [:div.item
         [:div.ui.icon.input
          [:input {:type "text" :placeholder "Seach..."}]
          [:i.search.link.icon]]]
        (if-let [github-name (:githubLogin (session/get :user))]
          [:a.ui.item {:on-click syn-star} github-name]
          [:a.ui.item {:on-click func/github-authorize } "Login"]
          )
          [:a.ui.item {:on-click #(print (:name (first (session/get :stars)))) } "test"]
        ] ] ] ]))

(defn mount-components []
  (common/get-data)
  (r/render [#'header] (.getElementById js/document "header"))
  (r/render [#'page] (.getElementById js/document "app"))
  (r/render [#'footer] (.getElementById js/document "footer"))
  (route/init-ui)
  )

(defn init! []
  ;(fetch-docs!)
  (route/hook-browser-navigation!)
  (mount-components)
  )
