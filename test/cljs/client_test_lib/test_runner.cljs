(ns client-test-lib.test-runner
  (:require [client-test-lib.core-test-cljs]
            [doo.runner :refer-macros [doo-tests doo-all-tests]]))

(enable-console-print!)

(doo-tests
  'client-test-lib.core-test-cljs)

