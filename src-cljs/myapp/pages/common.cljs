(ns myapp.pages.common
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            [myapp.func :as func :refer [up-state! app-state]]

            [myapp.fn.about :refer [get-avatar]]
            [myapp.fn.star :refer [get-stars]]
            ))

(defn test-t []
  (GET (func/token-url "t"){:handler #(get-stars)})
  )

(defn get-data []
  (func/get-items "auth"
             (fn [res]
               (get-avatar)
               )))


(defn header []
  (let [page (session/get :page)]
    [:div.ui.header.sticky.fixed.top {:style {:background "#f9f9f7"
                                              :left "0px"
                                              :width "1913px !important"
                                              :height "46px !important"
                                              :margin-top "0px"
                                              }}
     [:div.ui.orange.bottom.attached.progress {:id "load-progress" :style {:height "0.1rem"}} [:div.bar]]
     [:div.ui.menu.secondary.pointing
      {:style {:border-bottom "1px solid rgba(34, 36, 38, 0.05)" :margin-top "0px" }}
      [:div.ui.container
       [:a.item {:class (if (= :home page) "active")
                 :href "/#/"} "Home"]
       [:a.item {:class (if (= :blog page) "active")
                 :href "/#/blog"} "Blog"]
       [:a.item {:class (if (= :star page) "active")
                 :href "/#/star"} "GitStars"]
       [:a.item {:class (if (= :task page) "active")
                 :href "/#/task"} "Task"]
       [:a.item {:class (if (= :boutique page) "active")
                 :href "/#/boutique"} "Boutique"]
       (if (= "https://api.softidy.com" js/remote_domain )
         [:a.item {:class (if (= :enroll page) "active")
                   :href "/#/enroll"} "Enroll"])
       [:a.item {:class (if (= :about page) "active")
                 :href "/#/about"} "About"]
       [:div.right.menu
        [:div.item
         [:div.ui.icon.input
          [:input {:type "text" :placeholder "Seach..."}]
          [:i.search.link.icon]]]
        (if-let [github-name (:githubLogin (session/get :user))]
          [:a.ui.item {:on-click test-t} github-name]
          [:a.ui.item {:on-click func/github-authorize } "Login"]
          )
        [:a.ui.item {:on-click func/do-test} "Test"]
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
        btn-num 8
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
        seq (conj (vec seq) page-cnt)]
    seq
    )
  )

;;; (pages-seq 2 3 25) 

(defn pages [res-name callback]
  [:div.ui.basic.buttons
   (let [page (session/get (keyword (str res-name "-page")))
         page (int page)
         count (session/get (keyword (str res-name "-count")))
         count (int count)
         size (session/get (keyword (str res-name "-size")))
         size (int size)
         seq (pages-seq page size count)]
     (for [item seq] ^{:key item}
          [:button.ui.blue.button
           {:class (if (= page item) "active")
            :on-click #(callback {:page item})}
           item]
          )
     )
   ]
  )
