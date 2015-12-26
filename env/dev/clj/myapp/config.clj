(ns myapp.config
  (:require [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
            [myapp.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (timbre/info "\n-=[myapp started successfully using the development profile]=-"))
   :middleware wrap-dev})
