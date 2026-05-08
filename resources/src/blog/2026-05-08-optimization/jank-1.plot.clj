(let [groups [{:label "Fibonacci"
               :values [{:label "Clojure"
                         :value 200}
                        {:label "jank initial"
                         :value 5522}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ms"})))
