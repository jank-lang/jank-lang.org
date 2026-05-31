(let [groups [{:label "ray"
               :values [{:label "Clojure"
                         :value 2.53}
                        {:label "jank previous"
                         :value 3.02}
                        {:label "jank current"
                         :value 2.37}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "s"})))
