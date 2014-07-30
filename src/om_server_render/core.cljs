(ns om-server-render.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cognitect.transit :as t]
            [om-server-render.web :as web]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(defn counter [data owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (println "BUILDING!")
      (dom/button
       #js {:onClick #(do (println "clicked! " (om/get-state owner)) (om/update-state! owner :count inc))}
       (str "Count: " (:count state))))))

(defn make-om-str [initial-state]
  (dom/render-to-str
   (om/build counter app-state
             {:init-state initial-state})))

(defn init []
  (let [reader (t/reader :json)
        initial-state (t/read reader js/initialState)]

    (om/root
     counter
     app-state
     {:target (. js/document (getElementById "app"))
      :init-state initial-state})))

;; Checking if this is running in Node
(if (and (exists? js/module) (.-exports js/module))
  (set! (.-exports js/module) #js {:toOm web/make-om-str})
  (init))
