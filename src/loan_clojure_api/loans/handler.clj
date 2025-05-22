(ns loan_clojure_api.loans.handler
  (:require
            [loan_clojure_api.routes.loan :refer [loan-routes]]
            ;[loan_clojure_api.routes.user :refer [user-routes]]
            ;[loan_clojure_api.routes.health :refer [health-routes]]
            [compojure.core :refer [routes]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]))

(def app
  (-> (routes
        loan-routes
        ;user-routes
        ; health-routes
        )
      (wrap-json-body {:keywords? true})
      wrap-json-response
      wrap-params))
