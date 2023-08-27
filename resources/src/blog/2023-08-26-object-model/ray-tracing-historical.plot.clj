(let [groups [{:label "Ray tracing"
               :values [{:label "Clojure"
                         :value 69.44}
                        {:label "Old 'n' busted"
                         :value 797.49}
                        {:label "New hotness"
                         :value 36.96}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ms"})))
