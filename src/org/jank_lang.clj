(ns org.jank-lang
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.adapter.jetty :as jetty]
            [me.raynes.fs :as fs]
            [org.jank_lang.page.landing.view :as landing.view]
            [org.jank_lang.page.progress.view :as progress.view])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

(defroutes app-handler
  (GET "/" [] (fn [_]
                (landing.view/root)))
  (GET "/progress" [] (fn [_]
                        (progress.view/root)))
  (route/not-found "<h1>Page not found</h1>"))

(defonce app-server (atom nil))

(defn start! []
  (reset! app-server (jetty/run-jetty (-> #'app-handler
                                          wrap-reload
                                          (wrap-resource "public")
                                          wrap-content-type)
                                      {:port 3000
                                       :join? false})))

(defn stop! []
  (when-some [^Server s @app-server]
    (.stop s)
    (reset! app-server nil)))

(defn build! []
  (let [output-dir "build"
        pages {"index.html" landing.view/root
               "progress/index.html" progress.view/root}]
    (fs/delete-dir output-dir)
    (fs/copy-dir "resources/public" output-dir)
    (doseq [[output-file view-fn] pages]
      (fs/mkdirs (fs/parent (str output-dir "/" output-file)))
      (spit (str output-dir "/" output-file) (view-fn)))))

(defn -main [& [command & _args]]
  (case command
    "build" (build!)
    "serve" (start!)))

(comment
  ; http://localhost:3000
  (start!)
  (stop!))
