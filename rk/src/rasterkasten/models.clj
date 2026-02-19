(ns rasterkasten.models
  (:require
   [clojure.math :refer [sqrt ceil]]
   [scad-clj.model :refer [cube cylinder text
                           translate rotate mirror union hull difference 
                           deg->rad extrude-linear]]
   [rasterkasten.utils :refer [mapv-2d-idx]]
   [rasterkasten.shapes :refer [truncated-square-pyramid corner-cutter]]))

;;;; Box Specs


(def tile 
  "The width of a tile in Millimeter."
  60)

(def full-height 
  "The full height of a box in Millimeter.
   The height does not include the bottom grid connector (that is 
   both the grid-taper and grid-snap."
  80)

(def grid-taper-height 
  "The height of the tapered part of the grid connector in Millimeter."
  3)

(def grid-snap-height 
  "The height of the straight part of the grid connector in Millimeter."
  3)

(def grid-height 
  "The overall grid height in Millimeter."
  (+ grid-taper-height grid-snap-height))

(def wall 
  "The thikness of the box wall in Millimeter.
   That is the thikness for both outside walls and divider walls."
  1.2)

(def loose-fit-delta 
  "The small delta in Millimeter used to loosen the boxes bottom
   snap width to get a looser fit."
  0.2)

(def box-corner-radius 
  "The corner radius used for the boxes in Millimeter."
  2)




;;;; Implementation

(defn snap-width
  "Calculate the snap with.
   If `variant` is `:loose` then the snap width is slightly smaller to
   allow easy removal of the box from the grid."
  [variant]
  (if (= variant :loose)
    (- tile (* 2 (+ grid-taper-height loose-fit-delta)))
    (- tile grid-taper-height grid-taper-height)))


(defn single-snap-grid-tile
  "A single snapgrid tile."
  []
  (difference
    (cube tile tile (+ grid-snap-height grid-taper-height) :center false)
    (union
      ;; taper difference
      (translate [(/ tile 2) (/ tile 2) grid-taper-height]
        (rotate [(deg->rad 180) 0 0]
          (truncated-square-pyramid tile tile (/ tile 2) grid-taper-height :pivot :tip)))
      ;; snap difference
      (translate [grid-taper-height grid-taper-height 0]
        (cube (snap-width :full) (snap-width :full) grid-taper-height :center false)))))


(defn snapgrid
  "A snapgrid with given dimensions.
   Takes the amount of `rows` and `cols` of the grid."
  [rows cols]
  (union
    (mapv
      (fn [row]
        (mapv
          (fn [col]
            (translate [(* row tile) (* col tile) 0] (single-snap-grid-tile)))
          (range cols)))
      (range rows))))


(defn box-height
  "Calculate the height of the box.
   This will return the height of the box without(!) the bottom taper and
   snap."
  [h]
  (case h
    :quarter        (/ full-height 4)
    :third          (/ full-height 3)
    :half           (/ full-height 2)
    :twothirds      (* 2 (/ full-height 3))
    :threequarter   (* 3 (/ full-height 4))
    ;; default use full height
    full-height))


(defn box-body
  "The body of the box with four rounded edges.
   Takes the outside `x`, `y` and `z` size of the box and the
   corner radius `r`."
  [x y z r]
  (hull
    (union
      (translate [r r 0]
        (cylinder r z :center false))
      (translate [(- x r) r 0]
        (cylinder r z :center false))
      (translate [r (- y r) 0]
        (cylinder r z :center false))
      (translate [(- x r) (- y r) 0]
        (cylinder r z :center false)))))


(defn box-snap
  "Create the snap of the box.
   The snap is the bottom part that fits into the snap.
   Takes the square width `w` and the height `h` of the snap as well as the
   corner radius `r` for the four side edges."
  [w h r]
  (box-body w w h r))


(defn box-taper
  "The bottom taper of the box.
   Creates the bottom taper of a box with the given width `w` and height `h` and
   the radius `r` for a smooth transition of the rounded box body to the taper."
  [w h r]
  (let [xy-correction (- (/ r (* r (sqrt 2))) 0.2)] ;; TODO: this translations are not 100% right
    (difference
      (rotate [(deg->rad 180) 0 0]
        (truncated-square-pyramid w w (/ w 2) h :pivot :tip))
      ;; round the corners
      (union
        (translate [(+ (/ w -2) xy-correction) (+ (/ w -2) xy-correction) (* h -1)]
          (rotate [0 0 (deg->rad 45)]
            (corner-cutter r w)))
        (translate [(+ (/ w -2) xy-correction) (- (/ w 2) xy-correction) (* h -1)]
          (rotate [0 0 (deg->rad -45)]
            (corner-cutter r w)))
        (translate [(- (/ w 2) xy-correction) (- (/ w 2) xy-correction) (* h -1)]
          (rotate [0 0 (deg->rad -135)]
            (corner-cutter r w)))
        (translate [(- (/ w 2) xy-correction) (+ (/ w -2) xy-correction) (* h -1)]
          (rotate [0 0 (deg->rad 135)]
            (corner-cutter r w)))))))


(defn box-hull
  "Takes
   - `rows` - the row size of the box
   - `cols` - the column size of the box
   - `h` - the height of the box
   - `r` - the corner radius
   - `s-h` - the snap height"
  [rows cols h r s-h s]
  (let [snap-xy-offset  (if (= s :loose) 
                          (+ grid-snap-height loose-fit-delta) 
                          grid-snap-height)
        body-x          (* rows tile)
        body-y          (* cols tile)]
    (union
      ;; the upper body of the box with the radius
      (translate [0 0 (+ grid-taper-height grid-snap-height)]
        (box-body body-x body-y h r))
      ;; box lower taper and snap
      (mapv-2d-idx
        rows
        cols
        (fn [idx-r idx-c]
          (union
                ;; box bottom taper
                (translate [(+ (* idx-c tile) (/ tile 2)) 
                              (+ (* idx-r tile) (/ tile 2)) 
                              grid-taper-height]
                  (box-taper tile grid-taper-height r))
                ;; box bottom snap
                (translate [(+ (* idx-c tile) snap-xy-offset)
                              (+ (* idx-r tile) snap-xy-offset)
                              0]
                  (box-snap s-h grid-snap-height r))))))))


(defn box
  [rows cols h snap]
  (let [body-height         (box-height h)
        body-x              (* rows tile)
        body-y              (* cols tile)
        corner-radius       box-corner-radius
        snap-width          (snap-width snap)
        snap-cutout-width   (- snap-width wall wall)
        snap-cutout-offset  (+ snap-cutout-width wall wall grid-taper-height grid-taper-height)
        text-size           10]
  (difference
    ;; the positive box
    (box-hull rows cols body-height corner-radius snap-width snap)
    ;; remove inner stuff
    (union
      ;; top angle cutout
      (mapv-2d-idx
        rows
        cols
        (fn [idx-r idx-c]
          (translate [(+ (/ tile 2) (* idx-c tile)) 
                        (+ (/ tile 2) (* idx-r tile))
                        (+ body-height grid-snap-height grid-taper-height)]
            (rotate [(deg->rad 180) 0 0]
              (truncated-square-pyramid tile tile (/ tile 2) grid-taper-height :pivot :bottom-center)))))
      ;; body cutout
      (translate [wall wall (+ grid-snap-height grid-taper-height (/ wall 2))]
        (box-body (- body-x wall wall) (- body-y wall wall) body-height corner-radius))
      ;; bottom cutout
      (mapv-2d-idx
        rows
        cols
        (fn [idx-r idx-c]
          (translate [(+ wall grid-taper-height (* idx-c snap-cutout-offset)) 
                        (+ wall grid-taper-height (* idx-r snap-cutout-offset)) 
                        wall]
            (box-snap snap-cutout-width (+ grid-taper-height grid-snap-height) corner-radius))
          ))
      ;; snap label
      (when-not (= snap :loose)
        (translate [(- (/ tile 2) (/ text-size 2)) (- (/ tile 2) (/ text-size 2)) 0]
          
            (extrude-linear
              {:height (/ wall 3) :center false}
              (mirror [0 1 0] (text "S" :size text-size)))))))))

(comment
  ;;
  ;; ## Example Boxes
  ;;
  (def box-1-1
    {:spec '([])
             
     :opts {}})
  
  (def box-1-2
    {:spec '(
             [[] _ []]
             )
     :opts {}})
  
  (def box-1-2-divider
    {:spec '(
             [[] | []]
             )
     :opts {}})
  
  (def box-2-2
    {:spec '(
             [ [] _ [] ]
             [ _    _  ]
             [ [] _ [] ]
             )
     :opts {}})
  
  (def box-2-3-divider
    {:spec '(
             [[] | [] | []]
             [ =   =    = ]
             [[] | [] | []]
             )
     :opts {}})
  
  (def box-4-3-divider
    {:spec '(
             [[] | [] | []]
             [ =   =    = ]
             [[] | [] | []]
             [ =   =    = ]
             [[] | [] | []]
             [ =   =    = ]
             [[] | [] | []]
             )
     :opts {}})
  )

