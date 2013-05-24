(ns brick.examples.derefable-middleware
  (:use [quil.core :exclude [size]]
        brick.core)
  (:require [brick.drawable :as drawable]
            [brick.image :as image])
  (:gen-class))

;;; These Vars should be abstracted by let bindings in production usage
(def images (atom []))
(def layers (atom []))
(def commands (atom []))
(def dict {:bricks 1 :bush-l 4 :bush-r 5})

(def swap-img (atom []))
(def proxy-middleware (atom []))

(defn- images-init [old]
  (vec (concat old
               (image/load-images (load-image "resources/32x32.png") [32 32]))))

(defn- layers-init [old]
  (let [lookup #(@images (image/dictionary dict %))]
    (swap! proxy-middleware (fn [_]
                              (drawable/->DerefMiddleware (atom (lookup 7)))))
    @proxy-middleware))

(defn- init
  "Prepare a bricklet. This includes initializing tiles and layers."
  [bricklet]
  (frame-rate 2)
  (background 0)
  (swap! images images-init)
  (swap! (:target-drawable bricklet) layers-init)
  (swap! swap-img (fn [_]
                    (drawable/->Image (load-image "resources/32x32.png")))))

(defn -main [& args]
  (def br (drawable/->Bricklet (atom layers) commands
                             :init init
                             :size [500 500]
                             :title "Let there be title!"))
  (def br-sketch (bricklet-sketch br))
  (. Thread sleep 2000)
  (swap! commands conj (fn [bricklet]
                         (swap! (:target-drawable @proxy-middleware) conj @swap-img))))
