(let [groups [{:label "Map alloc"
               :values [{:label "Clojure"
                         :value 15.94}
                        {:label "jank before"
                         :value 30.86}
                        {:label "jank after"
                         :value 17}]}
              {:label "Map get"
               :values [{:label "Clojure"
                         :value 12.94}
                        {:label "jank before"
                         :value 5.12}
                        {:label "jank after"
                         :value 2.68}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "ns"})))
