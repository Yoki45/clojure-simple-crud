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

