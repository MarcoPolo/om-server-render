(ns om-server-render.core
  (:require
   [compojure.core :as c]
   [compojure.route :as route]
   [hiccup.core :as hiccup :refer [html]]
   [clj-http.client :as client]
   [cognitect.transit :as transit]
   [clojure.data.codec.base64 :as b64])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))


(defn cdn [x] (str "http://localhost:8080/" x))

(def node-server "http://localhost:8258")


(defn ->transit [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer x)
    out))

(defn ->transit-b64 [x]
  (let [out (->transit x)
        b64-out (ByteArrayOutputStream. 4096)]
    (b64/encoding-transfer
     (ByteArrayInputStream. (.toByteArray out))
     b64-out)
    (.toString b64-out)))

(defn ->transit-str [x]
  (.toString (->transit x)))


(c/defroutes app
  (c/GET "/:count" [count]
         (let [count (or (Integer. count) 42)
               rendered-info-raw (-> (client/post node-server
                                                  {:body
                                                   (->transit-str
                                                    {:initial-state
                                                     {:count count}})}) :body)
               in (ByteArrayInputStream. (.getBytes rendered-info-raw))
               reader (transit/reader in :json)
               rendered-info (transit/read reader)
               {:keys [state body]} rendered-info]

           (html
            [:body
             [:h1 "Count was " count]
             [:div#app
              body]
             [:script {:src "http://fb.me/react-0.9.0.js"}]
             [:script
              (format "initialState = atob('%s')" (->transit-b64 state))]
             [:script {:src (cdn "om_server_render.js" )}]
             [:script "goog.require(\"om_server_render.core\")"]])))
  (route/not-found "<h1>Page not found</h1>"))
