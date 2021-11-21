(ns kraken-api.clj-kraken
  (:require [kraken-api.cli :as cli]
            [kraken-api.requests :as req])
  (:gen-class))


(def api-key (System/getenv "KRAKEN_API_KEY"))
(def api-sec (System/getenv "KRAKEN_API_SEC"))

(defn api-keys-loaded?
  []
  (and api-key api-sec))

;; TODO: implement validation for options (should be in the format x=y)
;; TODO: pass the options correctly to kraken-api-request

(defn release-kraken [endpoint options]
  (print (req/kraken-api-request endpoint options)))

(defn -main [& args]
  (let [{:keys [endpoint options exit-message ok?]} (cli/validate-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      (release-kraken endpoint options))))
