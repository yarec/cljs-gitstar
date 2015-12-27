(ns myapp.routes.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]

            [goog.net.cookies :as cks]
            [cljsjs.jquery :as jq]
            [yarec.semantic :as sem]
            [clojure.walk :refer [keywordize-keys]]

            ;; pages
            [myapp.pages.star :as star :refer [star-page]]
            [myapp.pages.tag :as tag]
            [myapp.pages.common :as common :refer [header footer about-page]]

            ;; func
            [myapp.func :as func]
            [myapp.fn.tag :refer [add-tag get-tags]]
            [myapp.fn.star :refer [get-stars]]
            )
  (:import goog.History))

(def page-map
  {:home {:page star-page     :fn get-stars}
   :about    {:page about-page    :fn nil}
   })

(defn page []
  [(:page (page-map (session/get :page)))])

(defn do-page-fn [name]
  (let [page-fn (:fn (page-map (keyword name)))
        is-star (= "home" name)]
    (if page-fn
      (if is-star
        (page-fn "top")
        (page-fn)
        )
      (print page-fn)
      )
    )
  )

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(defn init-ui []
  ;(print "init-ui")
  (.accordion (js/$ ".ui.accordion"))
  ;(.sticky (js/$ ".ui.sticky"))
  ;(.sticky (js/$ ".ui.taglist.sticky") (js-obj "offset" 150))
  (.modal (js/$ ".ui.modal"))
  (.checkbox (js/$ ".checkbox"))

  (.dropdown (js/$ ".dropdown"))
  ;;(.sidebar (js/$ ".ui.labeled.icon.sidebar") "toggle")
  #_(.dropdown (js/$ ".dropdown.tag-list")
               (js-obj "keepOnScreen" true
                       "onAdd" func/on-filter-add-tag 
                       "onRemove" func/on-filter-rm-tag))
  (func/adjust-footer)
  ;;(.dropdown (js/$ ".dropdown.tag-list") "show")
  ;;(.tab (js/$ ".menu .item"))
  (.click (js/$ "#add-tag") add-tag)
  )


(defn def-routes [names]
  (doseq [name names]
    (secretary/defroute (str "/" (if (= "home" name ) "" name)) []
      (get-tags (str name "s"))
      (do-page-fn name)

      (session/put! :page (keyword name))
      #_(js/setTimeout #(.dropdown (js/$ ".dropdown")
                                 (js-obj "onNoResults" (fn [x] (println "xxx"))
                                         ;"action" (fn [x] (println "1xxx"))
                                         "allowAdditions" true
                                         )) 1000)
      (js/setTimeout #(init-ui)  200)
      )))

(def-routes ["home" "about"])

(secretary/defroute "/home" []
  (session/put! :page :home))


;(secretary/defroute "/task" [] (session/put! :page :task))
;(secretary/defroute "/about" [] (session/put! :page :about))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn handle-token [token]
  (let [x (clojure.string/split token "?")
        y (clojure.string/split (get x 1) "=")
        key (get y 0)
        val (get y 1)]
    (if (= "token" key)
      (.set goog.net.cookies key val 36000000))))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (handle-token (.-token event))
       (secretary/dispatch! (.-token event))
       ))
    (.setEnabled true)))


