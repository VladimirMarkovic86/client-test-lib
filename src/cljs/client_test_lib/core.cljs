(ns client-test-lib.core
  (:require [js-lib.core :as md]
            [utils-lib.core :as utils]
            [htmlcss-lib.core :refer [gen textarea]]))

(def done (atom 0))

(def number-of-bots (atom 1))

(defn opener-console
  ""
  [window-obj
   log-obj]
  (let [test-monitor (md/query-selector
                       "#testMonitor")
        opener-test-monitor (md/query-selector-on-element
                              (aget
                                (aget
                                  window-obj
                                  "opener")
                                "document")
                              "#testMonitor")
        monitor-inner-html (md/get-inner-html
                             opener-test-monitor)
        new-monitor-inner-html (str
                                 (js/Date.)
                                 ": "
                                 log-obj
                                 "\n"
                                 monitor-inner-html)]
    (md/set-inner-html
      test-monitor
      new-monitor-inner-html)
    (md/set-inner-html
      opener-test-monitor
      new-monitor-inner-html))
  )

(defn execute-vector-when-loaded
  ""
  [window-obj
   test-case-vector]
  (let [[wait-for-selector
         execute-fn
         fn-params] (first test-case-vector)]
    (md/timeout
      #(if-let [elem (md/query-selector-on-element
                       (aget window-obj "document")
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
  ""
  [window-name]
  (.open
    js/window
    "/"
    window-name))

(defn close-window
  ""
  [fn-params
   window-obj]
  (.close
    window-obj)
  (swap!
    done
    inc)
  (opener-console
    js/window
    "Done")
  (when (= @number-of-bots
           @done)
    (.close
      js/window))
  )

(defn click-elem
  ""
  [fn-selector
   window-obj]
  (let [fn-elem (md/query-selector-on-element
                  (aget window-obj "document")
                  fn-selector)]
    (md/click
      fn-elem
      window-obj))
  )

(defn append-element-fn
  ""
  [{element-selector :element-selector
    append-element :append-element}
   window-obj]
  (let [document (aget
                   window-obj
                   "document")
        content (md/query-selector-on-element
                  document
                  element-selector)]
    (md/append-element
      content
      append-element))
  )

(def windows-atom (atom []))

(defn open-windows
  ""
  [bots-number
   main-test-fn]
  (doseq [window-name (range
                        (inc
                          bots-number)
                        0
                        -1)]
    (swap!
      windows-atom
      conj
      (open-new-window
        window-name))
    )
  (let [last-window (last
                      @windows-atom)
        all-windows (utils/remove-index-from-vector
                      @windows-atom
                      (dec
                        (count
                          @windows-atom))
                     )
        textarea-field (gen
                         (textarea
                           ""
                           {:id "testMonitor"
                            :style {:height "calc(100% - 50px)"
                                    :width "100%"
                                    :resize "none"}
                            :readonly true}))
        last-textarea-field (gen
                              (textarea
                                ""
                                {:id "testMonitor"
                                 :style {:height "100%"
                                         :width "100%"
                                         :resize "none"}
                                 :readonly true}))
        indexes-array (into
                        []
                        (.split
                          (aget
                            main-test-fn
                            "name")
                          "$"))]
    (execute-vector-when-loaded
      last-window
      [[".content"
        append-element-fn
        {:element-selector ".content"
         :append-element last-textarea-field}]])
    (execute-vector-when-loaded
      js/window
      [[".content"
        append-element-fn
        {:element-selector ".content"
         :append-element textarea-field}]])
    (aset
      last-window
      "allWindows"
      all-windows)
    (aset
      last-window
      "mainTestFn"
      indexes-array)
    (aset
      last-window
      "botsNumber"
      bots-number))
 )

(defn run-tests
  ""
  [main-test-fn
   & [bots-number]]
  (open-windows
    (or bots-number
        @number-of-bots)
    main-test-fn))

(defn run-finally
  ""
  []
  (let [all-windows (aget
                      js/window
                      "allWindows")
        main-test-fn (aget
                       js/window
                       "mainTestFn")
        bots-number (aget
                      js/window
                      "botsNumber")
        find-and-run (fn [main-test-fn]
                       (let [main-test-fn (apply
                                            aget
                                            js/window
                                            main-test-fn)]
                         (doseq [window-obj all-windows]
                           (main-test-fn
                             window-obj))
                        ))]
    (reset!
      number-of-bots
      bots-number)
    (execute-vector-when-loaded
      js/window
      [[".content"
        find-and-run
        main-test-fn]]))
  )

(let [window-name (aget
                    js/window
                    "name")]
  (when (= window-name
           "1")
    (run-finally))
 )

