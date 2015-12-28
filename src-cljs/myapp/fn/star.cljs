(ns myapp.fn.star
  (:require [reagent.session :as session]
            [myapp.func :as func]))

(defn get-stars [& [tags]]
  (let [params1 (if tags
                  (cond (= "all" tags) {}
                        (= "other" tags) {:tags [""]}
                        :else {:tags [tags]}
                        )
                  (session/get :stars-filter))
        params (assoc params1
                      :size 1000
                      ;;:sort "stargazers_count"
                      :sort "_intm"
                      :asc -1)]
    (session/put! :stars-filter params1)
    (func/get-items "stars"
                    (fn [response]
                      (let [res-name (get response "res-name")
                            tags (:tags params1)
                            tags (cond (nil? tags) ["all"]
                                       :else tags)
                            tags (clojure.string/join "-" tags)
                            key-word (keyword (str res-name "-" tags "-count"))
                            cnt (get response "count")
                            ]
                        (session/put! key-word cnt)
                        )
                      (js/setTimeout func/fix-sticky 1000)
                      )
                    params)
    ))
