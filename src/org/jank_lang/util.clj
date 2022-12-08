(ns org.jank-lang.util
  (:require [clojure.string]
            [hickory.core :as hickory]
            [markdown.core :as markdown]))

(def ^:dynamic *building?* false)

(defn deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))

(defn deep-merge [& maps]
  (let [maps (filter some? maps)]
    (apply merge-with deep-merge* maps)))

(defn merge-attrs
  "Performs a deep merge, but also concatenates classes."
  ([s1]
   s1)
  ([s1 s2 & ss]
   (apply merge-attrs
          (if (contains? s1 :class)
            (-> (update s1 :class str " " (:class s2))
                (deep-merge (dissoc s2 :class)))
            (deep-merge s1 s2))
          ss)))

(defn slurp-html! [html-path]
  (slurp (str "resources/generated/html/" html-path)))

(defn html->hiccup [html]
  (map hickory/as-hiccup (hickory/parse-fragment html)))

(defn markdown->hiccup [md]
  ; TODO: Add custom class to code tags to distinguish from shiki code.
  (->> (clojure.string/replace md #"\s+" " ")
       (markdown/md-to-html-string)
       html->hiccup))
