(ns kraken-api.clj-kraken
  (:require [kraken-api.cli :as cli]
            [kraken-api.requests :as req])
  (:gen-class))

;; TODO
;; [X] write tests
;; [X] try running tests
;; [X] try building uberjar; test its usage; add docs to readme
;; [ ] ask ppl about better way of detecting nils in a list
;; [ ] ask ppl why uberjar doesnt print anything

(defn release-kraken [endpoint endpoint-args]
  (print (req/kraken-api-request endpoint endpoint-args)))

(defn -main [& args]
  (let [{:keys [endpoint endpoint-args exit-message ok?]} (cli/validate-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      (release-kraken endpoint endpoint-args))))
