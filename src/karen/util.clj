(ns karen.util
  (:require [schema.core :as s]))

(def ReturnedDecision [(s/cond-pre s/Keyword {s/Keyword s/Any})])

(s/defn yes :- ReturnedDecision
  ([m :- {s/Keyword s/Any}]
   [true m])
  ([]
   (yes {})))

(s/defn no :- ReturnedDecision
  ([m :- {s/Keyword s/Any}]
   [false m])
  ([]
   (no {})))

(s/defn yes? :- s/Bool
  [v :- ReturnedDecision]
  (true? (first v)))

(s/defn no? :- s/Bool
  [v :- ReturnedDecision]
  (false? (first v)))

(s/defn ->content :- s/Any
  [v :- ReturnedDecision]
  (second v))

(s/defn return-error-message :- {s/Keyword s/Str}
  [ctx :- {:error s/Any}]
  {:message (:error ctx)})

(s/defn page-range :- [s/Int]
  [per-page :- s/Int
   page     :- s/Int]
  (let [lower-end (inc (* (dec page) per-page))
        higher-end (* page per-page)]
    [lower-end higher-end]))
