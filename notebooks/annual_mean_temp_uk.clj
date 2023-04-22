(ns notebooks.annual-mean-temp-uk
  (:require [clojure.data.csv :as csv]
            [nextjournal.clerk :as clerk]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]))

(comment
  (require '[nextjournal.clerk :as clerk])
  (clerk/serve! {:watch-paths ["."] :browse? true}) 
(clerk/show! "notebooks/annual_mean_temp_uk.clj")
  )

(-> "data/annual-mean-temp-uk.csv" ;;name of the file
    slurp ;; read the contents of the file
    csv/read-csv ;; parse the result as csv data
    clerk/use-headers ;; tell Clerk to use the first row as headers
    clerk/table) ;;render the data as a table


(clerk/vl {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
           :data {:url "https://vega.github.io/vega-lite/data/co2-concentration.csv"
                  :format {:parse {:Date "utc:'%Y-%m-%d'"}}}
           :width 800
           :height 500
           :transform [{:calculate "year(datum.Date)" :as "year"}
                       {:calculate "floor(datum.year / 10)" :as "decade"}
                       {:calculate "(datum.year % 10) + (month(datum.Date)/12)"
                        :as "scaled_date"}
                       {:calculate "datum.first_date === datum.scaled_date ? 'first' : datum.last_date === datum.scaled_date ? 'last' : null"
                        :as "end"}]
           :encoding {:x {:type "quantitative" :title "Year into Decade" :axis {:tickCount 11}}
                      :y {:title "CO2 concentration in ppm" :type "quantitative" :scale {:zero false}}
                      :color {:field "decade" :type "ordinal" :scale {:scheme "magma"} :legend nil}}
           :layer [{:mark "line" :encoding {:x {:field "scaled_date"} :y {:field "CO2"}}}
                   {:mark {:type "text" :baseline "top" :aria false}
                    :encoding {:x {:aggregate "min" :field "scaled_date"}
                               :y {:aggregate {:argmin "scaled_date"} :field "CO2"}
                               :text {:aggregate {:argmin "scaled_date"} :field "year"}}}
                   {:mark {:type "text" :aria false}
                    :encoding {:x {:aggregate "max" :field "scaled_date"}
                               :y {:aggregate {:argmax "scaled_date"} :field "CO2"}
                               :text {:aggregate {:argmax "scaled_date"} :field "year"}}}]
           :config {:text {:align "left" :dx 3 :dy 1}}})