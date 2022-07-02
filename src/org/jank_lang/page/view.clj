(ns org.jank_lang.page.view
  (:require [clojure.string]
            [hiccup.page :as page]
            [stringer.core :refer [strcat]]
            [taoensso.timbre :as timbre]
            [org.jank_lang.page.util :as page.util]))

(defn header [_request]
  [:div
   #_[:div {:class "has-text-white has-background-primary py-4 has-text-centered"}
    "jank is currently under heavy development. Feedback is welcome!"]

   [:nav {:class "navbar is-white"}
    [:div {:class "container"}
     [:div {:class "navbar-brand"}
      [:a {:class "navbar-item title"
           :href "/"}
       "jank"]]

     [:div {:class "navbar-menu"}
      [:div {:class "navbar-end has-text-weight-semibold"}
       #_[:a {:class "navbar-item"
            :href "#"}
        [:span {:class "icon mr-1"}
         [:i {:class "gg-info"}]]
        "User Manual"]
       [:a {:class "navbar-item"
            :href "https://web.libera.chat/?channel=#jank"}
        [:span {:class "icon mr-1"}
         [:i {:class "gg-comment"}]]
        "Community"]]]]]])

(defn page-root [& body]
  (page/html5
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:link {:rel "icon"
            :type "image/svg+xml"
            :href "/img/favicon.svg"}]
    [:link {:rel "stylesheet"
            :href "/css/main.css"}]
    ; TODO: Configure my own bulma css.
    [:link {:rel "stylesheet"
            :href "https://css.gg/css?=sync|bulb|list|link|git-fork|info|comment"}]
    [:title "jank programming language - Clojure/LLVM/Gradual Typing"]
    (conj (into [:body] body)
          [:footer {:class "footer"}
           [:div {:class "container"}
            [:div {:class "columns has-text-centered"}
             #_[:div {:class "column is-pulled-right"
                    :style {:margin "auto"}}
              [:div [:img {:src "https://img.shields.io/github/stars/jeaye/jank"
                           :width "100px"}]]
              [:div [:img {:src "https://app.travis-ci.com/jeaye/jank.svg?branch=main"
                           :width "150px"}]]
              [:div [:img {:src "https://codecov.io/gh/jeaye/jank/branch/main/graph/badge.svg"
                           :width "150px"}]]
              [:div [:img {:src "https://img.shields.io/badge/libera%20irc-%23jank-blue"
                           :width "150px"}]]
              ]
             [:div {:class "column has-text-centered"}
              [:aside {:class "menu"}
               [:p {:class "menu-label"}
                "Resources"]
               [:ul {:class "menu-list"}
                #_[:li [:a {:href "#"} "User Manual"]]
                [:li [:a {:href "https://web.libera.chat/?channel=#jank"} "Community"]]
                [:li [:a {:href "https://github.com/jeaye/jank"} "Github"]]]
               ]]]
            [:div {:class "container has-text-centered"}
             [:div {:class "content is-small"}
              [:p
               "© 2022 Jeaye Wilkerson | All rights reserved."]]]
            ]])))
