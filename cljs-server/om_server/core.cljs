(ns om-server.core
  (:require
   [cljs.nodejs :as nodejs]
   [om-server-render.core :as web]
   [cognitect.transit :as t]))

;; Load the http module to create an http server.
(def http (js/require "http"))

;; Configure our HTTP server to respond with Hello World to all requests.

(defn make-server [callback]
  (.createServer
   http
   (fn [req resp]
     (println (pr-str (js->clj req)))

     (def body (atom ""))

     (.on req "data" (fn [data]
                       (reset! body (str @body data))
                       (when (> (count @body) 1e6)
                         (.destroy (.-connection req))
                         (.end resp "BAD BOY!"))))
     (.on req "end" #(callback resp @body))


     )))


(defn -main [& args]

  (.listen
   (make-server (fn [resp data]
                  (let [r (t/reader :json)
                        w (t/writer :json)
                        {:keys [initial-state]} (t/read r data)
                        rendered-info {:state initial-state
                                       :body (web/make-om-str initial-state)}
                        out-data (t/write w rendered-info)]
                    (println "Yum, data!" (pr-str rendered-info))
                    (.writeHead resp 200 #js {"Content-Type" "text/plain"})
                    (.end resp out-data))))
   8258))

(nodejs/enable-util-print!)
(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:main -main})
