(ns myapp.fn.star
  (:require [reagent.session :as session]
            [myapp.func :as func]))

(defn get-stars [& [tags]]
  (let [params1 (if tags
                  (if (= "all" tags)
                    {}
                    {:tags [tags]})
                  (session/get :stars-filter))
        params (assoc params1
                      :size 1000
                                        ;:sort "stargazers_count"
                      :sort "_intm"
                      :asc -1)]
    (session/put! :stars-filter params1)
    (func/get-items "stars"
                    (fn [response]
                      (js/setTimeout func/fix-sticky 1000))
                    params)
    ))
