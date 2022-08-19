(defproject org.jank-lang "0.1.0-SNAPSHOT"
  :url "https://jank-lang.org"
  :dependencies [[org.clojure/clojure "1.12.0-alpha1"]

                 ; Page building.
                 [garden "1.3.10"]
                 [hiccup "2.0.0-alpha2"]
                 [hickory "0.7.1"]
                 [markdown-clj "1.11.2"]

                 ; Optimized alternatives.
                 [stringer "0.4.1"]

                 ; HTTP
                 [ring/ring-core "2.0.0-alpha1"]
                 [ring/ring-devel "2.0.0-alpha-1"]
                 [ring/ring-jetty-adapter "2.0.0-alpha1"]
                 [compojure "1.7.0"]

                 ; Utils.
                 [com.taoensso/timbre "5.2.1"]
                 [com.fzakaria/slf4j-timbre "0.3.21-6-9f1d565-SNAPSHOT"]
                 [clj-commons/fs "1.6.310"]]
  :main ^:skip-aot org.jank-lang
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
