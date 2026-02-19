(ns development
  (:require
   [scad-clj.model :refer [with-fn]]
   [rasterkasten.models :refer [box! snapgrid]]
   [rasterkasten.render :refer [render*]]))



(comment
  (render* 
    "build/rendered.openscad"
    (with-fn 128
    
    ; (box!
    ;   {:spec '(
    ;            [[] | [] _ []]
    ;            [ _   _    _ ]
    ;            [[] | [] _ []]
    ;            [ _   _    _ ]
    ;            [[] | [] _ []]
    ;            )
    ;    :opts {:height :half
    ;           :fit :loose}})
    
    ; (box!
    ;   {:spec '(
    ;            [[] _ []]
    ;            [_    _ ]
    ;            [[] _ []]
    ;            [_    _ ]
    ;            [[] _ []]
    ;            )
    ;    :opts {:height :half
    ;           :fit :loose}})
      

    (snapgrid 3 3)  
    )))
