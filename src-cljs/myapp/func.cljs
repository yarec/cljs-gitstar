(ns myapp.func
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [clojure.walk :refer [keywordize-keys]]
            [ajax.core :refer [GET POST PUT]]
            [cljs-uuid-utils.core :as uuid]
            [goog.net.cookies :as cks]
            ))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:text "Hello world! 4"
                            :counter 3}))

(defn up-state-in! [f & args]
  (apply swap! app-state update-in [:tags] f args))

(defn up-state! [k v]
  (swap! app-state assoc k v))

(defn rm-state! [k]
  (swap! app-state dissoc k))

(defn data-handler [response]
  (let [res-name (get response "res-name")
        data (get response "data")]
    ;(println (count data))
    (session/put! (keyword res-name) (keywordize-keys data))))

(defn put-session [res-name response]
  (session/put! (keyword res-name) (keywordize-keys (get response "data")))
  (session/put! (keyword (str res-name "-count")) (get response "count"))
  (session/put! (keyword (str res-name "-page")) (get response "page"))
  (session/put! (keyword (str res-name "-size")) (get response "size"))
  )

(defn start-progress []
  (if (= 0 (or (:interval-id @app-state) 0))
    (let [interval-id (js/setInterval 
                        (fn []
                          (let [id (:interval-id @app-state)
                                val (:interval-val @app-state)]
                            (if (> val 99)
                              (do
                                (js/clearInterval id)
                                (up-state! :interval-id 0))
                              (let [percent (+ val 1)]
                                (up-state! :interval-val percent)
                                (.progress (js/$ "#load-progress") #js {:percent percent}))))) 300)]
      (up-state! :interval-id interval-id)
      (up-state! :interval-val 1)
      (.progress (js/$ "#load-progress") #js {:percent 28}))))

(defn finish-progress []
  (js/setTimeout #(.progress (js/$ "#load-progress") #js {:percent 100}) 200)
  (up-state! :interval-val 100)
  )

(defn get-data-handler [callback]
  (fn [response]
    (let [res-name (get response "res-name")]
      (put-session res-name response)
      (finish-progress)
      (if callback
        (callback response)))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn token-param [& [params]]
  (let [params (or params {})
        token (.get goog.net.cookies "token")]
    (if token
      (assoc params :token token) params)))

(defn url [path] (str js/remote_domain path))

(defn token-url [path]
  (url (str "/" path "?" (ajax.core/params-to-str (token-param)))))

(defn session-page [name & [page]]
  (let [page-key (keyword (str name "-page"))]
    (if page (session/put! page-key page))
    (session/get page-key)
    ))

(defn get-items [name & [callback params]]
  (let [token (.get goog.net.cookies "token")
        params (token-param params)
        page (or (:page params) (or (session-page name) 1))
        params (assoc params :size (or (:size params) 100)
                      :page page)
        path (str "/" name)]
    (session-page name page)
    (start-progress)
    (GET (url path)
         {:handler (get-data-handler callback)
          :error-handler error-handler
          :params params})))

(defn get-item [name id & [callback params]]
  (let [token (.get goog.net.cookies "token")
        params (token-param params)
        path (str "/" name "/" id)]
    ;(println path)
    (start-progress)
    (GET (url path)
         {:handler (get-data-handler callback)
          :error-handler error-handler
          :params params})
    )
  )

(defn do-test []
  (let [uuid-str (uuid/uuid-string (uuid/make-random-squuid))
        location (.-location js/window)]
    ;(println uuid-str)
    ;(println location)
    ;(println (token-param))
    ;(GET "http://star.softidy.com/t3" {:params (token-param)})
    ;(println (session/get :user))
    ;(println (ajax.core/params-to-str {:a "x"}))
    #_(POST (token-url "param")
          {:params {:name "nammmm"
                    :format 1
                    :token "tokkkk"
                    :value "centtt"}
           :format :json
                                        ;:handler #(get-tags)
           })
    (println (url "/test"))

    ))

(defn adjust-footer []
  (let [window-height (.height (js/$ js/window))
        header-height (.height (js/$ "#header"))
        footer-height (.height (js/$ "#footer"))
        min-height (- window-height footer-height header-height)]
    (.css (js/$ "#app") "min-height" min-height)
    ))

(defn github-authorize []
  (let [uuid-str (uuid/uuid-string (uuid/make-random-squuid))
        path (url "/auth/github/authorize")
        location (js/encodeURIComponent (str (.-location js/window) "?token=" uuid-str))
        location (js/encodeURIComponent location)
        new-location (str path "?_r=" location "&uuid=" uuid-str)]
    ;(println new-location)
    (set! (.-location js/window) new-location)
    ;(GET path )
    )
  )

(defn on-filter-add-tag [value text choice]
  (println value)
  )

(defn on-filter-rm-tag [value text choice]
  (println value)
  )

(defn to-top []
  (.animate (js/$ "html,body")
            (js-obj "scrollTop" 0)
            300))

(defn mce [] (.-activeEditor js/tinymce))

(defn hide-modal []
  (-> (js/$ ".ui.modal")
      (.modal "hide all")))


(defn show-editor [&[content]]
  (-> (js/$ ".ui.new-post")
      ;;(.modal "setting" "closable" false)
      (.modal (js-obj
               ;;"blurring" true
               ;;"inverted" false
               "closable" false
               ))
      (.modal "show")
      )
     (.setContent (mce) content (js-obj "format" "raw"))
  )


(defn fix-sticky []
  ;;(.sticky (js/$ ".ui.header.sticky") )
  (.sticky (js/$ ".ui.taglist.sticky") (js-obj "offset" 60))
  )
