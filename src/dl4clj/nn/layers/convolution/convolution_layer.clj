(ns dl4clj.nn.layers.convolution.convolution-layer
  (:import [org.deeplearning4j.nn.layers.convolution ConvolutionLayer])
  (:require [dl4clj.nn.api.layer :refer :all]
            [dl4clj.nn.api.model :refer :all]
            [dl4clj.nn.conf.constants :as enum]
            [dl4clj.nn.layers.base-layer :refer [calc-activation-mean
                                                 calc-gradient
                                                 derivative-activation
                                                 calc-error
                                                 get-input
                                                 init-params!
                                                 merge-layers!
                                                 validate-input!]]))

(defn new-convolution-layer
  "creates a new convolution-layer given a neural net conf and
  optionally some input data"
  [& {:keys [conf input]
      :as opts}]
  (assert (contains? opts :conf) "you must supply a neural network configuration")
  (if (contains? opts :input)
    (ConvolutionLayer. conf input)
    (ConvolutionLayer. conf)))