(ns org.jank_lang.page.util)

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

(comment
  (merge-attrs {} nil)
  (merge-attrs {:class "foo"
                :style {:foo :bar}}
               {:class "bar"
                :style {:spam :meow}})
  )
