(ns kraken-api.auth
  (:require [clojure.string :as s])
  (:import java.security.MessageDigest
           javax.crypto.Mac
           javax.crypto.spec.SecretKeySpec
           java.util.Base64))


(defn key-value-str
  [[k v]]
  (str k "=" v))

(defn encode-slash
  [s]
  (s/replace s #"/" "%2F"))

(defn encode-space
  [s]
  (s/replace s #" " "+"))

(defn map-str
  [m]
  (map key-value-str m))

;; DONE rewrite such that query is a list of strings of type "x=y"
(defn urlencode
  "
  This func is supposed to do what urllib.parse.urlencode does in python.
  ``query`` is a list of strings of kind x=y.
  "
  [params-list]
  (->> params-list
       (s/join "&")
       (encode-space)
       (encode-slash)))

(defn aug-postdata
  [query]
  (let [urlencoded (urlencode query)
        nonce (get query "nonce")]
    (str nonce urlencoded)))

(defn add-bytes
  ;; see https://stackoverflow.com/a/26791567/3561086
  [b1 b2]
  (with-open [os (java.io.ByteArrayOutputStream.)]
    (.write os b1)
    (.write os b2)
    (.toByteArray os)))

(defn decode-base64
  [msg]
  (.decode (Base64/getDecoder) (.getBytes msg)))

(defn encode-base64
  [msg]
  (.encode (Base64/getEncoder) msg))

(defn get-key-object
  [secret-key]
  (SecretKeySpec. (decode-base64 secret-key) "DES"))

(defn hmac-digest
  [msg secret-key]
  (.doFinal 
    (doto (Mac/getInstance "HmacSHA512")
      (.init (get-key-object secret-key)))
    msg))

(defn bytes-to-str
  "https://lambdaisland.com/blog/2017-06-12-clojure-gotchas-surrogate-pairs"
  [arr]
  (String. (byte-array arr) "UTF8"))

(defn get-message
  [url data]
  (let [postdata (aug-postdata data)
        message-digester (MessageDigest/getInstance "SHA-256")
        postdata-bytes (.getBytes postdata)
        postdata-digest (.digest message-digester postdata-bytes)
        url-bytes (.getBytes url)]
    (add-bytes url-bytes postdata-digest)))

(defn kraken-signature 
  [url data api-sec]
  (let [message (get-message url data)]
    (->> api-sec
         (hmac-digest message)
         (encode-base64)
         (bytes-to-str))))

(comment 
  (urlencode  '("hi=1" "bye=2"))
  )
