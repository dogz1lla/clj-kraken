(ns kraken-api.requests
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [kraken-api.utils :as utils]
            [kraken-api.auth :as auth]))


(def url "https://api.kraken.com")
(def public-uri-prefix "/0/public/")
(def private-uri-prefix "/0/private/")
(def api-key (System/getenv "KRAKEN_API_KEY"))
(def api-sec (System/getenv "KRAKEN_API_SEC"))

(def public-endpoints #{"Time" "SystemStatus" "Assets" "AssetPairs" 
                        "Ticker" "OHLC" "Depth" "Trades" "Spread"})
(def private-endpoints #{"Balance"})

(defn valid-endpoint? [endpoint]
  (or (public-endpoints endpoint) (private-endpoints endpoint)))

(defn valid-params? 
  "
  Check if the params-list contains only strings of pattern x=y.
  Credits to u/p-himik from clojure subreddit for the more elegant solution.
  "
  [params-list]
  (every? #(re-matches #"[a-z,A-Z]+=.+" %) params-list))

(defn make-headers [api-key uri payload api-sec]
  {"API-Key" api-key
   "API-Sign" (auth/kraken-signature uri payload api-sec)})

(defn make-request-params [uri payload]
  {:body (auth/urlencode payload) 
   :content-type :x-www-form-urlencoded
   :headers (make-headers api-key uri payload api-sec)
   :content-type-params {:charset "UTF-8"}})

(defn GET-request-payload
  [params-list]
  (if (seq params-list) (auth/urlencode params-list) ""))

(defn POST-request-payload
  [params-list]
  (let [nonce (str "nonce=" (utils/get-nonce))] 
    (if (seq params-list)
      (conj params-list nonce)
      (list nonce))))

(defn kraken-request [endpoint params-list]
  (if (public-endpoints endpoint)
      (let [uri (str public-uri-prefix endpoint)
            address (str url uri)
            params (GET-request-payload params-list)]
        (client/get (str address "?" params)))
      (let [uri (str private-uri-prefix endpoint)
            address (str url uri)
            params (POST-request-payload params-list)
            payload (make-request-params uri params)]
        (client/post address payload))))

(defn parse-response-body [response] 
  (let [body (:body response)]
    (if body
      (json/read-str body)
      {"error" ["Request failed: response doesnt contain :body key"]})))

(defn kraken-api-request 
  "endpoint is a string, params is a list of strings"
  [endpoint params]
  (parse-response-body (kraken-request endpoint params)))

(comment
  (kraken-api-request "Time" '())
  (kraken-api-request "OHLC" '("pair=XBTUSD"))
  (kraken-api-request "Assets" '())
  (kraken-api-request "TradeBalance" '())
  (kraken-api-request "Balance" '())
  (kraken-api-request "WrongEndpoint" '())
  (POST-request-payload '("hi=bye"))
  (valid-params? '("H1=bye"))
  (valid-params? '("Hi=bye"))
  (valid-endpoint? "Time")
  )

