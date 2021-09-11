(ns path.util
  (:import [java.nio.file
            Files]
           [java.nio.file.attribute
            FileAttribute]))

(defn create-temp-dir [prefix & file-attrs]
  (Files/createTempDirectory prefix (into-array FileAttribute file-attrs)))

(defn create-temp-file [prefix suffix & file-attrs]
  (Files/createTempFile prefix suffix (into-array FileAttribute file-attrs)))
