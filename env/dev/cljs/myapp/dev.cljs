(ns ^:figwheel-no-load myapp.app
  (:require [myapp.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:4449/figwheel-ws"
  :on-jsload core/mount-components)

(core/init!)
