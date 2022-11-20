(ns org.jank_lang.page.progress.view
  (:require [clojure.string]
            [org.jank_lang.page.view :as page.view]))

(def lex-parse-anal-eval [:lex :parse :analyze :eval])
(def lex-parse-anal-eval-done (into #{} lex-parse-anal-eval))
(def reader-macro [:lex])

; TODO: EDN file for each of these groups.
(def milestones [{:name "Clojure syntax parity"
                  :features [{:name "comments"
                              :tasks [:lex :parse]
                              :done #{:lex :parse}}
                             {:name "nil"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "integers"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "reals"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "bools"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "chars"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "strings"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "keywords/unqualified"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "keywords/qualified"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "keywords/auto-resolved-unqualified"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "keywords/auto-resolved-qualified"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "maps"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "vectors"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "sets"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "lists"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "regexes"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "symbols"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/def"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/if"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/do"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/let*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse :analyze}}
                             {:name "specials/quote"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/var"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/fn*/base"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/fn*/arities"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/fn*/variadic"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "specials/loop*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/recur"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/throw"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/try"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/monitor-enter"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "specials/monitor-exit"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "bindings/thread-local"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "bindings/conveyance"
                              :tasks lex-parse-anal-eval
                              :done #{}}
                             {:name "calls"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "destructuring"
                              :tasks lex-parse-anal-eval
                              :done #{:lex}}
                             {:name "macros"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "reader macros/shorthand fns"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/regex"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/quote"
                              :tasks reader-macro
                              :done lex-parse-anal-eval-done}
                             {:name "reader-macros/var"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/conditional"
                              :tasks reader-macro
                              :done #{}}]}
                 {:name "Clojure library parity"
                  :features [{:name "*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*'"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*1"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*2"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*3"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*agent*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*allow-unresolved-vars*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*assert*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*clojure-version*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*command-line-args*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*compile-files*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*compile-path*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*compiler-options*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*data-readers*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*default-data-reader-fn*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*e"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*err*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*file*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*flush-on-newline*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*fn-loader*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*in*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*math-context*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*ns*"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "*out*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-dup*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-length*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-level*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-meta*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-namespace-maps*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*print-readably*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*read-eval*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*reader-resolver*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*source-path*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*suppress-read*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*unchecked-math*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*use-context-classloader*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*verbose-defrecords*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "*warn-on-reflection*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "+"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "+'"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "-"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "-'"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->>"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->ArrayChunk"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->Eduction"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->Vec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->VecNode"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "->VecSeq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "-cache-protocol-fn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "-reset-methods"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name ".."
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "/"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "<"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "<="
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "="
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "=="
                              :tasks [:done :tested]
                              :done #{}}
                             {:name ">"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name ">="
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "EMPTY-NODE"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "Inst"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "NaN?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "PrintWriter-on"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "StackTraceElement->vec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "Throwable->map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "abs"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "accessor"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aclone"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "add-classpath"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "add-tap"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "add-watch"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "agent"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "agent-error"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "agent-errors"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aget"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "alength"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "alias"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "all-ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "alter"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "alter-meta!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "alter-var-root"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "amap"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ancestors"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "and"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "any?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "apply"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "areduce"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "array-map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "as->"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-boolean"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-byte"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-char"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-double"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-float"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-long"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "aset-short"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "assert"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "assoc"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "assoc!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "assoc-in"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "associative?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "atom"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "await"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "await-for"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "await1"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bases"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bean"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bigdec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bigint"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "biginteger"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "binding"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-and"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-and-not"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-clear"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-flip"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-not"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-or"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-shift-left"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-shift-right"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-test"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bit-xor"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "boolean"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "boolean-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "boolean?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "booleans"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bound-fn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bound-fn*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bound?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bounded-count"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "butlast"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "byte"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "byte-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bytes"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "bytes?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "case"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cast"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cat"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "char"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "char-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "char-escape-string"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "char-name-string"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "char?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chars"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-append"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-buffer"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-cons"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-first"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-next"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunk-rest"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "chunked-seq?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "class"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "class?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "clear-agent-errors"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "clojure-version"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "coll?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "comment"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "commute"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "comp"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "comparator"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "compare"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "compare-and-set!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "compile"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "complement"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "completing"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "concat"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cond"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cond->"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cond->>"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "condp"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "conj"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "conj!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cons"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "constantly"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "construct-proxy"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "contains?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "count"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "counted?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "create-ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "create-struct"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "cycle"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dec'"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "decimal?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "declare"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dedupe"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "default-data-readers"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "definline"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "definterface"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defmacro"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defmethod"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defmulti"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defn-"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defonce"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defprotocol"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defrecord"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "defstruct"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "deftype"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "delay"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "delay?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "deliver"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "denominator"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "deref"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "derive"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "descendants"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "destructure"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "disj"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "disj!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dissoc"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dissoc!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "distinct"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "distinct?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "doall"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dorun"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "doseq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dosync"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "dotimes"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "doto"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "double"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "double-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "double?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "doubles"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "drop"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "drop-last"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "drop-while"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "eduction"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "empty"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "empty?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ensure"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ensure-reduced"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "enumeration-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "error-handler"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "error-mode"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "eval"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "even?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "every-pred"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "every?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ex-cause"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ex-data"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ex-info"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ex-message"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "extend"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "extend-protocol"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "extend-type"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "extenders"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "extends?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "false?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ffirst"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "file-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "filter"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "filterv"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find-keyword"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find-ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find-protocol-impl"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find-protocol-method"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "find-var"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "first"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "flatten"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "float"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "float-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "float?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "floats"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "flush"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "fn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "fn?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "fnext"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "fnil"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "for"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "force"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "format"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "frequencies"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future-call"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future-cancel"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future-cancelled?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future-done?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "future?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "gen-class"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "gen-interface"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "gensym"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get-in"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get-method"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get-proxy-class"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get-thread-bindings"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "get-validator"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "group-by"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "halt-when"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash-combine"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash-map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash-ordered-coll"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash-set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "hash-unordered-coll"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ident?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "identical?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "identity"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "if-let"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "if-not"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "if-some"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ifn?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "import"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "in-ns"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "inc"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "inc'"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "indexed?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "infinite?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "init-proxy"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "inst-ms"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "inst-ms*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "inst?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "instance?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "int-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "int?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "integer?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "interleave"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "intern"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "interpose"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "into"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "into-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ints"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "io!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "isa?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "iterate"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "iteration"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "iterator-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "juxt"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "keep"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "keep-indexed"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "key"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "keys"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "keyword"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "keyword?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "last"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "lazy-cat"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "lazy-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "let"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "letfn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "line-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "list"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "list*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "list?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "load"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "load-file"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "load-reader"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "load-string"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "loaded-libs"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "locking"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "long"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "long-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "longs"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "loop"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "macroexpand"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "macroexpand-1"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "make-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "make-hierarchy"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "map-entry?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "map-indexed"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "map?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "mapcat"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "mapv"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "max"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "max-key"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "memfn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "memoize"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "merge"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "merge-with"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "meta"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "method-sig"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "methods"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "min"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "min-key"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "mix-collection-hash"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "mod"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "munge"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "name"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "namespace"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "namespace-munge"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nat-int?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "neg-int?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "neg?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "newline"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "next"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nfirst"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nil?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nnext"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "not"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "not-any?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "not-empty"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "not-every?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "not="
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-aliases"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-imports"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-interns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-name"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-publics"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-refers"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-resolve"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-unalias"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ns-unmap"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nth"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nthnext"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "nthrest"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "num"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "number?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "numerator"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "object-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "odd?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "or"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "parents"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "parse-boolean"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "parse-double"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "parse-long"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "parse-uuid"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "partial"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "partition"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "partition-all"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "partition-by"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pcalls"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "peek"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "persistent!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pmap"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pop"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pop!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pop-thread-bindings"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pos-int?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pos?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pr"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pr-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "prefer-method"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "prefers"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "primitives-classnames"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print-ctor"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print-dup"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print-method"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print-simple"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "print-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "printf"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "println"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "println-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "prn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "prn-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "promise"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "proxy"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "proxy-call-with-super"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "proxy-mappings"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "proxy-name"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "proxy-super"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "push-thread-bindings"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "pvalues"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "qualified-ident?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "qualified-keyword?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "qualified-symbol?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "quot"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rand"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rand-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rand-nth"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "random-sample"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "random-uuid"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "range"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ratio?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rational?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rationalize"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-find"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-groups"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-matcher"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-matches"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-pattern"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "re-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "read"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "read+string"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "read-line"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "read-string"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reader-conditional"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reader-conditional?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "realized?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "record?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reduce"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reduce-kv"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reduced"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reduced?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reductions"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ref"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ref-history-count"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ref-max-history"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ref-min-history"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "ref-set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "refer"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "refer-clojure"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reify"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "release-pending-sends"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rem"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove-all-methods"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove-method"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove-ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove-tap"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "remove-watch"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "repeat"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "repeatedly"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "replace"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "replicate"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "require"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "requiring-resolve"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reset!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reset-meta!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reset-vals!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "resolve"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rest"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "restart-agent"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "resultset-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reverse"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "reversible?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rseq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "rsubseq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "run!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "satisfies?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "second"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "select-keys"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "send"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "send-off"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "send-via"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "seq-to-map-for-destructuring"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "seq?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "seqable?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "seque"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sequence"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sequential?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set-agent-send-executor!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set-agent-send-off-executor!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set-error-handler!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set-error-mode!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set-validator!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "set?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "short"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "short-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "shorts"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "shuffle"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "shutdown-agents"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "simple-ident?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "simple-keyword?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "simple-symbol?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "slurp"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "some"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "some->"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "some->>"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "some-fn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "some?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sort"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sort-by"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sorted-map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sorted-map-by"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sorted-set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sorted-set-by"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "sorted?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "special-symbol?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "spit"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "split-at"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "split-with"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "string?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "struct"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "struct-map"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "subs"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "subseq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "subvec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "supers"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "swap!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "swap-vals!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "symbol"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "symbol?"
                              :tasks [:done :tested]
                              :done #{:done}}
                             {:name "sync"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "tagged-literal"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "tagged-literal?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "take"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "take-last"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "take-nth"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "take-while"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "tap>"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "test"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "the-ns"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "thread-bound?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "time"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "to-array"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "to-array-2d"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "trampoline"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "transduce"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "transient"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "tree-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "true?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "type"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-add"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-add-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-byte"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-char"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-dec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-dec-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-divide-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-double"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-float"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-inc"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-inc-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-long"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-multiply"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-multiply-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-negate"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-negate-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-remainder-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-short"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-subtract"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unchecked-subtract-int"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "underive"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unquote"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unquote-splicing"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unreduced"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "unsigned-bit-shift-right"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "update"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "update-in"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "update-keys"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "update-proxy"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "update-vals"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "uri?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "use"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "uuid?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "val"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vals"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "var-get"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "var-set"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "var?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vary-meta"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vec"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vector"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vector-of"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vector?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "volatile!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "volatile?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vreset!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "vswap!"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "when"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "when-first"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "when-let"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "when-not"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "when-some"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "while"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-bindings"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-bindings*"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-in-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-loading-context"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-local-vars"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-meta"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-open"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-out-str"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-precision"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-redefs"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "with-redefs-fn"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "xml-seq"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "zero?"
                              :tasks [:done :tested]
                              :done #{}}
                             {:name "zipmap"
                              :tasks [:done :tested]
                              :done #{}}]}
                 {:name "Native runtime"
                  :features [{:name "interop/include headers"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/link libraries"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/represent native objects"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/call native functions"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/explicitly box unbox native objects"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/refer to native globals"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/access native members"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/extract native value from jank object"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/convert native value to jank object"
                              :tasks [:done]
                              :done #{}}
                             {:name "interop/create native objects"
                              :tasks [:done]
                              :done #{}}]}
                 {:name "Gradual typing"
                  :features [{:name "type annotations"
                              :tasks [:done]
                              :done #{}}
                             {:name "infer left hand type"
                              :tasks [:done]
                              :done #{}}
                             {:name "infer right hand type"
                              :tasks [:done]
                              :done #{}}]}
                 {:name "Tooling"
                  :features [{:name "leiningen support"
                              :tasks [:done]
                              :done #{}}
                             {:name "nrepl support"
                              :tasks [:done]
                              :done #{}}
                             {:name "lsp support"
                              :tasks [:done]
                              :done #{}}]}])

(defn milestone-stats [{:keys [name features]}]
  (let [total+done (reduce (fn [acc feature]
                             (-> acc
                                 (update :total #(+ % (-> feature :tasks count)))
                                 (update :done #(+ % (-> feature :done count)))))
                           {:total 0
                            :done 0}
                           features)]
    (assoc total+done :percent-done (int (* 100 (/ (:done total+done) (:total total+done)))))))
(def milestone->stats (zipmap (map :name milestones) (map milestone-stats milestones)))

(defn feature->table-row [{:keys [name tasks done]}]
  (->> tasks
       (map (fn [task]
              (let [done? (contains? done task)]
                [:td {:class "is-vcentered"}
                 [:span {:class (str "icon-text " (if done?
                                                    "has-text-success"
                                                    "has-text-danger"))}
                  [:span {:class "icon mr-2"}
                   [:i {:class (if done?
                                 "gg-check-o"
                                 "gg-math-minus")}]]
                  (clojure.core/name task)]])))
       (into [:tr
              [:td name]])))

(defn milestone->table [{:keys [name features]}]
  [:tr
   [:th name]
   [:td
    [:table {:class "table is-fullwidth is-hoverable"}
     [:thead {}
      [:tr
       [:th {:width "25%"}
        "Feature"]
       [:th {:colspan "5"}
        (str "Status (Total percentage done " (:percent-done (milestone->stats name)) "%)")]]]
     (into [:tbody] (map feature->table-row features))]]])

(defn root []
  (page.view/page-root
    [:div {}
     (page.view/header {})

     [:section {:class "hero is-info"}
      [:div {:class "hero-body"}
       [:div {:class "container"}
        [:div {:class "content"}
         "jank is under heavy development. It's safest to assume that any
         feature advertised is partially developed or in the planning stages.
         There is no sales pitch here; just a lot of work and some big
         plans. All development happens on Github, so watch the repo there!"]
        [:div {:class "has-text-centered"}
         [:a {:class "button ml-4"
              :href "https://github.com/jank-lang/jank"}
          [:span {:class "icon"}
           [:i {:class "gg-git-fork"}]]
          [:strong "Github"]]
         [:a {:class "button ml-4"
              :href "https://github.com/sponsors/jeaye"}
          [:span {:class "icon"}
           [:i {:class "gg-heart"}]]
          [:strong "Sponsor"]]]]]]

     [:section {:id "milestones"
                :class "section"}
      [:div {:class "container"}
       [:table {:class "table is-fullwidth"}
        [:thead {}
         [:tr
          [:th {:width "20%"}
           "Milestone"]
          [:th]]]
        (into [:tbody] (map milestone->table milestones))]]]]))
