(def movies {:the-weather-man {:title "The Weather Man"
                               :genres [:drama :comedy]
                               :tomatometer 59}
             :nightcrawler {:title "Nightcrawler"
                            :genres [:drama :crime :thriller]
                            :tomatometer 95}
             :the-bourne-identity {:title "The Bourne Identity"
                                   :genres [:action :thriller]
                                   :tomatometer 84}})

(def genre->movie (reduce (fn [acc [id movie]]
                            (let [{:keys [genres]} movie
                                  genre->this-movie (zipmap genres (repeat [id]))]
                              (merge-with into acc genre->this-movie)))
                          {}
                          movies))

; genre->movie is now a useful index.
; =>
{:drama [:the-weather-man :nightcrawler],
 :comedy [:the-weather-man],
 :crime [:nightcrawler],
 :thriller [:nightcrawler],
 :action [:the-bourne-identity]}

; We can look up all movies by genre.
(->> (genre->movie :thriller)
     (map movies)
     (sort-by :tomatometer))
; =>
({:title "The Bourne Identity",
  :genres [:action :thriller],
  :tomatometer 84}
 {:title "Nightcrawler",
  :genres [:drama :crime :thriller],
  :tomatometer 95})
