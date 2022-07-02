(def max-text-length 256)
(defn truncate
  "Truncates the text to be no longer than the max length."
  [text max-length]
  (cond
    (<= max-length 0)
    ""

    (<= (count text) max-length)
    text

    :else
    (str (subs text 0 (dec max-length)) "…")))

(assert (= "" (truncate "wowzer" 0)))
(assert (= "wow…" (truncate "wowzer" 4)))
