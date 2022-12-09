(ns org.jank-lang
  (:require [clojure.string]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.adapter.jetty :as jetty]
            [me.raynes.fs :as fs]
            [org.jank_lang.page.landing.view :as landing.view]
            [org.jank_lang.page.progress.view :as progress.view]
            [org.jank_lang.page.blog.view :as blog.view]
            [org.jank-lang.util :as util])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

(defroutes app-handler
  (GET "/" [] (fn [_]
                (landing.view/root)))
  (GET "/progress" [] (fn [_]
                        (progress.view/root)))
  (GET "/blog" [] (fn [_]
                    (blog.view/root)))
  (GET "/blog/feed.xml" [] (fn [_]
                             (blog.view/feed-root)))
  (GET "/blog/:post-id" [post-id] (fn [_]
                                    (blog.view/post-root post-id)))
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

(defn build-blog! [output-dir]
  (let [[_root _dirs files] (first (fs/iterate-dir "resources/src/blog"))]
    (doseq [file files]
      (let [post-id (clojure.string/replace-first file #"\.md$" "")
            output-file (str "blog/" post-id "/index.html")]
        (println "Building" output-file)
        (fs/mkdirs (fs/parent (str output-dir "/" output-file)))
        (spit (str output-dir "/" output-file) (blog.view/post-root post-id))))))

(defn build! []
  (binding [util/*building?* true]
    (let [output-dir "build"
          pages {"index.html" landing.view/root
                 "progress/index.html" progress.view/root
                 "blog/index.html" blog.view/root
                 "blog/feed.xml" (comp :body blog.view/feed-root)}]
      (fs/delete-dir output-dir)
      (fs/copy-dir "resources/public" output-dir)
      (doseq [[output-file view-fn] pages]
        (println "Building" output-file)
        (fs/mkdirs (fs/parent (str output-dir "/" output-file)))
        (spit (str output-dir "/" output-file) (view-fn)))

      (build-blog! output-dir))))

(defn -main [& [command & _args]]
  (case command
    "build" (build!)
    "serve" (start!))
  (shutdown-agents))

(comment
  ; http://localhost:3000
  (start!)
  (stop!))
