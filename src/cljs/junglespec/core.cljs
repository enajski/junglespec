(ns junglespec.client
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.spec :as s]
            [clojure.test.check.generators :as gen]
            [junglespec.spec]))

(enable-console-print!)

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

(defn junglespec? [spec]
  (re-find (re-pattern "junglespec") (namespace spec)))

(defn MainComponent []
  (let [registry (->> (keys (s/registry))
                      (filter junglespec?))]
    [:div.main
     [:h1 [:pre "Jungle Docs"]]
     [SpecList registry]
     [SpecIndex registry]]))

(reagent/render [MainComponent] (js/document.getElementById "app"))
