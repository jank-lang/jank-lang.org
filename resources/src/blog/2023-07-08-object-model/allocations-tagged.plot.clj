(let [groups [{:label "Initial"
               :values [{:label "Clojure"
                         :value 15.94}
                        {:label "jank"
                         :value 30.86}]}
              {:label "Tagged"
               :values [{:label "Clojure"
                         :value 15.94}
                        {:label "jank"
                         :value 17}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 400})))
