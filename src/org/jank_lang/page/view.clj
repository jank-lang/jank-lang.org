(ns org.jank_lang.page.view
  (:require [clojure.string]
            [hiccup.page :as page]
            [stringer.core :refer [strcat]]
            [taoensso.timbre :as timbre]
            [org.jank_lang.page.util :as page.util]))

(defn header [_request]
  [:div
   [:nav {:class "navbar is-white"}
    [:div {:class "container"}
     [:div {:class "navbar-brand"}
      [:a {:class "navbar-item title"
           :href "/"
           :style {:font-family "Comfortaa"}}
       "jank"]]

     [:div {:class "navbar-menu"}
      [:div {:class "navbar-end has-text-weight-semibold"}
       [:a {:class "navbar-item"
            :href "/progress"}
        [:span {:class "icon mr-1"}
         [:i {:class "gg-list"}]]
        [:strong "Progress"]]
       [:a {:class "navbar-item"
            :href "https://github.com/jeaye/jank"}
        [:span {:class "icon mr-1"}
         [:i {:class "gg-git-fork"}]]
        [:strong "Github"]]
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
    ; TODO: Configure my own bulma css.
    [:link {:rel "stylesheet"
            :href "/css/main.css"}]
    [:link {:rel "stylesheet"
            :href "https://css.gg/css?=sync|bulb|list|link|git-fork|info|comment|math-minus|check-o|heart"}]
    [:title "jank programming language - Clojure/LLVM/Gradual Typing"]

    ; TODO: Include this font myself.
    "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
    <link href=\"https://fonts.googleapis.com/css2?family=Comfortaa\" rel=\"stylesheet\"> "

    "<!-- Matomo -->
    <script>
    var _paq = window._paq = window._paq || [];
    _paq.push(['trackPageView']);
    _paq.push(['enableLinkTracking']);
    (function() {
                 var u=\"//matomo.jeaye.com/\";
                 _paq.push(['setTrackerUrl', u+'matomo.php']);
                 _paq.push(['setSiteId', '1']);
                 var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
                 g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
                 })();
    </script>
    <noscript><p><img src=\"//matomo.jeaye.com/matomo.php?idsite=1&amp;rec=1\" style=\"border:0;\" alt=\"\" /></p></noscript>
    <!-- End Matomo Code -->"

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
