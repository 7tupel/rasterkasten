(ns rasterkasten.shapes
  (:require
   [scad-clj.model :refer [translate polyhedron cube cylinder difference -#]]))

(defn- align-square-pyramid-center
  "Align the pivot of a given model."
  [model pivot [x y z]]
  (case pivot
      :bottom-center  (translate [(/ x -2) (/ y -2) 0] model)
      :center         (translate [(/ x -2) (/ y -2) (/ z -2)] model)
      :tip            (translate [(/ x -2) (/ y -2) (* -1 z)] model)
      ; default use bottom left corner
      model))


(defn square-pyramid
  "Render a pyramid with a square base using the given dimensions."
  [x y h & {:keys [pivot] :or {pivot :default}}]
  (let [pyramid (polyhedron 
                  [[0 0 0] [x 0 0] [x y 0] [0 y 0] [(/ x 2) (/ y 2) h]]
                  [[0 3 2 1] [0 1 4] [1 2 4] [2 3 4] [3 0 4]])]
    (align-square-pyramid-center pyramid pivot [x y h])))


(defn truncated-square-pyramid
  "Render a pyramid with a square base using the given dimensions."
  [x y h t & {:keys [pivot] :or {pivot :default}}]
  (let [truncate  (fn [model]
                    (difference
                      model
                      (translate 
                        [0 0 t]
                        (-# (cube x y (- h t) :center false)))))]
    (-> (square-pyramid x y h)
      truncate
      (align-square-pyramid-center pivot [x y t]))))


(defn corner-cutter
  "Utility model used to cut a corner radius on another model.
   Takes the corner radius `r` and the height `h` of the cylindrical
   corner cutter."
  [r h]
  (difference
    (cylinder r h)
    (translate [r 0 0] (cylinder r h))))