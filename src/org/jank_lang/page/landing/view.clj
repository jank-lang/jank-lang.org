(ns org.jank_lang.page.landing.view
  (:require [clojure.string]
            [org.jank_lang.page.view :as page.view]
            [org.jank-lang.util :as util]))

(defn html->hiccup [props html]
  (->> (util/html->hiccup html)
       (into [:div (util/merge-attrs {:class "content has-text-left"} props)])))

(defn root []
  (page.view/page-root
    {:title "jank programming language - Clojure/LLVM/C++"
     :description "jank is a Clojure dialect on LLVM with a native runtime and C++ interop."}
    [:div {}
     (page.view/header {})

     [:section {:class "hero is-info"}
      [:div {:class "hero-body"}
       [:div {:class "container py-6"}
        [:div {:class "columns"}
         [:div {:class "column mt-6 is-offset-2 is-8"}
          [:p {:class "title"}
           "The jank programming language"]
          [:p {:class "content is-size-5"}
           [:p {:class "content is-size-5"}
            (util/markdown->hiccup "jank is a **general-purpose programming language**
                                   which embraces the **interactive, value-oriented**
                                   nature of Clojure as well as the desire for **native
                                   compilation and minimal runtimes**. jank is **strongly
                                   compatible with Clojure** and considers itself a dialect
                                   of Clojure. Please note that jank is under heavy development;
                                   assume all features are planned or incomplete.")]

           [:p {:class "content is-size-5"}
            (util/markdown->hiccup "Where jank differs from Clojure JVM is that its host
                                   is C++ on top of an **LLVM-based JIT**. This allows jank
                                   to offer the same benefits of **REPL-based
                                   development** while being able to **seamlessly reach into
                                   the native world** and compete seriously with JVM's performance.")]

           [:p {:class "content is-size-5"}
            (util/markdown->hiccup "Still, jank is a Clojure dialect and thus includes
                                   its **code-as-data philosophy and powerful macro
                                   system**. jank remains a functional-first language
                                   which builds upon Clojure's rich set of
                                   **persistent, immutable data structures**. When
                                   mutability is needed, jank offers a software
                                   transaction memory and reactive agent system to
                                   ensure **clean and correct multi-threaded designs**.")]]
          [:div {:class "has-text-centered"}
           [:a {:class "button mt-6 ml-4"
                :href "/progress"}
            [:span {:class "icon"}
             [:i {:class "gg-list"}]]
            [:strong "Current progress"]]
           [:a {:class "button mt-6 ml-4"
                :href "https://github.com/jank-lang/jank"}
            [:span {:class "icon"}
             [:i {:class "gg-git-fork"}]]
            [:strong "Github"]]
           [:a {:class "button mt-6 ml-4"
                :href "https://github.com/sponsors/jeaye"}
            [:span {:class "icon"
                    :style "color: rgb(201, 97, 152);"}
             [:i {:class "gg-heart"}]]
            [:strong "Sponsor"]]]]
         #_[:div {:class "column is-2"
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
         ]]]]

     [:section {:id "how-it-works"
                :class "section"}
      [:div {:class "container has-text-centered"}
       [:div {:class "section-header mb-6"}
        [:h2 {:class "title"}
         "Wide spectrum dynamics"]
        [:h3 {:class "subtitle"}
         "Enjoy both REPL iteration with JIT compilation and static AOT
         compilation to native executables."]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:span {:class "is-size-1"}
          "01"]
         [:h3 {:class "title"}
          "Iterate like you would with Clojure"]
         [:p {:class "has-text-left"}
          "Iterate in the REPL and build your program from the ground up without leaving your editor."]]
        [:div {:class "column is-6"}
         (html->hiccup {} (util/slurp-html! "landing/step-1.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:span {:class "is-size-1"}
          "02"]
         [:h3 {:class "title"}
          "Reach into the native world"]
         [:p {:class "has-text-left"}
          "Seamlessly switch to inline C++ within your Clojure source, while still having access
          to your Clojure code using interpolation."]]
        [:div {:class "column is-6"}
         (html->hiccup {} (util/slurp-html! "landing/step-2.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:span {:class "is-size-1"}
          "03"]
         [:h3 {:class "title"}
          "Compile to machine code"]
         [:p {:class "has-text-left"}
          "jank is built on an LLVM-based JIT. With AOT enabled, both
          statically and dynamically linked executables can be generated. The
          jank compiler itself has very speedy start times and low memory
          usage."]]
        [:div {:class "column is-6"}
         (html->hiccup {} (util/slurp-html! "landing/step-3-jank.html"))
         (html->hiccup {:class "mt-4"} (util/slurp-html! "landing/step-3-clj.html"))]]]]

     [:section {:id "features"
                :class "section has-background-primary has-text-white"}
      [:div {:class "container"}
       [:div {:class "section-header has-text-centered mb-6"}
        [:h2 {:class "title has-text-white"}
         "jank builds upon Clojure."]
        [:h3 {:class "subtitle has-text-white"}
         "Keep your existing code; gain more confidence and more speed."]]

       [:div {:class "columns is-centered"}
        [:div {:class "column is-9"}
         [:div {:class "columns"}
          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-bulb"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "Strongly compatible with Clojure"]]
           [:p
            "Any Clojure library without interop will compile into your jank projects."]]

          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-sync"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "REPL and native JIT"]]
           [:p
            "Use your favorite nREPL editor plugin. jank uses an LLVM-based JIT to compile machine code on the fly."]]]

         [:div {:class "columns"}
          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-list"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "Go native"]]
           [:p
            "Reach into native libraries or interact directly with your native code base. Seamlessly write both C++ and Clojure in the same file."]]

          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-link"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "Tooling friendly"]]
           [:p
            "Leiningen, LSP, nREPL planned from the start. jank's compiler is also written with tooling in mind, so it can be used for lexing, parsing, and analysis."]]
          ]
         ]]]]

     ; TODO: Interop
     ; TODO: Macros
     [:section {:id "examples"
                :class "section"}
      [:div {:class "container has-text-centered"}
       [:div {:class "section-header mb-6"}
        [:h2 {:class "title"}
         "jank examples"]
        [:h3 {:class "subtitle"}
         "All of the following examples are valid also Clojure code."]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Generate a movie index"]
         [:p {:class "has-text-left"}
          (util/markdown->hiccup "jank has very powerful capabilities for
                                 representing and transforming arbitrary data. Here,
                                 idiomatic usages of `reduce`, `zipmap`, `repeat`,
                                 and `merge-with` help create an index from genre to
                                 movie id with ease. No lenses are required for
                                 working with nested data.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (util/slurp-html! "landing/example/movies.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Convert bytes to human readable format"]
         [:p {:class "has-text-left"}
          (util/markdown->hiccup "Beyond the traditional `map`, `filter`, and `reduce`, jank provides
                                 a powerful `loop` macro for more imperative-style
                                 loops while still being purely functional. Each `loop` has one or more
                                 corresponding `recur` usages which must be in tail position.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (util/slurp-html! "landing/example/size-human-readable.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Truncate a string to a max length"]
         [:p {:class "has-text-left"}
          (util/markdown->hiccup "jank's strings, as well as most of its other data structures, are
                                 immutable. However, jank provides such powerful tools for working
                                 with data that mutability is very rarely a concern.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (util/slurp-html! "landing/example/truncate-string.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Redefine any var"]
         [:p {:class "has-text-left"}
          (util/markdown->hiccup "Every `def` or `defn` exists within a var, which is a stable,
                                 namespace-level container for values. Vars can be redefined to
                                 contain different values. `with-redefs` redefines a var within its
                                 body's scope, which is very useful for removing side effects from
                                 test cases or forcing functions to return specific values.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (util/slurp-html! "landing/example/with-redefs.html"))]]]]
     ]))
