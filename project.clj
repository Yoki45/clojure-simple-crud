(defproject loan-api "0.1.0-SNAPSHOT"
  :description "A simple Clojure API for loan applications"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.11.0"]
                 [compojure "1.7.0"]
                 [cheshire "5.11.0"]
                 [ring/ring-json "0.5.1"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [org.postgresql/postgresql "42.7.2"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/slf4j-simple "2.0.9"]
                 [environ "1.2.0"]]
  :plugins [[lein-ring "0.12.6"]]
  :ring {:handler loan_clojure_api.loans.handler/app}
  :profiles {:dev {:env {:database-url "jdbc:postgresql://localhost/loan_db"}}})
