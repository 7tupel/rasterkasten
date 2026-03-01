(ns development.tasks
  (:require
   [development.utils.proc-wrapper :refer [wrap-process]]))



;;;;

(defn nrepl
  [_]
  ;; run the Clojure nRepl server
  (wrap-process
    "clj -M:nREPL -m nrepl.cmdline --middleware \"[clj-commons.pretty.nrepl/wrap-pretty]\""
    {}))

(defn development
  [_]
  (nrepl nil))