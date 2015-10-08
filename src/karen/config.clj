(ns karen.config
  (:require [schema.core :as s]
            [karen.util :as ru]
            [clojure.string :as string]
            [cljc.susumu.util :as u]))

(def ReturnedConfig {s/Keyword s/Any})

(s/def service-config :- ReturnedConfig
  {:service-available? {:representation {:media-type "application/json"}}
   :handle-service-not-available {:message "service not available"}})

(s/def json-config :- ReturnedConfig
  {:available-media-types ["application/json"]
   :handle-not-acceptable {:message "media type not avaiable"}})

(s/defn accepted-methods :- ReturnedConfig
  [methods :- [(s/enum :get :post :put :delete)]]
  {:handle-method-not-allowed {:message "method not allowed"}
   :allowed-methods methods})

(s/defn unauthenticated-only :- ReturnedConfig
  [auth-fn :- u/Fn]
  {:authorized? auth-fn
   :handle-unauthorized {:message "unauthenticated only"}})

(s/defn not-allowed-message :- s/Str
  [user-roles :- [s/Keyword]]
  (str "insufficient permission: " (string/join ", " user-roles)))

(s/defn roles-sufficient? :- s/Bool
  [roles-allowed user-roles]
  (boolean (some #(u/in roles-allowed %) user-roles)))

(s/defn authenticated-only :- ReturnedConfig
  [auth-fn :- u/Fn
   & [get-roles-fn :- u/Fn
      roles        :- [s/Keyword]]]
  {:authorized? auth-fn
   :handle-unauthorized {:message "authenticated only"}
   :allowed? (fn [ctx]
               (if-not (and get-roles-fn roles)
                 (ku/yes)
                 (let [user-roles (get-roles-fn ctx)]
                   (if (roles-sufficient? roles user-roles)
                     (ku/yes)
                     (ku/no {:error (not-allowed-message user-roles)})))))
   :handle-forbidden ku/return-error-message})

(s/defn malform-body-check :- ReturnedConfig
  [get-body-fn   :- u/Fn
   validation-fn :- u/Fn]
  {:malformed? (fn [ctx]
                 (let [body (get-body-fn ctx)
                       error (validation-fn body)]
                   (if error
                     (ku/yes {:error error})
                     (ku/no {:body body}))))
   :handle-malformed ku/return-error-message})

(s/defn exists-check :- ReturnedConfig
  [entity-fn :- u/Fn]
  {:handle-ok :entity
   :exists? (fn [ctx]
              (if-let [entity (entity-fn ctx)]
                (ku/yes {:entity entity})
                (ku/no {:error "entity not found"})))
   :handle-not-found ku/return-error-message})

(s/def post-with-entity-config :- ReturnedConfig
  {:post-redirect? false
   :new? true
   :handle-created :entity})

(s/def put-with-entity-config :- ReturnedConfig
  {:can-put-to-missing? false
   :new? false
   :respond-with-entity? true
   :multiple-representations? false
   :handle-ok :entity})

(s/def delete-with-entity-config :- ReturnedConfig
  {:delete-enacted? true
   :respond-with-entity? true
   :multiple-representations? false
   :handle-ok :entity})