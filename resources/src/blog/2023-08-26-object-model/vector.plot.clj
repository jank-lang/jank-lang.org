(let [groups [{:label "Vector alloc"
               :values [{:label "Clojure"
                         :value 143}
                        {:label "jank before"
                         :value 152.76}
                        {:label "jank after"
                         :value 136.33}]}
              {:label "Vector get"
               :values [{:label "Clojure"
                         :value 36.55}
                        {:label "jank before"
                         :value 6.58}
                        {:label "jank after"
                         :value 3.62}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
