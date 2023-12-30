(let [groups [{:label "Small string creation"
               :values [{:label "jank"
                         :value 3.29}
                        {:label "libstdc++"
                         :value 4.93}
                        {:label "folly"
                         :value 4.01}]}
              {:label "Large string creation"
               :values [{:label "jank"
                         :value 17.92}
                        {:label "libstdc++"
                         :value 17.71}
                        {:label "folly"
                         :value 21.01}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
