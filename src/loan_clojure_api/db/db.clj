(ns loan-clojure-api.db.db
  (:require [clojure.tools.logging :as log]
            [next.jdbc :as jdbc] [next.jdbc.result-set :as rs]))


(def db-spec {:dbtype "postgresql"
              :dbname "loan_db"
              :user "stephenokiyo"
              ;:password "your_password"
              :host "localhost"})

(def datasource (jdbc/get-datasource db-spec))

(defn save-loan! [loan]
  (jdbc/execute-one! datasource
                     ["INSERT INTO loans (user_id, amount, duration_months)
      VALUES (?, ?, ?) RETURNING *"
                      (:user-id loan)
                      (:amount loan)
                      (:duration-months loan)]))

(defn get-loan [id]
  (jdbc/execute-one! datasource
                     ["SELECT * FROM loans WHERE id = ?" id]))

(defn get-loans
  [{:keys [user-id amount]}]
  (let [where-parts []
        values      []

        ;; Build WHERE clauses and parameter values
        [where-parts values] (cond-> [where-parts values]
                                     user-id  (-> (update 0 conj "user_id = ?")
                                                  (update 1 conj user-id))
                                     amount   (-> (update 0 conj "amount = ?")
                                                  (update 1 conj amount)))

        where-clause (when (seq where-parts)
                       (str " WHERE " (clojure.string/join " AND " where-parts)))

        sql (str "SELECT * FROM loans" where-clause)
        sql-params (into [sql] values)]

    (log/info "Running SQL:" sql-params)

    ;; - convert from clojure map list to json - {:builder-fn rs/as-unqualified-lower-maps}

    (jdbc/execute! datasource sql-params {:builder-fn rs/as-unqualified-lower-maps})))



(defn count-loans
  [{:keys [user-id amount]}]
  (let [conditions []
        values []
        [conditions values] (cond-> [conditions values]
                                    user-id (-> (update 0 conj "user_id = ?")
                                                (update 1 conj user-id))
                                    amount  (-> (update 0 conj "amount = ?")
                                                (update 1 conj amount)))
        where-clause (when (seq conditions)
                       (str " WHERE " (clojure.string/join " AND " conditions)))
        sql (str "SELECT COUNT(*) AS total FROM loans" where-clause)]
    (:total (jdbc/execute-one! datasource (into [sql] values)))))


(defn update-loan!
  "Updates specified fields for a loan by ID. Returns the updated loan."
  [loan-id fields-to-update]
  (let [set-clauses (->> fields-to-update
                         (map (fn [[field _]]
                                ;; Convert kebab-case keyword to snake_case column name
                                (str (clojure.string/replace (name field) "-" "_") " = ?")))
                         (clojure.string/join ", "))
        values       (mapv second fields-to-update)
        sql          (str "UPDATE loans SET " set-clauses " WHERE id = ? RETURNING *")
        query-params (into [sql] (conj values loan-id))]
    (jdbc/execute-one!
      datasource
      query-params
      {:builder-fn rs/as-unqualified-lower-maps}))) ;; returns {:id ..., :duration_months ...}


(defn delete-loan!
  "Deletes a loan by ID. Returns the deleted loan ID if found."
  [loan-id]
  (jdbc/execute-one!
    datasource
    ["DELETE FROM loans WHERE id = ? RETURNING id" loan-id]
    {:builder-fn rs/as-unqualified-lower-maps}))









