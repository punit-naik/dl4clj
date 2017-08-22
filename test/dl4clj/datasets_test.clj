(ns dl4clj.datasets-test
  (:refer-clojure :exclude [reset! rand])
  (:require [clojure.test :refer :all]
            [dl4clj.utils :refer [array-of get-labels]]
            [nd4clj.linalg.factory.nd4j :refer [indarray-of-rand]]
            [dl4clj.datasets.fetchers.default-dataset-fetchers :refer :all]
            [dl4clj.datasets.default-datasets :refer :all]
            [dl4clj.datasets.api.fetchers :refer :all]
            [dl4clj.datasets.iterators :refer :all]
            [dl4clj.datasets.api.iterators :refer :all]
            [dl4clj.datasets.input-splits :refer :all]
            [dl4clj.datasets.api.input-splits :refer :all]
            [dl4clj.datasets.new-datasets :refer :all]
            [dl4clj.datasets.api.datasets :refer :all]
            [dl4clj.datasets.pre-processors :refer :all]
            [dl4clj.datasets.api.pre-processors :refer :all]
            [dl4clj.datasets.record-readers :refer :all]
            [dl4clj.datasets.api.record-readers :refer :all])
  ;; image transforms have not been implemented so importing this default one for testing
  ;; https://deeplearning4j.org/datavecdoc/org/datavec/image/transform/package-summary.html
  (:import [org.datavec.image.transform ColorConversionTransform]))

;; good
(deftest dataset-fetchers-test
  (testing "dataset fetchers"
    ;; dl4clj.datasets.fetchers.default-dataset-fetchers
    (is (= org.deeplearning4j.datasets.fetchers.IrisDataFetcher (type (iris-fetcher))))
    (is (= '(org.deeplearning4j.datasets.fetchers.IrisDataFetcher.)
           (iris-fetcher :as-code? true)))

    (is (= org.deeplearning4j.datasets.fetchers.MnistDataFetcher (type (mnist-fetcher))))
    (is (= '(org.deeplearning4j.datasets.fetchers.MnistDataFetcher.)
           (mnist-fetcher :as-code? true)))
    (is (= org.deeplearning4j.datasets.fetchers.MnistDataFetcher
           (type (mnist-fetcher :binarize? true))))
    (is (= '(org.deeplearning4j.datasets.fetchers.MnistDataFetcher. true)
           (mnist-fetcher :binarize? true :as-code? true)))
    (is (= org.deeplearning4j.datasets.fetchers.MnistDataFetcher
           (type
            (mnist-fetcher :binarize? true :train? true :shuffle? true :seed 123))))
    (is (= '(org.deeplearning4j.datasets.fetchers.MnistDataFetcher. true true true 123)
           (mnist-fetcher :binarize? true :train? true :shuffle? true :seed 123 :as-code? true)))
    ;; dl4clj.datasets.fetchers.base-data-fetcher
    (is (= java.lang.Integer (type (fetcher-cursor (iris-fetcher)))))
    (is (= java.lang.Boolean (type (has-more? (iris-fetcher)))))
    (is (= java.lang.Integer (type (input-column-length (iris-fetcher)))))
    (is (= org.deeplearning4j.datasets.fetchers.IrisDataFetcher
           (type (reset-fetcher! (iris-fetcher)))))
    (is (= java.lang.Integer (type (n-examples-in-ds (iris-fetcher)))))
    (is (= java.lang.Integer (type (n-outcomes-in-ds (iris-fetcher)))))))

