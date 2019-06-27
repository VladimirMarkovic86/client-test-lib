(ns client-test-lib.core-test-cljs
  (:require [js-lib.core :as md]
            [clojure.test :refer-macros [deftest is testing]]
            [client-test-lib.core :refer [execute-if-element-loaded wait-for-element
                                          log-action opener-console
                                          execute-vector-when-loaded open-new-window
                                          close-window click-elem append-element-fn
                                          open-windows main-test-fn-a run-tests
                                          number-of-bots]]))

(deftest test-execute-if-element-loaded
  (testing "Test execute if element loaded"
    
    (let [wait-for-selector nil
          execute-fn nil
          fn-params nil
          result (execute-if-element-loaded
                   wait-for-selector
                   execute-fn
                   fn-params)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [wait-for-selector "body"
          execute-fn (fn [params]
                       params)
          fn-params "found body"
          result (execute-if-element-loaded
                   wait-for-selector
                   execute-fn
                   fn-params)]
      
      (is
        (= result
           fn-params)
       )
      
     )
    
   ))

(deftest test-wait-for-element
  (testing "Test wait for element"
    
    (let [wait-for-selector nil
          execute-fn nil
          fn-params nil
          result (wait-for-element
                   wait-for-selector
                   execute-fn
                   fn-params)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [wait-for-selector "body"
          result-a (atom nil)
          execute-fn (fn [params]
                       (reset!
                         result-a
                         params))
          fn-params "found body"
          result (wait-for-element
                   wait-for-selector
                   execute-fn
                   fn-params)]
      
      (is
        (= result
           2)
       )
      
     )
    
   ))

(deftest test-log-action
  (testing "Test log action"
    
    (let [log-obj nil
          log-action-param {:log-obj log-obj}
          result (log-action
                   log-action-param)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [test-monitor-element (.createElement
                                 js/document
                                 "textarea")
          void (.setAttribute
                 test-monitor-element
                 "id"
                 "test-monitor-id")
          void (md/append-element
                 "body"
                 test-monitor-element)
          log-obj "Test log"
          log-action-param {:log-obj log-obj}
          result (log-action
                   log-action-param)]
      
      (is
        (< -1
           (.indexOf
             result
             log-obj))
       )
      
      (md/remove-element
        "#test-monitor-id")
      
     )
    
   ))

(deftest test-opener-console
  (testing "Test opener console"
    
    (let [log-obj nil
          result (opener-console
                   log-obj)]
      
      (is
        (= result
           3)
       )
      
     )
    
   ))

(deftest test-execute-vector-when-loaded
  (testing "Test execute vector when loaded"
    
    (let [window-obj nil
          test-case-vector nil
          result (execute-vector-when-loaded
                   window-obj
                   test-case-vector)]
      
      (is
        (= result
           4)
       )
      
     )
    
   ))

(deftest test-open-new-window-and-close-window
  (testing "Test open new window and close window"
    
    (let [window-name "new-window"
          result-i (open-new-window
                     window-name)
          fn-params "fn params"
          result-ii (close-window
                      fn-params
                      result-i)]
      
      (is
        (instance?
          js/Window
          result-i)
       )
      
      (is
        (= result-ii
           5)
       )
      
     )
    
   ))

(deftest test-click-elem
  (testing "Test click elem"
    
    (let [fn-selector nil
          window-obj nil
          result (click-elem
                   fn-selector
                   window-obj)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [checkbox-element (.createElement
                             js/document
                             "input")
          void (.setAttribute
                 checkbox-element
                 "id"
                 "test-checkbox")
          void (.setAttribute
                 checkbox-element
                 "type"
                 "checkbox")
          void (md/append-element
                 "body"
                 checkbox-element)
          fn-selector "#test-checkbox"
          window-obj nil]
      
      (is
        (false?
          (aget
            checkbox-element
            "checked"))
       )
      
      (click-elem
        fn-selector
        window-obj)
      
      (is
        (true?
          (aget
            checkbox-element
            "checked"))
       )
      
      (md/remove-element
        fn-selector)
      
     )
    
   ))

(deftest test-append-element-fn
  (testing "Test append element fn"
    
    (let [element-selector nil
          append-element nil
          parameters-map {:element-selector element-selector
                          :append-element append-element}
          window-obj nil
          result (append-element-fn
                   parameters-map
                   window-obj)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [container-element (.createElement
                              js/document
                              "div")
          void (.setAttribute
                 container-element
                 "id"
                 "container")
          content-element (.createElement
                            js/document
                            "div")
          void (.setAttribute
                 content-element
                 "id"
                 "content")
          void (md/append-element
                 "body"
                 container-element)
          element-selector "#container"
          append-element content-element
          parameters-map {:element-selector element-selector
                          :append-element append-element}
          window-obj nil
          body-container-element (md/query-selector
                                   element-selector)]
      
      (is
        (= (md/get-outer-html
             body-container-element)
           "<div id=\"container\"></div>")
       )
      
      (append-element-fn
        parameters-map
        window-obj)
      
      (is
        (= (md/get-outer-html
             body-container-element)
           "<div id=\"container\"><div id=\"content\"></div></div>")
       )
      
      (md/remove-element
        element-selector)
      
     )
    
   ))

(deftest test-open-windows
  (testing "Test open windows"
    
    (let [main-test-fn nil]
      
      (open-windows
        main-test-fn)
      
      (is
        (nil?
          @main-test-fn-a)
       )
      
     )
    
    (let [main-test-fn (fn []
                         [1 2 3])]
      
      (is
        (nil?
          @main-test-fn-a)
       )
      
      (open-windows
        main-test-fn)
      
      (is
        (= @main-test-fn-a
           main-test-fn)
       )
      
      (reset!
        main-test-fn-a
        nil)
      
     )
    
   ))

(deftest test-run-tests
  (testing "Test run tests"
    
    (let [main-test-fn nil
          bots-number nil]
      
      (is
        (nil?
          @main-test-fn-a)
       )
      
      (is
        (= @number-of-bots
           1)
       )
      
      (run-tests
        main-test-fn
        bots-number)
      
      (is
        (= @number-of-bots
           1)
       )
      
      (is
        (= @main-test-fn-a
           main-test-fn)
       )
      
      (reset!
        main-test-fn-a
        nil)
      
      (reset!
        number-of-bots
        1)
      
     )
    
    (let [main-test-fn (fn []
                         [1 2 3])
          bots-number 3]
      
      (is
        (nil?
          @main-test-fn-a)
       )
      
      (is
        (= @number-of-bots
           1)
       )
      
      (run-tests
        main-test-fn
        bots-number)
      
      (is
        (= @main-test-fn-a
           main-test-fn)
       )
      
      (is
        (= @number-of-bots
           3)
       )
      
      (reset!
        main-test-fn-a
        nil)
      
      (reset!
        number-of-bots
        1)
      
     )
    
   ))

