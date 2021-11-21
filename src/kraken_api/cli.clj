(ns kraken-api.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.set :as s]
            [kraken-api.requests :as req]))


(def cli-options
  [["-h" "--help" "Print this documentation string."]])

(def implemented-endpoints
  (str "#{" 
       (string/join ", " (s/union req/public-endpoints 
                          req/private-endpoints)) 
       "}"))

(defn usage [options-summary]
  (->> ["CLI utility for accessing Kraken crypto exchange API."
        ""
        "Usage: clj -M -m kraken-api.clj-kraken [options] endpoint [endpoint-args]"
        ""
        "Options:"
        options-summary
        ""
        "Endpoints:"
        implemented-endpoints
        ""
        "Endpoint args:"
        "All endpoint args need to follow x=y pattern."
        "Please refer to https://docs.kraken.com/rest/ for the full list."
        ""
        "Please do not forget to set KRAKEN_API_KEY and KRAKEN_API_SEC vars!"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; check if all of the params are in the correct format
      (not (req/valid-params? (rest arguments)))
      {:exit-message "Error: params should be of the form param=value."}
      ;; custom validation on arguments
      (and (<= 1 (count arguments))
           (req/valid-endpoint? (first arguments)))
      {:endpoint (first arguments) :endpoint-args (rest arguments)}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))
