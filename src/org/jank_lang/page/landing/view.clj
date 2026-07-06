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

(defn root []
  (page.view/page-root
   {:title "jank programming language - Clojure/LLVM/C++"
    :description "jank is the native Clojure dialect with seamless C++ interop."}
   [:div {}
    (page.view/header {:home? false})

    [:section {:class "hero"}
     [:div {:class "hero-body pb-0"}
      [:div {:class "container"}
       [:div {:class "columns"}
        [:div {:class "column is-offset-2 is-8"}
         [:p {:class "title"}
          "The jank programming language"]
         [:p {:class "content is-size-5"}
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
           [:strong "Read the jank book!"]]]]
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
      [:div {:class "section-header mb-6"}
       [:h2 {:class "title"}
        "Unprecedented C++ interop"]
       [:h3 {:class "subtitle"}
        "jank completely blurs the line between Clojure and C++."]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title"}
         "Include any C++ library"]
        [:p {:class "has-text-left"}
         "jank can seamlessly blend between statically typed C++ interop and dynamically typed Clojure code. Bring in any C++ library you'd like and then wrap it in immutable data."]]
       [:div {:class "column is-7"}
        (html->hiccup {} (util/slurp-html! "landing/boost-file-size.html"))]]
      ]]

    [:section {:id "features"
               :class "section has-background-primary has-text-white"}
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
            "Robust project management"]]
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

    [:section {:id "how-it-works"
               :class "section"}
     [:div {:class "container has-text-centered"}
      [:div {:class "section-header mb-6"}
       [:h2 {:class "title"}
        "Native dependencies made easy"]
       [:h3 {:class "subtitle"}
        "jank brings a Cargo-inspired UX to C++ and Clojure."]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title"}
         "Easy project management"]
        [:p {:class "has-text-left"}
         "jank's native build system can find your installed native dependencies or build them from source. Adding a new dependency is a one line change."]]
       [:div {:class "column is-7 fluid-text-size-container"}
        (html->hiccup {} (util/slurp-html! "landing/imgui-project.html"))]]

      [:div {:class "columns is-vcentered"}
       [:div {:class "column is-5"}
        [:h3 {:class "title"}
         "jank is great for game dev"]
        [:p {:class "has-text-left"}
         "jank brings functional, immutable data and REPL-driven development to your game development workflow. With jank, you can reach into C++ whenever you want the extra performance. No bridges, no function/type registration, no marshalling, no callbacks."]]
       [:div {:class "column is-7 fluid-text-size-container"}
         (html->hiccup {} (util/slurp-html! "landing/imgui.html"))]]]]

    [:section {:id "features"
               :class "section has-background-primary has-text-white"}
     [:div {:class "container"}
      [:div {:class "section-header has-text-centered mb-6"}
       [:h2 {:class "title has-text-white"}
        "Delightful AOT compilation"]
       [:h3 {:class "subtitle has-text-white"}
        "Tinker all you want, but generate a compact binary when you're ready."]
       [:h3 {:class "subtitle has-text-white"}
        "Startup will be instantaneous."]]

      [:div {:class "columns is-centered"}
       [:div {:class "container"}
        [:div {:class "has-text-centered"}
         [:img {:src "/img/landing/lein-compile.png"
                :style "border-radius: 7px;"
                :width "75%"}]]]]]]

    [:section {:id "sponsors"
               :class "section"}
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
               :class "section has-background-primary has-text-white pb-0"}
     [:div {:class "container"}
      [:div {:class "section-header has-text-centered"}
       [:h2 {:class "title has-text-white"}
        "Try jank today!"]
       [:h3 {:class "subtitle"}
        "You've made it this far. Why not dive in?"]
       [:div {:class "has-text-centered"}
        [:a {:class "button mt-4 mb-4"
             :href "https://book.jank-lang.org/"}
         [:span {:class "icon"}
          [:i {:class "gg-file-document"}]]
         [:strong "Read the jank book!"]]]]
      ]]

    ]))
