(ns path.path-test
  (:require [clojure.test
             :refer [deftest is use-fixtures]]
            [path.path :as p])
  (:import [java.nio.file Files Path Paths]
           [java.nio.file.attribute FileAttribute])
  (:refer-clojure :exclude [resolve get spit slurp]))

(def ^:dynamic *root*        nil)
(def ^:dynamic *typed-roots* nil)

(defn- -create-tmp-tree [parent m]
  (doseq [[p v] m]
    (let [p       (Paths/get p (into-array String []))
          parent' (if (string? v)
                    parent
                    (.resolve ^Path parent ^Path p))]
      (Files/createDirectories ^Path parent' (into-array FileAttribute []))
      (cond
        (string? v) (clojure.core/spit (.toFile (.resolve ^Path parent' p)) v)
        (map?    v) (-create-tmp-tree parent' v)
        (nil?    v) nil))))

(defn- create-tmp-tree [m]
  (let [p (Files/createTempDirectory "path-test" (into-array FileAttribute []))]
    (-create-tmp-tree p m)
    p))

(def tmp-tree {"a" {"b.txt" "b.txt"}
               "b" {"c.txt" "c.txt"}
               "c" {"d" {"e" {"f.txt" "f.txt"}}}})

(use-fixtures :each
  (fn [f]
    (binding [*root* (create-tmp-tree tmp-tree)]
      (binding [*typed-roots* [*root* (str *root*) (.toFile ^Path *root*)]]
        (f)))))

(deftest get
  (doseq [p *typed-roots*]
    (is (instance? Path (p/get p)))))

(deftest get-equivalence
  (is (apply = (map p/get *typed-roots*))))

(deftest resolve
  (let [resolved (map #(p/resolve % "a") *typed-roots*)]
    (is (apply = resolved))))

(def dirs              ["a" "b" "c" "c/d" "c/d/e"])
(def files             ["a/b.txt" "b/c.txt" "c/d/e/f.txt"])
(def dirs+files        (concat dirs files))
(def non-existent      ["Z" "Z/a/" "b/c/d.e"])

(deftest exists?
  (doseq [c dirs+files]
    (is (p/exists? (p/resolve *root* c))))
  (doseq [c non-existent]
    (is (not (p/exists? (p/resolve *root* c))))))

(deftest dir?
  (doseq [d dirs]
    (is (p/dir? (p/resolve *root* d))))
  (doseq [f files]
    (is (not (p/dir? (p/resolve *root* f)))))
  (doseq [f non-existent]
    (is (not (p/dir? (p/resolve *root* f))))))

(defn ->components [^Path p]
  (loop [i   0
         acc []]
    (if (= i (.getNameCount p))
      acc
      (recur (inc i) (conj acc (str (.getName p i)))))))

(deftest components
  (is (= (p/components "/a/b/c") ["a" "b" "c"]))
  (doseq [p *typed-roots*]
    (is (= (p/components p) (->components (p/get p))))))

(deftest file?
  (doseq [d dirs]
    (is (not (p/file? (p/resolve *root* d)))))
  (doseq [x non-existent]
    (is (not (p/file? (p/resolve *root* x)))))
  (doseq [f files]
    (is (p/file? (p/resolve *root* f)))))

(deftest path-seq
  (let [[f-root] (filter #(instance? java.io.File %) *typed-roots*)
        f-seq    (file-seq f-root)]
    (doseq [root *typed-roots*]
      (is (= (map p/->file (p/path-seq root)) f-seq)))))

(deftest delete
  (let [p (->> files first (p/resolve *root*))]
    (is (p/exists? p))
    (p/delete p)
    (is (not (p/exists? p)))))

(deftest slurp
  (let [[f] files
        p   (p/resolve *root* f)]
    (is (= (p/slurp p) (p/name f)))))

(deftest spit
  (let [[f] files
        p   (p/resolve *root* f)
        s   (apply str (reverse (p/slurp p)))]
    (p/spit p s)
    (is (= (p/slurp p) s))))
