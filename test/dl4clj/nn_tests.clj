(ns dl4clj.nn-tests
  (:require [dl4clj.nn.conf.builders.builders :refer :all]
            [dl4clj.nn.conf.builders.nn-conf-builder :refer :all]
            [dl4clj.nn.conf.builders.multi-layer-builders :refer :all]
            [dl4clj.nn.conf.input-pre-processor :refer :all]
            [dl4clj.nn.conf.constants :refer :all]
            [dl4clj.nn.conf.distribution.distribution :refer :all]
            [dl4clj.nn.conf.step-fns :refer :all]
            [dl4clj.nn.conf.variational.dist-builders :refer :all]
            [dl4clj.nn.layers.layer-creation :refer :all]
            [dl4clj.nn.gradient.default-gradient :refer :all]
            [nd4clj.linalg.factory.nd4j :refer [zeros]]
            [dl4clj.nn.api.model :refer [set-param-table!]]
            [clojure.test :refer :all]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; helper fn for layer creation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn quick-nn-conf
  [layer]
  (nn-conf-builder :optimization-algo :stochastic-gradient-descent
                   :iterations 1
                   :learning-rate 0.006
                   :updater :nesterovs
                   :momentum 0.9
                   :regularization true
                   :l2 1e-4
                   :build? true
                   :layer layer))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; constants, value-of, input-type
;; dl4clj.nn.conf.constants
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest enum-testing
  (testing "the creation of dl4j enums/constants"
    ;; constants (helper fn)
    (is (= "RELU" (constants #(str %) :relu :activation? true)))
    (is (= "FooBaz" (constants #(str %) :foo-baz :camel? true)))
    (is (= "Relu" (constants #(str %) :relu :activation? true :camel? true)))
    (is (= "FOO_BAZ" (constants #(str %) :foo-baz)))

    (is (= org.nd4j.linalg.activations.Activation
           (type (value-of {:activation-fn :relu}))))
    (is (= org.deeplearning4j.nn.conf.GradientNormalization
           (type (value-of {:gradient-normalization :none}))))
    (is (= org.deeplearning4j.nn.conf.LearningRatePolicy
           (type (value-of {:learning-rate-policy :poly}))))
    (is (= org.deeplearning4j.nn.conf.Updater
           (type (value-of {:updater :adam}))))
    (is (= org.deeplearning4j.nn.weights.WeightInit
           (type (value-of {:weight-init :xavier}))))
    (is (= org.nd4j.linalg.lossfunctions.LossFunctions$LossFunction
           (type (value-of {:loss-fn :mse}))))
    (is (= org.deeplearning4j.nn.conf.layers.RBM$HiddenUnit
           (type (value-of {:hidden-unit :binary}))))
    (is (= org.deeplearning4j.nn.conf.layers.RBM$VisibleUnit
           (type (value-of {:visible-unit :binary}))))
    (is (= org.deeplearning4j.nn.conf.ConvolutionMode
           (type (value-of {:convolution-mode :strict}))))
    (is (= org.deeplearning4j.nn.conf.layers.ConvolutionLayer$AlgoMode
           (type (value-of {:cudnn-algo-mode :no-workspace}))))
    (is (= org.deeplearning4j.nn.conf.layers.PoolingType
           (type (value-of {:pool-type :avg}))))
    (is (= org.deeplearning4j.nn.conf.BackpropType
           (type (value-of {:backprop-type :standard}))))
    (is (= org.deeplearning4j.nn.api.OptimizationAlgorithm
           (type (value-of {:optimization-algorithm :lbfgs}))))
    (is (= org.deeplearning4j.nn.api.MaskState
           (type (value-of {:mask-state :active}))))
    (is (= org.deeplearning4j.nn.api.Layer$Type
           (type (value-of {:layer-type :feed-forward}))))
    (is (= org.deeplearning4j.nn.api.Layer$TrainingMode
           (type (value-of {:layer-training-mode :train}))))

    (is (= org.deeplearning4j.nn.conf.inputs.InputType$InputTypeRecurrent
           (type (input-types {:recurrent {:size 10}}))))
    (is (= org.deeplearning4j.nn.conf.inputs.InputType$InputTypeFeedForward
           (type (input-types {:feed-forward {:size 10}}))))
    (is (= org.deeplearning4j.nn.conf.inputs.InputType$InputTypeConvolutional
           (type (input-types {:convolutional {:height 1 :width 1 :depth 1}}))))
    (is (= org.deeplearning4j.nn.conf.inputs.InputType$InputTypeConvolutionalFlat
           (type (input-types {:convolutional-flat {:height 1 :width 1 :depth 1}}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; distributions to sample weights from
;; dl4clj.nn.conf.distribution.distribution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest distributions-test
  (testing "the creation of distributions for use in a nn-conf"
    (is (= org.deeplearning4j.nn.conf.distribution.UniformDistribution
           (type (new-uniform-distribution :lower 0.2 :upper 0.4))))
    (is (= org.deeplearning4j.nn.conf.distribution.NormalDistribution
           (type (new-normal-distribution :mean 0 :std 1))))
    (is (= org.deeplearning4j.nn.conf.distribution.GaussianDistribution
           (type (new-gaussian-distribution :mean 0.0 :std 1))))
    (is (= org.deeplearning4j.nn.conf.distribution.BinomialDistribution
           (type (new-binomial-distribution :number-of-trials 2
                                            :probability-of-success 0.5))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; reconstruction distribution
;; dl4clj.nn.conf.variational.dist-builders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest reconstruction-distribution-test
  (testing "the creation of reconstruction distributions for vae's"
    (is (= org.deeplearning4j.nn.conf.layers.variational.BernoulliReconstructionDistribution
           (type (new-bernoulli-reconstruction-distribution :activation-fn :relu))))
    (is (= org.deeplearning4j.nn.conf.layers.variational.BernoulliReconstructionDistribution
           (type (new-bernoulli-reconstruction-distribution))))

    (is (= org.deeplearning4j.nn.conf.layers.variational.ExponentialReconstructionDistribution
           (type (new-exponential-reconstruction-distribution :activation-fn :relu))))
    (is (= org.deeplearning4j.nn.conf.layers.variational.ExponentialReconstructionDistribution
           (type (new-exponential-reconstruction-distribution))))

    (is (= org.deeplearning4j.nn.conf.layers.variational.GaussianReconstructionDistribution
           (type (new-gaussian-reconstruction-distribution :activation-fn :relu))))
    (is (= org.deeplearning4j.nn.conf.layers.variational.GaussianReconstructionDistribution
           (type (new-gaussian-reconstruction-distribution))))

    (is (= org.deeplearning4j.nn.conf.layers.variational.CompositeReconstructionDistribution
           (type (new-composite-reconstruction-distribution
                  :distributions-to-add
                  ;; using a user facing fn
                  {0 {:dist (new-bernoulli-reconstruction-distribution
                             :activation-fn :relu)
                      :dist-size 2}
                   ;; using the multi method
                   1 {:bernoulli {:activation-fn :tanh
                                  :dist-size 5}}

                   2 {:exponential {:activation-fn :sigmoid
                                    :dist-size 3}}

                   3 {:gaussian {:activation-fn :hard-tanh
                                 :dist-size 1}}
                   4 {:bernoulli {:activation-fn :softmax
                                  :dist-size 4}}
                   ;; explicitly using the multimethod
                   5 {:dist (distributions {:bernoulli {:activation-fn :tanh}})
                      :dist-size 7}}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; step functions for use in nn-conf creation
;; dl4clj.nn.conf.step-fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest step-fn-test
  (testing "the creation of step fns"
    (is (= org.deeplearning4j.nn.conf.stepfunctions.DefaultStepFunction
           (type (new-default-step-fn))))
    (is (= org.deeplearning4j.nn.conf.stepfunctions.GradientStepFunction
           (type (new-gradient-step-fn))))
    (is (= org.deeplearning4j.nn.conf.stepfunctions.NegativeDefaultStepFunction
           (type (new-negative-default-step-fn))))
    (is (= org.deeplearning4j.nn.conf.stepfunctions.NegativeGradientStepFunction
           (type (new-negative-gradient-step-fn))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; creation of default gradients
;; dl4clj.nn.gradient.default-gradient
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest default-gradient-test
  (testing "the creation and manipulation of gradients"
    (let [grad-with-var (set-gradient-for! :grad (new-default-gradient)
                                           :variable "foo"
                                           :new-gradient (zeros [2 2]))]
     (is (= org.deeplearning4j.nn.gradient.DefaultGradient
           (type (new-default-gradient))))
    (is (= org.deeplearning4j.nn.gradient.DefaultGradient
           (type grad-with-var)))
    ;; I don't think this test is reliable bc it assumes cpu
    (is (= org.nd4j.linalg.cpu.nativecpu.NDArray
           (type (gradient :grad grad-with-var))))
    (is (= java.util.LinkedHashMap
           (type (gradient-for-variable :grad grad-with-var))))
    ;; gradient order was not explictly set
    (is (= nil
           (type (flattening-order-for-variables :grad grad-with-var
                                                 :variable "foo"))))
    (is (= java.util.LinkedHashMap
           (type (gradient-for-variable :grad grad-with-var)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; any layer builder
;; dl4clj.nn.conf.builders.builders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest layer-builder-test
  (testing "the creation of nearly any layer in dl4j"
    (let [activation-layer-conf (activation-layer-builder
                                 :n-in 10 :n-out 2 :activation-fn :relu
                                 :adam-mean-decay 0.2 :adam-var-decay 0.1
                                 :bias-init 0.7 :bias-learning-rate 0.1
                                 :dist (new-normal-distribution :mean 0 :std 1)
                                 :drop-out 0.2 :epsilon 0.3
                                 :gradient-normalization :none
                                 :gradient-normalization-threshold 0.9
                                 :l1 0.2 :l2 0.7 :layer-name "foo"
                                 :learning-rate 0.1 :learning-rate-policy :inverse
                                 :l1-bias 0.1 :l2-bias 0.2
                                 :learning-rate-schedule {0 0.2 1 0.5}
                                 :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                 :rho 0.7 :rms-decay 0.7 :updater :adam
                                 :weight-init :distribution)
          center-loss-output-layer-conf (center-loss-output-layer-builder
                                         :alpha 0.1 :gradient-check? false :lambda 0.1
                                         :loss-fn :mse
                                         :activation-fn :relu
                                         :adam-mean-decay 0.2 :adam-var-decay 0.1
                                         :bias-init 0.7 :bias-learning-rate 0.1
                                         :dist {:normal {:mean 0 :std 1}}
                                         :drop-out 0.2 :epsilon 0.3
                                         :gradient-normalization :none
                                         :gradient-normalization-threshold 0.9
                                         :l1 0.2 :l2 0.7 :layer-name "foo"
                                         :learning-rate 0.1 :learning-rate-policy :inverse
                                         :l1-bias 0.1 :l2-bias 0.2
                                         :learning-rate-schedule {0 0.2 1 0.5}
                                         :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                         :rho 0.7 :rms-decay 0.7 :updater :adam
                                         :weight-init :distribution)
          output-layer-conf (output-layer-builder
                             :n-in 10 :n-out 2 :loss-fn :mse
                             :activation-fn :relu
                             :adam-mean-decay 0.2 :adam-var-decay 0.1
                             :bias-init 0.7 :bias-learning-rate 0.1
                             :dist {:normal {:mean 0 :std 1}}
                             :drop-out 0.2 :epsilon 0.3
                             :gradient-normalization :none
                             :gradient-normalization-threshold 0.9
                             :l1 0.2 :l2 0.7 :layer-name "foo"
                             :learning-rate 0.1 :learning-rate-policy :inverse
                             :l1-bias 0.1 :l2-bias 0.2
                             :learning-rate-schedule {0 0.2 1 0.5}
                             :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                             :rho 0.7 :rms-decay 0.7 :updater :adam
                             :weight-init :distribution)
          rnn-output-layer-conf (rnn-output-layer-builder
                                 :n-in 10 :n-out 2 :loss-fn :mse
                                 :activation-fn :relu
                                 :adam-mean-decay 0.2 :adam-var-decay 0.1
                                 :bias-init 0.7 :bias-learning-rate 0.1
                                 :dist {:normal {:mean 0 :std 1}}
                                 :drop-out 0.2 :epsilon 0.3
                                 :gradient-normalization :none
                                 :gradient-normalization-threshold 0.9
                                 :l1 0.2 :l2 0.7 :layer-name "foo"
                                 :learning-rate 0.1 :learning-rate-policy :inverse
                                 :l1-bias 0.1 :l2-bias 0.2
                                 :learning-rate-schedule {0 0.2 1 0.5}
                                 :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                 :rho 0.7 :rms-decay 0.7 :updater :adam
                                 :weight-init :distribution)
          autoencoder-layer-conf (auto-encoder-layer-builder
                                  :n-in 10 :n-out 2 :pre-train-iterations 2
                                  :loss-fn :mse :visible-bias-init 0.1
                                  :corruption-level 0.7 :sparsity 0.4
                                  :activation-fn :relu
                                  :adam-mean-decay 0.2 :adam-var-decay 0.1
                                  :bias-init 0.7 :bias-learning-rate 0.1
                                  :dist {:normal {:mean 0 :std 1}}
                                  :drop-out 0.2 :epsilon 0.3
                                  :gradient-normalization :none
                                  :gradient-normalization-threshold 0.9
                                  :l1 0.2 :l2 0.7 :layer-name "foo"
                                  :learning-rate 0.1 :learning-rate-policy :inverse
                                  :l1-bias 0.1 :l2-bias 0.2
                                  :learning-rate-schedule {0 0.2 1 0.5}
                                  :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                  :rho 0.7 :rms-decay 0.7 :updater :adam
                                  :weight-init :distribution)
          rbm-layer-conf (rbm-layer-builder
                          :n-in 10 :n-out 2 :loss-fn :mse
                          :pre-train-iterations 1 :visible-bias-init 0.7
                          :hidden-unit :softmax :visible-unit :identity
                          :k 2 :sparsity 0.6
                          :activation-fn :relu
                          :adam-mean-decay 0.2 :adam-var-decay 0.1
                          :bias-init 0.7 :bias-learning-rate 0.1
                          :dist {:normal {:mean 0 :std 1}}
                          :drop-out 0.2 :epsilon 0.3
                          :gradient-normalization :none
                          :gradient-normalization-threshold 0.9
                          :l1 0.2 :l2 0.7 :layer-name "foo"
                          :learning-rate 0.1 :learning-rate-policy :inverse
                          :l1-bias 0.1 :l2-bias 0.2
                          :learning-rate-schedule {0 0.2 1 0.5}
                          :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                          :rho 0.7 :rms-decay 0.7 :updater :adam
                          :weight-init :distribution)
          graves-bidirectional-lstm-conf (graves-bidirectional-lstm-layer-builder
                                          :n-in 10 :n-out 2 :forget-gate-bias-init 0.2
                                          :gate-activation-fn :relu
                                          :activation-fn :relu
                                          :adam-mean-decay 0.2 :adam-var-decay 0.1
                                          :bias-init 0.7 :bias-learning-rate 0.1
                                          :dist {:normal {:mean 0 :std 1}}
                                          :drop-out 0.2 :epsilon 0.3
                                          :gradient-normalization :none
                                          :gradient-normalization-threshold 0.9
                                          :l1 0.2 :l2 0.7 :layer-name "foo"
                                          :learning-rate 0.1 :learning-rate-policy :inverse
                                          :l1-bias 0.1 :l2-bias 0.2
                                          :learning-rate-schedule {0 0.2 1 0.5}
                                          :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                          :rho 0.7 :rms-decay 0.7 :updater :adam
                                          :weight-init :distribution)
          graves-lstm-layer-conf (graves-lstm-layer-builder
                                  :n-in 10 :n-out 2 :forget-gate-bias-init 0.2
                                  :gate-activation-fn :relu
                                  :activation-fn :relu
                                  :adam-mean-decay 0.2 :adam-var-decay 0.1
                                  :bias-init 0.7 :bias-learning-rate 0.1
                                  :dist {:normal {:mean 0 :std 1}}
                                  :drop-out 0.2 :epsilon 0.3
                                  :gradient-normalization :none
                                  :gradient-normalization-threshold 0.9
                                  :l1 0.2 :l2 0.7 :layer-name "foo"
                                  :learning-rate 0.1 :learning-rate-policy :inverse
                                  :l1-bias 0.1 :l2-bias 0.2
                                  :learning-rate-schedule {0 0.2 1 0.5}
                                  :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                  :rho 0.7 :rms-decay 0.7 :updater :adam
                                  :weight-init :distribution)
          batch-normalization-layer-conf (batch-normalization-layer-builder
                                          :n-in 10 :n-out 2 :beta 0.5
                                          :decay 0.3 :eps 0.1 :gamma 0.1
                                          :mini-batch? false :lock-gamma-beta? true
                                          :activation-fn :relu
                                          :adam-mean-decay 0.2 :adam-var-decay 0.1
                                          :bias-init 0.7 :bias-learning-rate 0.1
                                          :dist {:normal {:mean 0 :std 1}}
                                          :drop-out 0.2 :epsilon 0.3
                                          :gradient-normalization :none
                                          :gradient-normalization-threshold 0.9
                                          :l1 0.2 :l2 0.7 :layer-name "foo"
                                          :learning-rate 0.1 :learning-rate-policy :inverse
                                          :l1-bias 0.1 :l2-bias 0.2
                                          :learning-rate-schedule {0 0.2 1 0.5}
                                          :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                          :rho 0.7 :rms-decay 0.7 :updater :adam
                                          :weight-init :distribution)
          convolutional-layer-conf (convolutional-layer-builder
                                    :n-in 10 :n-out 2
                                    :kernel-size [2 2] :padding [2 2] :stride [2 2]
                                    :activation-fn :relu
                                    :adam-mean-decay 0.2 :adam-var-decay 0.1
                                    :bias-init 0.7 :bias-learning-rate 0.1
                                    :dist {:normal {:mean 0 :std 1}}
                                    :drop-out 0.2 :epsilon 0.3
                                    :gradient-normalization :none
                                    :gradient-normalization-threshold 0.9
                                    :l1 0.2 :l2 0.7 :layer-name "foo"
                                    :learning-rate 0.1 :learning-rate-policy :inverse
                                    :l1-bias 0.1 :l2-bias 0.2
                                    :learning-rate-schedule {0 0.2 1 0.5}
                                    :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                    :rho 0.7 :rms-decay 0.7 :updater :adam
                                    :weight-init :distribution)
          convolutional-1d-layer-conf (convolution-1d-layer-builder
                                       :n-in 10 :n-out 2
                                       :kernel-size 6 :stride 3 :padding 3
                                       :activation-fn :relu
                                       :adam-mean-decay 0.2 :adam-var-decay 0.1
                                       :bias-init 0.7 :bias-learning-rate 0.1
                                       :dist {:normal {:mean 0 :std 1}}
                                       :drop-out 0.2 :epsilon 0.3
                                       :gradient-normalization :none
                                       :gradient-normalization-threshold 0.9
                                       :l1 0.2 :l2 0.7 :layer-name "foo"
                                       :learning-rate 0.1 :learning-rate-policy :inverse
                                       :l1-bias 0.1 :l2-bias 0.2
                                       :learning-rate-schedule {0 0.2 1 0.5}
                                       :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                       :rho 0.7 :rms-decay 0.7 :updater :adam
                                       :weight-init :distribution)
          dense-layer-conf (dense-layer-builder
                            :n-in 10 :n-out 2
                            :activation-fn :relu
                            :adam-mean-decay 0.2 :adam-var-decay 0.1
                            :bias-init 0.7 :bias-learning-rate 0.1
                            :dist {:normal {:mean 0 :std 1}}
                            :drop-out 0.2 :epsilon 0.3
                            :gradient-normalization :none
                            :gradient-normalization-threshold 0.9
                            :l1 0.2 :l2 0.7 :layer-name "foo"
                            :learning-rate 0.1 :learning-rate-policy :inverse
                            :l1-bias 0.1 :l2-bias 0.2
                            :learning-rate-schedule {0 0.2 1 0.5}
                            :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                            :rho 0.7 :rms-decay 0.7 :updater :adam
                            :weight-init :distribution)
          embedding-layer-conf (embedding-layer-builder
                                :n-in 10 :n-out 2
                                :activation-fn :relu
                                :adam-mean-decay 0.2 :adam-var-decay 0.1
                                :bias-init 0.7 :bias-learning-rate 0.1
                                :dist {:normal {:mean 0 :std 1}}
                                :drop-out 0.2 :epsilon 0.3
                                :gradient-normalization :none
                                :gradient-normalization-threshold 0.9
                                :l1 0.2 :l2 0.7 :layer-name "foo"
                                :learning-rate 0.1 :learning-rate-policy :inverse
                                :l1-bias 0.1 :l2-bias 0.2
                                :learning-rate-schedule {0 0.2 1 0.5}
                                :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                :rho 0.7 :rms-decay 0.7 :updater :adam
                                :weight-init :distribution)
          local-response-normalization-conf (local-response-normalization-layer-builder
                                             :alpha 0.2 :beta 0.2 :k 0.2 :n 1
                                             :activation-fn :relu
                                             :adam-mean-decay 0.2 :adam-var-decay 0.1
                                             :bias-init 0.7 :bias-learning-rate 0.1
                                             :dist {:normal {:mean 0 :std 1}}
                                             :drop-out 0.2 :epsilon 0.3
                                             :gradient-normalization :none
                                             :gradient-normalization-threshold 0.9
                                             :l1 0.2 :l2 0.7 :layer-name "foo"
                                             :learning-rate 0.1 :learning-rate-policy :inverse
                                             :l1-bias 0.1 :l2-bias 0.2
                                             :learning-rate-schedule {0 0.2 1 0.5}
                                             :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                             :rho 0.7 :rms-decay 0.7 :updater :adam
                                             :weight-init :distribution)
          subsampling-layer-conf (subsampling-layer-builder
                                  :kernel-size [2 2] :stride [2 2] :padding [2 2]
                                  :pooling-type :sum
                                  :build? true
                                  :activation-fn :relu
                                  :adam-mean-decay 0.2 :adam-var-decay 0.1
                                  :bias-init 0.7 :bias-learning-rate 0.1
                                  :dist {:normal {:mean 0 :std 1}}
                                  :drop-out 0.2 :epsilon 0.3
                                  :gradient-normalization :none
                                  :gradient-normalization-threshold 0.9
                                  :l1 0.2 :l2 0.7 :layer-name "foo"
                                  :learning-rate 0.1 :learning-rate-policy :inverse
                                  :l1-bias 0.1 :l2-bias 0.2
                                  :learning-rate-schedule {0 0.2 1 0.5}
                                  :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                  :rho 0.7 :rms-decay 0.7 :updater :adam
                                  :weight-init :distribution)
          subsampling-1d-layer-conf (subsampling-1d-layer-builder
                                     :kernel-size 2 :stride 2 :padding 2
                                     :pooling-type :sum
                                     :build? true
                                     :activation-fn :relu
                                     :adam-mean-decay 0.2 :adam-var-decay 0.1
                                     :bias-init 0.7 :bias-learning-rate 0.1
                                     :dist {:normal {:mean 0 :std 1}}
                                     :drop-out 0.2 :epsilon 0.3
                                     :gradient-normalization :none
                                     :gradient-normalization-threshold 0.9
                                     :l1 0.2 :l2 0.7 :layer-name "foo"
                                     :learning-rate 0.1 :learning-rate-policy :inverse
                                     :l1-bias 0.1 :l2-bias 0.2
                                     :learning-rate-schedule {0 0.2 1 0.5}
                                     :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                     :rho 0.7 :rms-decay 0.7 :updater :adam
                                     :weight-init :distribution)
          loss-layer-conf (loss-layer-builder
                           :loss-fn :mse
                           :activation-fn :relu
                           :adam-mean-decay 0.2 :adam-var-decay 0.1
                           :bias-init 0.7 :bias-learning-rate 0.1
                           :dist {:normal {:mean 0 :std 1}}
                           :drop-out 0.2 :epsilon 0.3
                           :gradient-normalization :none
                           :gradient-normalization-threshold 0.9
                           :l1 0.2 :l2 0.7 :layer-name "foo"
                           :learning-rate 0.1 :learning-rate-policy :inverse
                           :l1-bias 0.1 :l2-bias 0.2
                           :learning-rate-schedule {0 0.2 1 0.5}
                           :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                           :rho 0.7 :rms-decay 0.7 :updater :adam
                           :weight-init :distribution)
          dropout-layer-conf (dropout-layer-builder
                              :n-in 2 :n-out 10
                              :activation-fn :relu
                              :adam-mean-decay 0.2 :adam-var-decay 0.1
                              :bias-init 0.7 :bias-learning-rate 0.1
                              :dist {:normal {:mean 0 :std 1}}
                              :drop-out 0.2 :epsilon 0.3
                              :gradient-normalization :none
                              :gradient-normalization-threshold 0.9
                              :l1 0.2 :l2 0.7 :layer-name "foo"
                              :learning-rate 0.1 :learning-rate-policy :inverse
                              :l1-bias 0.1 :l2-bias 0.2
                              :learning-rate-schedule {0 0.2 1 0.5}
                              :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                              :rho 0.7 :rms-decay 0.7 :updater :adam
                              :weight-init :distribution)
          global-pooling-layer-conf (global-pooling-layer-builder
                                     :pooling-dimensions [3 2]
                                     :collapse-dimensions? true
                                     :pnorm 2
                                     :pooling-type :pnorm
                                     :activation-fn :relu
                                     :adam-mean-decay 0.2 :adam-var-decay 0.1
                                     :bias-init 0.7 :bias-learning-rate 0.1
                                     :dist {:normal {:mean 0 :std 1}}
                                     :drop-out 0.2 :epsilon 0.3
                                     :gradient-normalization :none
                                     :gradient-normalization-threshold 0.9
                                     :l1 0.2 :l2 0.7 :layer-name "foo"
                                     :learning-rate 0.1 :learning-rate-policy :inverse
                                     :l1-bias 0.1 :l2-bias 0.2
                                     :learning-rate-schedule {0 0.2 1 0.5}
                                     :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                     :rho 0.7 :rms-decay 0.7 :updater :adam
                                     :weight-init :distribution)
          zero-padding-layer-conf (zero-padding-layer-builder
                                   :pad-top 1 :pad-bot 2 :pad-left 3 :pad-right 4
                                   :activation-fn :relu
                                   :adam-mean-decay 0.2 :adam-var-decay 0.1
                                   :bias-init 0.7 :bias-learning-rate 0.1
                                   :dist {:normal {:mean 0 :std 1}}
                                   :drop-out 0.2 :epsilon 0.3
                                   :gradient-normalization :none
                                   :gradient-normalization-threshold 0.9
                                   :l1 0.2 :l2 0.7 :layer-name "foo"
                                   :learning-rate 0.1 :learning-rate-policy :inverse
                                   :l1-bias 0.1 :l2-bias 0.2
                                   :learning-rate-schedule {0 0.2 1 0.5}
                                   :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                                   :rho 0.7 :rms-decay 0.7 :updater :adam
                                   :weight-init :distribution)
          vae-layer-conf (variational-autoencoder-builder
                          :n-in 5 :n-out 10 :loss-fn :mse
                          :pre-train-iterations 1 :visible-bias-init 2
                          :decoder-layer-sizes [5 9]
                          :encoder-layer-sizes [7 2]
                          :reconstruction-distribution {:gaussian {:activation-fn :tanh}}
                          :vae-loss-fn {:output-activation-fn :tanh :loss-fn :mse}
                          :num-samples 2 :pzx-activation-function :tanh
                          :activation-fn :relu
                          :adam-mean-decay 0.2 :adam-var-decay 0.1
                          :bias-init 0.7 :bias-learning-rate 0.1
                          :dist {:normal {:mean 0 :std 1}}
                          :drop-out 0.2 :epsilon 0.3
                          :gradient-normalization :none
                          :gradient-normalization-threshold 0.9
                          :l1 0.2 :l2 0.7 :layer-name "foo"
                          :learning-rate 0.1 :learning-rate-policy :inverse
                          :l1-bias 0.1 :l2-bias 0.2
                          :learning-rate-schedule {0 0.2 1 0.5}
                          :momentum 0.2 :momentum-after {0 0.3 1 0.4}
                          :rho 0.7 :rms-decay 0.7 :updater :adam
                          :weight-init :distribution)]
      ;; activation layer
      (is (= org.deeplearning4j.nn.conf.layers.ActivationLayer
             (type activation-layer-conf)))
      (is (= :activation (layer-type {:nn-conf (quick-nn-conf activation-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.ActivationLayer
             (type (new-layer :nn-conf (quick-nn-conf activation-layer-conf)))))

      ;; center loss layer
      (is (= org.deeplearning4j.nn.conf.layers.CenterLossOutputLayer
             (type center-loss-output-layer-conf)))
      (is (= :center-loss-output-layer (layer-type {:nn-conf (quick-nn-conf center-loss-output-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.training.CenterLossOutputLayer
             (type (new-layer :nn-conf (quick-nn-conf center-loss-output-layer-conf)))))

      ;; output layer
      (is (= org.deeplearning4j.nn.conf.layers.OutputLayer
             (type output-layer-conf)))
      (is (= :output (layer-type {:nn-conf (quick-nn-conf output-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.OutputLayer
             (type (new-layer :nn-conf (quick-nn-conf output-layer-conf)))))

      ;; rnn output layer
      (is (= org.deeplearning4j.nn.conf.layers.RnnOutputLayer
             (type rnn-output-layer-conf)))
      (is (= :rnnoutput (layer-type {:nn-conf (quick-nn-conf rnn-output-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.recurrent.RnnOutputLayer
             (type (new-layer :nn-conf (quick-nn-conf rnn-output-layer-conf)))))

      ;; atuoencoders
      (is (= org.deeplearning4j.nn.conf.layers.AutoEncoder
             (type autoencoder-layer-conf)))
      (is (= :auto-encoder (layer-type {:nn-conf (quick-nn-conf autoencoder-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.feedforward.autoencoder.AutoEncoder
             (type (new-layer :nn-conf (quick-nn-conf autoencoder-layer-conf)))))

      ;; rbm
      (is (= org.deeplearning4j.nn.conf.layers.RBM (type rbm-layer-conf)))
      (is (= :rbm (layer-type {:nn-conf (quick-nn-conf rbm-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.feedforward.rbm.RBM
             (type (new-layer :nn-conf (quick-nn-conf rbm-layer-conf)))))

      ;; graves bidirectional lstm
      (is (= org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM
             (type graves-bidirectional-lstm-conf)))
      (is (= :graves-bidirectional-lstm (layer-type {:nn-conf (quick-nn-conf graves-bidirectional-lstm-conf)})))
      (is (= org.deeplearning4j.nn.layers.recurrent.GravesBidirectionalLSTM
             (type (new-layer :nn-conf (quick-nn-conf graves-bidirectional-lstm-conf)))))

      ;; graves lstm
      (is (= org.deeplearning4j.nn.conf.layers.GravesLSTM
             (type graves-lstm-layer-conf)))
      (is (= :graves-lstm (layer-type {:nn-conf (quick-nn-conf graves-lstm-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.recurrent.GravesLSTM
             (type (new-layer :nn-conf (quick-nn-conf graves-lstm-layer-conf)))))

      ;; batch normalization
      (is (= org.deeplearning4j.nn.conf.layers.BatchNormalization
             (type batch-normalization-layer-conf)))
      (is (= :batch-normalization (layer-type {:nn-conf (quick-nn-conf batch-normalization-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.normalization.BatchNormalization
             (type (new-layer :nn-conf (quick-nn-conf batch-normalization-layer-conf)))))

      ;; convolution
      (is (= org.deeplearning4j.nn.conf.layers.ConvolutionLayer
             (type convolutional-layer-conf)))
      (is (= :convolution (layer-type {:nn-conf (quick-nn-conf convolutional-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.convolution.ConvolutionLayer
             (type (new-layer :nn-conf (quick-nn-conf convolutional-layer-conf)))))

      ;; convolution1d
      (is (= org.deeplearning4j.nn.conf.layers.Convolution1DLayer
             (type convolutional-1d-layer-conf)))
      (is (= :convolution1d (layer-type {:nn-conf (quick-nn-conf convolutional-1d-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.convolution.Convolution1DLayer
             (type (new-layer :nn-conf (quick-nn-conf convolutional-1d-layer-conf)))))

      ;; dense
      (is (= org.deeplearning4j.nn.conf.layers.DenseLayer
             (type dense-layer-conf)))
      (is (= :dense (layer-type {:nn-conf (quick-nn-conf dense-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.feedforward.dense.DenseLayer
             (type (new-layer :nn-conf (quick-nn-conf dense-layer-conf)))))

      ;; embedding
      (is (= org.deeplearning4j.nn.conf.layers.EmbeddingLayer
             (type embedding-layer-conf)))
      (is (= :embedding (layer-type {:nn-conf (quick-nn-conf embedding-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.feedforward.embedding.EmbeddingLayer
             (type (new-layer :nn-conf (quick-nn-conf embedding-layer-conf)))))

      ;; local response normalization
      (is (= org.deeplearning4j.nn.conf.layers.LocalResponseNormalization
             (type local-response-normalization-conf)))
      (is (= :local-response-normalization (layer-type {:nn-conf (quick-nn-conf local-response-normalization-conf)})))
      (is (= org.deeplearning4j.nn.layers.normalization.LocalResponseNormalization
             (type (new-layer :nn-conf (quick-nn-conf local-response-normalization-conf)))))

      ;; subsampling
      (is (= org.deeplearning4j.nn.conf.layers.SubsamplingLayer
             (type subsampling-layer-conf)))
      (is (= :subsampling (layer-type {:nn-conf (quick-nn-conf subsampling-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.convolution.subsampling.SubsamplingLayer
             (type (new-layer :nn-conf (quick-nn-conf subsampling-layer-conf)))))

      ;; subsampling1d
      (is (= org.deeplearning4j.nn.conf.layers.Subsampling1DLayer
             (type subsampling-1d-layer-conf)))
      (is (= :subsampling1d (layer-type {:nn-conf (quick-nn-conf subsampling-1d-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.convolution.subsampling.Subsampling1DLayer
             (type (new-layer :nn-conf (quick-nn-conf subsampling-1d-layer-conf)))))

      ;; loss layer
      (is (= org.deeplearning4j.nn.conf.layers.LossLayer
             (type loss-layer-conf)))
      (is (= :loss (layer-type {:nn-conf (quick-nn-conf loss-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.LossLayer
             (type (new-layer :nn-conf (quick-nn-conf loss-layer-conf)))))

      ;; dropout
      (is (= org.deeplearning4j.nn.conf.layers.DropoutLayer
             (type dropout-layer-conf)))
      (is (= :dropout (layer-type {:nn-conf (quick-nn-conf dropout-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.DropoutLayer
             (type (new-layer :nn-conf (quick-nn-conf dropout-layer-conf)))))

      ;; global pooling
      (is (= org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer
             (type global-pooling-layer-conf)))
      (is (= :global-pooling (layer-type {:nn-conf (quick-nn-conf global-pooling-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.pooling.GlobalPoolingLayer
             (type (new-layer :nn-conf (quick-nn-conf global-pooling-layer-conf)))))

      ;; zero padding
      (is (= org.deeplearning4j.nn.conf.layers.ZeroPaddingLayer
             (type zero-padding-layer-conf)))
      (is (= :zero-padding (layer-type {:nn-conf (quick-nn-conf zero-padding-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.convolution.ZeroPaddingLayer
             (type (new-layer :nn-conf (quick-nn-conf zero-padding-layer-conf)))))

      ;; vae
      (is (= org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder
             (type vae-layer-conf)))
      (is (= :variational-autoencoder (layer-type {:nn-conf (quick-nn-conf vae-layer-conf)})))
      (is (= org.deeplearning4j.nn.layers.variational.VariationalAutoencoder
             (type (new-layer :nn-conf (quick-nn-conf vae-layer-conf)))))

      ;; forward pass
      (is (= org.deeplearning4j.nn.layers.recurrent.FwdPassReturn (type (new-foward-pass-return))))

      ;; frozen layer
      (is (= org.deeplearning4j.nn.layers.FrozenLayer
             (type (new-frozen-layer
                    :layer (set-param-table!
                            :model (new-layer
                                    :nn-conf (quick-nn-conf activation-layer-conf))
                            :param-table-map {"foo" (zeros [1])}))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; nn-conf-builder
;; dl4clj.nn.conf.builders.nn-conf-builder
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest nn-conf-test
  (testing "the creation of neural network configurations"
    (is (= org.deeplearning4j.nn.conf.NeuralNetConfiguration
           (type
            (nn-conf-builder
             :iterations 1
             :lr-policy-decay-rate 0.3
             :lr-policy-power 0.4
             :learning-rate-policy :poly
             :max-num-line-search-iterations 6
             :mini-batch? true
             :minimize? true
             :use-drop-connect? true
             :optimization-algo :lbfgs
             :lr-score-based-decay-rate 0.7
             :regularization? true
             :seed 123
             :step-fn (new-gradient-step-fn)
             :convolution-mode :strict
             :build? true))))
    (is (= org.deeplearning4j.nn.conf.NeuralNetConfiguration$Builder
           (type
            (nn-conf-builder
             :iterations 1
             :lr-policy-decay-rate 0.3
             :lr-policy-power 0.4
             :learning-rate-policy :poly
             :max-num-line-search-iterations 6
             :mini-batch? true
             :minimize? true
             :use-drop-connect? true
             :optimization-algo :lbfgs
             :lr-score-based-decay-rate 0.7
             :regularization? true
             :seed 123
             :step-fn :default-step-fn
             :convolution-mode :strict
             :build? false))))
    (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
           (type
               (nn-conf-builder :global-activation-fn :relu
                                :step-fn :negative-gradient-step-fn
                                :updater :none
                                :use-drop-connect true
                                :drop-out 0.2
                                :weight-init :xavier-uniform
                                :build? true
                                :gradient-normalization :renormalize-l2-per-layer
                                :layers {0 (dl4clj.nn.conf.builders.builders/dense-layer-builder
                                            :n-in 100
                                            :n-out 1000
                                            :layer-name "first layer"
                                            :activation-fn :tanh
                                            :gradient-normalization :none )
                                         1 {:dense-layer {:n-in 1000
                                                          :n-out 10
                                                          :layer-name "second layer"
                                                          :gradient-normalization :none}}}))))
    (is (= org.deeplearning4j.nn.conf.NeuralNetConfiguration$ListBuilder
           (type
               (nn-conf-builder :global-activation-fn :relu
                                :step-fn :negative-gradient-step-fn
                                :updater :none
                                :use-drop-connect true
                                :drop-out 0.2
                                :weight-init :xavier-uniform
                                :build? false
                                :gradient-normalization :renormalize-l2-per-layer
                                :layers {0 (dl4clj.nn.conf.builders.builders/dense-layer-builder
                                            :n-in 100
                                            :n-out 1000
                                            :layer-name "first layer"
                                            :activation-fn :tanh
                                            :gradient-normalization :none )
                                         1 {:dense-layer {:n-in 1000
                                                          :n-out 10
                                                          :layer-name "second layer"
                                                          :activation-fn :tanh
                                                          :gradient-normalization :none}}}))))
    (is (= org.deeplearning4j.nn.conf.NeuralNetConfiguration
           (type
               (nn-conf-builder :global-activation-fn :relu
                                :step-fn :negative-gradient-step-fn
                                :updater :none
                                :use-drop-connect true
                                :drop-out 0.2
                                :weight-init :xavier-uniform
                                :gradient-normalization :renormalize-l2-per-layer
                                :build? true
                                :layer (dl4clj.nn.conf.builders.builders/dense-layer-builder
                                          :n-in 100
                                          :n-out 1000
                                          :layer-name "first layer"
                                          :activation-fn :tanh
                                          :gradient-normalization :none)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; list-builder test
;; dl4clj.nn.conf.builders.multi-layer-builders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest list-builder-test
  (testing "the list builder for setting up the :layers key in nn-conf-builder"
    (is (= org.deeplearning4j.nn.conf.NeuralNetConfiguration$ListBuilder
           (type
            (list-builder
             (nn-conf-builder)
             {0 (dl4clj.nn.conf.builders.builders/dense-layer-builder
                 :n-in 100
                 :n-out 1000
                 :layer-name "first layer"
                 :activation-fn :tanh
                 :gradient-normalization :none)
              1 {:dense-layer {:n-in 1000
                               :n-out 10
                               :layer-name "second layer"
                               :activation-fn :tanh
                               :gradient-normalization :none}}}))))
    (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
           (type
            (.build
             (list-builder
              (nn-conf-builder)
              {0 (dl4clj.nn.conf.builders.builders/dense-layer-builder
                  :n-in 100
                  :n-out 1000
                  :layer-name "first layer"
                  :activation-fn :tanh
                  :gradient-normalization :none)
               1 {:dense-layer {:n-in 1000
                                :n-out 10
                                :layer-name "second layer"
                                :activation-fn :tanh
                                :gradient-normalization :none}}})))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; input pre-processors test
;; dl4clj.nn.conf.input-pre-processor
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest pre-processors-test
  (testing "the creation of input preprocessors for use in multi-layer-conf"
    (is (= org.deeplearning4j.nn.conf.preprocessor.BinomialSamplingPreProcessor
           (type (new-binominal-sampling-pre-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.UnitVarianceProcessor
           (type (new-unit-variance-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.RnnToCnnPreProcessor
           (type (new-rnn-to-cnn-pre-processor :input-height 1 :input-width 1
                                               :num-channels 1))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.ZeroMeanAndUnitVariancePreProcessor
           (type (new-zero-mean-and-unit-variance-pre-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.ZeroMeanPrePreProcessor
           (type (new-zero-mean-pre-pre-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor
           (type (new-cnn-to-feed-forward-pre-processor :input-height 1
                                                        :input-width 1
                                                        :num-channels 1))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.CnnToRnnPreProcessor
           (type (new-cnn-to-rnn-pre-processor :input-height 1 :input-width 1
                                               :num-channels 1))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnnPreProcessor
           (type (new-feed-forward-to-cnn-pre-processor :input-height 1
                                                        :input-width 1
                                                        :num-channels 1))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.RnnToFeedForwardPreProcessor
           (type (new-rnn-to-feed-forward-pre-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.FeedForwardToRnnPreProcessor
           (type (new-feed-forward-to-rnn-pre-processor))))
    (is (= org.deeplearning4j.nn.conf.preprocessor.ComposableInputPreProcessor
           (type (new-composable-input-pre-processor
                  :pre-processors [(new-zero-mean-pre-pre-processor)
                                   (new-binominal-sampling-pre-processor)]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; multi-layer-config-builder test
;; dl4clj.nn.conf.builders.multi-layer-builders
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest multi-layer-builder-test
  (testing "the creation of mutli layer nn's and setting top level params"
    (let [l-builder (nn-conf-builder :global-activation-fn :relu
                                     :step-fn :negative-gradient-step-fn
                                     :updater :none
                                     :use-drop-connect true
                                     :drop-out 0.2
                                     :weight-init :xavier-uniform
                                     :build? false
                                     :gradient-normalization :renormalize-l2-per-layer
                                     :layers {0 (dl4clj.nn.conf.builders.builders/dense-layer-builder
                                                 :n-in 100
                                                 :n-out 1000
                                                 :layer-name "first layer"
                                                 :activation-fn :tanh
                                                 :gradient-normalization :none)
                                              1 {:dense-layer {:n-in 1000
                                                               :n-out 10
                                                               :layer-name "second layer"
                                                               :activation-fn :tanh
                                                               :gradient-normalization :none}}})
          nn-conf (nn-conf-builder :global-activation-fn :relu
                                   :step-fn :negative-gradient-step-fn
                                   :updater :none
                                   :use-drop-connect true
                                   :drop-out 0.2
                                   :weight-init :xavier-uniform
                                   :gradient-normalization :renormalize-l2-per-layer
                                   :build? true
                                   :layer (dl4clj.nn.conf.builders.builders/dense-layer-builder
                                           :n-in 10
                                           :n-out 100
                                           :layer-name "another layer"
                                           :activation-fn :tanh
                                           :gradient-normalization :none))]
      ;; with a list builder, a built nn-conf, and all opts
      (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
             (type (multi-layer-config-builder
                    :list-builder l-builder
                    :nn-confs nn-conf
                    :backprop? true
                    :backprop-type :standard
                    :input-pre-processors {0 {:zero-mean-pre-pre-processor {}}
                                           1 (new-unit-variance-processor)}
                    :input-type {:feed-forward {:size 100}}
                    :pretrain? false))))
      ;; with no nn-confs and all other opts
      (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
             (type (multi-layer-config-builder
                    :list-builder l-builder
                    :backprop? true
                    :backprop-type :standard
                    :input-pre-processors {0 {:zero-mean-pre-pre-processor {}}
                                           1 (new-unit-variance-processor)}
                    :input-type {:feed-forward {:size 100}}
                    :pretrain? false))))
      ;; with no list-builder but nn-confs and all other opts
      (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
             (type (multi-layer-config-builder
                    :nn-confs [nn-conf nn-conf]
                    :backprop? true
                    :backprop-type :standard
                    :input-pre-processors {0 {:zero-mean-pre-pre-processor {}}
                                           1 (new-unit-variance-processor)}
                    :input-type {:feed-forward {:size 100}}
                    :pretrain? false))))
      ;; with a single nn-conf for nn-confs
      (is (= org.deeplearning4j.nn.conf.MultiLayerConfiguration
             (type (multi-layer-config-builder
                    :nn-confs nn-conf
                    :backprop? true
                    :backprop-type :standard
                    :input-pre-processors {0 {:zero-mean-pre-pre-processor {}}
                                           1 (new-unit-variance-processor)}
                    :input-type {:feed-forward {:size 100}}
                    :pretrain? false)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fine tuning/transfer learning
;; dl4clj.nn.transfer-learning.fine-tune-conf
;; dl4clj.nn.transfer-learning.helper
;; dl4clj.nn.transfer-learning.transfer-learning
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; dl4clj.nn.params.param-initializers
;; make sure you can call the constructors...

;; dl4clj.nn.updater.layer-updater
;; dl4clj.nn.updater.multi-layer-updater
;; dl4clj.nn.updater.updater-creator

;; dl4clj.nn.weights.weight-init-util


(comment

  (dl4clj.nn.conf.layers.shared-fns/instantiate
 :layer (dl4clj.nn.conf.builders.builders/activation-layer-builder
         :n-in 10
         :n-out 100
         :layer-name "another layer"
         :activation-fn :tanh
         :gradient-normalization :none)
 :conf
 (nn-conf-builder :global-activation-fn :relu
                  :step-fn :negative-gradient-step-fn
                  :updater :none
                  :use-drop-connect true
                  :drop-out 0.2
                  :weight-init :xavier-uniform
                  :gradient-normalization :renormalize-l2-per-layer
                  :build? true
                  :layer (dl4clj.nn.conf.builders.builders/activation-layer-builder
                          :n-in 10
                          :n-out 100
                          :layer-name "another layer"
                          :activation-fn :tanh
                          :gradient-normalization :none))
 :listeners (dl4clj.optimize.listeners.listeners/new-score-iteration-listener)
 #_(dl4clj.utils/array-of :data (dl4clj.optimize.listeners.listeners/new-score-iteration-listener)
                                   :java-type org.deeplearning4j.optimize.api.IterationListener)
 :layer-idx 0
 :layer-param-view (nd4clj.linalg.factory.nd4j/rand [10])
 :initialize-params? true))