(defn divider-height
  "Calculate the height of a divider.
   Takes the height `h` of the box as keyword."
  [h]
  (- (box-height h) grid-height))


(defn vertical-divider
  [h]
  (let [height (divider-height h)]
    (translate [0 0 grid-height] 
      (cube wall tile height :center false))))


(defn horizontal-divider
  [h]
  (let [height (divider-height h)]
    (translate [0 0 grid-height] 
      (cube tile wall height :center false))))


(defn col-count
  [b-spec]
  (int (ceil (/ (count b-spec) 2))))

(comment
  (col-count (:spec box-1-1)) ; -> 1
  (col-count (:spec box-1-2)) ; -> 2
  (col-count (:spec box-1-2-divider)) ; -> 2
  (col-count (:spec box-2-2)) ; -> 2
  (col-count (:spec box-2-3-divider)) ; -> 3
  )


(defn at-least-1
  [i]
  (if (< i 1) 1 i))


(defn row-count
  [b-spec]
  (at-least-1 (int (ceil (/ (count (first b-spec)) 2)))))

(comment
  (row-count (:spec box-1-1)) ; -> 1
  (row-count (:spec box-1-2)) ; -> 1
  (row-count (:spec box-1-2-divider)) ; -> 1
  (row-count (:spec box-2-2)) ; -> 2
  (row-count (:spec box-2-3-divider)) ; -> 2  
  )


