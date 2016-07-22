(ns myapp.pages.common
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            [myapp.func :as func :refer [up-state! app-state 
                                         get-hash-path admin?
                                         cur-page set-path resp-data]]

            [myapp.fn.about :refer [get-avatar]]
            [myapp.fn.tag :refer [add-tag get-tags]]
            ))

(defn get-stars-by-page [resp]
  (let [page (:syncstar-page @app-state)
        pages (:syncstar-totalpage @app-state)
        total (:syncstar-total @app-state)
        percent (/ (* page 100 100) total)
        percent (if (> percent 100) 100 percent)
        ]
    (if (<= page pages)
      (do
        (GET (func/token-url "syncstar"){:handler get-stars-by-page :params {:page page}})
        (up-state! :syncstar-page (+ page 1))
        (.progress (js/$ "#load-progress") #js {:percent percent})
        ))))

(defn sync-star [resp]
  (let [data (resp-data resp)
        total (:total (:data data))
        page-size 100
        pages (.ceil js/Math (/ total page-size))
        page-list (map inc (range pages))
        ]
    (up-state! :syncstar-total total)
    (up-state! :syncstar-totalpage pages)
    (up-state! :syncstar-page 1)
    (get-stars-by-page nil)
    )
  )

(defn sync-stars []
  (GET (func/token-url "totalstar-count"){:handler sync-star})
  )

(defn get-data []
  (func/get-items "auth/userinfo"
             (fn [res]
               (get-avatar)
               )))

(defn header-list []
  (let [page (session/get :page )
        href-list [{:path "/"         :name "Home"     :page :home}
                   {:path "/blog"     :name "Blog"     :page :blog}
                   {:path "/coll"     :name "Coll"     :page :coll}
                   {:path "/star"     :name "GitStar"  :page :star}
                   {:path "/task"     :name "Task"     :page :task}
                   {:path "/boutique" :name "Boutique" :page :boutique}
                   {:path "/enroll/races"   :name "Race"   :page :enroll :role "user"}
                   {:path "/activity" :name "Activity" :page :activity :role "admin"}
                   {:path "/about"    :name "About"    :page :about}
                   ]
        a-item (fn [item]
                 (let [role (or (:role item) "user")]
                   (if (or (not (= role "admin"))
                           (admin?))
                     ^{:key item}
                     [:a.item.clickable {:class (if (= (:page item) page) "active")
                               ;;:href (get-hash-path (:path item))
                               :on-click #(set-path (:path item))
                               }
                      (:name item)]
                     )))]

    (doall (map a-item href-list))
    ))

(defn header []
  (let [page (session/get :page)]
    [:div.ui.header.sticky.fixed.top {:id "header-sticky"
                                      :style {:background "#f9f9f7"
                                              :left "0px"
                                              ;:width "1913px !important"
                                              :height "46px !important"
                                              :margin-top "0px"
                                              }}
     [:div.ui.orange.bottom.attached.progress {:id "load-progress" :style {:height "0.16rem"}} [:div.bar]]
     [:div.ui.menu.secondary.pointing
      {:style {:border-bottom "1px solid rgba(34, 36, 38, 0.05)" :margin-top "0px" }}
      [:div.ui.container
       (header-list)
       [:div.right.menu
        [:div.item
         [:div.ui.icon.input
          [:input {:type "text" :placeholder "Seach..."}]
          [:i.search.link.icon]]]
        (if-let [github-name (:githubLogin (session/get :user))]
          [:a.ui.item github-name]
          [:a.ui.item.clickable {:on-click func/github-authorize } "Login"]
          )

        [:div.ui.menu.secondary

        [:div.ui.dropdown.icon.item
         [:i.settings.icon]
         [:div.menu
          [:a.ui.item {:on-click #(set-path "/user/login")} "Login"]
          [:a.ui.item {:on-click #(sync-stars)} "Sync Stars"]
          [:div.item
           [:i.dropdown.icon]
           [:span.text "New"]
           [:div.menu
            [:div.item
             [:div.item {:on-click #(set-path "/link/new")} "Link"]
             [:div.item "Document"]
             [:div.item "Image"]
             ]
            ]
           ]
          [:div.item "Open..."]
          [:div.item "Save..."]
          [:div.divider]
          [:div.header "Export csv file"]
          [:div.item "Share..."]
          [:div.item {:on-click #(set-path "/enroll/history")} "my reghistory" ]
          [:div.item {:on-click #(set-path "/enroll/list")} "my enroll" ]
          [:a.ui.item {:on-click func/do-test} "Test"]
          ]
         ]
         ]

        ]
       ]
      ]
     ]
    ))

(defn footer []
  [:div.ui.black.inverted.vertical.footer.segment
   {:style {:margin-top "40px" :padding "5em 0em"}}
   [:div.ui.container
    [:div.ui.stackable.inverted.divided.equal.height.stackable.grid

     [:div.three.wide.column
      [:h4.ui.inverted.header "About"]
      [:div.ui.inverted.link.list
       (for [item (range 4)] ^{:key item}
         [:a.item "Sitemap"]
         ) ] ]
     [:div.three.wide.column
      [:h4.ui.inverted.header "Service"]
      [:div.ui.inverted.link.list
       (for [item (range 4)] ^{:key item}
         [:a.item "Sitemap"]
         ) ] ]
     [:div.seven.wide.column
      [:h4.ui.inverted.header "Footer Header"]
      [:p "Extra space for a call to action inside the footer that could help re-engage users."]
      ]
     ]
    ]

   [:div.ui.long.modal.new-post
    [:i.close.icon]
    [:div.content.editable.edit-content
     "testowiej we <p></p>x"
     ]
    ]
   ]
  )

(defn blank-page [])

(defn about-page []
  [:div.ui.container
   [:div.ui.card.olive {:style {:margin-top "4em"}}
    [:div.image [:img {:src (:epic (:image_urls (session/get :avatar)))}]]
    [:div.content
     [:a.header (:username (session/get :avatar))]
     [:div.meta [:span.date "Joined in 2015"]]
     [:div.description
      "desc"]]
    [:div.extra.content
     [:a [:i.user.icon ] "22 Friends"]]]
   [:div "Helo"]]
  )

(defn pages-seq [page size count]
  (let [page (int page)
        page-cnt (Math/ceil (/ count size))
        btn-num 6
        half-btn-num (/ btn-num 2)
        seq1 (let [begin (- page half-btn-num)
                   end (+ half-btn-num page)
                   end (if (> end page-cnt) page-cnt end)]
               (concat
                [1]
                (range begin page)
                (range page end)))
        seq (if (> page (+ 1 half-btn-num))
              seq1
              (range 1 (+ 2 btn-num)))
        seq (conj (vec seq) page-cnt)
        ]
    ;;(print seq page size count page-cnt)
    seq
    )
  )

;;; (pages-seq 2 3 25) 

(defn page-n [n res-name]
  (let [page (cur-page res-name)
        page (int page)
        count (session/get (keyword (str res-name "-count")))
        count (int count)
        n (or n 0)
        ret-page (+ page n)
        ret-page (if (< ret-page 1) 1 ret-page)
        ret-page (if (> ret-page count) count ret-page)
        ]
    ret-page
    ))

(defn prev-page [res-name]
  (page-n -1 res-name))

(defn next-page [res-name]
  (page-n 1 res-name))

(defn first-page? [res-name]
  (= (page-n 0 res-name) 1))

(defn last-page? [res-name]
  (let [count (session/get (keyword (str res-name "-count")))
        count (int count)
        size (session/get (keyword (str res-name "-size")))
        size (int (or size 5))
        page-cnt (Math/ceil (/ count size))
        ]
    (= (page-n 0 res-name) page-cnt)
    ))

(defn set-page-size [res-name size]
  (session/put! (keyword (str res-name "-size")) size)
  )

(defn size-list [res-name]
  [:div.ui.icon.dropdown.button.basic
   "Size"
   ;;[:span.text "Size"]
   [:i.icon.angle.down]
   [:div.menu
    [:div.item {:on-click #(set-page-size res-name 5)} "5"]
    [:div.item {:on-click #(set-page-size res-name 10)} "10"]
    [:div.item {:on-click #(set-page-size res-name 20)} "20"]
    [:div.item {:on-click #(set-page-size res-name 30)} "30"]
    [:div.item {:on-click #(set-page-size res-name 50)} "50"]
    [:div.item {:on-click #(set-page-size res-name 100)} "100"]
    ]
   ]
  )

(defn pages [res-name callback]
  [:div
   [:div.ui.basic.buttons
    (if (not (first-page? res-name)) ^{:key "prev"}
      [:button.ui.button {:on-click #(callback {:page (prev-page res-name)})} "prev"])
    (let [page (page-n 0 res-name)
          count (session/get (keyword (str res-name "-count")))
          count (int count)
          size (session/get (keyword (str res-name "-size")))
          size (int (or size 5))
          seq (pages-seq page size count)]
      (for [item seq] ^{:key item}
        [:button.ui.button
         {:class (if (= page item) "active")
          :on-click #(callback {:page item})} item]))
    (if (not (last-page? res-name)) ^{:key "next"}
      [:button.ui.button {:on-click #(callback {:page (next-page res-name)})} "next"])
    [size-list res-name]
    ]
   ])


(defn init-ui []
  ;(print "init-ui")
  (.accordion (js/$ ".ui.accordion"))
  ;(.sticky (js/$ ".ui.sticky"))
  ;(.sticky (js/$ ".ui.taglist.sticky") (js-obj "offset" 150))
  (.modal (js/$ ".ui.modal"))
  (.checkbox (js/$ ".checkbox"))

  (.dropdown (js/$ ".dropdown"))
  ;;(.sidebar (js/$ ".ui.labeled.icon.sidebar") "toggle")
  #_(.dropdown (js/$ ".dropdown.tag-list")
               (js-obj "keepOnScreen" true
                       "onAdd" func/on-filter-add-tag 
                       "onRemove" func/on-filter-rm-tag))
  (func/adjust-footer)
  ;;(.dropdown (js/$ ".dropdown.tag-list") "show")
  ;;(.tab (js/$ ".menu .item"))
  (.click (js/$ "#add-tag") add-tag)
  ;;(.log js/console (.-location js/window))
  ;;(print (.-href (.-location js/window)))
  )

(defn initui []
  (js/setTimeout #(init-ui)  200)
  )
