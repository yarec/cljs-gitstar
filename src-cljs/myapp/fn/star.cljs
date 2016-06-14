(ns myapp.fn.star
  (:require [reagent.session :as session]
            [myapp.func :as func :refer [tags-to-param put-res-count]]))

(defn get-stars [& [tags]]
  (let [params1 (if tags
                  (tags-to-param tags)
                  (session/get :stars-filter))
        params (assoc params1
                      :size 1000
                      ;;:sort "stargazers_count"
                      :sort "_intm"
                      :asc -1)]
    (session/put! :stars-filter params1)
    (func/get-rest-items "stars"
                    (fn [response]
                      (put-res-count response params1)
                      (js/setTimeout func/fix-sticky 1000)
                      )
                    params)
    ))
