(defn -main [& args]
  (loop [game-state (new-game!)]
    (when (done? game-state)
      (end-game! game-state)
      (recur (next-state game-state)))))
