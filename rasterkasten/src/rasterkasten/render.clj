(ns rasterkasten.render
  (:require
   [scad-clj.scad :as s]))

(defn render*
  [out-path model]
  (spit out-path (s/write-scad model)))