(defn post! [_request]
  ; Assuming this performs some network effect.
  {:status 200
   :body (pr-str {:order 7821})})

(defn submit-order! []
  (let [request {:url "/submit-order"}
        order (post! request)
        order-body (-> order :body read-string)]
    (if (contains? order-body :error)
      ; This is the code path we want to test.
      {:error "failed to submit"
       :request request
       :response order-body}
      (:order order-body))))

; Later on, in tests, skip the side effect by redefining.
(deftest submit-order
  (testing "failed post"
    (with-redefs [post! (fn [_]
                          ; Fake error to see how the rest of the code handles it.
                          {:status 500
                           :body (pr-str {:error "uh oh"})})]
      (is (= "failed to submit" (:error (submit-order!)))))))
