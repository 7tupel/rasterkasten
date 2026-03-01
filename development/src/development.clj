(ns development
  (:require
   [scad-clj.model :refer [with-fn]]
   [rasterkasten.models :refer [box! snapgrid]]
   [rasterkasten.render :refer [render*]]))


(comment
  ;;
  ;; ## Examples
  ;; This provides some examples how to specify and render boxes.
  ;; Just uncomment the model you would like to render.
  ;;
  
  (render* 
    "build/rendered.scad"
    (with-fn 128
    
      ;; Build a 3x3 half height box with some creative dividers
      ;; The box uses a loose fitting snap for the grid
      ; (box!
      ;   {:spec '(
      ;            [[] | [] _ []]
      ;            [ =   _    _ ]
      ;            [[] | [] _ []]
      ;            [ _   _    _ ]
      ;            [[] | [] _ []]
      ;            )
      ;    :opts {:height :half
      ;           :fit :loose}})
      
      ;; Build a 2x2 box with full height and snaps fitting tightly into the grid
      ;; Also add one vertical divider over the full with of the box
      ; (box!
      ;   {:spec '(
      ;            [[] _ []]
      ;            [=    = ]
      ;            [[] _ []]
      ;            )
      ;    :opts {:height :full
      ;           :fit :tight}})
      
      ;; Build a 2x3 box with no dividers, half height and loose fit
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
        
      ;; Build a 3x3 grid
      ; (snapgrid 3 3)  
      
      ;; render a giant box. should onlu be used for debugging box scaling
      ;; because rendering on openscad takes very long and usual 3d printers
      ;; would no bew able to print this anyways
      (box!
        {:spec '(
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 [_    _    _    _    _    _    _ ]
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 [_    _    _    _    _    _    _ ]
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 [_    _    _    _    _    _    _ ]
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 [_    _    _    _    _    _    _ ]
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 [_    _    _    _    _    _    _ ]
                 [[] _ [] _ [] _ [] _ [] _ [] _ []]
                 )
         :opts {:height :half
                :fit :loose}})
      
      )))
