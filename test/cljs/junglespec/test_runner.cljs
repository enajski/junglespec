(ns junglespec.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [junglespec.core-test]
   [junglespec.common-test]))

(enable-console-print!)

(doo-tests 'junglespec.core-test
           'junglespec.common-test)