;; will be updated
(deftest ds-iteration-creation-test
  (testing "the creation of dataset iterators"
    ;; dl4clj.datasets.iterator.impl.default-datasets
    ;; come back and add tests for the code these produce
    ;; cifar dataset
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 2 :n-examples 100))))
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 2 :img-dims [1 1 1]))))
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 2 :n-examples 100 :train? true))))
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 2 :n-examples 100
                                              :train? true :img-dims [1 1 1]))))
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 3 :n-examples 100
                                              :train? true :img-dims [3 3 3]
                                              :use-special-pre-process-cifar? true))))
    (is (= org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
           (type (new-cifar-data-set-iterator :batch-size 3 :n-examples 100
                                              :train? true :img-dims [3 3 3]
                                              :use-special-pre-process-cifar? true
                                              :n-possible-labels 5
                                              :img-transform (ColorConversionTransform.)))))

    ;; iris dataset
    (is (= org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator
           (type (new-iris-data-set-iterator :batch-size 2 :n-examples 100))))

    ;; lfwd
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1]))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :batch-size 2 :n-examples 100))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :use-subset? true))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :n-labels 5 :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50 :n-labels 5
                                            :use-subset? true :seed 123))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50 :n-labels 5
                                            :use-subset? true :seed 123))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50 :n-labels 5
                                            :use-subset? true :seed 123
                                            :label-generator (new-parent-path-label-generator)))))
    (is (= org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator
           (type (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                            :n-examples 100 :train? true
                                            :split-train-test 0.50 :n-labels 5
                                            :use-subset? true :seed 123
                                            :label-generator (new-parent-path-label-generator)
                                            :img-transform (ColorConversionTransform.)))))

    ;;mnist
    (is (= org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
           (type (new-mnist-data-set-iterator :batch-size 5 :train? true
                                              :seed 123))))
    (is (= org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
           (type (new-mnist-data-set-iterator :batch 5 :n-examples 100))))
    (is (= org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
           (type (new-mnist-data-set-iterator :batch 5 :n-examples 100 :binarize? true))))
    (is (= org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
           (type (new-mnist-data-set-iterator :batch 5 :n-examples 100 :binarize? true
                                               :train? true :shuffle? true :seed 123))))

    ;; raw mnist
    (is (= org.deeplearning4j.datasets.iterator.impl.RawMnistDataSetIterator
           (type (new-raw-mnist-data-set-iterator :batch 5 :n-examples 100))))

    ;; dl4clj.datasets.iterator.impl.list-data-set-iterator
    (is (= org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
           (type (new-list-dataset-iterator
                  :dataset [(new-raw-mnist-data-set-iterator :batch 5 :n-examples 100)]))))
    (is (= org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
           (type (new-list-dataset-iterator
                  :dataset [(new-raw-mnist-data-set-iterator :batch 5 :n-examples 100)]
                  :batch 6))))

    ;; dl4clj.datasets.iterator.impl.multi-data-set-iterator-adapter
    (is (= org.deeplearning4j.datasets.iterator.impl.MultiDataSetIteratorAdapter
           (type (new-multi-data-set-iterator-adapter
                  :iter
                  (new-mnist-data-set-iterator :batch 5 :n-examples 100)))))

    ;; dl4clj.datasets.iterator.impl.singleton-multi-data-set-iterator
    (is (= org.deeplearning4j.datasets.iterator.impl.SingletonMultiDataSetIterator
           (type (new-singleton-multi-dataset-iterator
                  :multi-dataset
                  (next-example!
                   (reset-fetcher!
                    (new-multi-data-set-iterator-adapter
                     :iter
                     (new-mnist-data-set-iterator :batch 5 :n-examples 100))))))))))
