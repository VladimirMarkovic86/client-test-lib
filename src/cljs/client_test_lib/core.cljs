(ns client-test-lib.core
  (:require [js-lib.core :as md]
            [utils-lib.core :as utils]
            [htmlcss-lib.core :refer [gen textarea]]))

(def done
     (atom 0))

(def windows-atom
     (atom []))

(def number-of-bots
     (atom 1))

(def main-test-fn-a
     (atom nil))

(def wait-for-element-a-fn
     (atom nil))

(defn execute-if-element-loaded
  "Executes function if element is loaded"
  [wait-for-selector
   execute-fn
   fn-params]
  (when (and (string?
               wait-for-selector)
             (not
               (empty?
                 wait-for-selector))
             (fn?
               execute-fn))
    (if-let [elem (md/query-selector
                    wait-for-selector)]
      (execute-fn
        fn-params)
      (when (fn?
              @wait-for-element-a-fn)
        (@wait-for-element-a-fn
          wait-for-selector
          execute-fn
          fn-params))
     ))
 )

(defn wait-for-element
  "Wait for selected element to load"
  [wait-for-selector
   execute-fn
   fn-params]
  (when (and (string?
               wait-for-selector)
             (not
               (empty?
                 wait-for-selector))
             (fn?
               execute-fn))
    (md/timeout
      #(execute-if-element-loaded
         wait-for-selector
         execute-fn
         fn-params)
      100))
 )

(reset!
  wait-for-element-a-fn
  wait-for-element)

(defn log-action
  "Log performed action in textarea logger"
  [{log-obj :log-obj}]
  (when-let [test-monitor (md/query-selector
                            "#test-monitor-id")]
    (let [monitor-inner-html (md/get-inner-html
                               test-monitor)
          new-monitor-inner-html (str
                                   (js/Date.)
                                   ": "
                                   log-obj
                                   "\n"
                                   monitor-inner-html)]
      (md/set-inner-html
        test-monitor
        new-monitor-inner-html))
   ))

(defn opener-console
  "Log testing progress in textarea of opener window"
  [log-obj]
  (wait-for-element
    "#test-monitor-id"
    log-action
    {:log-obj log-obj}))

(defn execute-vector-when-loaded
  "Execute vector of selectors and functions
   
   window-obj - js/window object of where vector is going to be executed
   test-case-vector - vector of sub-vectors that contain
                        selector which is waited for
                          for function to be executed as first element of sub-vector
                        function to be executed as second element of sub-vector
                          second parameter of executing function
                          is window-obj cause it needs reference to
                          window where testing is going to take place
                        first parameter of that function as third parameter of sub-vector
   
   Example of vector: [[\".content\" executing-fn fn-params]
                       [\".body\" executing-fn-two fn-params-two]
                       ...]
   "
  [window-obj
   test-case-vector]
  (let [[wait-for-selector
         execute-fn
         fn-params] (first
                      test-case-vector)]
    (md/timeout
      #(if-let [elem (md/query-selector-on-element
                       (.-document
                         window-obj)
                       wait-for-selector)]
         (let [test-case-vector (utils/remove-index-from-vector
                                  test-case-vector
                                  0)]
           #_(opener-console
             window-obj
             (str
               "loaded "
               wait-for-selector))
           (execute-fn
             fn-params
             window-obj)
           (when-not (empty? test-case-vector)
             (execute-vector-when-loaded
               window-obj
               test-case-vector))
           )
         (do #_(opener-console
               window-obj
               (str
                 "not loaded "
                 wait-for-selector))
             (execute-vector-when-loaded
               window-obj
               test-case-vector))
        )
      100))
 )

(defn open-new-window
  "Opens new window with URL same as of it's opener's"
  [window-name]
  (.open
    js/window
    "/"
    window-name))

(defn close-window
  "Closes window of particular window-obj
  
   fn-params are inherited from execute-vector-when-loaded function call
   of execute-fn and it's not used
   
   done atom counts windows that are closed,
   if that number counts up to number of bots it closes the focused window too."
  [fn-params
   window-obj]
  (.close
    window-obj)
  (swap!
    done
    inc)
  (opener-console
    (str
      fn-params
      " done"))
 )

(defn click-elem
  "Helper function for clicking on an element selected by fn-selector in window-obj"
  [fn-selector
   & [window-obj]]
  (when (or (and (string?
                   fn-selector)
                 (not
                   (empty?
                     fn-selector))
             )
            (md/html?
              fn-selector))
    (let [window-obj (or window-obj
                         js/window)
          fn-elem (md/query-selector-on-element
                    (.-document
                      window-obj)
                    fn-selector)]
      (md/dispatch-event
        "click"
        fn-elem
        window-obj))
   ))

(defn append-element-fn
  "Helper function for appending textarea in opener and focused window"
  [{element-selector :element-selector
    append-element :append-element}
   & [window-obj]]
  (let [window-obj (or window-obj
                       js/window)
        document (.-document
                   window-obj)]
    (md/remove-element-content
      element-selector)
    (md/append-element
      element-selector
      append-element))
 )

(defn open-windows
  "Opens as much windows as bots-number fn parameter says so and one more as focus window
   focus window and opener window serve as monitor windows
   
   Tracks every opened window reference in global variable windows-atom
   and passes that variable to focused widnow
   
   Also main-test-fn parameter is passed to focused window and bots-number"
  [main-test-fn]
  (doseq [window-name (range
                        @number-of-bots
                        0
                        -1)]
    (swap!
      windows-atom
      conj
      (open-new-window
        window-name))
   )
  (let [textarea-field (gen
                         (textarea
                           ""
                           {:id "test-monitor-id"
                            :style {:height "100%"
                                    :width "100%"
                                    :resize "none"}
                            :readonly true}))]
    (execute-vector-when-loaded
      js/window
      [[".content"
        append-element-fn
        {:element-selector ".content"
         :append-element textarea-field}]])
    (reset!
      main-test-fn-a
      main-test-fn))
 )

(defn run-tests
  "Function that should be called from code that is being tested"
  [main-test-fn
   & [bots-number]]
  (reset!
    number-of-bots
    (or bots-number
        1))
  (reset!
    windows-atom
    [])
  (reset!
    done
    0)
  (open-windows
    main-test-fn)
  (doseq [window-obj @windows-atom]
    (when (fn?
            @main-test-fn-a)
      (@main-test-fn-a
        window-obj))
   ))

; mozilla -> about:config
; browser.tabs.loadDivertedInBackground=true

