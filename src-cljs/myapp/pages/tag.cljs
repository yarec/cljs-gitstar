(ns myapp.pages.tag
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [myapp.func :as func :refer [up-state! app-state]]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            [myapp.pages.common :as common :refer [pages]]
            ))