;; will be updated
(deftest ds-iteration-interaction-fns-test
  (testing "the api fns for ds iterators"
    (let [iter (new-mnist-data-set-iterator :batch 5 :n-examples 100)
          iter-w-labels (new-lfw-data-set-iterator :img-dims [1 1 1] :batch-size 2
                                                   :n-examples 100 :train? true
                                                   :split-train-test 0.50 :n-labels 5
                                                   :use-subset? true :rng 123
                                                   :label-generator (new-parent-path-label-generator :as-code? false)
                                                   :img-transform (ColorConversionTransform.))
          cifar-iter (new-cifar-data-set-iterator :batch-size 2 :n-examples 100)]
      ;; nd4clj.linalg.api.ds-iter
      (is (= java.lang.Boolean (type (async-supported? iter))))
      (is (= java.lang.Integer (type (get-batch-size iter))))
      (is (= java.lang.Integer (type (get-current-cursor iter))))
      (is (= java.util.ArrayList (type (get-labels iter-w-labels))))
      (is (= org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
             (type (set-pre-processor!
                    :iter iter
                    :pre-processor (new-min-max-normalization-ds-preprocessor :min-val 0 :max-val 1)))))
      (is (= org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
             (type (new-min-max-normalization-ds-preprocessor :min-val 0 :max-val 1))
             (type (get-pre-processor iter))))
      (is (= java.lang.Integer (type (get-input-columns iter))))
      (is (= org.nd4j.linalg.dataset.DataSet
             (type (next-n-examples! :iter iter-w-labels :n 2))))
      (is (= java.lang.Integer (type (get-num-examples iter))))
      (is (= (type iter-w-labels) (type (reset-iter! iter-w-labels))))
      (is (= java.lang.Boolean (type (reset-supported? iter-w-labels))))
      (is (= java.lang.Integer (type (get-total-examples iter))))
      (is (= java.lang.Integer (type (get-total-outcomes iter))))

      ;; dl4clj.datasets.iterator.impl.default-datasets
      (is (= java.lang.Boolean (type (has-next? iter))))
      (is (= org.nd4j.linalg.dataset.DataSet (type (next-example! iter-w-labels))))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;; this is going to fail when this is running in an enviro with gpus or spark I think
      ;; need to implement other forms of computation to verify this
      (is (= org.nd4j.linalg.cpu.nativecpu.NDArray
             (type (get-features (next-example! (reset-iter! iter-w-labels)))))))))

;; good
(deftest label-generators-test
  (testing "the creation of label generators and their functionality"
    (is (= org.datavec.api.io.labels.ParentPathLabelGenerator
           (type (new-parent-path-label-generator :as-code? false))))
    (is (= '(org.datavec.api.io.labels.ParentPathLabelGenerator.)
           (new-parent-path-label-generator)))
    (is (= org.datavec.api.io.labels.PatternPathLabelGenerator
           (type (new-pattern-path-label-generator :pattern "." :as-code? false))))
    (is (= '(if nil
              (org.datavec.api.io.labels.PatternPathLabelGenerator. "." nil)
              (org.datavec.api.io.labels.PatternPathLabelGenerator. "."))
           (new-pattern-path-label-generator :pattern ".")))
    (is (= org.datavec.api.io.labels.PatternPathLabelGenerator
           (type (new-pattern-path-label-generator :pattern "." :pattern-position 0 :as-code? false))))
    (is (= '(if 0
              (org.datavec.api.io.labels.PatternPathLabelGenerator. "." 0)
              (org.datavec.api.io.labels.PatternPathLabelGenerator. "."))
           (new-pattern-path-label-generator :pattern "." :pattern-position 0)))
    (is (= org.datavec.api.writable.Text
           (type
            (get-label-for-path :label-generator (new-parent-path-label-generator :as-code? false)
                                :path "resources/paravec/labeled/finance"))))))

