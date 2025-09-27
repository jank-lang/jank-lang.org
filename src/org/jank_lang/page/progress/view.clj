(ns org.jank_lang.page.progress.view
  (:require [clojure.string]
            [hiccup2.core :as hiccup.core]
            [hiccup.util]
            [org.jank_lang.page.view :as page.view])
  (:import org.apache.commons.text.StringEscapeUtils))

(def lex-parse-anal-eval [:lex :parse :analyze :eval])
(def lex-parse-anal-eval-done (into #{} lex-parse-anal-eval))
(def reader-macro [:lex :parse])
(def reader-macro-done (into #{} reader-macro))

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
                              :done lex-parse-anal-eval-done}
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
                              :done lex-parse-anal-eval-done}
                             {:name "maps"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "vectors"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "sets"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "lists"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "symbols"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "ratios"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/def"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/if"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/do"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/let*"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/quote"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/var"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/fn*/base"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/fn*/arities"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/fn*/variadic"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/fn*/recur"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/loop*"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/loop*/recur"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/throw"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/try"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "specials/monitor-enter"
                              :tasks #{:na}
                              :done #{:na}}
                             {:name "specials/monitor-exit"
                              :tasks #{:na}
                              :done #{:na}}
                             {:name "specials/set!"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "specials/case*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "specials/letfn*"
                              :tasks lex-parse-anal-eval
                              :done #{:lex :parse}}
                             {:name "bindings/thread-local"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "bindings/conveyance"
                              :tasks [:done]
                              :done #{}}
                             {:name "calls"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "destructuring"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "macros"
                              :tasks lex-parse-anal-eval
                              :done lex-parse-anal-eval-done}
                             {:name "macros/&env param"
                              :tasks #{:pass :set}
                              :done #{:pass}}
                             {:name "syntax-quoting"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "syntax-quoting/unquote"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "meta hints"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader macros/comment"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader macros/set"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader macros/shorthand fns"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader-macros/regex"
                              :tasks reader-macro
                              :done #{}}
                             {:name "reader-macros/deref"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader-macros/quote"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader-macros/var quoting"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader-macros/conditional"
                              :tasks reader-macro
                              :done reader-macro-done}
                             {:name "reader-macros/tagged-literal"
                              :tasks reader-macro
                              :done #{}}]}
                 {:name "Clojure library parity"
                  :features [{:name "*", :tasks [:done], :done #{:done}}
                             {:name "*'", :tasks [:done], :done #{}}
                             {:name "*1", :tasks [:done], :done #{}}
                             {:name "*2", :tasks [:done], :done #{}}
                             {:name "*3", :tasks [:done], :done #{}}
                             {:name "*agent*", :tasks [:done], :done #{}}
                             {:name "*allow-unresolved-vars*", :tasks [:done], :done #{}}
                             {:name "*assert*", :tasks [:done], :done #{:done}}
                             {:name "*clojure-version*", :tasks [:done], :done #{}}
                             {:name "*command-line-args*", :tasks [:done], :done #{}}
                             {:name "*compile-files*", :tasks [:done], :done #{:done}}
                             {:name "*compile-path*", :tasks [:done], :done #{}}
                             {:name "*compiler-options*", :tasks [:done], :done #{}}
                             {:name "*data-readers*", :tasks [:done], :done #{}}
                             {:name "*default-data-reader-fn*", :tasks [:done], :done #{}}
                             {:name "*e", :tasks [:done], :done #{}}
                             {:name "*err*", :tasks [:done], :done #{}}
                             {:name "*file*", :tasks [:done], :done #{:done}}
                             {:name "*flush-on-newline*", :tasks [:done], :done #{}}
                             {:name "*fn-loader*", :tasks [:done], :done #{}}
                             {:name "*in*", :tasks [:done], :done #{}}
                             {:name "*math-context*", :tasks [:done], :done #{}}
                             {:name "*ns*", :tasks [:done], :done #{:done}}
                             {:name "*out*", :tasks [:done], :done #{}}
                             {:name "*print-dup*", :tasks [:done], :done #{}}
                             {:name "*print-length*", :tasks [:done], :done #{}}
                             {:name "*print-level*", :tasks [:done], :done #{}}
                             {:name "*print-meta*", :tasks [:done], :done #{}}
                             {:name "*print-namespace-maps*", :tasks [:done], :done #{}}
                             {:name "*print-readably*", :tasks [:done], :done #{}}
                             {:name "*read-eval*", :tasks [:done], :done #{}}
                             {:name "*reader-resolver*", :tasks [:done], :done #{}}
                             {:name "*source-path*", :tasks [:done], :done #{}}
                             {:name "*suppress-read*", :tasks [:done], :done #{}}
                             {:name "*unchecked-math*", :tasks [:done], :done #{}}
                             {:name "*verbose-defrecords*", :tasks [:done], :done #{}}
                             {:name "+", :tasks [:done], :done #{:done}}
                             {:name "+'", :tasks [:done], :done #{}}
                             {:name "-", :tasks [:done], :done #{:done}}
                             {:name "-'", :tasks [:done], :done #{}}
                             {:name "->", :tasks [:done], :done #{:done}}
                             {:name "->>", :tasks [:done], :done #{:done}}
                             {:name "/", :tasks [:done], :done #{:done}}
                             {:name "<", :tasks [:done], :done #{:done}}
                             {:name "<=", :tasks [:done], :done #{:done}}
                             {:name "=", :tasks [:done], :done #{:done}}
                             {:name "==", :tasks [:done], :done #{:done}}
                             {:name ">", :tasks [:done], :done #{:done}}
                             {:name ">=", :tasks [:done], :done #{:done}}
                             {:name "Inst", :tasks [:done], :done #{}}
                             {:name "NaN?", :tasks [:done], :done #{:done}}
                             {:name "accessor", :tasks [], :done #{}}
                             {:name "aclone", :tasks [:done], :done #{}}
                             {:name "add-classpath", :tasks [:done], :done #{}}
                             {:name "add-tap", :tasks [:done], :done #{}}
                             {:name "add-watch", :tasks [:done], :done #{}}
                             {:name "agent", :tasks [:done], :done #{}}
                             {:name "agent-error", :tasks [:done], :done #{}}
                             {:name "agent-errors", :tasks [:done], :done #{}}
                             {:name "aget", :tasks [:done], :done #{}}
                             {:name "alength", :tasks [:done], :done #{}}
                             {:name "alias", :tasks [:done], :done #{:done}}
                             {:name "all-ns", :tasks [:done], :done #{}}
                             {:name "alter", :tasks [:done], :done #{}}
                             {:name "alter-meta!", :tasks [:done], :done #{:done}}
                             {:name "alter-var-root", :tasks [:done], :done #{:done}}
                             {:name "amap", :tasks [:done], :done #{}}
                             {:name "ancestors", :tasks [:done], :done #{:done}}
                             {:name "and", :tasks [:done], :done #{:done}}
                             {:name "any?", :tasks [:done], :done #{:done}}
                             {:name "apply", :tasks [:done], :done #{:done}}
                             {:name "areduce", :tasks [:done], :done #{}}
                             {:name "array-map", :tasks [:done], :done #{}}
                             {:name "as->", :tasks [:done], :done #{:done}}
                             {:name "aset", :tasks [:done], :done #{}}
                             {:name "aset-boolean", :tasks [:done], :done #{}}
                             {:name "aset-byte", :tasks [:done], :done #{}}
                             {:name "aset-char", :tasks [:done], :done #{}}
                             {:name "aset-double", :tasks [:done], :done #{}}
                             {:name "aset-float", :tasks [:done], :done #{}}
                             {:name "aset-int", :tasks [:done], :done #{}}
                             {:name "aset-long", :tasks [:done], :done #{}}
                             {:name "aset-short", :tasks [:done], :done #{}}
                             {:name "assert", :tasks [:done], :done #{:done}}
                             {:name "assoc", :tasks [:done], :done #{:done}}
                             {:name "assoc!", :tasks [:done], :done #{:done}}
                             {:name "assoc-in", :tasks [:done], :done #{:done}}
                             {:name "associative?", :tasks [:done], :done #{:done}}
                             {:name "atom", :tasks [:done], :done #{:done}}
                             {:name "await", :tasks [:done], :done #{}}
                             {:name "await-for", :tasks [:done], :done #{}}
                             {:name "await1", :tasks [:done], :done #{}}
                             {:name "bases", :tasks [:na], :done #{:na}}
                             {:name "bean", :tasks [:done], :done #{}}
                             {:name "bigdec", :tasks [:done], :done #{}}
                             {:name "bigint", :tasks [:done], :done #{}}
                             {:name "biginteger", :tasks [:done], :done #{}}
                             {:name "binding", :tasks [:done], :done #{:done}}
                             {:name "bit-and", :tasks [:done], :done #{:done}}
                             {:name "bit-and-not", :tasks [:done], :done #{:done}}
                             {:name "bit-clear", :tasks [:done], :done #{:done}}
                             {:name "bit-flip", :tasks [:done], :done #{:done}}
                             {:name "bit-not", :tasks [:done], :done #{:done}}
                             {:name "bit-or", :tasks [:done], :done #{:done}}
                             {:name "bit-set", :tasks [:done], :done #{:done}}
                             {:name "bit-shift-left", :tasks [:done], :done #{:done}}
                             {:name "bit-shift-right", :tasks [:done], :done #{:done}}
                             {:name "bit-test", :tasks [:done], :done #{:done}}
                             {:name "bit-xor", :tasks [:done], :done #{:done}}
                             {:name "boolean", :tasks [:done], :done #{:done}}
                             {:name "boolean-array", :tasks [:done], :done #{}}
                             {:name "boolean?", :tasks [:done], :done #{:done}}
                             {:name "booleans", :tasks [:done], :done #{}}
                             {:name "bound-fn", :tasks [:done], :done #{}}
                             {:name "bound-fn*", :tasks [:done], :done #{:done}}
                             {:name "bound?", :tasks [:done], :done #{:done}}
                             {:name "bounded-count", :tasks [:done], :done #{:done}}
                             {:name "butlast", :tasks [:done], :done #{:done}}
                             {:name "byte", :tasks [:done], :done #{}}
                             {:name "byte-array", :tasks [:done], :done #{}}
                             {:name "bytes", :tasks [:done], :done #{}}
                             {:name "bytes?", :tasks [:done], :done #{}}
                             {:name "case", :tasks [:done], :done #{}}
                             {:name "cast", :tasks [:done], :done #{}}
                             {:name "cat", :tasks [:done], :done #{:done}}
                             {:name "char", :tasks [:done], :done #{}}
                             {:name "char-array", :tasks [:done], :done #{}}
                             {:name "char-escape-string", :tasks [:done], :done #{}}
                             {:name "char-name-string", :tasks [:done], :done #{}}
                             {:name "char?", :tasks [:done], :done #{:done}}
                             {:name "chars", :tasks [:done], :done #{}}
                             {:name "chunk", :tasks [:done], :done #{:done}}
                             {:name "chunk-append", :tasks [:done], :done #{:done}}
                             {:name "chunk-buffer", :tasks [:done], :done #{:done}}
                             {:name "chunk-cons", :tasks [:done], :done #{:done}}
                             {:name "chunk-first", :tasks [:done], :done #{:done}}
                             {:name "chunk-next", :tasks [:done], :done #{:done}}
                             {:name "chunk-rest", :tasks [:done], :done #{:done}}
                             {:name "chunked-seq?", :tasks [:done], :done #{:done}}
                             {:name "class", :tasks [:done], :done #{}}
                             {:name "class?", :tasks [:done], :done #{}}
                             {:name "clear-agent-errors", :tasks [:done], :done #{}}
                             {:name "clojure-version", :tasks [:done], :done #{:done}}
                             {:name "coll?", :tasks [:done], :done #{:done}}
                             {:name "comment", :tasks [:done], :done #{:done}}
                             {:name "commute", :tasks [:done], :done #{}}
                             {:name "comp", :tasks [:done], :done #{:done}}
                             {:name "comparator", :tasks [:done], :done #{:done}}
                             {:name "compare", :tasks [:done], :done #{:done}}
                             {:name "compare-and-set!", :tasks [:done], :done #{:done}}
                             {:name "compile", :tasks [:done], :done #{:done}}
                             {:name "complement", :tasks [:done], :done #{:done}}
                             {:name "completing", :tasks [:done], :done #{:done}}
                             {:name "concat", :tasks [:done], :done #{:done}}
                             {:name "cond", :tasks [:done], :done #{:done}}
                             {:name "cond->", :tasks [:done], :done #{:done}}
                             {:name "cond->>", :tasks [:done], :done #{:done}}
                             {:name "condp", :tasks [:done], :done #{}}
                             {:name "conj", :tasks [:done], :done #{:done}}
                             {:name "conj!", :tasks [:done], :done #{:done}}
                             {:name "cons", :tasks [:done], :done #{:done}}
                             {:name "constantly", :tasks [:done], :done #{:done}}
                             {:name "construct-proxy", :tasks [:done], :done #{}}
                             {:name "contains?", :tasks [:done], :done #{:done}}
                             {:name "count", :tasks [:done], :done #{:done}}
                             {:name "counted?", :tasks [:done], :done #{:done}}
                             {:name "create-ns", :tasks [:done], :done #{:done}}
                             {:name "create-struct", :tasks [], :done #{}}
                             {:name "cycle", :tasks [:done], :done #{:done}}
                             {:name "dec", :tasks [:done], :done #{:done}}
                             {:name "dec'", :tasks [:done], :done #{}}
                             {:name "decimal?", :tasks [:done], :done #{}}
                             {:name "declare", :tasks [:done], :done #{}}
                             {:name "dedupe", :tasks [:done], :done #{:done}}
                             {:name "default-data-readers", :tasks [:done], :done #{:done}}
                             {:name "definline", :tasks [:done], :done #{}}
                             {:name "definterface", :tasks [:done], :done #{}}
                             {:name "defmacro", :tasks [:done], :done #{:done}}
                             {:name "defmethod", :tasks [:done], :done #{}}
                             {:name "defmulti", :tasks [:done], :done #{:done}}
                             {:name "defn", :tasks [:done], :done #{:done}}
                             {:name "defn-", :tasks [:done], :done #{:done}}
                             {:name "defonce", :tasks [:done], :done #{:done}}
                             {:name "defprotocol", :tasks [:done], :done #{}}
                             {:name "defrecord", :tasks [:done], :done #{}}
                             {:name "defstruct", :tasks [], :done #{}}
                             {:name "deftype", :tasks [:done], :done #{}}
                             {:name "delay", :tasks [:done], :done #{}}
                             {:name "delay?", :tasks [:done], :done #{}}
                             {:name "deliver", :tasks [:done], :done #{}}
                             {:name "denominator", :tasks [:done], :done #{:done}}
                             {:name "deref", :tasks [:done], :done #{:done}}
                             {:name "derive", :tasks [:done], :done #{:done}}
                             {:name "descendants", :tasks [:done], :done #{:done}}
                             {:name "destructure", :tasks [:done], :done #{:done}}
                             {:name "disj", :tasks [:done], :done #{:done}}
                             {:name "disj!", :tasks [:done], :done #{:done}}
                             {:name "dissoc", :tasks [:done], :done #{:done}}
                             {:name "dissoc!", :tasks [:done], :done #{:done}}
                             {:name "distinct", :tasks [:done], :done #{:done}}
                             {:name "distinct?", :tasks [:done], :done #{:done}}
                             {:name "doall", :tasks [:done], :done #{:done}}
                             {:name "dorun", :tasks [:done], :done #{:done}}
                             {:name "doseq", :tasks [:done], :done #{:done}}
                             {:name "dosync", :tasks [:done], :done #{}}
                             {:name "dotimes", :tasks [:done], :done #{:done}}
                             {:name "doto", :tasks [:done], :done #{:done}}
                             {:name "double", :tasks [:done], :done #{}}
                             {:name "double-array", :tasks [:done], :done #{}}
                             {:name "double?", :tasks [:done], :done #{:done}}
                             {:name "doubles", :tasks [:done], :done #{}}
                             {:name "drop", :tasks [:done], :done #{:done}}
                             {:name "drop-last", :tasks [:done], :done #{:done}}
                             {:name "drop-while", :tasks [:done], :done #{:done}}
                             {:name "eduction", :tasks [:done], :done #{}}
                             {:name "empty", :tasks [:done], :done #{:done}}
                             {:name "empty?", :tasks [:done], :done #{:done}}
                             {:name "ensure", :tasks [:done], :done #{}}
                             {:name "ensure-reduced", :tasks [:done], :done #{:done}}
                             {:name "enumeration-seq", :tasks [:na], :done #{:na}}
                             {:name "error-handler", :tasks [:done], :done #{}}
                             {:name "error-mode", :tasks [:done], :done #{}}
                             {:name "eval", :tasks [:done], :done #{:done}}
                             {:name "even?", :tasks [:done], :done #{:done}}
                             {:name "every-pred", :tasks [:done], :done #{:done}}
                             {:name "every?", :tasks [:done], :done #{:done}}
                             {:name "ex-cause", :tasks [:done], :done #{:done}}
                             {:name "ex-data", :tasks [:done], :done #{:done}}
                             {:name "ex-info", :tasks [:done], :done #{:done}}
                             {:name "ex-message", :tasks [:done], :done #{:done}}
                             {:name "extend", :tasks [:done], :done #{}}
                             {:name "extend-protocol", :tasks [:done], :done #{}}
                             {:name "extend-type", :tasks [:done], :done #{}}
                             {:name "extenders", :tasks [:done], :done #{}}
                             {:name "extends?", :tasks [:done], :done #{}}
                             {:name "false?", :tasks [:done], :done #{:done}}
                             {:name "ffirst", :tasks [:done], :done #{:done}}
                             {:name "file-seq", :tasks [:done], :done #{}}
                             {:name "filter", :tasks [:done], :done #{:done}}
                             {:name "filterv", :tasks [:done], :done #{:done}}
                             {:name "find", :tasks [:done], :done #{:done}}
                             {:name "find-keyword", :tasks [:done], :done #{}}
                             {:name "find-ns", :tasks [:done], :done #{:done}}
                             {:name "find-protocol-impl", :tasks [:done], :done #{}}
                             {:name "find-protocol-method", :tasks [:done], :done #{}}
                             {:name "find-var", :tasks [:done], :done #{:done}}
                             {:name "first", :tasks [:done], :done #{:done}}
                             {:name "flatten", :tasks [:done], :done #{:done}}
                             {:name "float", :tasks [:done], :done #{:done}}
                             {:name "float-array", :tasks [:done], :done #{}}
                             {:name "float?", :tasks [:done], :done #{:done}}
                             {:name "floats", :tasks [:done], :done #{}}
                             {:name "flush", :tasks [:done], :done #{}}
                             {:name "fn", :tasks [:done], :done #{:done}}
                             {:name "fn?", :tasks [:done], :done #{:done}}
                             {:name "fnext", :tasks [:done], :done #{:done}}
                             {:name "fnil", :tasks [:done], :done #{:done}}
                             {:name "for", :tasks [:done], :done #{:done}}
                             {:name "force", :tasks [:done], :done #{:done}}
                             {:name "format", :tasks [:done], :done #{}}
                             {:name "frequencies", :tasks [:done], :done #{:done}}
                             {:name "future", :tasks [:done], :done #{}}
                             {:name "future-call", :tasks [:done], :done #{}}
                             {:name "future-cancel", :tasks [:done], :done #{}}
                             {:name "future-cancelled?", :tasks [:done], :done #{}}
                             {:name "future-done?", :tasks [:done], :done #{}}
                             {:name "future?", :tasks [:done], :done #{}}
                             {:name "gen-class", :tasks [], :done #{}}
                             {:name "gen-interface", :tasks [], :done #{}}
                             {:name "gensym", :tasks [:done], :done #{:done}}
                             {:name "get", :tasks [:done], :done #{:done}}
                             {:name "get-in", :tasks [:done], :done #{:done}}
                             {:name "get-method", :tasks [:done], :done #{:done}}
                             {:name "get-proxy-class", :tasks [:done], :done #{}}
                             {:name "get-thread-bindings", :tasks [:done], :done #{:done}}
                             {:name "get-validator", :tasks [:done], :done #{}}
                             {:name "group-by", :tasks [:done], :done #{:done}}
                             {:name "halt-when", :tasks [:done], :done #{}}
                             {:name "hash", :tasks [:done], :done #{:done}}
                             {:name "hash-combine", :tasks [:done], :done #{}}
                             {:name "hash-map", :tasks [:done], :done #{:done}}
                             {:name "hash-ordered-coll", :tasks [:done], :done #{}}
                             {:name "hash-set", :tasks [:done], :done #{:done}}
                             {:name "hash-unordered-coll", :tasks [:done], :done #{:done}}
                             {:name "ident?", :tasks [:done], :done #{:done}}
                             {:name "identical?", :tasks [:done], :done #{:done}}
                             {:name "identity", :tasks [:done], :done #{:done}}
                             {:name "if-let", :tasks [:done], :done #{}}
                             {:name "if-not", :tasks [:done], :done #{}}
                             {:name "if-some", :tasks [:done], :done #{}}
                             {:name "ifn?", :tasks [:done], :done #{:done}}
                             {:name "import", :tasks [:done], :done #{}}
                             {:name "in-ns", :tasks [:done], :done #{:done}}
                             {:name "inc", :tasks [:done], :done #{:done}}
                             {:name "inc'", :tasks [:done], :done #{}}
                             {:name "indexed?", :tasks [:done], :done #{}}
                             {:name "infinite?", :tasks [:done], :done #{:done}}
                             {:name "init-proxy", :tasks [:done], :done #{}}
                             {:name "inst-ms", :tasks [:done], :done #{}}
                             {:name "inst-ms*", :tasks [:done], :done #{}}
                             {:name "inst?", :tasks [:done], :done #{}}
                             {:name "instance?", :tasks [:done], :done #{}}
                             {:name "int", :tasks [:done], :done #{:done}}
                             {:name "int-array", :tasks [:done], :done #{}}
                             {:name "int?", :tasks [:done], :done #{:done}}
                             {:name "integer?", :tasks [:done], :done #{:done}}
                             {:name "interleave", :tasks [:done], :done #{:done}}
                             {:name "intern", :tasks [:done], :done #{:done}}
                             {:name "interpose", :tasks [:done], :done #{:done}}
                             {:name "into", :tasks [:done], :done #{:done}}
                             {:name "into-array", :tasks [:done], :done #{}}
                             {:name "ints", :tasks [:done], :done #{}}
                             {:name "io!", :tasks [:done], :done #{}}
                             {:name "isa?", :tasks [:done], :done #{:done}}
                             {:name "iterate", :tasks [:done], :done #{:done}}
                             {:name "iteration", :tasks [:done], :done #{}}
                             {:name "iterator-seq", :tasks [:na], :done #{:na}}
                             {:name "juxt", :tasks [:done], :done #{:done}}
                             {:name "keep", :tasks [:done], :done #{:done}}
                             {:name "keep-indexed", :tasks [:done], :done #{:done}}
                             {:name "key", :tasks [:done], :done #{:done}}
                             {:name "keys", :tasks [:done], :done #{:done}}
                             {:name "keyword", :tasks [:done], :done #{:done}}
                             {:name "keyword?", :tasks [:done], :done #{:done}}
                             {:name "last", :tasks [:done], :done #{:done}}
                             {:name "lazy-cat", :tasks [:done], :done #{}}
                             {:name "lazy-seq", :tasks [:done], :done #{}}
                             {:name "let", :tasks [:done], :done #{:done}}
                             {:name "letfn", :tasks [:done], :done #{}}
                             {:name "line-seq", :tasks [:done], :done #{}}
                             {:name "list", :tasks [:done], :done #{:done}}
                             {:name "list*", :tasks [:done], :done #{:done}}
                             {:name "list?", :tasks [:done], :done #{:done}}
                             {:name "load", :tasks [:done], :done #{:done}}
                             {:name "load-file", :tasks [:done], :done #{}}
                             {:name "load-reader", :tasks [:done], :done #{}}
                             {:name "load-string", :tasks [:done], :done #{}}
                             {:name "loaded-libs", :tasks [:done], :done #{:done}}
                             {:name "locking", :tasks [:done], :done #{}}
                             {:name "long", :tasks [:done], :done #{}}
                             {:name "long-array", :tasks [:done], :done #{}}
                             {:name "longs", :tasks [:done], :done #{}}
                             {:name "loop", :tasks [:done], :done #{:done}}
                             {:name "macroexpand", :tasks [:done], :done #{:done}}
                             {:name "macroexpand-1", :tasks [:done], :done #{:done}}
                             {:name "make-array", :tasks [:done], :done #{}}
                             {:name "make-hierarchy", :tasks [:done], :done #{:done}}
                             {:name "map", :tasks [:done], :done #{:done}}
                             {:name "map-entry?", :tasks [:done], :done #{:done}}
                             {:name "map-indexed", :tasks [:done], :done #{:done}}
                             {:name "map?", :tasks [:done], :done #{:done}}
                             {:name "mapcat", :tasks [:done], :done #{:done}}
                             {:name "mapv", :tasks [:done], :done #{:done}}
                             {:name "max", :tasks [:done], :done #{:done}}
                             {:name "max-key", :tasks [:done], :done #{:done}}
                             {:name "memfn", :tasks [:done], :done #{}}
                             {:name "memoize", :tasks [:done], :done #{:done}}
                             {:name "merge", :tasks [:done], :done #{:done}}
                             {:name "merge-with", :tasks [:done], :done #{:done}}
                             {:name "meta", :tasks [:done], :done #{:done}}
                             {:name "method-sig", :tasks [:done], :done #{}}
                             {:name "methods", :tasks [:done], :done #{:done}}
                             {:name "min", :tasks [:done], :done #{:done}}
                             {:name "min-key", :tasks [:done], :done #{:done}}
                             {:name "mix-collection-hash", :tasks [:done], :done #{}}
                             {:name "mod", :tasks [:done], :done #{:done}}
                             {:name "munge", :tasks [:done], :done #{}}
                             {:name "name", :tasks [:done], :done #{:done}}
                             {:name "namespace", :tasks [:done], :done #{:done}}
                             {:name "namespace-munge", :tasks [:done], :done #{}}
                             {:name "nat-int?", :tasks [:done], :done #{:done}}
                             {:name "neg-int?", :tasks [:done], :done #{:done}}
                             {:name "neg?", :tasks [:done], :done #{:done}}
                             {:name "newline", :tasks [:done], :done #{}}
                             {:name "next", :tasks [:done], :done #{:done}}
                             {:name "nfirst", :tasks [:done], :done #{:done}}
                             {:name "nil?", :tasks [:done], :done #{:done}}
                             {:name "nnext", :tasks [:done], :done #{:done}}
                             {:name "not", :tasks [:done], :done #{:done}}
                             {:name "not-any?", :tasks [:done], :done #{:done}}
                             {:name "not-empty", :tasks [:done], :done #{:done}}
                             {:name "not-every?", :tasks [:done], :done #{:done}}
                             {:name "not=", :tasks [:done], :done #{:done}}
                             {:name "ns", :tasks [:done], :done #{:done}}
                             {:name "ns-aliases", :tasks [:done], :done #{}}
                             {:name "ns-imports", :tasks [:done], :done #{}}
                             {:name "ns-interns", :tasks [:done], :done #{}}
                             {:name "ns-map", :tasks [:done], :done #{:done}}
                             {:name "ns-name", :tasks [:done], :done #{:done}}
                             {:name "ns-publics", :tasks [:done], :done #{:done}}
                             {:name "ns-refers", :tasks [:done], :done #{}}
                             {:name "ns-resolve", :tasks [:done], :done #{:done}}
                             {:name "ns-unalias", :tasks [:done], :done #{}}
                             {:name "ns-unmap", :tasks [:done], :done #{}}
                             {:name "nth", :tasks [:done], :done #{:done}}
                             {:name "nthnext", :tasks [:done], :done #{:done}}
                             {:name "nthrest", :tasks [:done], :done #{:done}}
                             {:name "num", :tasks [:done], :done #{}}
                             {:name "number?", :tasks [:done], :done #{:done}}
                             {:name "numerator", :tasks [:done], :done #{:done}}
                             {:name "object-array", :tasks [:done], :done #{}}
                             {:name "odd?", :tasks [:done], :done #{:done}}
                             {:name "or", :tasks [:done], :done #{:done}}
                             {:name "parents", :tasks [:done], :done #{:done}}
                             {:name "parse-boolean", :tasks [:done], :done #{:done}}
                             {:name "parse-double", :tasks [:done], :done #{:done}}
                             {:name "parse-long", :tasks [:done], :done #{:done}}
                             {:name "parse-uuid", :tasks [:done], :done #{:done}}
                             {:name "partial", :tasks [:done], :done #{:done}}
                             {:name "partition", :tasks [:done], :done #{:done}}
                             {:name "partition-all", :tasks [:done], :done #{:done}}
                             {:name "partition-by", :tasks [:done], :done #{:done}}
                             {:name "pcalls", :tasks [:done], :done #{}}
                             {:name "peek", :tasks [:done], :done #{:done}}
                             {:name "persistent!", :tasks [:done], :done #{:done}}
                             {:name "pmap", :tasks [:done], :done #{}}
                             {:name "pop", :tasks [:done], :done #{:done}}
                             {:name "pop!", :tasks [:done], :done #{:done}}
                             {:name "pop-thread-bindings", :tasks [:done], :done #{:done}}
                             {:name "pos-int?", :tasks [:done], :done #{:done}}
                             {:name "pos?", :tasks [:done], :done #{:done}}
                             {:name "pr", :tasks [:done], :done #{:done}}
                             {:name "pr-str", :tasks [:done], :done #{:done}}
                             {:name "prefer-method", :tasks [:done], :done #{:done}}
                             {:name "prefers", :tasks [:done], :done #{:done}}
                             {:name "primitives-classnames", :tasks [:done], :done #{}}
                             {:name "print", :tasks [:done], :done #{:done}}
                             {:name "print-ctor", :tasks [:done], :done #{}}
                             {:name "print-dup", :tasks [:done], :done #{}}
                             {:name "print-method", :tasks [:done], :done #{}}
                             {:name "print-simple", :tasks [:done], :done #{}}
                             {:name "print-str", :tasks [:done], :done #{}}
                             {:name "printf", :tasks [:done], :done #{}}
                             {:name "println", :tasks [:done], :done #{:done}}
                             {:name "println-str", :tasks [:done], :done #{}}
                             {:name "prn", :tasks [:done], :done #{:done}}
                             {:name "prn-str", :tasks [:done], :done #{}}
                             {:name "promise", :tasks [:done], :done #{}}
                             {:name "proxy", :tasks [:done], :done #{}}
                             {:name "proxy-call-with-super", :tasks [:done], :done #{}}
                             {:name "proxy-mappings", :tasks [:done], :done #{}}
                             {:name "proxy-name", :tasks [:done], :done #{}}
                             {:name "proxy-super", :tasks [:done], :done #{}}
                             {:name "push-thread-bindings", :tasks [:done], :done #{:done}}
                             {:name "pvalues", :tasks [:done], :done #{}}
                             {:name "qualified-ident?", :tasks [:done], :done #{:done}}
                             {:name "qualified-keyword?", :tasks [:done], :done #{:done}}
                             {:name "qualified-symbol?", :tasks [:done], :done #{:done}}
                             {:name "quot", :tasks [:done], :done #{:done}}
                             {:name "rand", :tasks [:done], :done #{:done}}
                             {:name "rand-int", :tasks [:done], :done #{:done}}
                             {:name "rand-nth", :tasks [:done], :done #{:done}}
                             {:name "random-sample", :tasks [:done], :done #{:done}}
                             {:name "random-uuid", :tasks [:done], :done #{:done}}
                             {:name "range", :tasks [:done], :done #{:done}}
                             {:name "ratio?", :tasks [:done], :done #{:done}}
                             {:name "rational?", :tasks [:done], :done #{:done}}
                             {:name "rationalize", :tasks [:done], :done #{}}
                             {:name "re-find", :tasks [:done], :done #{}}
                             {:name "re-groups", :tasks [:done], :done #{}}
                             {:name "re-matcher", :tasks [:done], :done #{}}
                             {:name "re-matches", :tasks [:done], :done #{}}
                             {:name "re-pattern", :tasks [:done], :done #{}}
                             {:name "re-seq", :tasks [:done], :done #{}}
                             {:name "read", :tasks [:done], :done #{}}
                             {:name "read+string", :tasks [:done], :done #{}}
                             {:name "read-line", :tasks [:done], :done #{}}
                             {:name "read-string", :tasks [:done], :done #{:done}}
                             {:name "reader-conditional", :tasks [:done], :done #{}}
                             {:name "reader-conditional?", :tasks [:done], :done #{}}
                             {:name "realized?", :tasks [:done], :done #{}}
                             {:name "record?", :tasks [:done], :done #{}}
                             {:name "reduce", :tasks [:done], :done #{:done}}
                             {:name "reduce-kv", :tasks [:done], :done #{:done}}
                             {:name "reduced", :tasks [:done], :done #{:done}}
                             {:name "reduced?", :tasks [:done], :done #{:done}}
                             {:name "reductions", :tasks [:done], :done #{:done}}
                             {:name "ref", :tasks [:done], :done #{}}
                             {:name "ref-history-count", :tasks [:done], :done #{}}
                             {:name "ref-max-history", :tasks [:done], :done #{}}
                             {:name "ref-min-history", :tasks [:done], :done #{}}
                             {:name "ref-set", :tasks [:done], :done #{}}
                             {:name "refer", :tasks [:done], :done #{:done}}
                             {:name "refer-clojure", :tasks [:done], :done #{}}
                             {:name "reify", :tasks [:done], :done #{}}
                             {:name "release-pending-sends", :tasks [:done], :done #{}}
                             {:name "rem", :tasks [:done], :done #{:done}}
                             {:name "remove", :tasks [:done], :done #{:done}}
                             {:name "remove-all-methods", :tasks [:done], :done #{:done}}
                             {:name "remove-method", :tasks [:done], :done #{:done}}
                             {:name "remove-ns", :tasks [:done], :done #{:done}}
                             {:name "remove-tap", :tasks [:done], :done #{:done}}
                             {:name "remove-watch", :tasks [:done], :done #{}}
                             {:name "repeat", :tasks [:done], :done #{:done}}
                             {:name "repeatedly", :tasks [:done], :done #{:done}}
                             {:name "replace", :tasks [:done], :done #{:done}}
                             {:name "replicate", :tasks [:done], :done #{:done}}
                             {:name "require", :tasks [:done], :done #{:done}}
                             {:name "requiring-resolve", :tasks [:done], :done #{}}
                             {:name "reset!", :tasks [:done], :done #{:done}}
                             {:name "reset-meta!", :tasks [:done], :done #{:done}}
                             {:name "reset-vals!", :tasks [:done], :done #{:done}}
                             {:name "resolve", :tasks [:done], :done #{:done}}
                             {:name "rest", :tasks [:done], :done #{:done}}
                             {:name "restart-agent", :tasks [:done], :done #{}}
                             {:name "resultset-seq", :tasks [:na], :done #{:na}}
                             {:name "reverse", :tasks [:done], :done #{:done}}
                             {:name "reversible?", :tasks [:done], :done #{}}
                             {:name "rseq", :tasks [:done], :done #{}}
                             {:name "rsubseq", :tasks [:done], :done #{}}
                             {:name "run!", :tasks [:done], :done #{:done}}
                             {:name "satisfies?", :tasks [], :done #{}}
                             {:name "second", :tasks [:done], :done #{:done}}
                             {:name "select-keys", :tasks [:done], :done #{:done}}
                             {:name "send", :tasks [:done], :done #{}}
                             {:name "send-off", :tasks [:done], :done #{}}
                             {:name "send-via", :tasks [:done], :done #{}}
                             {:name "seq", :tasks [:done], :done #{:done}}
                             {:name "seq-to-map-for-destructuring", :tasks [:done], :done #{}}
                             {:name "seq?", :tasks [:done], :done #{:done}}
                             {:name "seqable?", :tasks [:done], :done #{:done}}
                             {:name "seque", :tasks [:done], :done #{}}
                             {:name "sequence", :tasks [:done], :done #{}}
                             {:name "sequential?", :tasks [:done], :done #{:done}}
                             {:name "set", :tasks [:done], :done #{:done}}
                             {:name "set-agent-send-executor!", :tasks [:done], :done #{}}
                             {:name "set-agent-send-off-executor!", :tasks [:done], :done #{}}
                             {:name "set-error-handler!", :tasks [:done], :done #{}}
                             {:name "set-error-mode!", :tasks [:done], :done #{}}
                             {:name "set-validator!", :tasks [:done], :done #{}}
                             {:name "set?", :tasks [:done], :done #{:done}}
                             {:name "short", :tasks [:done], :done #{}}
                             {:name "short-array", :tasks [:done], :done #{}}
                             {:name "shorts", :tasks [:done], :done #{}}
                             {:name "shuffle", :tasks [:done], :done #{}}
                             {:name "shutdown-agents", :tasks [:done], :done #{}}
                             {:name "simple-ident?", :tasks [:done], :done #{:done}}
                             {:name "simple-keyword?", :tasks [:done], :done #{:done}}
                             {:name "simple-symbol?", :tasks [:done], :done #{:done}}
                             {:name "slurp", :tasks [:done], :done #{}}
                             {:name "some", :tasks [:done], :done #{:done}}
                             {:name "some->", :tasks [:done], :done #{:done}}
                             {:name "some->>", :tasks [:done], :done #{:done}}
                             {:name "some-fn", :tasks [:done], :done #{:done}}
                             {:name "some?", :tasks [:done], :done #{:done}}
                             {:name "sort", :tasks [:done], :done #{}}
                             {:name "sort-by", :tasks [:done], :done #{}}
                             {:name "sorted-map", :tasks [:done], :done #{:done}}
                             {:name "sorted-map-by", :tasks [:done], :done #{}}
                             {:name "sorted-set", :tasks [:done], :done #{:done}}
                             {:name "sorted-set-by", :tasks [:done], :done #{:done}}
                             {:name "sorted?", :tasks [:done], :done #{:done}}
                             {:name "special-symbol?", :tasks [:done], :done #{}}
                             {:name "spit", :tasks [:done], :done #{}}
                             {:name "split-at", :tasks [:done], :done #{:done}}
                             {:name "split-with", :tasks [:done], :done #{:done}}
                             {:name "str", :tasks [:done], :done #{:done}}
                             {:name "string?", :tasks [:done], :done #{:done}}
                             {:name "struct", :tasks [], :done #{}}
                             {:name "struct-map", :tasks [], :done #{}}
                             {:name "subs", :tasks [:done], :done #{:done}}
                             {:name "subseq", :tasks [:done], :done #{}}
                             {:name "subvec", :tasks [:done], :done #{:done}}
                             {:name "supers", :tasks [:na], :done #{:na}}
                             {:name "swap!", :tasks [:done], :done #{:done}}
                             {:name "swap-vals!", :tasks [:done], :done #{:done}}
                             {:name "symbol", :tasks [:done], :done #{:done}}
                             {:name "symbol?", :tasks [:done], :done #{:done}}
                             {:name "sync", :tasks [:done], :done #{}}
                             {:name "tagged-literal", :tasks [:done], :done #{:done}}
                             {:name "tagged-literal?", :tasks [:done], :done #{:done}}
                             {:name "take", :tasks [:done], :done #{:done}}
                             {:name "take-last", :tasks [:done], :done #{:done}}
                             {:name "take-nth", :tasks [:done], :done #{:done}}
                             {:name "take-while", :tasks [:done], :done #{:done}}
                             {:name "tap>", :tasks [:done], :done #{}}
                             {:name "test", :tasks [:done], :done #{:done}}
                             {:name "the-ns", :tasks [:done], :done #{:done}}
                             {:name "thread-bound?", :tasks [:done], :done #{:done}}
                             {:name "time", :tasks [:done], :done #{:done}}
                             {:name "to-array", :tasks [:done], :done #{}}
                             {:name "to-array-2d", :tasks [:done], :done #{}}
                             {:name "trampoline", :tasks [:done], :done #{:done}}
                             {:name "transduce", :tasks [:done], :done #{:done}}
                             {:name "transient", :tasks [:done], :done #{:done}}
                             {:name "tree-seq", :tasks [:done], :done #{:done}}
                             {:name "true?", :tasks [:done], :done #{:done}}
                             {:name "type", :tasks [:done], :done #{:done}}
                             {:name "unchecked-add", :tasks [:done], :done #{}}
                             {:name "unchecked-add-int", :tasks [:done], :done #{}}
                             {:name "unchecked-byte", :tasks [:done], :done #{}}
                             {:name "unchecked-char", :tasks [:done], :done #{}}
                             {:name "unchecked-dec", :tasks [:done], :done #{}}
                             {:name "unchecked-dec-int", :tasks [:done], :done #{}}
                             {:name "unchecked-divide-int", :tasks [:done], :done #{}}
                             {:name "unchecked-double", :tasks [:done], :done #{}}
                             {:name "unchecked-float", :tasks [:done], :done #{}}
                             {:name "unchecked-inc", :tasks [:done], :done #{}}
                             {:name "unchecked-inc-int", :tasks [:done], :done #{}}
                             {:name "unchecked-int", :tasks [:done], :done #{}}
                             {:name "unchecked-long", :tasks [:done], :done #{}}
                             {:name "unchecked-multiply", :tasks [:done], :done #{}}
                             {:name "unchecked-multiply-int", :tasks [:done], :done #{}}
                             {:name "unchecked-negate", :tasks [:done], :done #{}}
                             {:name "unchecked-negate-int", :tasks [:done], :done #{}}
                             {:name "unchecked-remainder-int", :tasks [:done], :done #{}}
                             {:name "unchecked-short", :tasks [:done], :done #{}}
                             {:name "unchecked-subtract", :tasks [:done], :done #{}}
                             {:name "unchecked-subtract-int", :tasks [:done], :done #{}}
                             {:name "underive", :tasks [:done], :done #{:done}}
                             {:name "unquote", :tasks [:done], :done #{:done}}
                             {:name "unquote-splicing", :tasks [:done], :done #{:done}}
                             {:name "unreduced", :tasks [:done], :done #{:done}}
                             {:name "unsigned-bit-shift-right", :tasks [:done], :done #{:done}}
                             {:name "update", :tasks [:done], :done #{:done}}
                             {:name "update-in", :tasks [:done], :done #{:done}}
                             {:name "update-keys", :tasks [:done], :done #{:done}}
                             {:name "update-proxy", :tasks [:done], :done #{}}
                             {:name "update-vals", :tasks [:done], :done #{:done}}
                             {:name "uri?", :tasks [:done], :done #{}}
                             {:name "use", :tasks [:done], :done #{:done}}
                             {:name "uuid?", :tasks [:done], :done #{:done}}
                             {:name "val", :tasks [:done], :done #{:done}}
                             {:name "vals", :tasks [:done], :done #{:done}}
                             {:name "var-get", :tasks [:done], :done #{:done}}
                             {:name "var-set", :tasks [:done], :done #{}}
                             {:name "var?", :tasks [:done], :done #{:done}}
                             {:name "vary-meta", :tasks [:done], :done #{:done}}
                             {:name "vec", :tasks [:done], :done #{:done}}
                             {:name "vector", :tasks [:done], :done #{:done}}
                             {:name "vector-of", :tasks [:done], :done #{}}
                             {:name "vector?", :tasks [:done], :done #{:done}}
                             {:name "volatile!", :tasks [:done], :done #{:done}}
                             {:name "volatile?", :tasks [:done], :done #{:done}}
                             {:name "vreset!", :tasks [:done], :done #{:done}}
                             {:name "vswap!", :tasks [:done], :done #{:done}}
                             {:name "when", :tasks [:done], :done #{:done}}
                             {:name "when-first", :tasks [:done], :done #{:done}}
                             {:name "when-let", :tasks [:done], :done #{:done}}
                             {:name "when-not", :tasks [:done], :done #{:done}}
                             {:name "when-some", :tasks [:done], :done #{:done}}
                             {:name "while", :tasks [:done], :done #{:done}}
                             {:name "with-bindings", :tasks [:done], :done #{:done}}
                             {:name "with-bindings*", :tasks [:done], :done #{:done}}
                             {:name "with-in-str", :tasks [:done], :done #{}}
                             {:name "with-loading-context", :tasks [:done], :done #{}}
                             {:name "with-local-vars", :tasks [:done], :done #{}}
                             {:name "with-meta", :tasks [:done], :done #{:done}}
                             {:name "with-open", :tasks [:done], :done #{}}
                             {:name "with-out-str", :tasks [:done], :done #{}}
                             {:name "with-precision", :tasks [:done], :done #{}}
                             {:name "with-redefs", :tasks [:done], :done #{:done}}
                             {:name "with-redefs-fn", :tasks [:done], :done #{:done}}
                             {:name "xml-seq", :tasks [:done], :done #{}}
                             {:name "zero?", :tasks [:done], :done #{:done}}
                             {:name "zipmap", :tasks [:done], :done #{:done}}]}
                 {:name "Clojure library tests"
                  :features [{:name "*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*'"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*1"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*2"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*3"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*agent*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*allow-unresolved-vars*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*assert*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*clojure-version*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*command-line-args*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*compile-files*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*compile-path*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*compiler-options*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*data-readers*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*default-data-reader-fn*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*e"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*err*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*file*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*flush-on-newline*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*fn-loader*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*in*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*math-context*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*ns*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*out*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-dup*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-length*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-level*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-meta*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-namespace-maps*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*print-readably*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*read-eval*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*reader-resolver*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*source-path*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*suppress-read*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*unchecked-math*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "*verbose-defrecords*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "+"
                              :tasks [:tested]
                              :done #{}}
                             {:name "+'"
                              :tasks [:tested]
                              :done #{}}
                             {:name "-"
                              :tasks [:tested]
                              :done #{}}
                             {:name "-'"
                              :tasks [:tested]
                              :done #{}}
                             {:name "->"
                              :tasks [:tested]
                              :done #{}}
                             {:name "->>"
                              :tasks [:tested]
                              :done #{}}
                             {:name "/"
                              :tasks [:tested]
                              :done #{}}
                             {:name "<"
                              :tasks [:tested]
                              :done #{}}
                             {:name "<="
                              :tasks [:tested]
                              :done #{}}
                             {:name "="
                              :tasks [:tested]
                              :done #{}}
                             {:name "=="
                              :tasks [:tested]
                              :done #{}}
                             {:name ">"
                              :tasks [:tested]
                              :done #{}}
                             {:name ">="
                              :tasks [:tested]
                              :done #{}}
                             {:name "Inst"
                              :tasks [:tested]
                              :done #{}}
                             {:name "NaN?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "accessor"
                              :tasks []
                              :done #{}}
                             {:name "aclone"
                              :tasks [:tested]
                              :done #{}}
                             {:name "add-classpath"
                              :tasks [:tested]
                              :done #{}}
                             {:name "add-tap"
                              :tasks [:tested]
                              :done #{}}
                             {:name "add-watch"
                              :tasks [:tested]
                              :done #{}}
                             {:name "agent"
                              :tasks [:tested]
                              :done #{}}
                             {:name "agent-error"
                              :tasks [:tested]
                              :done #{}}
                             {:name "agent-errors"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aget"
                              :tasks [:tested]
                              :done #{}}
                             {:name "alength"
                              :tasks [:tested]
                              :done #{}}
                             {:name "alias"
                              :tasks [:tested]
                              :done #{}}
                             {:name "all-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "alter"
                              :tasks [:tested]
                              :done #{}}
                             {:name "alter-meta!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "alter-var-root"
                              :tasks [:tested]
                              :done #{}}
                             {:name "amap"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ancestors"
                              :tasks [:tested]
                              :done #{}}
                             {:name "and"
                              :tasks [:tested]
                              :done #{}}
                             {:name "any?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "apply"
                              :tasks [:tested]
                              :done #{}}
                             {:name "areduce"
                              :tasks [:tested]
                              :done #{}}
                             {:name "array-map"
                              :tasks [:tested]
                              :done #{}}
                             {:name "as->"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-boolean"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-byte"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-char"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-double"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-float"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-long"
                              :tasks [:tested]
                              :done #{}}
                             {:name "aset-short"
                              :tasks [:tested]
                              :done #{}}
                             {:name "assert"
                              :tasks [:tested]
                              :done #{}}
                             {:name "assoc"
                              :tasks [:tested]
                              :done #{}}
                             {:name "assoc!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "assoc-in"
                              :tasks [:tested]
                              :done #{}}
                             {:name "associative?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "atom"
                              :tasks [:tested]
                              :done #{}}
                             {:name "await"
                              :tasks [:tested]
                              :done #{}}
                             {:name "await-for"
                              :tasks [:tested]
                              :done #{}}
                             {:name "await1"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bases"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bean"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bigdec"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bigint"
                              :tasks [:tested]
                              :done #{}}
                             {:name "biginteger"
                              :tasks [:tested]
                              :done #{}}
                             {:name "binding"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-and"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-and-not"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-clear"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-flip"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-not"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-or"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-shift-left"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-shift-right"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-test"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bit-xor"
                              :tasks [:tested]
                              :done #{}}
                             {:name "boolean"
                              :tasks [:tested]
                              :done #{}}
                             {:name "boolean-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "boolean?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "booleans"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bound-fn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bound-fn*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bound?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bounded-count"
                              :tasks [:tested]
                              :done #{}}
                             {:name "butlast"
                              :tasks [:tested]
                              :done #{}}
                             {:name "byte"
                              :tasks [:tested]
                              :done #{}}
                             {:name "byte-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bytes"
                              :tasks [:tested]
                              :done #{}}
                             {:name "bytes?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "case"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cast"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cat"
                              :tasks [:tested]
                              :done #{}}
                             {:name "char"
                              :tasks [:tested]
                              :done #{}}
                             {:name "char-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "char-escape-string"
                              :tasks [:tested]
                              :done #{}}
                             {:name "char-name-string"
                              :tasks [:tested]
                              :done #{}}
                             {:name "char?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chars"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-append"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-buffer"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-cons"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-first"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-next"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunk-rest"
                              :tasks [:tested]
                              :done #{}}
                             {:name "chunked-seq?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "class"
                              :tasks [:tested]
                              :done #{}}
                             {:name "class?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "clear-agent-errors"
                              :tasks [:tested]
                              :done #{}}
                             {:name "clojure-version"
                              :tasks [:tested]
                              :done #{}}
                             {:name "coll?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "comment"
                              :tasks [:tested]
                              :done #{}}
                             {:name "commute"
                              :tasks [:tested]
                              :done #{}}
                             {:name "comp"
                              :tasks [:tested]
                              :done #{}}
                             {:name "comparator"
                              :tasks [:tested]
                              :done #{}}
                             {:name "compare"
                              :tasks [:tested]
                              :done #{}}
                             {:name "compare-and-set!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "compile"
                              :tasks [:tested]
                              :done #{}}
                             {:name "complement"
                              :tasks [:tested]
                              :done #{}}
                             {:name "completing"
                              :tasks [:tested]
                              :done #{}}
                             {:name "concat"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cond"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cond->"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cond->>"
                              :tasks [:tested]
                              :done #{}}
                             {:name "condp"
                              :tasks [:tested]
                              :done #{}}
                             {:name "conj"
                              :tasks [:tested]
                              :done #{}}
                             {:name "conj!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "cons"
                              :tasks [:tested]
                              :done #{}}
                             {:name "constantly"
                              :tasks [:tested]
                              :done #{}}
                             {:name "construct-proxy"
                              :tasks [:tested]
                              :done #{}}
                             {:name "contains?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "count"
                              :tasks [:tested]
                              :done #{}}
                             {:name "counted?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "create-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "create-struct"
                              :tasks []
                              :done #{}}
                             {:name "cycle"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dec"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dec'"
                              :tasks [:tested]
                              :done #{}}
                             {:name "decimal?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "declare"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dedupe"
                              :tasks [:tested]
                              :done #{}}
                             {:name "default-data-readers"
                              :tasks [:tested]
                              :done #{}}
                             {:name "definline"
                              :tasks [:tested]
                              :done #{}}
                             {:name "definterface"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defmacro"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defmethod"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defmulti"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defn-"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defonce"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defprotocol"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defrecord"
                              :tasks [:tested]
                              :done #{}}
                             {:name "defstruct"
                              :tasks []
                              :done #{}}
                             {:name "deftype"
                              :tasks [:tested]
                              :done #{}}
                             {:name "delay"
                              :tasks [:tested]
                              :done #{}}
                             {:name "delay?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "deliver"
                              :tasks [:tested]
                              :done #{}}
                             {:name "denominator"
                              :tasks [:tested]
                              :done #{}}
                             {:name "deref"
                              :tasks [:tested]
                              :done #{}}
                             {:name "derive"
                              :tasks [:tested]
                              :done #{}}
                             {:name "descendants"
                              :tasks [:tested]
                              :done #{}}
                             {:name "destructure"
                              :tasks [:tested]
                              :done #{}}
                             {:name "disj"
                              :tasks [:tested]
                              :done #{}}
                             {:name "disj!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dissoc"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dissoc!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "distinct"
                              :tasks [:tested]
                              :done #{}}
                             {:name "distinct?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "doall"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dorun"
                              :tasks [:tested]
                              :done #{}}
                             {:name "doseq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dosync"
                              :tasks [:tested]
                              :done #{}}
                             {:name "dotimes"
                              :tasks [:tested]
                              :done #{}}
                             {:name "doto"
                              :tasks [:tested]
                              :done #{}}
                             {:name "double"
                              :tasks [:tested]
                              :done #{}}
                             {:name "double-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "double?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "doubles"
                              :tasks [:tested]
                              :done #{}}
                             {:name "drop"
                              :tasks [:tested]
                              :done #{}}
                             {:name "drop-last"
                              :tasks [:tested]
                              :done #{}}
                             {:name "drop-while"
                              :tasks [:tested]
                              :done #{}}
                             {:name "eduction"
                              :tasks [:tested]
                              :done #{}}
                             {:name "empty"
                              :tasks [:tested]
                              :done #{}}
                             {:name "empty?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ensure"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ensure-reduced"
                              :tasks [:tested]
                              :done #{}}
                             {:name "enumeration-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "error-handler"
                              :tasks [:tested]
                              :done #{}}
                             {:name "error-mode"
                              :tasks [:tested]
                              :done #{}}
                             {:name "eval"
                              :tasks [:tested]
                              :done #{}}
                             {:name "even?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "every-pred"
                              :tasks [:tested]
                              :done #{}}
                             {:name "every?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ex-cause"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ex-data"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ex-info"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ex-message"
                              :tasks [:tested]
                              :done #{}}
                             {:name "extend"
                              :tasks [:tested]
                              :done #{}}
                             {:name "extend-protocol"
                              :tasks [:tested]
                              :done #{}}
                             {:name "extend-type"
                              :tasks [:tested]
                              :done #{}}
                             {:name "extenders"
                              :tasks [:tested]
                              :done #{}}
                             {:name "extends?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "false?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ffirst"
                              :tasks [:tested]
                              :done #{}}
                             {:name "file-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "filter"
                              :tasks [:tested]
                              :done #{}}
                             {:name "filterv"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find-keyword"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find-protocol-impl"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find-protocol-method"
                              :tasks [:tested]
                              :done #{}}
                             {:name "find-var"
                              :tasks [:tested]
                              :done #{}}
                             {:name "first"
                              :tasks [:tested]
                              :done #{}}
                             {:name "flatten"
                              :tasks [:tested]
                              :done #{}}
                             {:name "float"
                              :tasks [:tested]
                              :done #{}}
                             {:name "float-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "float?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "floats"
                              :tasks [:tested]
                              :done #{}}
                             {:name "flush"
                              :tasks [:tested]
                              :done #{}}
                             {:name "fn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "fn?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "fnext"
                              :tasks [:tested]
                              :done #{}}
                             {:name "fnil"
                              :tasks [:tested]
                              :done #{}}
                             {:name "for"
                              :tasks [:tested]
                              :done #{}}
                             {:name "force"
                              :tasks [:tested]
                              :done #{}}
                             {:name "format"
                              :tasks [:tested]
                              :done #{}}
                             {:name "frequencies"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future-call"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future-cancel"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future-cancelled?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future-done?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "future?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "gen-class"
                              :tasks []
                              :done #{}}
                             {:name "gen-interface"
                              :tasks []
                              :done #{}}
                             {:name "gensym"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get-in"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get-method"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get-proxy-class"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get-thread-bindings"
                              :tasks [:tested]
                              :done #{}}
                             {:name "get-validator"
                              :tasks [:tested]
                              :done #{}}
                             {:name "group-by"
                              :tasks [:tested]
                              :done #{}}
                             {:name "halt-when"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash-combine"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash-map"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash-ordered-coll"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash-set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "hash-unordered-coll"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ident?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "identical?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "identity"
                              :tasks [:tested]
                              :done #{}}
                             {:name "if-let"
                              :tasks [:tested]
                              :done #{}}
                             {:name "if-not"
                              :tasks [:tested]
                              :done #{}}
                             {:name "if-some"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ifn?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "import"
                              :tasks [:tested]
                              :done #{}}
                             {:name "in-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "inc"
                              :tasks [:tested]
                              :done #{}}
                             {:name "inc'"
                              :tasks [:tested]
                              :done #{}}
                             {:name "indexed?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "infinite?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "init-proxy"
                              :tasks [:tested]
                              :done #{}}
                             {:name "inst-ms"
                              :tasks [:tested]
                              :done #{}}
                             {:name "inst-ms*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "inst?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "instance?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "int-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "int?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "integer?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "interleave"
                              :tasks [:tested]
                              :done #{}}
                             {:name "intern"
                              :tasks [:tested]
                              :done #{}}
                             {:name "interpose"
                              :tasks [:tested]
                              :done #{}}
                             {:name "into"
                              :tasks [:tested]
                              :done #{}}
                             {:name "into-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ints"
                              :tasks [:tested]
                              :done #{}}
                             {:name "io!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "isa?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "iterate"
                              :tasks [:tested]
                              :done #{}}
                             {:name "iteration"
                              :tasks [:tested]
                              :done #{}}
                             {:name "iterator-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "juxt"
                              :tasks [:tested]
                              :done #{}}
                             {:name "keep"
                              :tasks [:tested]
                              :done #{}}
                             {:name "keep-indexed"
                              :tasks [:tested]
                              :done #{}}
                             {:name "key"
                              :tasks [:tested]
                              :done #{}}
                             {:name "keys"
                              :tasks [:tested]
                              :done #{}}
                             {:name "keyword"
                              :tasks [:tested]
                              :done #{}}
                             {:name "keyword?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "last"
                              :tasks [:tested]
                              :done #{}}
                             {:name "lazy-cat"
                              :tasks [:tested]
                              :done #{}}
                             {:name "lazy-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "let"
                              :tasks [:tested]
                              :done #{}}
                             {:name "letfn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "line-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "list"
                              :tasks [:tested]
                              :done #{}}
                             {:name "list*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "list?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "load"
                              :tasks [:tested]
                              :done #{}}
                             {:name "load-file"
                              :tasks [:tested]
                              :done #{}}
                             {:name "load-reader"
                              :tasks [:tested]
                              :done #{}}
                             {:name "load-string"
                              :tasks [:tested]
                              :done #{}}
                             {:name "loaded-libs"
                              :tasks [:tested]
                              :done #{}}
                             {:name "locking"
                              :tasks [:tested]
                              :done #{}}
                             {:name "long"
                              :tasks [:tested]
                              :done #{}}
                             {:name "long-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "longs"
                              :tasks [:tested]
                              :done #{}}
                             {:name "loop"
                              :tasks [:tested]
                              :done #{}}
                             {:name "macroexpand"
                              :tasks [:tested]
                              :done #{}}
                             {:name "macroexpand-1"
                              :tasks [:tested]
                              :done #{}}
                             {:name "make-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "make-hierarchy"
                              :tasks [:tested]
                              :done #{}}
                             {:name "map"
                              :tasks [:tested]
                              :done #{}}
                             {:name "map-entry?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "map-indexed"
                              :tasks [:tested]
                              :done #{}}
                             {:name "map?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "mapcat"
                              :tasks [:tested]
                              :done #{}}
                             {:name "mapv"
                              :tasks [:tested]
                              :done #{}}
                             {:name "max"
                              :tasks [:tested]
                              :done #{}}
                             {:name "max-key"
                              :tasks [:tested]
                              :done #{}}
                             {:name "memfn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "memoize"
                              :tasks [:tested]
                              :done #{}}
                             {:name "merge"
                              :tasks [:tested]
                              :done #{}}
                             {:name "merge-with"
                              :tasks [:tested]
                              :done #{}}
                             {:name "meta"
                              :tasks [:tested]
                              :done #{}}
                             {:name "method-sig"
                              :tasks [:tested]
                              :done #{}}
                             {:name "methods"
                              :tasks [:tested]
                              :done #{}}
                             {:name "min"
                              :tasks [:tested]
                              :done #{}}
                             {:name "min-key"
                              :tasks [:tested]
                              :done #{}}
                             {:name "mix-collection-hash"
                              :tasks [:tested]
                              :done #{}}
                             {:name "mod"
                              :tasks [:tested]
                              :done #{}}
                             {:name "munge"
                              :tasks [:tested]
                              :done #{}}
                             {:name "name"
                              :tasks [:tested]
                              :done #{}}
                             {:name "namespace"
                              :tasks [:tested]
                              :done #{}}
                             {:name "namespace-munge"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nat-int?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "neg-int?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "neg?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "newline"
                              :tasks [:tested]
                              :done #{}}
                             {:name "next"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nfirst"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nil?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nnext"
                              :tasks [:tested]
                              :done #{}}
                             {:name "not"
                              :tasks [:tested]
                              :done #{}}
                             {:name "not-any?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "not-empty"
                              :tasks [:tested]
                              :done #{}}
                             {:name "not-every?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "not="
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-aliases"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-imports"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-interns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-map"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-name"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-publics"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-refers"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-resolve"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-unalias"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ns-unmap"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nth"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nthnext"
                              :tasks [:tested]
                              :done #{}}
                             {:name "nthrest"
                              :tasks [:tested]
                              :done #{}}
                             {:name "num"
                              :tasks [:tested]
                              :done #{}}
                             {:name "number?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "numerator"
                              :tasks [:tested]
                              :done #{}}
                             {:name "object-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "odd?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "or"
                              :tasks [:tested]
                              :done #{}}
                             {:name "parents"
                              :tasks [:tested]
                              :done #{}}
                             {:name "parse-boolean"
                              :tasks [:tested]
                              :done #{}}
                             {:name "parse-double"
                              :tasks [:tested]
                              :done #{}}
                             {:name "parse-long"
                              :tasks [:tested]
                              :done #{}}
                             {:name "parse-uuid"
                              :tasks [:tested]
                              :done #{}}
                             {:name "partial"
                              :tasks [:tested]
                              :done #{}}
                             {:name "partition"
                              :tasks [:tested]
                              :done #{}}
                             {:name "partition-all"
                              :tasks [:tested]
                              :done #{}}
                             {:name "partition-by"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pcalls"
                              :tasks [:tested]
                              :done #{}}
                             {:name "peek"
                              :tasks [:tested]
                              :done #{}}
                             {:name "persistent!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pmap"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pop"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pop!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pop-thread-bindings"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pos-int?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pos?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pr"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pr-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "prefer-method"
                              :tasks [:tested]
                              :done #{}}
                             {:name "prefers"
                              :tasks [:tested]
                              :done #{}}
                             {:name "primitives-classnames"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print-ctor"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print-dup"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print-method"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print-simple"
                              :tasks [:tested]
                              :done #{}}
                             {:name "print-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "printf"
                              :tasks [:tested]
                              :done #{}}
                             {:name "println"
                              :tasks [:tested]
                              :done #{}}
                             {:name "println-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "prn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "prn-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "promise"
                              :tasks [:tested]
                              :done #{}}
                             {:name "proxy"
                              :tasks [:tested]
                              :done #{}}
                             {:name "proxy-call-with-super"
                              :tasks [:tested]
                              :done #{}}
                             {:name "proxy-mappings"
                              :tasks [:tested]
                              :done #{}}
                             {:name "proxy-name"
                              :tasks [:tested]
                              :done #{}}
                             {:name "proxy-super"
                              :tasks [:tested]
                              :done #{}}
                             {:name "push-thread-bindings"
                              :tasks [:tested]
                              :done #{}}
                             {:name "pvalues"
                              :tasks [:tested]
                              :done #{}}
                             {:name "qualified-ident?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "qualified-keyword?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "qualified-symbol?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "quot"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rand"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rand-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rand-nth"
                              :tasks [:tested]
                              :done #{}}
                             {:name "random-sample"
                              :tasks [:tested]
                              :done #{}}
                             {:name "random-uuid"
                              :tasks [:tested]
                              :done #{}}
                             {:name "range"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ratio?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rational?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rationalize"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-find"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-groups"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-matcher"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-matches"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-pattern"
                              :tasks [:tested]
                              :done #{}}
                             {:name "re-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "read"
                              :tasks [:tested]
                              :done #{}}
                             {:name "read+string"
                              :tasks [:tested]
                              :done #{}}
                             {:name "read-line"
                              :tasks [:tested]
                              :done #{}}
                             {:name "read-string"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reader-conditional"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reader-conditional?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "realized?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "record?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reduce"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reduce-kv"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reduced"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reduced?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reductions"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ref"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ref-history-count"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ref-max-history"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ref-min-history"
                              :tasks [:tested]
                              :done #{}}
                             {:name "ref-set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "refer"
                              :tasks [:tested]
                              :done #{}}
                             {:name "refer-clojure"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reify"
                              :tasks [:tested]
                              :done #{}}
                             {:name "release-pending-sends"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rem"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove-all-methods"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove-method"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove-tap"
                              :tasks [:tested]
                              :done #{}}
                             {:name "remove-watch"
                              :tasks [:tested]
                              :done #{}}
                             {:name "repeat"
                              :tasks [:tested]
                              :done #{}}
                             {:name "repeatedly"
                              :tasks [:tested]
                              :done #{}}
                             {:name "replace"
                              :tasks [:tested]
                              :done #{}}
                             {:name "replicate"
                              :tasks [:tested]
                              :done #{}}
                             {:name "require"
                              :tasks [:tested]
                              :done #{}}
                             {:name "requiring-resolve"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reset!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reset-meta!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reset-vals!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "resolve"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rest"
                              :tasks [:tested]
                              :done #{}}
                             {:name "restart-agent"
                              :tasks [:tested]
                              :done #{}}
                             {:name "resultset-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reverse"
                              :tasks [:tested]
                              :done #{}}
                             {:name "reversible?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rseq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "rsubseq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "run!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "satisfies?"
                              :tasks []
                              :done #{}}
                             {:name "second"
                              :tasks [:tested]
                              :done #{}}
                             {:name "select-keys"
                              :tasks [:tested]
                              :done #{}}
                             {:name "send"
                              :tasks [:tested]
                              :done #{}}
                             {:name "send-off"
                              :tasks [:tested]
                              :done #{}}
                             {:name "send-via"
                              :tasks [:tested]
                              :done #{}}
                             {:name "seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "seq-to-map-for-destructuring"
                              :tasks [:tested]
                              :done #{}}
                             {:name "seq?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "seqable?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "seque"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sequence"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sequential?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set-agent-send-executor!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set-agent-send-off-executor!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set-error-handler!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set-error-mode!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set-validator!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "set?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "short"
                              :tasks [:tested]
                              :done #{}}
                             {:name "short-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "shorts"
                              :tasks [:tested]
                              :done #{}}
                             {:name "shuffle"
                              :tasks [:tested]
                              :done #{}}
                             {:name "shutdown-agents"
                              :tasks [:tested]
                              :done #{}}
                             {:name "simple-ident?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "simple-keyword?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "simple-symbol?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "slurp"
                              :tasks [:tested]
                              :done #{}}
                             {:name "some"
                              :tasks [:tested]
                              :done #{}}
                             {:name "some->"
                              :tasks [:tested]
                              :done #{}}
                             {:name "some->>"
                              :tasks [:tested]
                              :done #{}}
                             {:name "some-fn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "some?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sort"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sort-by"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sorted-map"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sorted-map-by"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sorted-set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sorted-set-by"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sorted?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "special-symbol?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "spit"
                              :tasks [:tested]
                              :done #{}}
                             {:name "split-at"
                              :tasks [:tested]
                              :done #{}}
                             {:name "split-with"
                              :tasks [:tested]
                              :done #{}}
                             {:name "str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "string?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "struct"
                              :tasks []
                              :done #{}}
                             {:name "struct-map"
                              :tasks []
                              :done #{}}
                             {:name "subs"
                              :tasks [:tested]
                              :done #{}}
                             {:name "subseq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "subvec"
                              :tasks [:tested]
                              :done #{}}
                             {:name "supers"
                              :tasks [:tested]
                              :done #{}}
                             {:name "swap!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "swap-vals!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "symbol"
                              :tasks [:tested]
                              :done #{}}
                             {:name "symbol?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "sync"
                              :tasks [:tested]
                              :done #{}}
                             {:name "tagged-literal"
                              :tasks [:tested]
                              :done #{}}
                             {:name "tagged-literal?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "take"
                              :tasks [:tested]
                              :done #{}}
                             {:name "take-last"
                              :tasks [:tested]
                              :done #{}}
                             {:name "take-nth"
                              :tasks [:tested]
                              :done #{}}
                             {:name "take-while"
                              :tasks [:tested]
                              :done #{}}
                             {:name "tap>"
                              :tasks [:tested]
                              :done #{}}
                             {:name "test"
                              :tasks [:tested]
                              :done #{}}
                             {:name "the-ns"
                              :tasks [:tested]
                              :done #{}}
                             {:name "thread-bound?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "time"
                              :tasks [:tested]
                              :done #{}}
                             {:name "to-array"
                              :tasks [:tested]
                              :done #{}}
                             {:name "to-array-2d"
                              :tasks [:tested]
                              :done #{}}
                             {:name "trampoline"
                              :tasks [:tested]
                              :done #{}}
                             {:name "transduce"
                              :tasks [:tested]
                              :done #{}}
                             {:name "transient"
                              :tasks [:tested]
                              :done #{}}
                             {:name "tree-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "true?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "type"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-add"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-add-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-byte"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-char"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-dec"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-dec-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-divide-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-double"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-float"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-inc"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-inc-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-long"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-multiply"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-multiply-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-negate"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-negate-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-remainder-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-short"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-subtract"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unchecked-subtract-int"
                              :tasks [:tested]
                              :done #{}}
                             {:name "underive"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unquote"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unquote-splicing"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unreduced"
                              :tasks [:tested]
                              :done #{}}
                             {:name "unsigned-bit-shift-right"
                              :tasks [:tested]
                              :done #{}}
                             {:name "update"
                              :tasks [:tested]
                              :done #{}}
                             {:name "update-in"
                              :tasks [:tested]
                              :done #{}}
                             {:name "update-keys"
                              :tasks [:tested]
                              :done #{}}
                             {:name "update-proxy"
                              :tasks [:tested]
                              :done #{}}
                             {:name "update-vals"
                              :tasks [:tested]
                              :done #{}}
                             {:name "uri?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "use"
                              :tasks [:tested]
                              :done #{}}
                             {:name "uuid?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "val"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vals"
                              :tasks [:tested]
                              :done #{}}
                             {:name "var-get"
                              :tasks [:tested]
                              :done #{}}
                             {:name "var-set"
                              :tasks [:tested]
                              :done #{}}
                             {:name "var?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vary-meta"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vec"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vector"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vector-of"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vector?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "volatile!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "volatile?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vreset!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "vswap!"
                              :tasks [:tested]
                              :done #{}}
                             {:name "when"
                              :tasks [:tested]
                              :done #{}}
                             {:name "when-first"
                              :tasks [:tested]
                              :done #{}}
                             {:name "when-let"
                              :tasks [:tested]
                              :done #{}}
                             {:name "when-not"
                              :tasks [:tested]
                              :done #{}}
                             {:name "when-some"
                              :tasks [:tested]
                              :done #{}}
                             {:name "while"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-bindings"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-bindings*"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-in-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-loading-context"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-local-vars"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-meta"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-open"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-out-str"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-precision"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-redefs"
                              :tasks [:tested]
                              :done #{}}
                             {:name "with-redefs-fn"
                              :tasks [:tested]
                              :done #{}}
                             {:name "xml-seq"
                              :tasks [:tested]
                              :done #{}}
                             {:name "zero?"
                              :tasks [:tested]
                              :done #{}}
                             {:name "zipmap"
                              :tasks [:tested]
                              :done #{}}]}
                 {:name "C++ interop"
                  :features [{:name "interop/include headers"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/link libraries"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/represent native objects"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/call native functions"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/explicitly box unbox native objects"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/refer to native globals"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/access native members"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/extract native value from jank object"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/convert native value to jank object"
                              :tasks [:done]
                              :done #{:done}}
                             {:name "interop/create native objects"
                              :tasks [:done]
                              :done #{:done}}]}
                 {:name "Tooling"
                  :features [{:name "leiningen support"
                              :tasks [:done]
                              :done #{}}
                             {:name "deps.edn"
                              :tasks [:done]
                              :done #{}}
                             {:name "nrepl support"
                              :tasks [:done]
                              :done #{}}
                             {:name "lsp server"
                              :tasks [:done]
                              :done #{}}
                             {:name "dap server"
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
              [:td (hiccup.core/raw (StringEscapeUtils/escapeHtml3 name))]])))

(defn milestone->table [{:keys [name features]}]
  [:div
   [:button {:class "collapsible"
             :type "button"}
    [:b "Milestone: "]
    name
    [:span {:class "vertical-rule"}]
    [:b "Status: "]
    (str (:percent-done (milestone->stats name)) "% complete")
    [:i {:class "arrow arrow-down"
         :style "float: right;"}]]
   [:table {:class "table is-fullwidth is-hoverable collapsible-content"}
    [:thead {}
     [:tr
      [:th {:width "25%"}
       "Feature"]]]
    (into [:tbody] (map feature->table-row features))]])

(defn root []
  (let [description "jank is under heavy development. It's safest to assume that any
        feature advertised is partially developed or in the planning stages.
        There is no sales pitch here; just a lot of work and some big
        plans. All development happens on Github, so watch the repo there!"]
    (page.view/page-root
    {:title "jank programming language - Clojure/LLVM/C++"
     :description description}
    [:div {}
     (page.view/header {})

     [:section {:class "hero is-info has-text-weight-medium"}
      [:div {:class "hero-body"}
       [:div {:class "container"}
        [:div {:class "content"}
         description]
        [:div {:class "has-text-centered"}
         [:a {:class "button ml-4"
              :href "https://github.com/jank-lang/jank"}
          [:span {:class "icon"}
           [:i {:class "gg-git-fork"}]]
          [:strong "Github"]]
         [:a {:class "button ml-4"
              :href "https://github.com/sponsors/jeaye"}
          [:span {:class "icon"
                  :style "color: rgb(201, 97, 152);"}
           [:i {:class "gg-heart"}]]
          [:strong "Sponsor"]]]]]]

     [:section {:id "milestones"
                :class "section"}
      [:div {:class "container"}
       (into [:div] (map milestone->table milestones))]]])))
