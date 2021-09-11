(defproject org.datopia/path "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url          "https://github.com/datopia/path"
  :license      {:name "MIT License"
                 :url  "http://opensource.org/licenses/MIT"}
  :scm          {:name "git"
                 :url  "https://github.com/datopia/path"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :repl-options {:init-ns path.util}
  :profiles
  {:dev {:global-vars  {*warn-on-reflection* true}
         :dependencies [[io.datopia/codox-theme "0.1.0"]]
         :plugins      [[lein-codox "0.10.5"]]
         :codox        {:namespaces [#"^path\."]
                        :metadata   {:doc/format :markdown}
                        :themes     [:default [:datopia
                                               {:datopia/github
                                                "https://github.com/datopia/path"}]]}}})
