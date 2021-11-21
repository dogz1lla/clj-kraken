(ns kraken-api.utils)

(defn get-nonce 
  "Return number of milliseconds since UNIX epoch as int."
  []
  (System/currentTimeMillis))

