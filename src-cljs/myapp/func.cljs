(ns myapp.func
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [clojure.walk :refer [keywordize-keys]]
            [ajax.core :refer [GET POST PUT]]
            [cljs-uuid-utils.core :as uuid]
            [goog.net.cookies :as cks]
            [secretary.core :as secretary :include-macros true]
            ))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:auto-progress true
                            :text "Hello world! 4"
                            :counter 3}))

(defn up-state-in! [f & args]
  (apply swap! app-state update-in [:tags] f args))

(defn up-state! [k v]
  (swap! app-state assoc k v))

(defn rm-state! [k]
  (swap! app-state dissoc k))

(defn resp-data [resp]
  (let [kw-data (keywordize-keys resp)]
    kw-data
      ))

(defn get-hash-path [path]
  (str (.-pathname (.-location js/window)) path))

(defn get-href [path]
  (let [href-root (or (:href-root @app-state)
                      (let [href (.-href (.-location js/window))
                            href (get (clojure.string/split href "#") 0)
                            len (.-length href)]
                        (subs href 0 (- len 1))
                        ))
        ;;path (str href-root path)
        path (str href-root "/#" path)
        ]
    (up-state! :href-root href-root)
    ;;(print href-root)
    ;;(print path)
    path
    ))

(defn set-path [path]
  (up-state! :page path)
  (secretary/dispatch! path)
  (.pushState (.-history js/window ) nil nil (get-href path)))

;;;;;;;;;;; Fetch and Send Data
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

(defn put-res-count [response params]
  (let [res-name (get response "res-name")
        tags (:tags params)
        tags (cond (nil? tags) ["all"]
                   :else tags)
        tags (clojure.string/join "-" tags)
        key-word (keyword (str res-name "-" tags "-count"))
        cnt (get response "count")
        ]
    (session/put! key-word cnt)
    )
  )

