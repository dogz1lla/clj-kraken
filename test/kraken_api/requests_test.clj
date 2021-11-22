(ns kraken-api.requests-test
  (:require [clojure.test :as t]
            [clojure.data.json :as json]
            [kraken-api.requests :as req]))


(t/deftest test-requests

  (t/testing "test valid-endpoint?"
    (t/testing "correct public endpoint"
      (let [endpoint "Time"]
        (t/is (= (req/valid-endpoint? endpoint) endpoint))))
    (t/testing "correct private endpoint"
      (let [endpoint "Balance"]
        (t/is (= (req/valid-endpoint? endpoint) endpoint))))
    (t/testing "incorrect endpoint"
      (let [endpoint "IncorrectEndpoint"]
        (t/is (= (req/valid-endpoint? endpoint) nil)))))

  (t/testing "test valid-params?"
    (t/testing "correct list"
      (let [params-list (list "hi=bye" "yes=no")]
        (t/is (= (req/valid-params? params-list) true))))
    (t/testing "incorrect list"
      (let [params-list (list "hi=bye" "yes=no" "wrong")]
        (t/is (= (req/valid-params? params-list) false))))
    (t/testing "empty list"
      (let [params-list (list)]
        (t/is (= (req/valid-params? params-list) true))))
    (t/testing "capital letters/numbers in value"
      (let [params-list (list "HI=BYE" "yes=1")]
        (t/is (= (req/valid-params? params-list) true))))
    (t/testing "non-alphabetic chars in param name"
      (let [params-list (list "HI2=BYE" "yes1=1")]
        (t/is (= (req/valid-params? params-list) false)))))

  (t/testing "test parse-response-body"
    (t/testing "arbitrary map with :body key"
      (let [body "body" 
            body-json (json/write-str body)
            response {:body body-json}]
        (t/is (= (req/parse-response-body response) body))))
    (t/testing "arbitrary map without :body key"
      (let [body (json/write-str "body") 
            response {:not-body body}]
        (t/is (= (req/parse-response-body response) 
                 {"error" ["Request failed: response doesnt contain :body key"]}))))))

