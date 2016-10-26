(ns junglespec.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defn MainComponent []
  [:div
   [:h1 (:text @app-state)]
   [:button {:on-click (fn [_]
                         (go (let [response (<! (http/get "/song"))
                                   song (-> response
                                            :body)]
                               (swap! app-state assoc :text song))))}]])

(reagent/render [MainComponent] (js/document.getElementById "app"))
