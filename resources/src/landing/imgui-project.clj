(defproject imgui+glfw "0.1-SNAPSHOT"
  :dependencies [[org.jank-lang.commons/imgui-glfw-sys "2026.06-6"]
                 [org.jank-lang.commons/imgui-opengl2-sys "2026.06-6"]
                 [org.jank-lang.commons/gl-sys "2026.06-1"]]
  :plugins [[org.jank-lang/lein-jank "2026.06-2"]]
  :middleware [leiningen.jank/middleware]
  :main imgui+glfw.main
  :profiles {:base {:jank {:target-dir "target/debug"
                           :optimization-level 0}}
             :release {:jank {:target-dir "target/release"
                              :optimization-level 3}}})
