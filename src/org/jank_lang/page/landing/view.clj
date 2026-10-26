(ns org.jank_lang.page.landing.view
  (:require [clojure.string]
            [org.jank_lang.page.view :as page.view]
            [org.jank-lang.util :as util]))

(defn html->hiccup [props html]
  (->> (util/html->hiccup html)
       (into [:div (util/merge-attrs {:class "content has-text-left"} props)])))

; C++ interop
;   boost file size example
;   RAII
;   DSL
; Native build system
; nrepl
; aot compilation
; embedding
; clojure compatibility
; performance
; error reporting

; Feedback
;   mention these
;     Fast startup time, lower CPU and memory usage, standalone binaries, reach new platforms, and embedding clojure in non JVM applications.
;   maybe remove the imgui example and link to examples
;   [done] intro is left aligned; feels off balance
;   [done] intro can deliver more bang per word
;   [done] "the jank programming language" header is wasted real estate
;     some sort of tag line
;   [done] change "read the jank book" to something more engaging
;   [done] trim down intro and bring features + code up higher
;   [done] better term for "robust project management"
;     "native dependencies made easy" lands well

(defn root []
  (page.view/page-root
   {:title "jank programming language - Clojure/LLVM/C++"
    :description "jank is the native Clojure dialect with seamless C++ interop."}
   [:div {}
    (page.view/header {:home? false})

    #_[:section {:class "hero"}
     [:div {:class "hero-body pb-0"}
      [:div {:class "container has-text-centered"}
       [:p {:class "title"}
        "Delightful functional coding with seamless C++ access."]
       [:div {:class "columns"}
        [:div {:class "column is-offset-2 is-8"}
         #_[:p {:class "title"}
          "The jank programming language"]
         #_[:p {:class "content is-size-5"}
          [:p {:class "content is-size-5"}
           (util/markdown->hiccup "jank is a unique dialect of both **Clojure and C++**. It bridges the gap between the functional programming world and the systems programming world. jank allows for interactive REPL-driven development and includes a full Clang-based C++ JIT compiler.")]
          [:p {:class "content is-size-5"}
           (util/markdown->hiccup "jank has **world-class C++ interop**, unlike any other language. Templates, virtual functions, exceptions, overload resolution, and RAII all work in jank just as in C++. No function, type, or value registration is required. Just include your headers and go!")]]
         [:div {:class "has-text-centered"}
          [:a {:class "button mt-6 ml-4"
               :href "https://book.jank-lang.org/"
               :target "_blank"}
           [:span {:class "icon"}
            [:i {:class "gg-file-document"}]]
           [:strong "Install jank"]]
          [:a {:class "button mt-6 ml-4"
               :href "https://book.jank-lang.org/"
               :target "_blank"}
           [:span {:class "icon"}
            [:i {:class "gg-file-document"}]]
           [:strong "Learn jank!"]]]]
        #_[:div {:class "column is-2"
                 :style {:margin "auto"}}
           [:div [:img {:src "https://img.shields.io/github/stars/jeaye/jank"
                        :width "100px"}]]
           [:div [:img {:src "https://app.travis-ci.com/jeaye/jank.svg?branch=main"
                        :width "150px"}]]
           [:div [:img {:src "https://codecov.io/gh/jeaye/jank/branch/main/graph/badge.svg"
                        :width "150px"}]]
           [:div [:img {:src "https://img.shields.io/badge/libera%20irc-%23jank-blue"
                        :width "150px"}]]]]]]]

    [:section {:id "how-it-works"
               :class "section"}
     [:div {:class "container has-text-centered"}
      #_[:div {:class "section-header mb-6"}
       [:h2 {:class "title"}
        "Unprecedented C++ interop"]
       [:h3 {:class "subtitle"}
        "jank completely blurs the line between Clojure and C++."]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title has-text-left"}
         [:i {:class "has-text-info"} "Native"] " Clojure"]
        [:h3 {:class "title has-text-left"}
         [:i {:class "has-text-warning"} "Hand-crafted"] " compiler"]
        [:h3 {:class "title has-text-left"}
         [:i {:class "has-text-link"} "Unprecedented "] " C++ interop"]
        [:p {:class "content has-text-left"}
         [:b "jank is Clojure on top of C++. "]
         "It has C++ interop unlike any other language. Templates, virtual functions, exceptions, overload resolution, and RAII all just work in jank. No boilerplate necessary. Just include your headers and go!"]
        [:p {:class "content has-text-left"}
         [:b "jank is a robust Clojure dialect. "]
         "It offers interactive REPL-driven development powered by a Clang-based C++ JIT compiler."]

      [:div {:class "has-text-centered"}
       [:a {:class "button is-link mt-6 ml-4"
            :href "https://book.jank-lang.org/getting-started/01-installation.html"
            :target "_blank"}
        [:span {:class "icon"}
         [:i {:class "gg-file-document"}]]
        [:strong "Install jank"]]
       [:a {:class "button mt-6 ml-4"
            :href "https://book.jank-lang.org/"
            :target "_blank"}
        [:span {:class "icon"}
         [:i {:class "gg-file-document"}]]
        [:strong "Learn jank"]]]
        ]
       [:div {:class "column is-7"}
        (html->hiccup {} (util/slurp-html! "landing/boost-file-size.html"))]
       ]

      ]]

    [:section {:id "features"
               :class "section odd-section"}
     [:div {:class "container"}
      [:div {:class "section-header has-text-centered mb-6"}
       [:h2 {:class "title has-text-white"}
        "jank builds upon Clojure"]
       [:h3 {:class "subtitle has-text-white"}
        "Keep your existing code; unlock the native world."]]

      [:div {:class "columns is-centered"}
       [:div {:class "column is-9"}
        [:div {:class "columns"}
         [:div {:class "column is-6"}
          [:div {:class "icon-text mb-4"}
           [:span {:class "icon"}
            [:i {:class "gg-bulb"}]]
           [:h3 {:class "title is-4 has-text-white"}
            "jank is Clojure"]]
          [:p
           (util/markdown->hiccup "jank is a full-on Clojure dialect, with C++ interop instead of Java interop. There's a `:jank` reader conditional for when you need it.")]]

         [:div {:class "column is-6"}
          [:div {:class "icon-text mb-4"}
           [:span {:class "icon"}
            [:i {:class "gg-list"}]]
           [:h3 {:class "title is-4 has-text-white"}
            "jank is C++"]]
          [:p
           "jank can include any C++ source, link to any C or C++ library, and it uses the C++ type system. jank even compiles to C++!"]]]

        [:div {:class "columns"}
         [:div {:class "column is-6"}
          [:div {:class "icon-text mb-4"}
           [:span {:class "icon"}
            [:i {:class "gg-link"}]]
           [:h3 {:class "title is-4 has-text-white"}
            "Cargo-inspired tooling"]]
          [:p
           "jank uses Leiningen-based project management and has a rich native build system. It's easy to add C and C++ dependencies to your project."]]

         [:div {:class "column is-6"}
          [:div {:class "icon-text mb-4"}
           [:span {:class "icon"}
            [:i {:class "gg-sync"}]]
           [:h3 {:class "title is-4 has-text-white"}
            "REPL and native JIT compiler"]]
          [:p
           "Use your favorite nREPL editor plugin. jank contains an LLVM-based JIT runtime to compile C++ code on the fly."]]]]]]]

    #_[:section {:id "use-cases"
               :class "section"}
     [:div {:class "container has-text-centered"}
      [:div {:class "section-header mb-6"}
       [:h2 {:class "title"}
        "jank is perfect for"]
       [:h3 {:class "subtitle"}
        "Click into the tiles to explore jank's ideal use cases."]

      [:div {:class "tiles is-flex-wrap-wrap"}
       [:a {:class "button use-case-tile is-flex is-flex-direction-column"
            :style "gap: 0.5em;"
            :href "/use-case/game-development"}
        [:div
         [:span {:class "icon mr-4"}
          [:i {:class "gg-games"}]]
         [:strong "Game development"]]
        [:h2 {:class "subtitle is-size-7"}
         "jank provides easy access to Raylib, SDL, Box3D, and more."]]
       [:a {:class "button use-case-tile is-flex is-flex-direction-column"
            :style "gap: 0.5em;"
            :href "/use-case/desktop-gui"}
        [:div {:class "icon-text is-align-items-center"}
         [:span {:class "icon mr-4"}
          [:i {:class "gg-desktop"}]]
         [:strong "Desktop GUIs"]]
        [:h2 {:class "subtitle is-size-7"}
         "Take advantage of GUI frameworks like GTK, Slint, or even RayGUI."]]
       [:a {:class "button use-case-tile is-flex is-flex-direction-column"
            :style "gap: 0.5em;"
            :href "/use-case/desktop-tui"}
        [:div {:class "icon-text is-align-items-center"}
         [:span {:class "icon mr-4"}
          [:i {:class "gg-desktop"}]]
         [:strong "Desktop TUIs"]]
        [:h2 {:class "subtitle is-size-7"}
         "jank AOT compiles to easy-to-distribute binaries."]]]]]]

    [:section {:id "error-messages"
               :class "section"}
     [:div {:class "container has-text-centered"}
      [:div {:class "section-header mb-6"}
       [:h2 {:class "title has-text-white"}
        "jank's error messages are beautiful"]
       [:h3 {:class "subtitle has-text-white"}
        "Find your mistakes more easily and look good while doing it."]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title has-text-white"}
         "Trace your code through macros"]
        [:p {:class "has-text-left"}
         "jank's compiler will provide macro usage context and can re-trace source information to ensure you always get the best error possible."]]
       [:div {:class "column is-7 fluid-text-size-container"}
        [:img {:src "/img/blog/2026-09-04-better-and-better/def.png"
               :style "border-radius: 7px;"}]]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title has-text-white"}
         "Reimagined C++ errors"]
        [:p {:class "has-text-left"}
         "jank improves upon traditional Clang/GCC error messages for C++ interop, so writing C++ from jank feels even better than just writing normal C++."]]
       [:div {:class "column is-7 fluid-text-size-container"}
        [:img {:src "/img/blog/2026-09-04-better-and-better/rank.png"
               :style "border-radius: 7px;"}]
]]]]

     [:section {:id "sponsors"
       :class "section odd-section"}
      [:div {:class "container has-text-centered"}
       [:div {:class "section-header mb-6"}
        [:h2 {:class "title"}
         "jank's sponsors"]
        [:h3 {:class "subtitle"}
         "These organizations are investing in jank's future. You can help out, too! See "
         [:a {:href "https://github.com/sponsors/jeaye"
              :target "_blank"}
          "here"]
         "."]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         ; Clojurists Together
         [:a {:href "https://www.clojuriststogether.org/"
              :target "_blank"}
          [:img {:src "https://camo.githubusercontent.com/d557ab5a5618687a92c60df84db7bbc5037300701f1a18ac66db166b18e846aa/68747470733a2f2f7777772e636c6f6a757269737473746f6765746865722e6f72672f6865616465722d6c6f676f2e737667"
                 :style "border-radius: 7px;"
                 :width "75%"}]]]

        [:div {:class "column is-6"}
         ; Nubank
         [:a {:href "https://nubank.com.br/"
              :target "_blank"}
          [:img {:src "https://camo.githubusercontent.com/deacb1f05446ff6797e488b7a41405e0624601fc6f688012d9e78d99ee0666ae/68747470733a2f2f75706c6f61642e77696b696d656469612e6f72672f77696b6970656469612f636f6d6d6f6e732f662f66372f4e7562616e6b5f6c6f676f5f323032312e737667"
                 :style "border-radius: 7px;"
                 :width "75%"}]]]
        ]]]

    [:section {:id "final-cta"
               :class "section odd-section pb-0"}
     [:div {:class "container"}
      [:div {:class "section-header has-text-centered"}
       [:h2 {:class "title has-text-white"}
        "Try jank today!"]
       [:h3 {:class "subtitle has-text-white"}
        "You've made it this far. Why not dive in?"]
       [:div {:class "has-text-centered pb-6"}
        [:a {:class "button is-link ml-4"
             :href "https://book.jank-lang.org/getting-started/01-installation.html"
             :target "_blank"}
         [:span {:class "icon"}
          [:i {:class "gg-file-document"}]]
         [:strong "Install jank"]]
        [:a {:class "button ml-4"
             :href "https://book.jank-lang.org/"
             :target "_blank"}
         [:span {:class "icon"}
          [:i {:class "gg-file-document"}]]
         [:strong "Learn jank"]]]
       ]
      ]]

    ]))
