(ns dl4clj.spark.api.dl4j-multi-layer
  (:import [org.deeplearning4j.spark.impl.multilayer SparkDl4jMultiLayer])
  (:require [clojure.core.match :refer [match]]
            [dl4clj.utils :refer [obj-or-code? gensym*]]
            [dl4clj.spark.data.java-rdd :as rdd]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; getters
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-network
  "returns the mln from the Spark-mln"
  [spark-mln & {:keys [as-code?]
                :or {as-code? true}}]
  (match [spark-mln]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getNetwork ~spark-mln))
         :else
         (.getNetwork spark-mln)))

(defn get-score
  "Gets the last (average) minibatch score from calling fit."
  [spark-mln & {:keys [as-code?]
                :or {as-code? true}}]
  (match [spark-mln]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getScore ~spark-mln))
         :else
         (.getScore spark-mln)))

(defn get-spark-context
  "returns the spark context from the spark-mln"
  [spark-mln & {:keys [as-code?]
                :or {as-code? true}}]
  (match [spark-mln]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getSparkContext ~spark-mln))
         :else
         (.getSparkContext spark-mln)))

(defn get-spark-training-stats
  "Get the training statistics, after collection of stats has been enabled"
  [spark-mln & {:keys [as-code?]
                :or {as-code? true}}]
  (match [spark-mln]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getSparkTrainingStats ~spark-mln))
         :else
         (.getSparkTrainingStats spark-mln)))

(defn get-training-master
  "return the training master used to configure the spark-mln"
  [spark-mln & {:keys [as-code?]
                :or {as-code? true}}]
  (match [spark-mln]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getTrainingMaster ~spark-mln))
         :else
         (.getTrainingMaster spark-mln)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; misc, these need to throw when passed a spark-mln and rdd as code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn spark-calc-score
  "Calculate the score for all examples in the provided rdd

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the rdd containing the data
   - can also be RDD<org.nd4j.linalg.dataset.DataSet>

  :average? (boolean), determines if score is calculated via averaging or summing
   over the entire dataset

  :mini-batch-size (int), the size of the mini batches within the rdd
   - should only be supplied when :rdd is a JavaRDD and then still optional

  :iter ..."
  ;;TODO: update to work with iters
  [& {:keys [spark-mln rdd average? mini-batch-size]
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :average? _
           :mini-batch-size _}]
         (.calculateScore spark-mln rdd average? mini-batch-size)
         [{:spark-mln _
           :rdd _
           :average? _}]
         (.calculateScore spark-mln rdd average?)
         ))

(defn eval-classification-spark-mln
  "Evaluate the network in a distributed manner on the provided data

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the rdd containing the data
   - can also be RDD<org.nd4j.linalg.dataset.DataSet>

  :labels (list), a collection of strings which serve as the labels in evaluation

  :batch-size (int), the batch size for the data within the rdd

   - required args: :spark-mln, :rdd"
  [& {:keys [spark-mln rdd labels batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :labels _
           :batch-size _}]
         (.evaluate spark-mln rdd labels batch-size)
         [{:spark-mln _
           :rdd _
           :labels _}]
         (.evaluate spark-mln rdd labels)
         [{:spark-mln _
           :rdd _}]
         (.evaluate spark-mln rdd)))

(defn eval-regression-spark-mln
  "Evaluate the network in a distributed manner on the provided data

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>), the data

  :mini-batch-size (int), Minibatch size to use when doing performing evaluation
   - optional"
  [& {:keys [spark-mln rdd mini-batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :mini-batch-size _}]
         (.evaluateRegression spark-mln rdd mini-batch-size)
         [{:spark-mln _
           :rdd _}]
         (.evaluateRegression spark-mln rdd)))

(defn eval-roc-spark-mln
  "Perform ROC analysis/evaluation on the given DataSet in a distributed manner

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the data

  :threshold-steps (int), Number of threshold steps for ROC
   - see: dl4clj.eval.roc.rocs

  :mini-batch-size (int) Minibatch size to use when performing eval

  - :threshold-steps and :mini-batch-size are optional but must be supplied together"
  [& {:keys [spark-mln rdd threshold-steps mini-batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :threshold-steps _
           :mini-batch-size _}]
         (.evaluateROC spark-mln rdd threshold-steps mini-batch-size)
         [{:spark-mln _
           :rdd _}]
         (.evaluateROC spark-mln rdd)))

