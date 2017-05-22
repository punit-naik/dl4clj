(ns ^{:doc "implementation of the eval class in dl4j.  Used to get performance metrics for a model
see: https://deeplearning4j.org/doc/org/deeplearning4j/eval/Evaluation.html and
https://deeplearning4j.org/doc/org/deeplearning4j/eval/RegressionEvaluation.html"}
    dl4clj.eval.evaluation
  (:import [org.deeplearning4j.eval Evaluation RegressionEvaluation BaseEvaluation])
  (:require [dl4clj.nn.conf.utils :refer [contains-many?]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create a classification or regression evaluator
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-evaluator
  ;; turn this into a multimethod for consistency
  "creates an evaluation object for evaling a trained network.

  args:

  :classification? (boolean, default true): determines if a classification or regression
   evaler is created

  :classification? = true args

  -- :n-classes (int): number of classes to account for in the evaluation
  -- :labels (list of strings): the labels to include with the evaluation
  -- :top-n (int): when looking for the top N accuracy of a model
  -- :label-to-idx-map {label-idx (int) label-value (string)}, replaces the use of :labels

  :classification? = false args

  -- :n-columns (int): number of columns in the dataset
  -- :precision (int): specified precision to be returned when you call stats
  -- :column-names (coll of strings): names of the columns"
  [{:keys [classification? n-classes labels
           top-n label-to-idx-map n-columns
           precision column-names]
    :or {classification? true}
    :as opts}]
  (if (true? classification?)
    (cond (contains-many? opts :labels :top-n)
          (Evaluation. labels top-n)
          (contains? opts :labels)
          (Evaluation. labels)
          (contains? opts :label-to-idx-map)
          (Evaluation. label-to-idx-map)
          (contains? opts :n-classes)
          (Evaluation. n-classes)
          :else
          (Evaluation.))
    (cond (contains-many? opts :column-names :precision)
          (RegressionEvaluation. column-names precision)
          (contains-many? opts :n-columns :precision)
          (RegressionEvaluation. n-columns precision)
          (contains? opts :column-names)
          (RegressionEvaluation. column-names)
          (contains? opts :n-columns)
          (RegressionEvaluation. n-columns)
          :else
          (assert
           false
           "you must supply either the number of columns or their names for regression evaluation"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; interact with the evaler created above
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-accuracy
  "Accuracy: (TP + TN) / (P + N)"
  [evaler]
  (.accuracy evaler))

(defn add-to-confusion
  "Adds to the confusion matrix"
  [& {:keys [evaler real-value guess-value]}]
  (doto evaler
    (.addToConfusion real-value guess-value)))

(defn class-count
  "Returns the number of times the given label has actually occurred"
  [& {:keys [evaler class-label-idx]}]
  (.classCount evaler class-label-idx))

(defn confusion-to-string
  "Get a String representation of the confusion matrix"
  [evaler]
  (.confusionToString evaler))

(defn f1
  "TP: true positive FP: False Positive FN: False Negative
  F1 score = 2 * TP / (2TP + FP + FN),

  the calculation will only be done for a single class if that classes idx is supplied
   -here class refers to the labels the network was trained on"
  [& {:keys [evaler class-label-idx]
      :as opts}]
  (if (contains? opts :class-label-idx)
    (.f1 evaler class-label-idx)
    (.f1 evaler)))

(defn false-alarm-rate
  "False Alarm Rate (FAR) reflects rate of misclassified to classified records"
  [evaler]
  (.falseAlarmRate evaler))

(defn false-negative-rate
  "False negative rate based on guesses so far Takes into account all known classes
  and outputs average fnr across all of them

  can be scoped down to a single class if class-label-idx supplied"
  [& {:keys [evaler class-label-idx edge-case]
      :as opts}]
  (cond (contains-many? opts :class-label-idx :edge-case :evaler)
        (.falseNegativeRate evaler class-label-idx edge-case)
        (contains-many? opts :evaler :class-label-idx)
        (.falseNegativeRate evaler class-label-idx)
        (contains? opts :evaler)
        (.falseNegativeRate evaler)
        :else
        (assert false "you must atleast provide an evaler to get the false negative rate of the model being evaluated")))

(defn false-negatives
  "False negatives: correctly rejected"
  [evaler]
  (.falseNegatives evaler))

(defn false-positive-rate
  "False positive rate based on guesses so far Takes into account all known classes
  and outputs average fpr across all of them

  can be scoped down to a single class if class-label-idx supplied"
  [& {:keys [evaler class-label-idx edge-case]
      :as opts}]
  (cond (contains-many? opts :class-label-idx :edge-case :evaler)
        (.falsePositiveRate evaler class-label-idx edge-case)
        (contains-many? opts :evaler :class-label-idx)
        (.falsePositiveRate evaler class-label-idx)
        (contains? opts :evaler)
        (.falsePositiveRate evaler)
        :else
        (assert false "you must atleast provide an evaler to get the false positive rate of the model being evaluated")))

(defn false-positives
  "False positive: wrong guess"
  [evaler]
  (.falsePositives evaler))

(defn get-class-label
  "get the class a label is associated with given an idx"
  [& {:keys [evaler label-idx]}]
  (.getClassLabel evaler label-idx))

(defn get-confusion-matrix
  "Returns the confusion matrix variable"
  [evaler]
  (.getConfusionMatrix evaler))

(defn get-num-row-counter
  [evaler]
  (.getNumRowCounter evaler))

(defn get-prediction-by-predicted-class
  "Get a list of predictions, for all data with the specified predicted class,
  regardless of the actual data class."
  [& {:keys [evaler idx-of-predicted-class]}]
  (.getPredictionByPredictedClass evaler idx-of-predicted-class))

(defn get-prediction-errors
  "Get a list of prediction errors, on a per-record basis"
  [evaler]
  (.getPredictionErrors evaler))

(defn get-predictions
  "Get a list of predictions in the specified confusion matrix entry
  (i.e., for the given actua/predicted class pair)"
  [& {:keys [evaler actual-class-idx predicted-class-idx]}]
  (.getPredictions evaler actual-class-idx predicted-class-idx))

(defn get-predictions-by-actual-class
  "Get a list of predictions, for all data with the specified actual class,
  regardless of the predicted class."
  [& {:keys [evaler actual-class-idx]}]
  (.getPredictionsByActualClass evaler actual-class-idx))

(defn get-top-n-correct-count
  "Return the number of correct predictions according to top N value."
  [evaler]
  (.getTopNCorrectCount evaler))

(defn get-top-n-total-count
  "Return the total number of top N evaluations."
  [evaler]
  (.getTopNTotalCount evaler))

(defn increment-false-negatives!
  [& {:keys [evaler class-label-idx]}]
  (doto evaler
    (.incrementFalseNegatives class-label-idx)))

(defn increment-false-positives!
  [& {:keys [evaler class-label-idx]}]
  (doto evaler
    (.incrementFalsePositives class-label-idx)))

(defn increment-true-negatives!
  [& {:keys [evaler class-label-idx]}]
  (doto evaler
    (.incrementTrueNegatives class-label-idx)))

(defn increment-true-positives!
  [& {:keys [evaler class-label-idx]}]
  (doto evaler
    (.incrementTruePositives class-label-idx)))

(defn total-negatives
  "Total negatives true negatives + false negatives"
  [evaler]
  (.negative evaler))

(defn total-positives
  "Returns all of the positive guesses: true positive + false negative"
  [evaler]
  (.positive evaler))

(defn get-precision
  "Precision based on guesses so far Takes into account all known classes and
  outputs average precision across all of them.

  can be scoped to a label given its idx"
  [& {:keys [evaler class-label-idx edge-case]
      :as opts}]
  (cond (contains-many? opts :class-label-idx :edge-case :evaler)
        (.precision evaler class-label-idx edge-case)
        (contains-many? opts :evaler :class-label-idx)
        (.precision evaler class-label-idx)
        (contains? opts :evaler)
        (.precision evaler)
        :else
        (assert false "you must atleast provide an evaler to get the precision of the model being evaluated")))

(defn recall
  "Recall based on guesses so far Takes into account all known classes
  and outputs average recall across all of them

  can be scoped to a label given its idx"
  [& {:keys [evaler class-label-idx edge-case]
      :as opts}]
  (cond (contains-many? opts :class-label-idx :edge-case :evaler)
        (.recall evaler class-label-idx edge-case)
        (contains-many? opts :evaler :class-label-idx)
        (.recall evaler class-label-idx)
        (contains? opts :evaler)
        (.recall evaler)
        :else
        (assert false "you must atleast provide an evaler to get the recall of the model being evaluated")))

(defn get-stats
  "Method to obtain the classification report as a String"
  [& {:keys [evaler suppress-warnings?]
      :as opts}]
  (if (contains? opts :suppress-warnings?)
    (.stats evaler suppress-warnings?)
    (.stats evaler)))

(defn top-n-accuracy
  "Top N accuracy of the predictions so far."
  [evaler]
  (.topNAccuracy evaler))

(defn true-negatives
  "True negatives: correctly rejected"
  [evaler]
  (.trueNegatives evaler))

(defn true-positives
  "True positives: correctly rejected"
  [evaler]
  (.truePositives evaler))

(defn get-mean-squared-error
  "returns the MSE"
  [& {:keys [regression-evaler column-idx]}]
  (.meanSquaredError regression-evaler column-idx))

(defn get-mean-absolute-error
  "returns MAE"
  [& {:keys [regression-evaler column-idx]}]
  (.meanAbsoluteError regression-evaler column-idx))

(defn get-root-mean-squared-error
  "returns rMSE"
  [& {:keys [regression-evaler column-idx]}]
  (.rootMeanSquaredError regression-evaler column-idx))

(defn get-correlation-r2
  "return the R2 correlation"
  [& {:keys [regression-evaler column-idx]}]
  (.correlationR2 regression-evaler column-idx))

(defn get-relative-squared-error
  "return relative squared error"
  [& {:keys [regression-evaler column-idx]}]
  (.relativeSquaredError regression-evaler column-idx))
