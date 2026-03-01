# Rasterkasten

Rasterkasten - the german word for _Grid Box_ - is a simple system for storage and assortment boxes that fit into a grid system.

Rasterkasten is build using Clojure and the wonderful [scad-clj](https://github.com/farrellm/scad-clj) library. The later enables building openSCAD models using Clojure. The library generates openSCAD compatible code which can then be rendered into a *.stl* file ready for 3D Printing.


## Why another Storage Box System?

The basic idea of Rasterkasten is very similar to Gridfinity. Instead of reinventing the wheel, I actually build Rasterkasten when Gridfinity did not exist yet. And just as Gridfinity, Rasterkasten was inspired by [Alexandre Chappel](https://www.youtube.com/watch?v=aiDHXKRxLeQ) and his Box system. When Alexandre Chappel first published his box system, I build my own version of it. I did so because I wanted to have maximum flexibily instead of relying on the models published by someone else. And also I liked the challenge to build it myself using Clojure and OpenSCAD.

Now should you use Rasterkasten over Gridfinity or ModuBOX? It will depend but to be honest, but probably not. Gridfinity has a very large ecosystem with thousands of models ready to be used. ModuBOX is very thoughfully engineered and also has lots of models available.

Still, I like to be able to build the models of Rasterkasten fully as code using Clojure.

Should you choose to go with Rasterkasten, be warned, it is **not compatible** with either Gridfinity or ModuBOX!


## Specifications

Rasterkasten uses a base tile size of 60 by 60 Millimeters and defines the full height of 80 Millimeters. The height does not include the bottom taper of the box that goes into the grid! 

All Boxes fit into the snapgrid. The grid has the height of 6 Millimeters, it has a tapered part and a square part. It provides a tight yet loose enough fit to securely hold the boxes while allowing easy inserting and removal. Boxes can be rendered using a tightness modificator that makes the fit a bit tighter if desired.

Boxes can be rendered in the heights _quarter_, _third_, _half_, _two-thirds_, _three-quarters_ and _full_. The top of the box is slightly tapered to allow stacking.

The box generator is also able to add internal dividers, both vertical, horizontal and any combination.


## Generate a Box

The `rasterkasten.models` namespace provides two functions that are mainly used to generate both the grid and boxes:

```Clojure
(ns readme)
;; we would like to have a very smooth model
(require '[scad-clj.model :refer [with-fn]])
;; import functions to render the grid and the box
(require '[rasterkasten.models :refer [generate-box snapgrid]])
;; utility to write the openscad model to disk
(require '[rasterkasten.render :refer [render*]])
```

For example, the following code builds the openSCAD model for a 3x4 grid:

```Clojure
(render* 
  "build/rendered.scad"
  (with-fn 128
    (snapgrid 3 4)))
```


To build the model for the boxes, Rasterkasten has a very simple DSL to define the box. The next example creates a model for a 2x2 box with full height and a horizontal divider. The model also uses the fit modificator to get a very tight fit.

```Clojure
(render* 
  "build/rendered.scad"
  (with-fn 128
    (generate-box
        {:spec '(
                 [[] _ []]
                 [=    = ]
                 [[] _ []]
                 )
         :opts {:height :full
                :fit    :tight}})))
```

Here are some more examples:

```Clojure
;; Build a 2x3 box with no dividers, half height and loose fit
(generate-box
  {:spec '(
           [[] _ []]
           [_    _ ]
           [[] _ []]
           [_    _ ]
           [[] _ []]
           )
   :opts {:height :half
          :fit    :loose}})

;; Build a 3x3 quarter height box with some creative dividers
;; The box uses a loose fitting snap for the grid
(generate-box
  {:spec '(
           [[] | [] _ []]
           [ =   _    _ ]
           [[] | [] _ []]
           [ _   _    _ ]
           [[] | [] _ []]
           )
   :opts {:height :quater
          :fit    :loose}})
```

As you can see, you can set vertical and horizontal dividers between any tiles. The `generate-box` function does not check for you if the divider makes sense. This is up to you and allows you to do any wild divider variation you would like to have.


## 3D Printing

Rasterkasten can be printed on pretty much any printer. The angle of the grid taper is choosen so that no support is needed when printing the boxes.

In general, any materials can be used. For most applications I find PLA is strong enough, for more tough usage I would recommend to use PETG. I have not tried printing them with ABS or ASA, I would expect that some tuning is required in the slicer to account for material shrinkage when the material cools. A layer height of 0.3 mm is good enough to print the boxes, but you can of cause use a smaller layer height and achieve slightly better looking walls.


## Development

The `development` directory provides a Clojure project to run and improve rasterkasten. Repl driven development is encouraged, as such the development project has a babashka task that starts an nrepl server loading all bricks and products of the repo into a single interactive development project. Just connect your favorite repl enabled editor and start building.


### What the hell is a Repl?

_Repl_ stands for _Read Evaluate Print Loop_. Is is an interactive programming environment. You could pretty much say it is like an ssh session into your running program allowing you to interact with the program, manipulate it live and get instant feedback. The Repl helps you to interactively try out code and quickly build great software.

Like a SSH client you need an IDE or editor (or simply a shell) to connect to the running program. There are several options available, I personally prefer using [Sublime Text](https://www.sublimetext.com/) with [Clojure Sublimed](https://github.com/tonsky/Clojure-Sublimed) to connect to the Repl.


## Future Development

I would like to migrate some more specialized boxes I build in the past using an older version of Rasterkasten to the new codebase. These include i.e. boxes for round and triangular sandpaper for sanders.

In addition to the Repl based rendering I'm thinking about adding a cli tool to make it easier for non Clojure developers to use Rasterkasten.

Of cause I would be happy if anyone likes to build their own models and create a PR :)