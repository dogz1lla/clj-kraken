(ns kraken-api.requests
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [kraken-api.auth :as auth]))


;; [X] Extract API key and secret
;; [X] Extract endpoint name and params
;; [ ] Create a url encoded payload out of params
;; [X] Make a get/post request based on the endpoint name
;; [X] Parse request response
;; [ ] Rewrite the requests logic using strings of params of the kind x=y

(def url "https://api.kraken.com")
(def public-uri-prefix "/0/public/")
(def private-uri-prefix "/0/private/")
(def api-key (System/getenv "KRAKEN_API_KEY"))
(def api-sec (System/getenv "KRAKEN_API_SEC"))

; ;; all endpoints
; (def public-endpoints #{"Time" "SystemStatus" "Assets" "AssetPairs" 
;                         "Ticker" "OHLC" "Depth" "Trades" "Spread"})
; (def private-endpoints #{"Balance" "TradeBalance" "OpenOrders" "ClosedOrders"
;                          "QueryOrders" "TradesHistory" "QueryTrades"
;                          "OpenPositions" "Ledgers" "QueryLedgers" 
;                          "TradeVolume"})

(def public-endpoints #{"Time" "SystemStatus" "OHLC" "Assets"})
(def private-endpoints #{"Balance"})

(defn valid-endpoint? [endpoint]
  (or (public-endpoints endpoint) (private-endpoints endpoint)))

(defn get-nonce 
  "Return number of milliseconds since UNIX epoch as int."
  []
  (System/currentTimeMillis))

(defn make-headers [api-key uri payload api-sec]
  {"API-Key" api-key
   "API-Sign" (auth/kraken-signature uri payload api-sec)})

;; 7. finally the params list is passed here
;; TODO the problem with the current map/string thing is inside of the :body
(defn make-request-params [uri payload]
  {:body (auth/urlencode payload) 
   :headers (make-headers api-key uri payload api-sec)
   :content-type :x-www-form-urlencoded
   :content-type-params {:charset "UTF-8"}})

(defn params-valid? [params-list]
  (if (some nil? (map (fn [s] (re-matches #"[a-z,A-Z]+=.+" s)) params-list))
    false
    true))

(defn get-request-payload
  [params-list]
  (if (seq params-list) (auth/urlencode params-list) ""))

;; 4. [X] need to change this function to return a list of strings x=y instead
;; 5. [X] need to a add a checker that all the elements in the options list are of
;;    the kind "x=y"
;; TODO change the name of the function
(defn post-request-payload
  [params-list]
  (let [nonce (str "nonce=" (get-nonce))] 
    (if (seq params-list)
      (conj params-list nonce)
      (list nonce))))

(defn parse-response-body [response] 
  (json/read-str (:body response)))

;; 6. [X] here can perhaps coerce both methods into one post (need to check 
;;    with get and options)
;; DONE actually all of the endpoints except public ones are post;
;;      all public endpoints are GET; need to check public endpoint that 
;;      requires params eg /OHLC
(defn kraken-request [endpoint params-list]
  (if (public-endpoints endpoint)
      (let [uri (str public-uri-prefix endpoint)
            address (str url uri)
            params (get-request-payload params-list)]
        (client/get (str address "?" params)))
      (let [uri (str private-uri-prefix endpoint)
            address (str url uri)
            params (post-request-payload params-list)
            payload (make-request-params uri params)]
        (client/post address payload))))

; (defn kraken-get [uri]
;   (client/get (str url uri) (make-request-params uri {})))

; (defn kraken-post [uri payload]
;   (client/post (str url uri) (make-request-params uri payload)))

;; 1. [ ] external call to this func
;; 2. [X] should make params NOT an optional, what is passed is a list of strings 
;;    (possibly empty)
;; 3. [X] params is then passed to post-request-payload
(defn kraken-api-request 
  "endpoint is a string, params is a list of strings"
  [endpoint params]
  (parse-response-body (kraken-request endpoint params)))

(comment
  ; (server-time)
  ; (system-status)
  ; (asset-info "USD" "currency")
  ; (balance)
  ; (trade-balance "XXMR")
  ; (open-orders)
  ; (closed-orders)
  (kraken-api-request "Time" '())
  (kraken-api-request "OHLC" '("pair=XBTUSD"))
  (kraken-api-request "Assets" '())
  (kraken-api-request "TradeBalance" '())
  (kraken-api-request "Balance" '())
  (kraken-api-request "WrongEndpoint" '())
  (post-request-payload '("hi=bye"))
  (params-valid? '("H1=bye"))
  (params-valid? '("Hi=bye"))
  )

