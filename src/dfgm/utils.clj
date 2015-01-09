(ns dfgm.utils
  (:import [java.io FileNotFoundException]))

(defn deep-merge
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn parse-ns-var! [s-ns-var]
  (if-let [[_ s-ns s-var] (re-matches
                           #"^([a-zA-Z0-9\.\-]+)/([a-zA-Z0-9\.\-]+)$"
                           s-ns-var)]
    [s-ns s-var]
    (throw (IllegalArgumentException.
            (str "Invalid format:\n\n\t"
                 s-ns-var
                 "\n\ns-ns-var must be of the form: '<namespace>/<var-name>'.")))))

(defn resolve-ns-var! [s-ns s-var]
  (let [sym-ns  (symbol s-ns)
        sym-var (symbol s-var)]
    (try (require sym-ns)
         (catch FileNotFoundException e
           (throw (IllegalArgumentException.
                   (format "Unable to load var: %s/%s" s-ns s-var)))))
    (or (ns-resolve sym-ns sym-var)
        (throw (IllegalArgumentException.
                (format "Unable to load var: %s/%s" s-ns s-var))))))

(defmacro conj-> [expr & clauses]
  (let [pstep (fn [[test step]] [test `(conj ~step)])]
    `(cond-> ~expr
             ~@(apply concat (map pstep (partition 2 clauses))))))
