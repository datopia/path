(ns path.path
  "All path arguments may be of type `String`, `File`, `URI` or `Path`, they'll be
  coerced as needed.

  Functions accepting a final optional argument sequence
  corresponding to a enum vararg on the underlying method, e.g.
  `LinkOption/NOFOLLOW_LINKS` are passed as keywords, e.g.
  `:LinkOption/NOFOLLOW_LINKS`. If the vararg is not an enum, it will be passed
  through to the underlying method."
  (:import [java.net URI]
           [java.nio.file
            Files Path Paths LinkOption OpenOption]
           [java.nio.file.attribute
            FileAttribute]
           [java.io File])
  (:refer-clojure :exclude [get name resolve list slurp spit]))

(defn- -enum-vals->kw-map [cls-name vals]
  (into {}
    (map
     (fn [e]
       [(keyword cls-name (.name ^Enum e)) e]))
    vals))

(defmacro ^:private enum-vals->kw-map [cls]
  (let [cls (eval cls)]
    `(-enum-vals->kw-map
      (.getSimpleName ^Class ~cls) (. ~cls ^Enum values))))

(def ^:private k->LinkOption (enum-vals->kw-map LinkOption))

(defprotocol ^:private Get
  (-get [s]))

(extend-protocol Get
  String
  (-get [s]
    (Paths/get s (into-array String [])))
  Path
  (-get [s]
    s)
  File
  (-get [s]
    (.toPath s))
  URI
  (-get [s]
    (Paths/get URI)))

(defn ^Path get [s]
  "Convert a `String` or `File` into a `Path`. Will return the input if passed a
  `Path`."
  (-get s))

(defn absolute? [p]
  (.isAbsolute (get p)))

(defn starts-with? "Does `p` start with path `x`?"
  [p x]
  (.startsWith (get p) (get x)))

(defn ends-with? "Does `p` end with path `x`?"
  [p x]
  (.endsWith (get p) (get x)))

(defn parent [p]
  (.getParent (get p)))

(defn resolve [p x]
  (.resolve (get p) (get x)))

(defn resolve-sibling [p x]
  (.resolveSibling (get p) (get x)))

(defn normalize [p]
  (.normalize (get p)))

(defn relativize [p x]
  (.relativize  (get p) (get x)))

(defn components "Return a seq of string path components"
  [p]
  (map str (iterator-seq (.iterator (get p)))))

(defprotocol ^:private CoerceOutput
  (-coerce [i o]))

(extend-protocol CoerceOutput
  String
  (-coerce [i o]
    (str o))
  File
  (-coerce [i o]
    (.toPath ^File o))
  Path
  (-coerce [i o]
    o))

(defn ->absolute "Output type will match the input type"
  [p]
  (-coerce p (.toAbsolutePath (get p))))

(defn ->uri [p]
  (.toUri (get p)))

(defn ->file [p]
  (.toFile (get p)))

(defn dir? "Is `p` an extant directory?"
  [p & link-opts]
  (Files/isDirectory (get p) (into-array LinkOption (map k->LinkOption link-opts))))

(defn file? "Is `p` a regular file?"
  [p & link-opts]
  (Files/isRegularFile (get p) (into-array LinkOption (map k->LinkOption link-opts))))

(defn exists? `"Does `p` exist?"
  [p & link-opts]
  (Files/exists (get p) (into-array LinkOption (map k->LinkOption link-opts))))

(defn not-exists "Similar to `not-empty` - return `nil` when path doesn't exist, otherwise
  return path. Return type will matches the input type."
  [p & link-opts]
  (when (apply exists? p link-opts)
    p))

(defn delete "Delete `p` if it exists."
  [p]
  (Files/deleteIfExists (get p)))

(defn list "Return a seq of `Path` instances representing the contents of `d`."
  [d]
  (iterator-seq (.iterator (Files/list (get d)))))

(defn ->output-stream [p & open-options]
  (Files/newOutputStream (get p) (into-array OpenOption open-options)))

(defn ->input-stream [p & open-options]
  (Files/newInputStream (get p) (into-array OpenOption open-options)))

(defn path-seq
  "A tree seq of `Path` objects."
  [dir]
  (tree-seq dir? list (get dir)))

(defn slurp [p]
  (clojure.core/slurp (->input-stream (get p))))

(defn spit [p contents]
  (clojure.core/spit (->output-stream (get p)) contents))

(defn name "Return last path segment as a string."
  [p]
  (str (.getFileName ^Path (get p))))

(defn create-dirs [p & file-attrs]
  (Files/createDirectories (get p) (into-array FileAttribute file-attrs)))
