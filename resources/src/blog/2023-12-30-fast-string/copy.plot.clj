(let [groups [{:label "Small string copy"
               :values [{:label "jank"
                         :value 0.87}
                        {:label "libstdc++"
                         :value 3.53}
                        {:label "folly"
                         :value 0.95}]}
              {:label "Large string copy"
               :values [{:label "jank"
                         :value 4.70}
                        {:label "libstdc++"
                         :value 15.04}
                        {:label "folly"
                         :value 17.58}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
