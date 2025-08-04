(defproject file-manager "0.0.1"
  :description "A file manager written in Clojure that aims to be portable yet powerful, and efficient."
  :url "https://github.com/theyee0/file-manager"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 [com.drewnoakes/metadata-extractor "2.19.0"]
                 [org.jxmapviewer/jxmapviewer2 "2.8"]
                 [com.formdev/flatlaf "3.6"]]
  :java-source-paths ["java"]
  :aot [file-manager.filetree-model]
  :main ^:skip-aot file-manager.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
