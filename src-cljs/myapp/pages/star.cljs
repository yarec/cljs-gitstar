(ns myapp.pages.star
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [GET POST PUT]]
            [clojure.walk :refer [keywordize-keys]]
            [myapp.func :as func]
            [myapp.pages.common :refer [pages]]

            [myapp.fn.star :refer [get-stars]]
            [myapp.fn.tag :refer [edit-tag]]
            ))

(defn star-list [items]
  [:div.ui.items.divided ;menu.vertical.fluid
   [:div.item
    [:a.ui.basic {:href "http://github.com/stars" :target "_blank"}
     "github-star"]
    ]
   (for [item items]
     (let [star (or (:star item) item)] ^{:key (:id item)}
          [:div.item {:style {:padding "2em"}}
           [:div.content
            [:a.ui.large.header.olive
             {:href (:html_url star ) :target "_blank"} (:login star) "/"]
            [:a.ui.large.header.green
             {:href (:html_url star ) :target "_blank"} [:strong (:name star)]]
            [:div.meta [:span [:p (:description star)]]]
            [:div.extra
             [:div.ui.grid
              [:i.star.icon] (:stargazers_count star)
              (:language star)
              (for [tag (:tags item )] ^{:key  tag}
                   [:a.ui.floated.circular.label tag]
                   )
              [edit-tag "stars" item]]]]]))])

(defn tag-list [items]
  [:div.ui.vertical.fluid.secondary;.menu
   #_(
      [:div.ui.multiple.active.dropdown.tag-list
       [:input {:type "hidden" :name "filters" :width "150px"}]
       [:i.filter.icon {:width "50px"}]
       [:span.text "Filter Posts"]
       [:div.menu
        [:div.ui.icon.search.input
         [:i.search.icon]
         [:input.text {:placeholder "Search tags..."}]
         ]
        [:div.divider]
        [:div.header
         [:i.tags.icon "Tag label"]]
        [:div.scrolling.menu
         (for [item items] ^{:key item}
              [:div.item {:data-value (:name item)}
               [:div.ui.blue.empty.circular.label ] (:name item) 
               [:div.ui.label (:count item)]
               ]
              )
         ]
        ]
       ]
      )

   #_(
   [:a.item {:on-click #(get-stars "all")}
    "All" [:div.ui.label (count (session/get :stars))]]
   [:a.item {:on-click #(get-stars "")}
    "Other" [:div.ui.label (count (session/get :stars))]]
   (for [item items] ^{:key item}
        [:a.item {:on-click #(get-stars (:name item))}
         (:name item)
         [:div.ui.label (:count item)]])
   )
   (for [item 
         (into [{:name "all" :count (session/get :stars-all-count) :key "all"}
                {:name "other" :count (session/get :stars--count) :key "other"}]
           items)
         ] ^{:key item}
    [:div.ui.button.basic.item
     {:on-click #(get-stars (:name item))
      :style {:margin "2px"
              :position "relative"
              :color "red !important"
              :padding "10px"
              :padding-right "15px"
              :font-size "16px !important"
              }}
     (:name item)
     [:div.floating.ui.gray.basic.label;.circular
      {:style {:margin-top "8px !important"
               :margin-right "0px !important"
               :padding "2px !important"
               :border "0px !important"
               :background-color "rgba(0, 0, 0, 0.03) !important"
               :color "green !important"}}
      (:count item)]
     ])
   ]
  )

(defn lang-list [items]
  [:div.ui.vertical.menu.fluid.secondary
   (for [item items]
     ^{:key item}
     [:a.item "lang" [:div.ui.label "100" item ]])])

(defn right-menu []
  [:div.ui.styled.accordion
   [:div.active.title [:i.dropdown.icon] "Filter by Tags"]
   [:div.active.content [tag-list (session/get :tags)]]
   [:div.title [:i.dropdown.icon] "Filter by languages"]
   [:div.content [lang-list (range 10)]]])

(defn search []
  [:div.ui.search
   [:div.ui.icon.input
    [:input.prompt {:type "text" :placeholder "Search items"}]
    [:i.search.icon]]
   [:div.ui.divider]
   [:div.results]])

(defn stars []
  [:div.ui.container
   [:br]
   [search]
   [:div.ui.grid
    [:div.twelve.wide.column [star-list (session/get :stars)]]
    [:div.four.wide.column [:div.ui.taglist.sticky [right-menu]]]
    ]
   ])

(defn star-page []
  [stars])
