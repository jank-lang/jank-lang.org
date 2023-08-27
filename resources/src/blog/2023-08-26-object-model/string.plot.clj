(let [groups [{:label "Short string alloc"
               :values [{:label "Clojure"
                         :value 8.69}
                        {:label "jank before"
                         :value 35.81}
                        {:label "jank after"
                         :value 25.40}]}
              {:label "Long string alloc"
               :values [{:label "Clojure"
                         :value 8.63}
                        {:label "jank before"
                         :value 64.10}
                        {:label "jank after"
                         :value 53.16}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
