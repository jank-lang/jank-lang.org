(ns org.jank_lang.page.landing.view
  (:require [clojure.string]
            [hickory.core :as hickory]
            [markdown.core :as markdown]
            [org.jank_lang.page.util :refer [merge-attrs] :as page.util]
            [org.jank_lang.page.view :as page.view]))

(defn slurp-html! [html-path]
  (slurp (str "resources/generated/html/" html-path)))

(defn html->hiccup [props html]
  (->> (map hickory/as-hiccup (hickory/parse-fragment html))
       (into [:div (merge-attrs {:class "has-text-left"} props)])))

(defn markdown->hiccup [md]
  ; TODO: Add custom class to code tags to distinguish from shiki code.
  (->> (clojure.string/replace md #"\s+" " ")
       (markdown/md-to-html-string)
       (html->hiccup {:style {:line-height "1.7em"}})))

(defn root []
  (page.view/page-root
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
            (markdown->hiccup "jank is a **general-purpose programming language**
                              which embraces the **interactive, value-oriented**
                              nature of Clojure as well as the desire for **native
                              compilation and minimal runtimes**. jank is **100%
                              compatible with Clojure**.")]

           [:p {:class "content is-size-5"}
            (markdown->hiccup "Where jank differs from Clojure is that its host
                              is C++ on top of an **LLVM-based JIT**. Furthermore,
                              jank has a built-in **gradual type system** which
                              allows for malli-style type annotations which
                              result in **static type analysis**. This allows jank
                              to offer the same benefits of **REPL-based
                              development** while being able to reach much further
                              into the lands of both **correctness and
                              performance**.")]

           [:p {:class "content is-size-5"}
            (markdown->hiccup "Still, jank is a Clojure dialect and thus includes
                              its **code-as-data philosophy and powerful macro
                              system**. jank remains a functional-first language
                              which builds upon Clojure's rich set of
                              **persistent, immutable data structures**. When
                              mutability is needed, jank offers a software
                              transaction memory and reactive Agent system to
                              ensure **clean and correct multi-threaded designs**.")]]
          [:div {:class "has-text-centered"}
           [:a {:class "button mt-6 ml-4"
                :href "https://github.com/jeaye/jank"}
            [:span {:class "icon"}
             [:i {:class "gg-git-fork"}]]
            [:strong "Github"]]]]
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
         "Enjoy both dynamic typing and static typing gradually. Enjoy both
         REPL iteration with JIT compilation and static AOT compilation to
         native executables."]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:span {:class "is-size-1"}
          "01"]
         [:h3 {:class "title"}
          "Iterate like you would with Clojure"]
         [:p {:class "has-text-left"}
          "As you iterate in the REPL and figure out your data shapes, static
          typing will not be in your way."]]
        [:div {:class "column is-6"}
         (html->hiccup {} (slurp-html! "landing/step-1.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:span {:class "is-size-1"}
          "02"]
         [:h3 {:class "title"}
          "Add type annotations to lock down data shapes"]
         [:p {:class "has-text-left"}
          "Rather than using spec or malli to define your contracts, use jank's
          malli-like type definitions and then gain static type checking for
          any direct or indirect uses of that data."]]
        [:div {:class "column is-6"}
         (html->hiccup {} (slurp-html! "landing/step-2.html"))]]

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
         (html->hiccup {} (slurp-html! "landing/step-3-jank.html"))
         (html->hiccup {:class "mt-4"} (slurp-html! "landing/step-3-clj.html"))]]]]

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
             "100% compatible with Clojure"]]
           [:p
            "Any Clojure library without interop will compile into your jank projects."]]

          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-sync"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "REPL and native JIT"]]
           [:p
            "Use your favority nrepl editor plugin. jank uses an LLVM-based JIT to compile machine code on the fly."]]]

         [:div {:class "columns"}
          [:div {:class "column is-6"}
           [:div {:class "icon-text mb-4"}
            [:span {:class "icon"}
             [:i {:class "gg-list"}]]
            [:h3 {:class "title is-4 has-text-white"}
             "Be gradual"]]
           [:p
            "Add types where you want them or disable static typing altogether. Generate dynamic binaries or static binaries, both using AOT compilation. Your choice."]]

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
          (markdown->hiccup "jank has very powerful capabilities for
                            representing and transforming arbitrary data. Here,
                            idiomatic usages of `reduce`, `zipmap`, `repeat`,
                            and `merge-with` help create an index from genre to
                            movie id with ease. No lenses are required for
                            working with nested data.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (slurp-html! "landing/example/movies.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Convert bytes to human readable format"]
         [:p {:class "has-text-left"}
          (markdown->hiccup "Beyond the traditional `map`, `filter`, and `reduce`, jank provides
                            a powerful `loop` macro for more imperative-style
                            loops while still being purely functional. Each meow `loop` has one or more
                            corresponding `recur` usages which must be in tail position.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (slurp-html! "landing/example/size-human-readable.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Truncate a string to a max length"]
         [:p {:class "has-text-left"}
          (markdown->hiccup "jank's strings, as well as most of its other data structures, are
                            immutable. However, jank provides such powerful tools for working
                            with data that mutability is very rarely a concern.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (slurp-html! "landing/example/truncate-string.html"))]]

       [:div {:class "columns is-vcentered"}
        [:div {:class "column is-6"}
         [:h3 {:class "title"}
          "Redefine any var"]
         [:p {:class "has-text-left"}
          (markdown->hiccup "Every `def` or `defn` exists within a var, which is a stable,
                            namespace-level container for values. Vars can be redefined to
                            contain different values. `with-redefs` redefines a var within its
                            body's scope, which is very useful for removing side effects from
                            test cases or forcing functions to return specific values.")]]
        [:div {:class "column is-8"}
         (html->hiccup {} (slurp-html! "landing/example/with-redefs.html"))]]]]
     ]))
