(ns myapp.fn.coll
  (:require [myapp.func :as func :refer [tags-to-param put-res-count] ]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            ))

(defn get-colls [& [{page :page size :size tags :tags}]]
  (let [size (or size 5)
        params-session (if tags
                         (tags-to-param tags)
                         (session/get :colls-filter))
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
    (session/put! :colls-filter params-session)
    (func/get-rest-items "colls" (fn [res] 
                              (put-res-count res params-session)
                              (func/to-top)
                              (js/setTimeout #(.dropdown (js/$ ".dropdown")) 1000)
                              )
                    params
                    )))


(defn edit-blog [id]
  #_(func/get-item "blogs" id
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
    ;;(save-blogs id {:name title-val})
    (.dropdown (js/$ ".dropdown") "hide")
    )
  )

(defn continue-new-blog []
  (let [title (.val (js/$ "#new-blog-dropdown"))
        params {:name title :format 1}]
    #_(POST (func/token-url "blogs")
          {:params params
           :format :json
           :handler (fn [res]
                      (let [data (keywordize-keys (get res "data"))
                            id (:id data)]
                        (edit-blog id)))})
    (func/hide-modal)))
