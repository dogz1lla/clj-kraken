(ns kraken-api.requests
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [kraken-api.utils :as utils]
            [kraken-api.auth :as auth]))


;; [X] Extract API key and secret
;; [X] Extract endpoint name and params
;; [X] Create a url encoded payload out of params
;; [X] Make a get/post request based on the endpoint name
;; [X] Parse request response
;; [X] Rewrite the requests logic using strings of params of the kind x=y

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

(defn valid-params? [params-list]
  (if (some nil? (map (fn [s] (re-matches #"[a-z,A-Z]+=.+" s)) params-list))
    false
    true))

(defn make-headers [api-key uri payload api-sec]
  {"API-Key" api-key
   "API-Sign" (auth/kraken-signature uri payload api-sec)})

(defn make-request-params [uri payload]
  {:body (auth/urlencode payload) 
   :headers (make-headers api-key uri payload api-sec)
   :content-type :x-www-form-urlencoded
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
  (json/read-str (:body response)))

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
  )

