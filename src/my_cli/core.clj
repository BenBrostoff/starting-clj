(ns my-cli.core
  (:use (compojure handler
        [core :only (GET POST defroutes)]))
  (:require [net.cgrand.enlive-html :as en]
            [ring.util.response :as response] 
            [ring.adapter.jetty :as jetty]))


;value does not change once its defined once
(defonce counter (atom 100))
(defonce urls (atom {}))
(def brostoff "http://benbrostoff.github.io/")

(defn get-link
  [id]
  (if (= (@urls id) nil) brostoff (@urls id)))

;could write with some data store
(defn shorten
  [url]
  (let [id (swap! counter inc)
        id (Long/toString id 36)]
   (swap! urls assoc id url)))

(defn app
  [request]
  {:status 200
   :body (:with-out-str
           (println request))})

(defn server  
  []
  (shorten brostoff)
  (jetty/run-jetty #'app {:port 8080 :join? false}))


(en/deftemplate homepage
  (en/xml-resource "homepage.html")
  [request]
  ;;destructure
  [:#listing :li] (en/clone-for [[id url] @urls]
                                [:a] (comp
                                       ;compose fns together; calls last first
                                       (en/content (format "%s : %s" id url))
                                       (en/set-attr :href (str \/ id)))))


(defn redirect
  [id]
  (response/redirect (get-link id)))

(defroutes app*
  (GET "/" request (homepage request))
  (POST "/shorten" request
    ;; clj threading macro
    ;; equivalent to (:url (:params request))
    (let [id (shorten (-> request :params :url))]
      (response/redirect "/")))
  (GET "/:id" [id] 
    (redirect id) ))

;;apply middleware
(def app (compojure.handler/site app*))
