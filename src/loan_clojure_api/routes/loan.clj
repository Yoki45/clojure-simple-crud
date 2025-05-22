(ns loan_clojure_api.routes.loan
  (:require [compojure.core :refer :all]
            [loan-clojure-api.loans.service :as service] ))

(defroutes loan-routes
           ;; POST /apply-loan - handles loan application
           (POST "/apply-loan" req
             (let [loan-data (:body req)]
               (service/create-loan! loan-data)))

           ;; GET /loan/:id - fetches a loan by ID

           (GET "/loans" [user-id amount]
             (let [filters {:user-id user-id
                            :amount (when amount (Integer/parseInt amount))}
                   loans (service/fetch-loans filters)]
               {:status 200
                :body loans}))


           )
