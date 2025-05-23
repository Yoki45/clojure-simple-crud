(ns loan-clojure-api.loans.service
  (:require [loan-clojure-api.db.db :as db]
            [clojure.tools.logging :as log]))


(defn valid-loan? [loan]
  (and (string? (:user-id loan))
       (number? (:amount loan))))

(defn create-loan! [loan]
  (log/info "Loan request received:" loan)
  (if-not (valid-loan? loan)
    {:status 400
     :body {:error "Invalid loan payload"}}

    (try
      (let [saved (db/save-loan! loan)]
        (log/info "Saved loan:"saved)
        {:status 201
         :body {:message "Loan created"
                :body saved}})

      (catch Exception e
        (log/error (str "Failed to save loan to database: " (.getMessage e)))
        {:status 500
         :body {:error "Something went wrong while saving loan"}}))))


(defn fetch-loans
  [{:keys [user-id amount limit offset]}]
  (let [limit  (or (some-> limit Integer/parseInt) 10)
        offset (or (some-> offset Integer/parseInt) 0)
        filters {:user-id user-id :amount amount :limit limit :offset offset}]
    (try
      (let [loans (db/get-loans filters)
            total (db/count-loans filters)
            total-pages (int (Math/ceil (/ (double total) limit)))
            current-page (inc (int (/ offset limit)))
            pagination {:limit limit
                        :offset offset
                        :total total
                        :has-next (< (+ offset limit) total)
                        :has-prev (> offset 0)
                        :total-pages total-pages
                        :current-page current-page}]
        {:status 200 :body {:pagination pagination :loans loans}})
      (catch Exception e
        (log/error e "Failed to fetch paginated loans")
        {:status 500 :body {:error "Unable to fetch loans"}}))))



(defn update-loan "This method updates loan details"
  [id update-fields]
  (try
    (let [validated-fields
          (->> update-fields
               ;; Keep only fields we allow
               (filter (fn [[field _]]
                         (contains? #{:duration-months :amount} field)))

               ;; Validate each supported field
               (map (fn [[field value]]
                      (cond
                        ;; Duration must be a positive integer
                        (= field :duration-months)
                        (if (and (int? value) (pos? value))
                          [field value]
                          (throw (ex-info "Invalid duration-months"
                                          {:field field :value value})))

                        ;; Amount must be a positive number
                        (= field :amount)
                        (if (and (number? value) (pos? value))
                          [field value]
                          (throw (ex-info "Invalid amount"
                                          {:field field :value value})))

                        ;; Safety fallback — shouldn't happen due to filter
                        :else
                        (throw (ex-info "Unsupported field"
                                        {:field field})))))

               ;; Turn back into map
               (into {}))]

      ;; Don't attempt update with no valid data
      (if (empty? validated-fields)
        {:status 400 :body {:error "No valid fields provided"}}
        (if-let [updated-loan (db/update-loan! id validated-fields)]
          {:status 200 :body updated-loan}
          {:status 404 :body {:error "Loan not found"}})))

    ;; Handle validation failures (ex-info)
    (catch clojure.lang.ExceptionInfo e
      (log/error e "Validation error")
      {:status 400
       :body {:error (.getMessage e)
              :details (ex-data e)}})

    ;; Handle everything else
    (catch Exception e
      (log/error e "Unexpected error updating loan")
      {:status 500 :body {:error "Unexpected failure"}})))


(defn delete-loan
  [loan-id]
  (try
    (if-let [deleted (db/delete-loan! loan-id)]
      {:status 200
       :body {:message "Loan deleted successfully"
              :id (:id deleted)}}
      {:status 404
       :body {:error "Loan not found"}})
    (catch Exception e
      (log/error e "Failed to delete loan")
      {:status 500
       :body {:error "Unable to delete loan"}})))





