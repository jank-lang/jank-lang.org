(ns org.jank_lang.page.use-case.view
  (:require [org.jank_lang.page.view :as page.view]
            [org.jank-lang.util :as util]))

(defn- root [title description & body]
  (page.view/page-root
   {:title title
    :description description}
   (into [:div
    (page.view/header {:home? true})] body)))

(defn game-development-root []
  (root "Game development with jank"
        "Using jank to build games."
        [:div {:class "section-header mb-6"}
         [:section {:id "features"
                    :class "section"}
          [:div {:class "container"}
           [:div {:class "section-header has-text-centered mb-6"}
            [:h2 {:class "title"}
             "jank is " [:i {:class "has-text-link"} "perfect for game development"]]
            [:h3 {:class "subtitle"}
             "Either build your whole game in jank or embed it as a scripting library."]]

           [:div {:class "columns is-centered"}
            [:div {:class "column is-9"}
             [:div {:class "columns"}
              [:div {:class "column is-6"}
               [:div {:class "icon-text mb-4"}
                [:span {:class "icon"}
                 [:i {:class "gg-bulb"}]]
                [:h3 {:class "title is-4"}
                 "Immutable data"]]
               [:p "Data structures in jank are immutable by default, making multi-threading trivial. The provided mutable containers are atomic by default, so you don't need synchronization."]]

              [:div {:class "column is-6"}
               [:div {:class "icon-text mb-4"}
                [:span {:class "icon"}
                 [:i {:class "gg-list"}]]
                [:h3 {:class "title is-4"}
                 "Access to rich C++ ecosystem"]]
               [:p
                "C++ is the de facto game dev language and all of that code is still at your fingertips, within jank. We have Raylib, SDL, Box 2D/3D, ImGUI, and much more."]]]

           [:div {:class "columns"}
            [:div {:class "column is-6"}
             [:div {:class "icon-text mb-4"}
              [:span {:class "icon"}
               [:i {:class "gg-link"}]]
              [:h3 {:class "title is-4"}
               "Competitive performance"]]
             [:p
              "jank compiles to C++ and allows you to trivially switch between data-oriented functional programming and straight up systems programming, within the same file."]]

            [:div {:class "column is-6"}
             [:div {:class "icon-text mb-4"}
              [:span {:class "icon"}
               [:i {:class "gg-sync"}]]
              [:h3 {:class "title is-4"}
               "Interactive development"]]
             [:p
              "Forget hot reloading. Connect your text editor to your running game and inspect the values! Redefine any function or evaluate any singular expression with ease. This is the way."]]]]]]]

         [:section {:id "bare-bones"
                    :class "section odd-section"}
          [:div {:class "container has-text-centered"}
           [:div {:class "section-header mb-6"}
            [:h2 {:class "title has-text-white"}
             "Bare-bones library examples"]
            [:h3 {:class "subtitle has-text-white"}
             "Click on your preferred libary to start building your own engine or your own light-weight games."]]

           [:div {:class "columns"}
            [:div {:class "column is-4"}
             [:a {:href ""
                  :target "_blank"}
              [:img {:class "library-image"
                    :src "/img/use-case/game-development/sdl.webp"}]]]

            [:div {:class "column is-4"}
              [:a {:href ""
                  :target "_blank"}
               [:img {:class "library-image"
                     :src "/img/use-case/game-development/raylib.png"}]]]

            [:div {:class "column is-4"}
             [:a {:href ""
                  :target "_blank"}
              [:img {:class "library-image"
                    :src "/img/use-case/game-development/imgui.png"}]]]
            ]]]

          #_[:section {:id "full-games"
                    :class "section"}
          [:div {:class "container has-text-centered"}
           [:div {:class "section-header mb-6"}
            [:h2 {:class "title"}
             "Full game examples"]
            [:h3 {:class "subtitle"}
             "Check out these open source games written in jank!"]]

           [:div {:class "columns is-vcentered"}
            [:div {:class "column"}
             [:img {:src "/img/blog/2026-09-04-better-and-better/def.png"
                    :style "width: 20vw;"}]
             [:p {:class "subtitle is-6"}
              "Simple DirectMedia Layer (SDL) is a cross-platform software development library designed to provide a hardware abstraction layer for computer multimedia hardware components."]]
            [:div {:class "column"}
              [:img {:src "/img/blog/2026-09-04-better-and-better/def.png"
                     :style "width: 20vw;"}]
              [:p {:class "subtitle is-6"}
              "raylib is a cross-platform open-source software development library. The library was made to create graphical applications and games."]]]

            ]]

        ]))

; Bare bones examples:
;   Raylib
;   SDL
;   ImGUI
;
; See more at awesome-jank list
;
; Full examples:
;   Kyle's 4x4 driver?
;   Sand painting?
;   Game jam entries

(defn desktop-gui-root []
  (root "Desktop GUI and TUI applications with jank"
        "Build native desktop GUI and terminal applications with jank."
        "Desktop GUI / TUI"
        "jank lets desktop GUI and terminal applications use Clojure's interactive workflow alongside native C and C++ libraries."))

(defn desktop-tui-root []
  (root "Desktop GUI and TUI applications with jank"
        "Build native desktop GUI and terminal applications with jank."
        "Desktop GUI / TUI"
        "jank lets desktop GUI and terminal applications use Clojure's interactive workflow alongside native C and C++ libraries."))
