(ns org.jank_lang.page.blog.view
  (:require [clojure.string]
            [clojure.walk :refer [postwalk]]
            [clojure.java.shell :refer [sh]]
            [hiccup2.core :as hiccup.core]
            [java-time.api :as java-time]
            [markdown.core :as markdown]
            [me.raynes.fs :as fs]
            [org.jank_lang.page.view :as page.view]
            [org.jank-lang.util :as util])
  (:import org.apache.commons.text.StringEscapeUtils))

; We use this to speed up blog home page and feed generation, since
; we don't need to highlight all code snippets for those.
(def ^:dynamic *highlight?* false)

(defn html->hiccup [html]
  (let [hiccup (->> (util/html->hiccup html)
                    (into [:div {:class "content"}]))]
    ; TODO: Try prewalk instead
    (postwalk (fn [form]
                (cond
                  (and (vector? form)
                       (= :code (first form))
                       (-> form second :class empty? not)
                       *highlight?*)
                  (let [file-type (-> form second :class)
                        file (fs/ephemeral-file file-type)]
                    (spit file (-> (nth form 2)
                                   StringEscapeUtils/unescapeHtml3
                                   clojure.string/trim))
                    (-> (:out (sh "./bin/highlight" (.getPath file) file-type))
                        util/html->hiccup
                        first))

                  (and (vector? form)
                       (= :pre (first form))
                       (= :pre (-> form (nth 2) first))
                       (clojure.string/includes? (or (-> form (nth 2) second :class) "") "shiki"))
                  (nth form 2)

                  :else
                  form))
              hiccup)))

(defn parse-markdown [md]
  (let [{:keys [html metadata]} (markdown/md-to-html-string-with-meta md)]
    {:hiccup (html->hiccup html)
     :metadata metadata}))

(defn metadata->str [md kw]
  (clojure.string/join " " (-> md :metadata kw)))

(defn post-root [post-id]
  (let [md (binding [*highlight?* true]
             (parse-markdown (slurp (str "resources/src/blog/" post-id ".md"))))
        post-title (metadata->str md :title)]
    (page.view/page-root
      (merge {:title post-title
              :description (metadata->str md :description)}
             (let [image (metadata->str md :image)]
               (when-not (empty? image)
                 {:image image})))
      [:div {}
       (page.view/header {:title "jank blog"
                          :title-url "/blog"
                          :primary? true
                          :sponsor? true
                          :progress? false})
       [:section {:class "section"}
        [:div {:class "container blog-container"}
         [:span {:class "is-size-1"}
          post-title]
         [:div {:class "is-size-6 has-text-weight-light"}
          (metadata->str md :date)
          " · "
          [:a {:class "has-text-weight-normal"
               :href (metadata->str md :author-url)}
           (metadata->str md :author)]]
         [:hr]
         (:hiccup md)]]])))

(defn root []
  (let [[_root _dirs files] (first (fs/iterate-dir "resources/src/blog"))
        post-ids (->> (sort files)
                      reverse
                      (map #(clojure.string/replace-first % #"\.md$" "")))
        ; TODO: Dynamic var to not highlight code.
        post-mds (map #(-> (parse-markdown (slurp (str "resources/src/blog/" % ".md")))
                           (assoc :id %))
                      post-ids)]
    (page.view/page-root
      {:title "jank - blog"
       :description "jank is a Clojure dialect hosted on LLVM with native C++ interop."}
      [:div {}
       (page.view/header {:title "jank blog"
                          :title-url "/blog"
                          :primary? true
                          :sponsor? true
                          :progress? false})
       (into [:div {:class "container blog-container"}]
             (map (fn [md]
                    (when (or (empty? (metadata->str md :draft)) (not util/*building?*))
                      [:section {:class "section"}
                       [:div {:class "level"}
                        [:div {:class "level-left"
                               :style "flex: 1;"}
                         [:div {:class "level-item"
                                :style "flex: 1; justify-content: left;"}
                          [:a {:href (str "/blog/" (:id md))}
                           [:h1 {:class "is-size-3 has-text-weight-bold"}
                            (metadata->str md :title)]]]]
                        [:div {:class "level-right"}
                         [:div {:class "level-item"}
                          [:h1 {:class "is-size-6 "}
                           (metadata->str md :date)]]]]
                       [:div
                        (-> md :hiccup (nth 2))]]))
                  post-mds))])))

(defn post-date->instant [date]
  (java-time/format "yyyy-MM-dd'T'HH:mm:'00Z'" (java-time/local-date-time "MMM dd, yyyy" date 0)))

(defn feed-root []
  (let [[_root _dirs files] (first (fs/iterate-dir "resources/src/blog"))
        post-ids (->> (sort files)
                      reverse
                      (map #(clojure.string/replace-first % #"\.md$" "")))
        post-mds (map #(-> (parse-markdown (slurp (str "resources/src/blog/" % ".md")))
                           (assoc :id %))
                      post-ids)]
    {:status 200
     :header {"Content-Type" "application/atom+xml"}
     :body (->> (into [:feed {:xmlns "http://www.w3.org/2005/Atom"}
                       [:link {:href "https://jank-lang.org/blog/feed.xml"
                               :rel "self"
                               :type "application/atom+xml"}]
                       [:link {:href "https://jank-lang.org/blog/"
                               :rel "alternate"
                               :type "text/html"}]
                       [:updated (str (java-time/instant))]
                       [:title "jank blog"]
                       [:id "https://jank-lang.org/blog/"]]
                      (map (fn [md]
                             [:entry
                              [:title {:type "html"}
                               (metadata->str md :title)]
                              [:link {:href (str "https://jank-lang.org/blog/" (:id md))
                                      :rel "alternate"
                                      :type "text/html"
                                      :title (metadata->str md :title)}]
                              [:published (post-date->instant (metadata->str md :date))]
                              [:updated (post-date->instant (metadata->str md :date))]
                              [:id (str "https://jank-lang.org/blog/" (:id md))]
                              [:author
                               [:name (metadata->str md :author)]]
                              [:summary {:type "html"}
                               (str (hiccup.core/html (-> md :hiccup (nth 2))))]])
                           post-mds))
                (hiccup.core/html {:mode :xml})
                str)}))
