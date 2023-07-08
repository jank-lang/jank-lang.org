(defn size->human-readable
  "Converts a size, in bytes, to a human readable format, such as 0 B, 1.5 kB,
   10 GB, etc."
  [size-in-bytes]
  (if (< -1000 size-in-bytes 1000)
    (str size-in-bytes " B")
    (let [res (loop [acc size-in-bytes
                     suffixes "kMGTPE"]
                (if (< -999950 acc 999950)
                  {:size acc
                   :suffix (first suffixes)}
                  (recur (/ acc 1000) (drop 1 suffixes))))]
      (format "%.1f %cB" (float (/ (:size res) 1000)) (:suffix res)))))

(assert (= "0 B" (size->human-readable 0)))
(assert (= "57.0 kB" (size->human-readable (* 57 1000))))
