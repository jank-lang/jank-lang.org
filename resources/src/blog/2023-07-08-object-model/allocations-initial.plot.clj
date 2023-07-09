(let [groups [{:label "Initial"
               :values [{:label "Clojure"
                         :value 15.94}
                        {:label "jank"
                         :value 30.86}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 400
                :unit "ns"})))
