(ns myapp.fn.blog
  (:require [myapp.func :as func :refer [tags-to-param put-res-count app-state] ]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            ))

(defn toggle-blog-list-type []
  (let [type (session/get :blog-list-type)
        type (if (= type 1) 0 1)]
    (session/put! :blog-list-type type)
    ))

(defn get-blogs [& [{page :page size :size tags :tags}]]
  (let [size (or size 5)
        params-session (if tags
                         (tags-to-param tags)
                         (session/get :blogs-filter))
        params (assoc params-session
                      :size size
                      :page page
                      :fields ["name" "id" "uid"
                               "tid" "format" "desc"
                               "ename" "pagename"
                               "tags"
                               "_intm" "_uptm"]
                      :sort "id"
                      :asc  -1
                      )
        ]
    (session/put! :blogs-filter params-session)
    (func/get-rest-items "blogs" (fn [res] 
                                   (put-res-count res params-session)
                                   (func/to-top)
                                   (js/setTimeout #(.dropdown (js/$ ".dropdown")) 1000)
                                   ;; (.log js/console (js/$ ".toggle.checkbox"))
                                   (.checkbox (js/$ ".toggle.checkbox")
                                              (js-obj "onChange" #(toggle-blog-list-type)
                                               )))
                         params
                         )))

(defn save-blogs [& [id params]]
  (let [id (or id (session/get :edit-blog-id))
        params (or params {:value (.getContent (func/mce))})
        value (:value params)
        params (if value
                 (assoc params :desc (subs (.text (js/$ value)) 0 300)) 
                 params)
        ]
    (if (= 0 id)
      (let [params (assoc params 
                          :format 1
                          :name "testpost3"
                          )]
        (POST (func/token-rest-url "blogs")
              {:params params
               :format :json
               :handler #(get-blogs)})
        )
      (let [a 1]
        (PUT (func/token-rest-url (str "blogs/" id))
             {:params params
              :format :json
              :handler #(get-blogs)})
        )
      )
    (func/hide-modal)
    )
  )

(defn edit-blog [id]
  (func/get-rest-item "blogs" id
                 (fn [response]
                   (let [res-name (get response "res-name")
                         data (keywordize-keys (get response "data"))
                         content (or (:value data) "")]
                     (session/put! (keyword "edit-blog-id") id)
                     (func/show-editor content)
                     )
                   ))
  )

(defn save-post-title [id]
  (let [title-val (.val (js/$ (str "#" id)))]
    (println title-val)
    (save-blogs id {:name title-val})
    (.dropdown (js/$ ".dropdown") "hide")
    )
  )

(defn continue-new-blog []
  (let [title (.val (js/$ "#new-blog-dropdown"))
        params {:name title :format 1}]
    (POST (func/token-rest-url "blogs")
          {:params params
           :format :json
           :handler (fn [res]
                      (let [data (keywordize-keys (get res "data"))
                            id (:id data)]
                        (edit-blog id)))})
    (func/hide-modal)))
