(let [groups [{:label "Startup time"
               :values [{:label "JIT"
                         :value 12}
                        {:label "Clojure"
                         :value 0.655}
                        {:label "PCM"
                         :value 0.3}
                        {:label "AOT"
                         :value 0.05}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 300
                :unit "s"})))
