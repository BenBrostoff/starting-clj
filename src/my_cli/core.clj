(ns my-cli.core
  (:use (compojure handler
        [core :only (GET POST defroutes)]))
  (:require [ring.util.response :as response] 
            [ring.adapter.jetty :as jetty]))


;value does not change once its defined once
(defonce counter (atom 100))
(defonce urls (atom {}))

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
  (shorten "http://benbrostoff.github.io/")
  (jetty/run-jetty #'app {:port 8080 :join? false}))

(defn homepage
  [request]
  (str @urls))

(defn redirect
  [id]
  (response/redirect (@urls id)))

(defroutes app
  (GET "/" request (homepage request))
  (GET "/:id" [id] (redirect id) ))

