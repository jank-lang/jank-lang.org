(let [groups [{:label "C++"
               :values [{:label "Load from source"
                         :value 12}
                        {:label "Compile module"
                         :value (* 60 4)}
                        {:label "Load from compiled module"
                         :value 0.300}]}
              {:label "LLVM IR"
               :values [{:label "Load from source"
                         :value 2}
                        {:label "Compile module"
                         :value 2}
                        {:label "Load from compiled module"
                         :value 0.150}]}]]
  (with-page {}
    (bar-chart {:groups groups
                :height 500
                :unit "s"})))
