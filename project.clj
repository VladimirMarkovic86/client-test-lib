(defproject org.clojars.vladimirmarkovic86/client-test-lib "0.2.14"
  :description "Simple client test library"
  :url "https://github.com/VladimirMarkovic86/client-test-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojurescript "1.10.339"]
                 [org.clojars.vladimirmarkovic86/utils-lib "0.4.10"]
                 [org.clojars.vladimirmarkovic86/js-lib "0.1.16"]
                 [org.clojars.vladimirmarkovic86/htmlcss-lib "0.1.6"]
                 ]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/cljs"]

  :plugins [[lein-cljsbuild  "1.1.7"]
            [lein-doo "0.1.11"]
            ]

  :cljsbuild
    {:builds
      {:test
        {:source-paths ["src/cljs" "test/cljs"]
         :compiler     {:main client-test-lib.test-runner
                        :optimizations :whitespace
                        :output-dir "resources/public/assets/js/out/test"
                        :output-to "resources/public/assets/js/test.js"}}
       }}
 )