(defn get-spec-h-dividers
  [spec]
  (take-nth 2 (rest spec)))

(comment
  (get-spec-h-dividers (:spec box-1-1)) ; -> ()
  (get-spec-h-dividers (:spec box-1-2)) ; -> ()
  (get-spec-h-dividers (:spec box-1-2-divider)) ; -> ()
  (get-spec-h-dividers (:spec box-2-2)) ; -> ([_ _])
  (get-spec-h-dividers (:spec box-2-3-divider)) ; -> ([ = = = ])
  (get-spec-h-dividers (:spec box-4-3-divider)) ; -> ([ = = = ] [ = = = ] [ = = = ])
  )


(defn get-spec-v-dividers
  [spec]
  (->> (take-nth 2 spec)
    (mapv (fn [v] (into [] (filter symbol? v))))))

(comment
  (get-spec-v-dividers (:spec box-1-1)) ; -> ()
  (get-spec-v-dividers (:spec box-1-2)) ; -> ([_])
  (get-spec-v-dividers (:spec box-1-2-divider)) ; -> ([|])
  (get-spec-v-dividers (:spec box-2-2)) ; -> ([_] [_])
  (get-spec-v-dividers (:spec box-2-3-divider)) ; -> ([ | | ] [ | | ])
  (get-spec-v-dividers (:spec box-4-3-divider)) ; -> ([ | | ] [ | | ] [ | | ] [ | | ])
  )


(defn with-vertical-dividers
  [model spec height]
  (let [v-dividers  (get-spec-v-dividers (reverse spec))]
    (union
      model
      (map-indexed
        (fn [c-idx row]
          (map-indexed
            (fn [r-idx c]
              (when (= '| c)
                (translate [(- (+ (* (inc r-idx) tile)) wall)
                              (+ (* c-idx tile) 0)
                              0]
                  (vertical-divider height))))
            row))
        v-dividers))))


(defn with-horizontal-dividers
  [model spec height]
  (let [h-dividers  (reverse (get-spec-h-dividers spec))]
    (union
      model
      (map-indexed
        (fn [c-idx row]
          (map-indexed
            (fn [r-idx c]
              (when (= '= c)
                (translate [(+ (* r-idx tile) )
                              (- (* (inc c-idx) tile) wall)
                              0]
                  (horizontal-divider height))))
            row))
        h-dividers))))


(defn box!
  [{:keys [spec opts]}]
  (let [rows    (row-count spec)
        cols    (col-count spec)
        height  (or (:height opts) :full)
        fit     (or (:fit opts) :loose)]
    (-> (box rows cols height fit)
      (with-horizontal-dividers spec height)
      (with-vertical-dividers spec height))))