(defn eval-multi-class-roc-spark-mln
  "Perform ROC analysis/evaluation on the given DataSet in a distributed manner
   - using a ROCMultiClass evaluation object

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the data

  :threshold-steps (int), Number of threshold steps for ROC
   - see: dl4clj.eval.roc.rocs

  :mini-batch-size (int) Minibatch size to use when performing eval

  - :threshold-steps and :mini-batch-size are optional but must be supplied together"
  [& {:keys [spark-mln rdd threshold-steps mini-batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :threshold-steps _
           :mini-batch-size _}]
         (.evaluateROCMultiClass spark-mln rdd threshold-steps mini-batch-size)
         [{:spark-mln _
           :rdd _}]
         (.evaluateROCMultiClass spark-mln rdd)))

(defn feed-forward-with-key
  "Feed-forward the specified data, with the given keys.

  :pair-rdd (JavaPairRDD<K,org.nd4j.linalg.api.ndarray.INDArray>) the data

  :batch-size (int), the batch size of the data"
  [& {:keys [spark-mln pair-rdd batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :pair-rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         :else
         (.feedForwardWithKey spark-mln pair-rdd batch-size)))

(defn fit-spark-mln!
  "Fit the supplied model to the supplied dataset

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the data
   - can also be RDD<org.nd4j.linalg.dataset.DataSet>

  :path-to-data (str), path to a directory of serialized DataSet objects
   - The assumption here is that the directory contains a number of DataSet objects,
     each serialized using (save-dataset! DataSet OutputStream)

  either :rdd or :path-to-data should be supplied, not both"
  [& {:keys [spark-mln rdd path-to-data n-epochs as-code?]
      :or {n-epochs 10
           as-code? false}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "use train-with-spark in dl4clj.nn.training"))
         [{:spark-mln _
           :rdd _}]
         (do (dotimes [n n-epochs]
               (doto spark-mln (.fit rdd)))
             spark-mln)
         [{:spark-mln (_ :guard seq?)
           :path-to-data (:or (_ :guard string?)
                              (_ :guard seq?))}]
         (let [n (gensym* :sym "number-of-epochs")]
           (obj-or-code?
            as-code?
            `(do (dotimes [~n ~n-epochs]
                   (doto ~spark-mln (.fit ~path-to-data)))
                 ~spark-mln)))
         [{:spark-mln _
           :path-to-data _}]
         (do (dotimes [n n-epochs]
               (doto spark-mln (.fit path-to-data)))
             spark-mln)))

(defn fit-continous-labeled-point!
  "Fits a MultiLayerNetwork using Spark MLLib LabeledPoint instances
  This will convert labeled points that have continuous labels used
  for regression to the internal DL4J data format and train the model on that

  :rdd (JavaRDD<org.apache.spark.mllib.regression.LabeledPoint>) the data"
  [& {:keys [spark-mln rdd]
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         :else
         (.fitContinuousLabeledPoint spark-mln rdd)))

(defn fit-labeled-point!
  "Fit a MultiLayerNetwork using Spark MLLib LabeledPoint instances.

  :rdd JavaRDD<org.apache.spark.mllib.regression.LabeledPoint>"
  [& {:keys [spark-mln rdd]
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         :else
         (.fitLabeledPoint spark-mln rdd)))

(defn fit-from-paths!
  "Fit the network using a list of paths for serialized DataSet objects.

  :rdd JavaRDD<java.lang.String>"
  [& {:keys [spark-mln rdd as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         :else
         (.fitPaths spark-mln rdd)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remember to look into creation of the spark matrices/vectors
;; should have a conversion fn for clj-vector/matrix -> spark-vector/matrix
#_(defn predict-spark-mln
  "feed data through the network and get its prediction

  :input (matrix or vector) the data to be fed through
   - org.apache.spark.mllib.linalg.Matrix or org.apache.spark.mllib.linalg.Vector"
  [& {:keys [spark-mln input as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))}]
         (obj-or-code? as-code? `(.predict ~spark-mln ~input))
         :else
         (.predict spark-mln input)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn score-examples-spark-mln
  "Score the examples individually

  :rdd (JavaRDD<org.nd4j.linalg.dataset.DataSet>) the data
   - can also be a pair-rdd (JavaPairRDD<K,org.nd4j.linalg.dataset.DataSet>) or
     RDD<org.nd4j.linalg.dataset.DataSet>

  :include-regularization-terms? (boolean), should regularization be factored into scoring

  :batch-size (int), the batch size for the data
   - optional arg"
  [& {:keys [spark-mln rdd include-regularization-terms? batch-size as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         [{:spark-mln _
           :rdd _
           :include-regularization-terms? _
           :batch-size _}]
         (.scoreExamples spark-mln rdd include-regularization-terms? batch-size)
         [{:spark-mln _
           :rdd _
           :include-regularization-terms? _}]
         (.scoreExamples spark-mln rdd include-regularization-terms?)))
