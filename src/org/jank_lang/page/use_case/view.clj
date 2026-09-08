(ns org.jank_lang.page.use-case.view
  (:require [org.jank_lang.page.view :as page.view]))

(defn- root [title description heading content]
  (page.view/page-root
   {:title title
    :description description}
   [:div
    (page.view/header {:home? true})
    [:section {:class "section"}
     [:div {:class "container"}
      [:div {:class "section-header has-text-centered mb-6"}
       [:h1 {:class "title"} heading]
       [:p {:class "subtitle"} content]]]]]))

(defn game-development-root []
  (root "Game development with jank"
        "Use jank's Clojure workflow and seamless C++ interop for game development."
        "Game development"
        "jank brings functional, immutable data and REPL-driven development to game development, with direct access to C++ whenever performance matters."))

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
