(ns junglespec.client
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cljs.spec :as s]
            [cljs-bach.synthesis :as bach]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.test.check.generators :as gen]
            [junglespec.common])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defonce app-state (atom {:text   "Jungle Docs!"
                          :search ""}))

(defonce quil-context (atom nil))

;(defn draw []
;  (q/background 255)
;  (q/fill 0)
;  (map #(q/text (str @quil-context) 0 0 100 100)))

  ;(q/ellipse 100 46 55 55))


;(q/defsketch data-vis
;             :host "data-vis"
;             :draw draw
;             :size [300 100]
;             :no-start true
;             :middleware [m/fun-mode])
;
;(defn hello-world []
;  (reagent/create-class
;    {:reagent-render      (fn [] [:canvas#foo {:width 300 :height 300}])
;     :component-did-mount data-vis}))
;
;(reagent/render-component [hello-world]
;                          (js/document.getElementById "quil"))

(defn Sketch []
  [:canvas#data-vis])

(defn Gen [spec]
  (let [generated-example (s/conform spec (gen/generate (s/gen spec)))]
    [:div
     [:pre.language-klipse (str generated-example)]]))


(defn SpecIndex [registry]
  [:div.spec-index
   (for [spec registry]
     ^{:key (name spec)}
     [:pre (name spec)])])

(defn Spec [spec]
  [:div.spec
   [:pre (name spec)]
   [:p [:i (str spec)]]
   [:p (str (s/describe spec))]
   [Gen spec]
   [:hr]])

(defn SpecList [registry]
  [:div.spec-list
   (for [spec registry]
     ^{:key (name spec)}
     [Spec spec])])

(defn SpecSearch []
  [:label "Filter: "
   [:input.search {:type      "text"
                   :on-key-up (fn [e]
                                (let [value (.-value (.-target e))]
                                  (swap! app-state assoc :search value)
                                  (println value)))}]])

(defn junglespec? [spec]
  (re-find (re-pattern "junglespec") (namespace spec)))

(defn MainComponent []
  (let [registry (->> (keys (s/registry))
                      (filter junglespec?))]
    [:div.main
     [:h1 [:pre (:text @app-state)]]
     ;[SpecSearch]
     [Sketch]
     [SpecList registry]
     [SpecIndex registry]
     [:button {:on-click (fn [_]
                           (go (let [response (<! (http/get "/song"))
                                     song (-> response
                                              :body)]
                                 (swap! app-state assoc :text song))))}
      "Yo"]]))

(reagent/render [MainComponent] (js/document.getElementById "app"))
