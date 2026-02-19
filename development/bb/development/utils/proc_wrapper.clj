(ns development.utils.proc-wrapper
  "Taken from: https://github.com/babashka/nbb/blob/proc-wrapper/script/proc_wrapper.clj
  as a result of this discussion https://github.com/babashka/process/discussions/102."
  (:require
    [babashka.process :as process :refer [process]]
    [clojure.string :as str]))


(def output-lock (Object.))


(defn- output-wrapper
  [stream writer]
  (let [buf (byte-array 1024)
        buffer (atom nil)]
    (loop []
      (let [read (.read stream buf)]
        (when-not (= -1 read)
          (let [s (String. buf 0 read)
                last-newline (str/last-index-of s \newline)
                before-last-newline (when last-newline (subs s 0 last-newline))
                after-last-newline (if last-newline
                                     (subs s (inc last-newline))
                                     s)]
            (when before-last-newline
              (let [buffered @buffer
                    _ (reset! buffer nil)
                    lines (str/split-lines (str buffered before-last-newline))]
                (doseq [l lines]
                  (locking output-lock
                    (binding [*out* writer]
                      (println l))))))
            ;; (Thread/sleep (rand-int 100))
            (swap! buffer (fn [buffer]
                            (if after-last-newline
                              (str buffer after-last-newline)
                              buffer)))
            (recur)))))))


#_(defn output-wrapper [stream prefix writer]
    (let [rdr (io/reader stream)
          lines (line-seq rdr)]
      (run! #(binding [*out* writer]
               (println prefix %)) lines)))


(defn wrap-process
  "Wrap a Process output and forward it to the current stdout and stderr."
  ([cmd] (wrap-process cmd {}))
  ([cmd opts] 
     (let [proc (process opts cmd)
           output-out (future (output-wrapper (:out proc)  *out*))
           output-err (future (output-wrapper (:err proc)  *err*))
           checked (process/check proc)]
       @output-out
       @output-err
       checked)))
