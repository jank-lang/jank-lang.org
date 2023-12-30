(let [groups [{:label "Small string substr"
               :values [{:label "jank"
                         :value 3.59}
                        {:label "libstdc++"
                         :value 3.59}
                        {:label "folly"
                         :value 4.79}]}
              {:label "Large string substr"
               :values [{:label "jank"
                         :value 1.68}
                        {:label "libstdc++"
                         :value 14.70}
                        {:label "folly"
                         :value 15.73}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
