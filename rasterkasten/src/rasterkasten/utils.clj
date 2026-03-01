(ns rasterkasten.utils)

(defn mapv-2d-idx
  "Map over two indices in a column-row order.
   Takes an index `xr` and `yr` and runs a two dimensional 
   map in order xr yr and applies the given functions to the index pair."
  [xr yr f]
  (mapv 
    (fn [x]
      (mapv (fn [y] (f x y))
        (range xr)))
    (range yr)))