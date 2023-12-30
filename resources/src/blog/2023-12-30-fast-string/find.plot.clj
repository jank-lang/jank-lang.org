(let [groups [{:label "Small string find"
               :values [{:label "jank"
                         :value 3.56}
                        {:label "libstdc++"
                         :value 3.31}
                        {:label "folly"
                         :value 4.77}]}
              {:label "Large string find"
               :values [{:label "jank"
                         :value 10.68}
                        {:label "libstdc++"
                         :value 10.48}
                        {:label "folly"
                         :value 32.67}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
