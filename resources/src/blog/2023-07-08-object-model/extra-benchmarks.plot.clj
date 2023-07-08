(let [groups [{:label "Map Get"
               :values [{:label "Clojure"
                         :value 20.74}
                        {:label "jank: initial"
                         :value 4.61}
                        {:label "jank: tagged"
                         :value 1.75}]}
              {:label "Map Count"
               :values [{:label "Clojure"
                         :value 20.24}
                        {:label "jank: initial"
                         :value 2.52}
                        {:label "jank: tagged"
                         :value 0.75}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 400})))
