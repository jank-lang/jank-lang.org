(let [groups [{:label "Fibonacci"
               :values [{:label "Clojure"
                         :value 200}
                        {:label "jank previous"
                         :value 5522}
                        {:label "jank current"
                         :value 2309}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ms"})))
