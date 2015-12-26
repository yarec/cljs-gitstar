(ns myapp.fn.about
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            ))

(defn get-avatar []
  (GET "http://uifaces.com/api/v1/random"
      {:handler (fn [response]
                  (session/put! :avatar (keywordize-keys response)))}
    ))
