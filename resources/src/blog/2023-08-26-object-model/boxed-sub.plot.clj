(let [groups [{:label "Boxed sub"
               :values [{:label "Clojure"
                         :value 50.32}
                        {:label "jank before"
                         :value 38.37}
                        {:label "jank after"
                         :value 16.52}]}
              {:label "Partially boxed sub"
               :values [{:label "Clojure"
                         :value 10.41}
                        {:label "jank before"
                         :value 14.63}
                        {:label "jank after"
                         :value 2.06}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
