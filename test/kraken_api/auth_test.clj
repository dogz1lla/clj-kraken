(ns kraken-api.auth-test
  (:require [clojure.test :as t]
            [kraken-api.auth :as auth]))


(t/deftest test-auth

  (t/testing "test encode-slash"
    (t/testing "test 1"
      (let [s "test"]
        (t/is (= (auth/encode-slash s) s))))
    (t/testing "test 2"
      (let [s "test/"]
        (t/is (= (auth/encode-slash s) "test%2F")))))

  (t/testing "test encode-space"
    (t/testing "test 1"
      (let [s "test"]
        (t/is (= (auth/encode-space s) s))))
    (t/testing "test 2"
      (let [s "test "]
        (t/is (= (auth/encode-space s) "test+")))))

  (t/testing "test urlencode"
    (t/testing "test 1"
      (let [params-list (list "hi=bye" "yes=no")]
        (t/is (= (auth/urlencode params-list) "hi=bye&yes=no"))))
    (t/testing "test 2"
      (let [params-list (list "hi=bye/" "yes=no")]
        (t/is (= (auth/urlencode params-list) "hi=bye%2F&yes=no"))))
    (t/testing "test 3"
      (let [params-list (list)]
        (t/is (= (auth/urlencode params-list) "")))))

  (t/testing "test aug-postdata"
    (t/testing "test 1"
      (let [params-list (list "nonce=123" "hi=bye" "yes=no")]
        (t/is (= (auth/aug-postdata params-list) "123nonce=123&hi=bye&yes=no"))))

  (t/testing "test kraken-signature"
    (t/testing "test from kraken website"
      (let [url "/0/private/AddOrder"
            api-sec "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg=="
            test-data (list "nonce=1616492376594" 
                            "ordertype=limit" 
                            "pair=XBTUSD"
                            "price=37500" 
                            "type=buy"
                            "volume=1.25")]
        (t/is (= (auth/kraken-signature url test-data api-sec) "4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==")))))))

