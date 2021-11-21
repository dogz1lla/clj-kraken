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

(def public-endpoints #{"Time" "SystemStatus"})
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

;; 4. need to change this function to return a list of strings x=y instead
;; 5. need to a add a checker that all the elements in the options list are of
;;    the kind "x=y"
(defn post-request-payload
  [& params]
  (if params
    (assoc params "nonce" (get-nonce))
    {"nonce" (get-nonce)}))

(defn parse-response-body [response] 
  (json/read-str (:body response)))

;; 6. here can perhaps coerce both methods into one post (need to check with get and 
;;    options)
;; TODO actually all of the endpoints except public ones are post;
;;      all public endpoints are GET; need to check public endpoint that 
;;      requires params eg /OHLC
(defn kraken-get [uri]
  (client/get (str url uri) (make-request-params uri {})))

(defn kraken-post [uri payload]
  (client/post (str url uri) (make-request-params uri payload)))

;; 1. external call to this func
;; 2. should make params NOT an optional, what is passed is a list of strings 
;;    (possibly empty)
;; 3. params is then passed to post-request-payload
(defn kraken-api-request 
  [endpoint & params]
  (parse-response-body 
    (cond 
      (public-endpoints endpoint) (kraken-get (str public-uri-prefix endpoint))
      (private-endpoints endpoint) 
      (let [uri (str private-uri-prefix endpoint)
            payload (if params (post-request-payload params) (post-request-payload))] 
        (kraken-post uri payload))
      :else  {:body (json/write-str {"error" ["Unknown endpoint!"]})})))

(comment
  ; (server-time)
  ; (system-status)
  ; (asset-info "USD" "currency")
  ; (balance)
  ; (trade-balance "XXMR")
  ; (open-orders)
  ; (closed-orders)
  (kraken-api-request "Time")
  (kraken-api-request "TradeBalance")
  (kraken-api-request "Balance")
  (kraken-api-request "WrongEndpoint")
  )