(defn start-progress []
  (if (and (= 0 (or (:interval-id @app-state) 0))
          (:auto-progress @app-state))
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
  (if (:auto-progress @app-state)
    (do
      (js/setTimeout #(.progress (js/$ "#load-progress") #js {:percent 100}) 200)
      (up-state! :interval-val 100)
      )))

(defn get-data-handler [callback]
  (fn [response]
    (let [res-name (get response "res-name")]
      (put-session res-name response)
      (finish-progress)
      (if callback
        (callback response)))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn tags-to-param [tags]
  (cond (= "all" tags) {}
        (= "other" tags) {:tags [""]}
        :else {:tags [tags]}
        ))

(defn token-param [& [params]]
  (let [params (or params {})
        token (.get goog.net.cookies "token")]
    (if token
      (assoc params :token token) params)))

(defn url [path] (str js/remote_domain path))
(defn rest-prefix [] (str js/rest_prefix))

(defn token-url [path]
  (url (str "/" path "?" (ajax.core/params-to-str (token-param)))))

(defn token-rest-url [path]
  (token-url (str (rest-prefix) path)))

(defn cur-page [name & [page]]
  (let [page-key (keyword (str name "-page"))
        val (get @app-state page-key)]
    (if page (up-state! page-key page))
    val))

(defn get-items [name & [callback params]]
  (let [names (clojure.string/split name (rest-prefix))
        base-name (get names 1)
        token (.get goog.net.cookies "token")
        params (token-param params)
        page (or (:page params) (cur-page name) 1)
        params (assoc params :size (or (:size params) 100)
                      :page page)
        path (str "/" name)]
    (cur-page base-name page)
    (start-progress)
    (GET (url path)
         {:handler (get-data-handler callback)
          :error-handler error-handler
          :params params})))

(defn get-rest-items [name & [callback params]]
  (get-items (str (rest-prefix) name) callback params))

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

(defn get-rest-item [name id & [callback params]]
  (get-item (str (rest-prefix) name) id callback params))


;;;;;;;;;; Tiny MCE ;;;;;;;;;
(defn mce [] (.-activeEditor js/tinymce))

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


;;;;;;;;;; Fit Semantic UI ;;;;;;;;;;;;
(defn fix-sticky []
  ;;(.sticky (js/$ ".ui.header.sticky") )
  (.sticky (js/$ ".ui.taglist.sticky") (js-obj "offset" 60))
  )

(defn adjust-footer []
  (let [window-height (.height (js/$ js/window))
        window-width (.width (js/$ js/window))
        header-height (.height (js/$ "#header"))
        footer-height (.height (js/$ "#footer"))
        min-height (- window-height footer-height header-height)]
    (.css (js/$ "#header-sticky") "width" window-width)
    (.css (js/$ "#app") "min-height" min-height)
    ))

(defn hide-modal []
  (-> (js/$ ".ui.modal")
      (.modal "hide all")))

(defn to-top []
  (.animate (js/$ "html,body")
            (js-obj "scrollTop" 0)
            300))

;;;;;;;;; Auth ;;;;;;;;;
(defn github-authorize []
  (let [uuid-str (uuid/uuid-string (uuid/make-random-squuid))
        path (url "/auth/github/authorize")
        cur-loc (.-location js/window)
        cur-path (.-pathname (.-location js/window))
        cur-hash (.-hash (.-location js/window))
        hash (if (= "" cur-hash) "#/" "")
        cur-url (str cur-loc hash "?token=" uuid-str)
        location (js/encodeURIComponent cur-url)
        location (js/encodeURIComponent location)
        new-location (str path "?_r=" location "&uuid=" uuid-str)]
    ;(.log js/console cur-loc)
    ;(println cur-url)
    (set! (.-location js/window) new-location)
    ;(GET path )
    )
  )

(defn auth-url [client_id]
  (str "https://github.com/login/oauth/authorize"
       "?client_id=" client_id
       "&redirect_uri=auth://example"
       "&state=antiestablishmentarianism")
  )

(defn get-access-token [code callback id secret]
  (let [url "https://github.com/login/oauth/access_token"
        params {:code code
                :client_id id
                :client_secret secret} ]
    (POST url {:params params
               :format :json
               :handler callback}
          )))

(defn admin? []
  (let [user (session/get :user)
        role (or (:role user) "user")
        id (or (:id user) 0)]
    (= role "admin")))

;;;;;;;;; Misc ;;;;;;;;;

(defn get-form-data [id]
  (let [data (.serializeArray (js/$ id))
        data1 (js->clj data :keywordize-keys true)
        data2 (map (fn [x]
                     {(keyword (:name x)) (:value x)})
                   data1)

        params (reduce (fn [v s]
                         (conj v s))
                       data2)
        ]
    params
    )
  )

(defn rand-color []
  (let [colors ["red" "orange" "yellow" "olive"
                "green" "teal" "blue" "purple"
                "violet" "pink" "brown" "grey"]
        rand (rand-int 11)
        ]
    (colors rand)
    )
  )

(defn on-filter-add-tag [value text choice]
  (println value)
  )

(defn on-filter-rm-tag [value text choice]
  (println value)
  )

(defn parse-url [url]
  (let [query (clojure.string/split url "?")
        params (clojure.string/split (get query 1) "&")
        m (map (fn [param] 
                 (let [kv (clojure.string/split param "=")
                       k (get kv 0)
                       v (get kv 1)]
                   {(keyword k) v}
                   ))
               params)
        mm (reduce merge m)]
    mm))

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn filter-tags [type]
  (let [filter (session/get (keyword (str type "-filter")))
        tags (:tags filter)]
    tags
    ))

(defn in-tags? [tag type]
  (let [tags (filter-tags type)]
    (seq-contains? tags tag)
    ))

(defn jso->clj [data]
  (let [
        data1 (js->clj data :keywordize-keys true)
        #_(
        data2 (map (fn [x]
                     {(keyword (:name x)) (:value x)})
                   data1)
        params (reduce (fn [v s]
                        (conj v s))
                      data2)
           )
        ]
    (.log js/console data)
    (.log js/console data1)
    (print data1)
    )
  )

;;;;;;;;;;; Debug ;;;;;;;;;;
(defn do-test []
  (let [uuid-str (uuid/uuid-string (uuid/make-random-squuid))
        location (.-location js/window)
        href "http://clj.softidy.com/#/blog"
        hash-split (clojure.string/split href "#")
        cks goog.net.cookies 
        ihuipao_auth (.get goog.net.cookies "ihuipao_auth")
        ]
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
    ;;(println (url "/test"))
    ;;(print (:blogs-page @app-state))
    ;;(print (get hash-split 0))
    ;;(print @app-state)
    ;;(print (in-tags? "ios" "colls"))
    ;;(jso->clj cks)
    ;; (print "do-test: " ihuipao_auth)
    #_(GET "http://test.ihuipao.com/api/event?raceid=17"
         {:handler
          (fn [resp]
            (print resp)
            )
          :with-credentials true
          }
         )

    #_(let [data (session/get :events)]
        (print data)
        )

    #_(-> js/$LAB
        (.script "http://cdn.bootcss.com/moment.js/2.13.0/moment.min.js")
        (.wait #(print (-> js/window
                           .moment
                           (.subtract 1 "days")
                           .calendar
                           )))
        )

    ))
