(ns myapp.fn.tag
  (:require [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [myapp.func :as func :refer [up-state! app-state]]
            ))

(defn get-tags [& [type]]
  (let [type (or type "blogs")]
    (func/get-rest-items "tags"
                    (fn [res]
                      (js/setTimeout func/fix-sticky 1000)
                      (let [tags (session/get :tags)]
                        (.remove (js/$ ".tags-form > .menu > .item"))
                        (doseq [tag tags]
                          (let [name (:name tag)
                                div (str "<div class=\"item\"
                                         data-value=\"" name "\"
                                         data-text=\"" name "\"> "
                                         name
                                         "</div>")]
                            (.append (js/$ ".tags-form > .menu") div)))
                        ))
                    {:sort "count" :asc -1 :type type})))

(defn add-tag []
  (let [tag (.val (js/$ "#add-tag-value"))
        tag-type (or (session/get :tag-type) "blog")]
    (if (= tag "")
      (println "empty tag")
      (do
        ;;(print (str "type: " tag-type))
        (.val (js/$ "#add-tag-value") "")
        (POST (func/token-rest-url "tags")
            {:params {:name tag :count 0 :type tag-type}
             :format :json
             :handler #(get-tags tag-type)})
        (.focus (js/$ ".tags-form > .search"))
        ))))

(defn update-tag-count [fn-key tag-name]
  (let [tags-all (session/get :tags)
        tag (some #(if (= (:name %) tag-name) %) tags-all)
        tag-id (:id tag)]
    (PUT (func/token-rest-url (str "tags/" tag-id))
        {:params {fn-key "count"}
         :format :json})))

(defn save-item-tags [type id]
  (let [tags (.val (js/$ "#tags-value"))
        tags-trim (clojure.string/replace tags #"^," "")
        vec-tags (clojure.string/split tags-trim ",")
        save-tags-cb (:save-tags-cb @app-state)
        ]
    (let [tag-add (:tags-add @app-state)
          tag-rm (:tags-rm @app-state)
          tags-all (session/get :tags)]
      (doseq [tag-name tag-add] (update-tag-count :inc tag-name))
      (doseq [tag-name tag-rm] (update-tag-count :dec tag-name))
      (js/setTimeout #(get-tags type) 500))

    (.modal (js/$ ".ui.edit-tags") "hide")
    (PUT (func/token-rest-url (str type "/" id))
        {:params {:tags vec-tags}
         :format :json
         :handler #(save-tags-cb)
         })))


(defn on-add-tag [value text choice]
  (let [tag-add (:tags-add @app-state)
        tag-rm (:tags-rm @app-state)]
    (if (some #{value} tag-rm)
      (up-state! :tags-rm (remove #{value} tag-add))
      (up-state! :tags-add (conj tag-add value)))))

(defn on-rm-tag [value]
  (let [tag-add (:tags-add @app-state)
        tag-rm (:tags-rm @app-state)]
    (if (and (not (some #{value} tag-add))
             (not (some #{value} tag-rm)))
      (up-state! :tags-rm (conj tag-rm value)))))

(defn fn-edit-tag [id tags type item]
  ;;(r/unmount-component-at-node (.getElementById js/document "pop"))
  ;;(set! (. (.getElementById js/document "pop") -innerHTML) "")
  #_(r/render [edit-tags id (clojure.string/join "," tags)]
              (.getElementById js/document "pop"))

  (session/put! :tag-type type)

  (set! (. (.getElementById js/document "tags-value") -value)
        (clojure.string/join "," tags))
  (.remove (js/$ ".tags-form > a"))
  (.removeClass (js/$ ".tags-form > .menu > .item") "active filtered")
  (.dropdown (js/$ ".dropdown.tags-form")
             (js-obj "allowAdditions" false
                     "onAdd" on-add-tag 
                     "onRemove" on-rm-tag))
  (up-state! :tags-rm [])
  (up-state! :tags-add [])

  (.click (.unbind (js/$ "#save-tags") "click") #(save-item-tags type id))
  (.click (.unbind (js/$ "#save-tags-quick") "click") #(save-item-tags type id))
  (.html (js/$ "#desc-in-tag") (:full_name item))
  (.modal (js/$ ".ui.edit-tags") "show")
  (js/setTimeout #(.focus (js/$ ".tags-form > .search")) 650)
  )

(defn edit-tag []
  (fn [type {:keys [id tags star]}]
    [:a.ui {:on-click #(fn-edit-tag id tags type star)}
     [:i.angle.double.right.icon.large.grey]]))