;; good
(deftest path-filter-tests
  (testing "the creation of path filters and their functionality"
    (is (= org.datavec.api.io.filters.BalancedPathFilter
           (type
            (new-balanced-path-filter :seed 10
                                      :extensions [".txt"]
                                      :label-generator (new-parent-path-label-generator
                                                        :as-code? true)
                                      :max-paths 1
                                      :max-labels 3
                                      :min-paths-per-label 1
                                      :max-paths-per-label 1
                                      :labels []
                                      :as-code? false))))
    (is (= '(org.datavec.api.io.filters.BalancedPathFilter.
             (java.util.Random. 10)
             (dl4clj.utils/array-of :data [".txt"]
                                    :java-type java.lang.String)
             (org.datavec.api.io.labels.ParentPathLabelGenerator.)
             1 3 1 1
             (dl4clj.utils/array-of :data [] :java-type java.lang.String))
           (new-balanced-path-filter :seed 10
                                     :extensions [".txt"]
                                     :label-generator (new-parent-path-label-generator
                                                       :as-code? true)
                                     :max-paths 1
                                     :max-labels 3
                                     :min-paths-per-label 1
                                     :max-paths-per-label 1
                                     :labels [])))

    (is (= org.datavec.api.io.filters.RandomPathFilter
           (type
            (new-random-path-filter :seed 123
                                    :extensions [".txt"]
                                    :max-paths 2
                                    :as-code? false))))
    (is (= '(org.datavec.api.io.filters.RandomPathFilter.
             (java.util.Random. 123)
             (dl4clj.utils/array-of :data [".txt"]
                                    :java-type java.lang.String)
             2)
           (new-random-path-filter :seed 123
                                   :extensions [".txt"]
                                   :max-paths 2)))
    (is (= (type
            (array-of :data [(java.net.URI/create "foo")]
                      :java-type java.net.URI))
           (type
            (filter-paths :path-filter (new-random-path-filter :seed 123
                                                               :extensions [".txt"]
                                                               :max-paths 2
                                                               :as-code? false)
                          :paths ["foo"]))))))

