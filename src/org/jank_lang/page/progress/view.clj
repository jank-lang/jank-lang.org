(ns org.jank_lang.page.progress.view
  (:require [clojure.string]
            [org.jank_lang.page.view :as page.view]))

(def lex-parse-anal-eval [:lex :parse :analyze :eval])
(def lex-parse-anal-eval-done (into #{} lex-parse-anal-eval))
(def reader-macro [:lex])

(def progress [{:feature "comments"
                :tasks [:lex :parse]
                :done #{:lex}}
               {:feature "nil"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "integers/positive"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "integers/negative"
                :tasks lex-parse-anal-eval
                :done #{:parse :analyze :eval}}
               {:feature "floats/positive"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "floats/negative"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "bools"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "chars"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "strings"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "keywords/unqualified"
                :tasks lex-parse-anal-eval
                :done #{:lex :parse}}
               {:feature "keywords/qualified"
                :tasks lex-parse-anal-eval
                :done #{:lex :parse}}
               {:feature "keywords/aliased"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "maps"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "vectors"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "sets"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "lists"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "regexes"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "symbols"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "specials/def"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "specials/if"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/do"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/let*"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/quote"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "specials/var"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/fn*"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "specials/loop*"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/recur"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/throw"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/try"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/monitor-enter"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "specials/monitor-exit"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "bindings/thread-local"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "bindings/conveyance"
                :tasks lex-parse-anal-eval
                :done #{}}
               {:feature "calls"
                :tasks lex-parse-anal-eval
                :done lex-parse-anal-eval-done}
               {:feature "destructuring"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "macros"
                :tasks lex-parse-anal-eval
                :done #{:lex}}
               {:feature "reader-macros/shorthand-fns"
                :tasks reader-macro
                :done #{}}
               {:feature "reader-macros/regex"
                :tasks reader-macro
                :done #{}}
               {:feature "reader-macros/quote"
                :tasks reader-macro
                :done #{:lex}}
               {:feature "reader-macros/var"
                :tasks reader-macro
                :done #{}}
               {:feature "reader-macros/conditional"
                :tasks reader-macro
                :done #{}}])
(def task-stats (reduce (fn [acc feature-progress]
                          (-> acc
                              (update :total #(+ % (-> feature-progress :tasks count)))
                              (update :done #(+ % (-> feature-progress :done count)))))
                        {:total 0
                         :done 0}
                        progress))
(def done-percent (int (* 100 (/ (:done task-stats) (:total task-stats)))))

(defn feature-progress->table-row [{:keys [feature tasks done]}]
  (->> tasks
       (map (fn [task]
              (let [done? (contains? done task)]
                [:td {:class "is-vcentered"}
                 [:span {:class (str "icon-text " (if done?
                                                    "has-text-success"
                                                    "has-text-danger"))}
                  [:span {:class "icon mr-2"}
                   [:i {:class (if done?
                                 "gg-check-o"
                                 "gg-math-minus")}]]
                  (name task)]])))
       (into [:tr
              [:th feature]])))

(defn root []
  (page.view/page-root
    [:div {}
     (page.view/header {})

     [:section {:class "hero is-info"}
      [:div {:class "hero-body"}
       [:div {:class "container"}
        [:div {:class "content"}
         "jank is under heavy development. It's safest to assume that any
         feature advertised is partially developed or in the planning stages.
         There is no sales pitch here; just a lot of work and some big
         plans. All development happens on Github, so watch the repo there!"]
        [:div {:class "has-text-centered"}
         [:a {:class "button ml-4"
              :href "https://github.com/jeaye/jank"}
          [:span {:class "icon"}
           [:i {:class "gg-git-fork"}]]
          [:strong "Github"]]]]]]

     [:section {:id "progress"
                :class "section"}
      [:div {:class "container"}
       [:table {:class "table is-fullwidth is-hoverable"}
        [:thead {}
         [:tr
          [:th {:width "25%"}
           "Feature"]
          [:th {:colspan "5"}
           (str "Status (Total percentage done " done-percent "%)")]]]
        (into [:tbody] (map feature-progress->table-row progress))]]]]))
