(let [groups [{:label "ray"
               :values [{:label "Clojure"
                         :value 2.53}
                        {:label "jank initial"
                         :value 8.10}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "s"})))