;; good
(deftest file-split-testing
  (testing "base level io stuffs"
    ;; file split
    (is (= org.datavec.api.split.FileSplit
           (type (new-filesplit :path "resources/"
                                :as-code? false))))
    (is (= '(org.datavec.api.split.FileSplit.
             (clojure.java.io/as-file "resources/"))
           (new-filesplit :path "resources/")))
    (is (= org.datavec.api.split.FileSplit
           (type (new-filesplit :path "resources/"
                                :seed 123
                                :as-code? false))))
    (is (= '(org.datavec.api.split.FileSplit.
             (clojure.java.io/as-file "resources/")
             (java.util.Random. 123))
           (new-filesplit :path "resources/"
                          :seed 123)))
    (is (= org.datavec.api.split.FileSplit
           (type (new-filesplit :path "resources/"
                                :allow-format ".csv"
                                :as-code? false))))
    (is (= '(org.datavec.api.split.FileSplit.
             (clojure.java.io/as-file "resources/")
             (dl4clj.utils/array-of :data ".csv"
                                    :java-type java.lang.String))
           (new-filesplit :path "resources/"
                          :allow-format ".csv")))
    (is (= org.datavec.api.split.FileSplit
           (type (new-filesplit :path "resources/"
                                :allow-format ".csv"
                                :recursive? true
                                :as-code? false))))
    (is (= '(org.datavec.api.split.FileSplit.
             (clojure.java.io/as-file "resources/")
             (dl4clj.utils/array-of :data ".csv"
                                    :java-type java.lang.String)
             true)
           (new-filesplit :path "resources/"
                          :allow-format ".csv"
                          :recursive? true)))
    (is (= org.datavec.api.split.FileSplit
           (type (new-filesplit :path "resources/"
                                :allow-format ".csv"
                                :seed 123
                                :as-code? false))))
    (is (= '(org.datavec.api.split.FileSplit.
             (clojure.java.io/as-file "resources/")
             (dl4clj.utils/array-of :data ".csv"
                                    :java-type java.lang.String))
           (new-filesplit :path "resources/"
                          :allow-format ".csv"
                          :seed 123)))
    (is (= java.io.File (type (get-root-dir (new-filesplit :path "resources/" :as-code? false)))))

    ;; collection input split
    (is (= org.datavec.api.split.CollectionInputSplit
           (type (new-collection-input-split :coll ['(new java.net.URI "foo")
                                                    '(new java.net.URI "baz")]
                                             :as-code? false))))
    (is (= '(org.datavec.api.split.CollectionInputSplit. [(new java.net.URI "foo")
                                                          (new java.net.URI "baz")])
           (new-collection-input-split :coll ['(new java.net.URI "foo")
                                              '(new java.net.URI "baz")])))

    ;; input stream input split
    (let [data '(clojure.java.io/input-stream "resources/poker-hand-testing.csv")
          other-data '(clojure.java.io/input-stream "resources/poker-hand-training.csv")]
      (is (= org.datavec.api.split.InputStreamInputSplit
             (type (new-input-stream-input-split
                    :in-stream data
                    :file-path "resources/poker-hand-testing.csv"
                    :as-code? false))))
      (is (= org.datavec.api.split.InputStreamInputSplit
             (type (new-input-stream-input-split
                    :in-stream data
                    :as-code? false))))

      (is (= java.io.BufferedInputStream
             (type (get-is (new-input-stream-input-split
                            :in-stream data
                            :as-code? false)))))
      (= org.datavec.api.split.InputStreamInputSplit
         (type (set-is! :input-stream-input-split (new-input-stream-input-split
                                                   :in-stream data
                                                   :as-code? false)
                        :is (eval other-data)))))

    ;; list string input split
    (is (= org.datavec.api.split.ListStringSplit
           (type (new-list-string-split :data  ["foo" "baz"]
                                        :as-code? false))))
    (is (= '(org.datavec.api.split.ListStringSplit.
             (clojure.core/reverse (clojure.core/into () ["foo" "baz"])))
           (new-list-string-split :data  ["foo" "baz"])))
    (is (= (list "foo" "baz")
           (get-list-string-split-data
            (new-list-string-split :data ["foo" "baz"]
                                   :as-code? false))))

    ;; numbered file input split
    (is (= org.datavec.api.split.NumberedFileInputSplit
           (type
            (new-numbered-file-input-split :base-string "f_%d.txt"
                                           :inclusive-min-idx 0
                                           :inclusive-max-idx 10
                                           :as-code? false))))
    (is (= '(org.datavec.api.split.NumberedFileInputSplit. "f_%d.txt" 0 10)
           (new-numbered-file-input-split :base-string "f_%d.txt"
                                          :inclusive-min-idx 0
                                          :inclusive-max-idx 10)))

    ;; string split
    (is (= org.datavec.api.split.StringSplit
           (type (new-string-split :data "foo baz bar" :as-code? false))))
    (is (= '(org.datavec.api.split.StringSplit. "foo baz bar")
           (new-string-split :data "foo baz bar")))
    (is (= "foo baz bar" (get-list-string-split-data
                          (new-string-split :data "foo baz bar" :as-code? false))))

    ;; transform split
    (is (= org.datavec.api.split.TransformSplit
           (type
            (new-transform-split :base-input-split (new-collection-input-split
                                                    :coll ['(new java.net.URI "foo")
                                                           '(new java.net.URI "baz")])
                                 :to-be-replaced "foo"
                                 :replaced-with "oof"
                                 :as-code? false))))
    (is (= '(org.datavec.api.split.TransformSplit/ofSearchReplace
             (org.datavec.api.split.CollectionInputSplit.
              [(new java.net.URI "foo") (new java.net.URI "baz")]) "foo" "oof")
           (new-transform-split :base-input-split (new-collection-input-split
                                                   :coll ['(new java.net.URI "foo")
                                                          '(new java.net.URI "baz")])
                                :to-be-replaced "foo"
                                :replaced-with "oof")))

    ;; sample
    (is (= (type (new-collection-input-split
                  :coll ['(new java.net.URI "foo")
                         '(new java.net.URI "baz")]
                  :as-code? false))
           (type (first (sample :split (new-collection-input-split
                                        :coll ['(new java.net.URI "foo")
                                               '(new java.net.URI "baz")]
                                        :as-code? false)
                                :weights [50 50]
                                :as-code? false
                                :path-filter (new-random-path-filter
                                              :seed 123
                                              :extensions [".txt"]
                                              :max-paths 2
                                              :as-code? false))))))
    (is (= (type (new-collection-input-split
                  :coll ['(new java.net.URI "foo")
                         '(new java.net.URI "baz")]
                  :as-code? false))
           (type (first (sample :split (new-collection-input-split
                                        :coll ['(new java.net.URI "foo")
                                               '(new java.net.URI "baz")]
                                        :as-code? false)
                                :weights [50 50])))))
    (is (= 2 (count (sample :split (new-collection-input-split
                                    :coll ['(new java.net.URI "foo")
                                           '(new java.net.URI "baz")]
                                    :as-code? false)
                            :weights [50 50]))))))

;; good
(deftest input-split-interface-testing
  (testing "the interfaces used by input splits"
    ;; datavec.api.writeable
    (let [f-split (new-filesplit :path "resources/poker/" :as-code? false)]
      (is (= java.lang.Long (type (length f-split))))
      (is (= java.net.URI (type (first (locations f-split)))))
      (is (= org.datavec.api.util.files.UriFromPathIterator
             (type (locations-iterator f-split))))
      (is (= java.util.Collections$1
             (type (locations-path-iterator f-split))))
      (is (= (type f-split) (type (reset-input-split! f-split)))))))

;; good
(deftest record-readers-test
  (testing "the creation of record readers"
    ;; datavec.api.records.readers
    ;; csv-nlines-seq-rr
    (is (= org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader
           (type (new-csv-nlines-seq-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader.)
           (new-csv-nlines-seq-record-reader)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader
           (type (new-csv-nlines-seq-record-reader :n-lines-per-seq 5 :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader. 5)
         (new-csv-nlines-seq-record-reader :n-lines-per-seq 5)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader
           (type (new-csv-nlines-seq-record-reader :n-lines-per-seq 5
                                                   :delimiter ","
                                                   :skip-n-lines 1
                                                   :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader. 5 1 ",")
           (new-csv-nlines-seq-record-reader :n-lines-per-seq 5
                                             :delimiter "," :skip-n-lines 1)))

    ;; csv-rr
    (is (= org.datavec.api.records.reader.impl.csv.CSVRecordReader
           (type (new-csv-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVRecordReader.)
           (new-csv-record-reader)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVRecordReader
           (type (new-csv-record-reader :skip-n-lines 1 :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVRecordReader. 1)
           (new-csv-record-reader :skip-n-lines 1)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVRecordReader
           (type (new-csv-record-reader :skip-n-lines 1 :delimiter "," :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVRecordReader. 1 ",")
           (new-csv-record-reader :skip-n-lines 1 :delimiter ",")))

    (is (= org.datavec.api.records.reader.impl.csv.CSVRecordReader
           (type (new-csv-record-reader :skip-n-lines 1 :delimiter ","
                                        :strip-quotes nil :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVRecordReader. 1 "," nil)
           (new-csv-record-reader :skip-n-lines 1 :delimiter ","
                                  :strip-quotes nil)))

    ;; csv-seq-rr
    (is (= org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader
           (type (new-csv-seq-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader.)
           (new-csv-seq-record-reader)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader
           (type (new-csv-seq-record-reader :skip-n-lines 1 :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader. 1)
           (new-csv-seq-record-reader :skip-n-lines 1)))

    (is (= org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader
           (type (new-csv-seq-record-reader :skip-n-lines 1 :delimiter "," :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader. 1 ",")
           (new-csv-seq-record-reader :skip-n-lines 1 :delimiter ",")))

    ;; file-rr
    (is (= org.datavec.api.records.reader.impl.FileRecordReader
           (type (new-file-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.FileRecordReader.)
           (new-file-record-reader)))

    ;; line-rr
    (is (= org.datavec.api.records.reader.impl.LineRecordReader
           (type (new-line-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.LineRecordReader.)
           (new-line-record-reader)))

    ;; list-string-rr
    (is (= org.datavec.api.records.reader.impl.collection.ListStringRecordReader
           (type (new-list-string-record-reader :as-code? false))))
    (is (= '(org.datavec.api.records.reader.impl.collection.ListStringRecordReader.)
           (new-list-string-record-reader)))))

;; good
(deftest record-readers-interface
  (testing "the api fns for record readers"
    (let [rr (new-file-record-reader :as-code? false)
          init-rr (initialize-rr! :rr rr :input-split
                                  (new-filesplit
                                   :path "resources/poker-hand-testing.csv"
                                   :as-code? false))]
      ;; these tests do not cover the entire ns but the most imporant fns
      (is (= java.lang.Boolean (type (has-next-record? init-rr))))
      (is (= org.datavec.api.records.impl.Record (type (next-record-with-meta! init-rr))))
      (is (= java.util.ArrayList (type (next-record! (reset-rr! init-rr))))))))

;; good
(deftest pre-processors-test
  (testing "testing the creation of pre-processors"
    ;; nd4clj.linalg.dataset.api.pre-processors
    (is (= org.nd4j.linalg.dataset.api.preprocessor.ImageFlatteningDataSetPreProcessor
           (type (new-image-flattening-ds-preprocessor :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.ImageFlatteningDataSetPreProcessor.)
           (new-image-flattening-ds-preprocessor)))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler
           (type (new-image-scaling-ds-preprocessor :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler.)
           (new-image-scaling-ds-preprocessor)))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler
           (type (new-image-scaling-ds-preprocessor :min-range 0 :max-range 150))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler. 0 150)
           (new-image-scaling-ds-preprocessor :min-range 0 :max-range 150)))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor
           (type (new-vgg16-image-preprocessor :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor.)
           (new-vgg16-image-preprocessor)))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
           (type (new-min-max-normalization-ds-preprocessor :min-val 0 :max-val 1
                                                            :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler. 0 1)
           (new-min-max-normalization-ds-preprocessor :min-val 0 :max-val 1 )))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
           (type (new-min-max-normalization-ds-preprocessor :min-val 7 :max-val 15
                                                            :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler. 7 15)
           (new-min-max-normalization-ds-preprocessor :min-val 7 :max-val 15)))

    (is (= org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize
           (type (new-standardize-normalization-ds-preprocessor :as-code? false))))
    (is (= '(org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize.)
           (new-standardize-normalization-ds-preprocessor)))

    (is (= org.deeplearning4j.datasets.iterator.CombinedPreProcessor
           (type (new-combined-pre-processor :pre-processor {0 (new-image-flattening-ds-preprocessor)
                                                             1 {:image-scaling {:min-range 0 :max-range 10}}}
                                             :as-code? false))))
    (is (= '(.build
             (doto
                 (org.deeplearning4j.datasets.iterator.CombinedPreProcessor$Builder.)
               (.addPreProcessor 0
                (org.nd4j.linalg.dataset.api.preprocessor.ImageFlatteningDataSetPreProcessor.))
               (.addPreProcessor 1
                (org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler. 0 10))))
           (new-combined-pre-processor :pre-processor {0 (new-image-flattening-ds-preprocessor)
                                                       1 {:image-scaling {:min-range 0 :max-range 10}}})))))

;; will be updated
(deftest ds-iterators-test
  (testing "the creation of various dataset iterators"
    ;; dl4clj.datasets.iterator.iterators
    (let [iter (new-mnist-data-set-iterator :batch 5 :n-examples 100)
          pp1 (fit-iter! :normalizer (new-image-scaling-ds-preprocessor)
                         :iter iter)
          pp2 (fit-iter! :normalizer (new-standardize-normalization-ds-preprocessor)
                         :iter iter)
          multi-iter (new-multi-data-set-iterator-adapter
                      :iter
                      (new-mnist-data-set-iterator :batch 5 :n-examples 100))]
      (is (= org.deeplearning4j.datasets.iterator.CombinedPreProcessor
             (type (new-combined-pre-processor {0 pp1 1 pp2}))))
      (is (= org.deeplearning4j.datasets.iterator.AsyncDataSetIterator
             (type (new-async-dataset-iterator :iter iter))))
      (is (= org.deeplearning4j.datasets.iterator.AsyncDataSetIterator
             (type (new-async-dataset-iterator :iter iter
                                                :que-size 10))))
      (is (= org.deeplearning4j.datasets.iterator.ExistingDataSetIterator
             (type (new-existing-dataset-iterator :iter iter))))
      (is (= org.deeplearning4j.datasets.iterator.SamplingDataSetIterator
             (type
              (new-sampling-dataset-iterator :sampling-source (new-iris-ds)
                                             :batch-size 10
                                             :total-n-samples 10))))
      (is (= org.deeplearning4j.datasets.iterator.ReconstructionDataSetIterator
             (type (new-reconstruction-dataset-iterator :iter iter))))
      (is (= org.deeplearning4j.datasets.iterator.AsyncMultiDataSetIterator
             (type (new-async-multi-dataset-iterator
                    :multi-dataset-iter multi-iter
                    :que-length 10))))
      (is (= org.deeplearning4j.datasets.iterator.IteratorDataSetIterator
             (type (new-iterator-dataset-iterator :iter iter :batch-size 10))))
      (is (= org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
             (type (new-multiple-epochs-iterator :iter iter :n-epochs 1))))
      (is (= org.deeplearning4j.datasets.iterator.DoublesDataSetIterator
             (type (new-doubles-dataset-iterator :features [0.2 0.4]
                                              :labels [0.4 0.8]
                                              :batch-size 2))))
      (is (= org.deeplearning4j.datasets.iterator.FloatsDataSetIterator
             (type (new-floats-dataset-iterator :features [0.2 0.4]
                                                 :labels [0.4 0.8]
                                                 :batch-size 2))))
      (is (= org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator
             (type (new-INDArray-dataset-iterator :features [1 2]
                                                  :labels [2 2]
                                                  :batch-size 2)))))))
;; will be updated
(deftest rr-ds-iterator-test
  (testing "the creation of record reader dataset iterators"
    ;; dl4clj.datasets.datavec
    (let [fs (new-filesplit :path "resources/poker-hand-training.csv")
          rr (initialize-rr! :rr (new-csv-record-reader)
                             :input-split fs)
          seq-rr (initialize-rr! :rr (new-csv-seq-record-reader)
                                 :input-split fs)]
      (is (= org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
             (type
              (new-record-reader-dataset-iterator :record-reader rr
                                                  :batch-size 10))))
      (is (= org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
             (type
              (new-record-reader-dataset-iterator :record-reader rr
                                                  :batch-size 10
                                                  :label-idx 6
                                                  :n-possible-labels 10))))
      (is (= org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
             (type
              (new-record-reader-dataset-iterator :record-reader rr
                                                  :batch-size 10
                                                  :label-idx-from 0
                                                  :label-idx-to 7
                                                  :regression? true))))
      (is (= org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
             (type
              (new-record-reader-dataset-iterator :record-reader rr
                                                  :batch-size 10
                                                  :label-idx 6
                                                  :n-possible-labels 10
                                                  :max-num-batches 2))))

      (is (= org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator
           (type (new-seq-record-reader-dataset-iterator
                  :record-reader seq-rr
                  :mini-batch-size 5
                  :n-possible-labels 10
                  :label-idx 10
                  :regression? false))))
      (is (= org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator
             (type (new-seq-record-reader-dataset-iterator
                    :record-reader seq-rr
                    :mini-batch-size 5
                    :n-possible-labels 10
                    :label-idx 10))))
      (is (= org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator
             (type (new-record-reader-multi-dataset-iterator
                    :alignment-mode :equal-length
                    :batch-size 10
                    :add-seq-reader {:reader-name "foo" :record-reader seq-rr}
                    :add-reader {:reader-name "baz" :record-reader rr}
                    :add-input {:reader-name "baz" :first-column 0 :last-column 11})))))))
