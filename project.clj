(defproject org.jank-lang "0.1.0-SNAPSHOT"
  :url "https://jank-lang.org"
  :dependencies [[org.clojure/clojure "1.10.1"]

                 ; Page building.
                 [garden "1.3.10"]
                 [hiccup "2.0.0-alpha2"]
                 [hickory "0.7.1"]
                 [markdown-clj "1.11.1"]

                 ; Optimized alternatives.
                 [stringer "0.4.1"]

                 ; HTTP
                 [ring/ring-core "1.9.4"]
                 [ring/ring-devel "1.9.4"]
                 [ring/ring-jetty-adapter "1.9.4"]
                 [compojure "1.7.0"]

                 ; Utils.
                 [com.taoensso/timbre "5.1.2"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]
                 [clj-commons/fs "1.6.310"]]
  :main ^:skip-aot org.jank-lang
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
