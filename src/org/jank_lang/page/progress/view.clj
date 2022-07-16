(ns org.jank_lang.page.progress.view
  (:require [clojure.string]
            [org.jank_lang.page.view :as page.view]))

(def lex-parse-anal-eval [:lex :parse :analyze :eval])
(def lex-parse-anal-eval-done (into #{} lex-parse-anal-eval))
(def reader-macro [:lex])

(def milestones [{:name "Clojure parity"
                  :features [{:name "comments"
                              :tasks [:lex :parse]
                              :done #{:lex}}
                             {:name "nil"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "integers/positive"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "integers/negative"
                              :tasks lex-parse-anal-eval
                              :done #{:parse :analyze :eval}}
                             {:name "floats/positive"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "floats/negative"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "bools"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "chars"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "strings"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "keywords/unqualified"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "keywords/qualified"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "keywords/aliased"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "maps"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "vectors"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "sets"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "lists"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "regexes"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "symbols"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/def"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/if"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/do"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/let*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse :analyze}}
                             {:name "specials/quote"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/var"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/fn*"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/loop*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/recur"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/throw"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/try"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/monitor-enter"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/monitor-exit"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "bindings/thread-local"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "bindings/conveyance"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "calls"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "destructuring"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "macros"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "reader macros/shorthand fns"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/regex"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/quote"
                              :tasks reader-macro
                              :done #{:lex}}
                             {:name "reader-macros/var"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/conditional"
                              :tasks reader-macro
                              :done #{}}]}
                 {:name "Native runtime"
                  :features [{:name "interop/include headers"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/link libraries"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/represent native objects"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/call native functions"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/explicitly box unbox native objects"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/refer to native globals"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/access native members"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/extract native value from jank object"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/convert native value to jank object"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/create native objects"
                              :tasks [:done]
                              :done #{}}]}
                 {:name "Gradual typing"
                  :features [{:name "type annotations"
                              :tasks [:done]
                              :done #{}}
                             {:name "infer left hand type"
                              :tasks [:done]
                              :done #{}}
                             {:name "infer right hand type"
                              :tasks [:done]
                              :done #{}}]}
                 {:name "Tooling"
                  :features [{:name "leiningen support"
                              :tasks [:done]
                              :done #{}}
                             {:name "nrepl support"
                              :tasks [:done]
                              :done #{}}
                             {:name "lsp support"
                              :tasks [:done]
                              :done #{}}]}])

(defn milestone-stats [{:keys [name features]}]
  (let [total+done (reduce (fn [acc feature]
                             (-> acc
                                 (update :total #(+ % (-> feature :tasks count)))
                                 (update :done #(+ % (-> feature :done count)))))
                           {:total 0
                            :done 0}
                           features)]
    (assoc total+done :percent-done (int (* 100 (/ (:done total+done) (:total total+done)))))))
(def milestone->stats (zipmap (map :name milestones) (map milestone-stats milestones)))

(defn feature->table-row [{:keys [name tasks done]}]
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
                  (clojure.core/name task)]])))
       (into [:tr
              [:td name]])))

(defn milestone->table [{:keys [name features]}]
  [:tr
   [:th name]
   [:td
    [:table {:class "table is-fullwidth is-hoverable"}
     [:thead {}
      [:tr
       [:th {:width "25%"}
        "Feature"]
       [:th {:colspan "5"}
        (str "Status (Total percentage done " (:percent-done (milestone->stats name)) "%)")]]]
     (into [:tbody] (map feature->table-row features))]]])

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
          [:strong "Github"]]
         [:a {:class "button ml-4"
              :href "https://github.com/sponsors/jeaye"}
          [:span {:class "icon"}
           [:i {:class "gg-heart"}]]
          [:strong "Sponsor"]]]]]]

     [:section {:id "milestones"
                :class "section"}
      [:div {:class "container"}
       [:table {:class "table is-fullwidth"}
        [:thead {}
         [:tr
          [:th {:width "20%"}
           "Milestone"]
          [:th]]]
        (into [:tbody] (map milestone->table milestones))]]]]))
