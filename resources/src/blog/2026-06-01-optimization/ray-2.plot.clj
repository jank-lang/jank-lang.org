(let [groups [{:label "ray"
               :values [{:label "Clojure"
                         :value 2.53}
                        {:label "jank previous"
                         :value 8.10}
                        {:label "jank current"
                         :value 4.16}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "s"})))
