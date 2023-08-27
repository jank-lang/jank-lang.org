(let [groups [{:label "Ray tracing"
               :values [{:label "Clojure"
                         :value 69.44}
                        {:label "jank before"
                         :value 69}
                        {:label "jank after"
                         :value 36.96}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ms"})))
