(ns clj-hal.core)

;;;;   You can technically have resources without self links! However, this
;;;; implementation enforces self links in all resources.
;;;;   Resources are plain Clojure data structures which map directly to json.

(defn validate-templated
  "Ensures that a link is minimally templated; full documentation is here:
  http://tools.ietf.org/html/rfc6570"
  [link]
  (re-find #"\{.+\}" link))

(def link-properties
  #{:href :templated :type :deprecation :name :profile :title :hreflang})
(def reserved-resource-properties
  #{:_links :_embedded})

(defn is-resource
  [resource]
  (-> resource :_links :self :href))

(defn validate-link
  "Validates the link. Not really a very elegant way to do this; exceptions
  are probably a better approach?"
  [link]
  (if-let [properties (second (first link))]
    (and
      (not= (keyword (ffirst link)) :curies)
      (every? link-properties (keys properties))
      (if (:templated properties) (validate-templated (:href properties)) true))))

(defn new-resource
  "Creates a new resource. Resources are maps with reserved keys _links and
  _embedded, as well as a mandatory :self link."
  [self]
  {:_links {:self {:href self}}})

(defn new-link
  "Creates a new link. Links are maps of the form {rel href & properties}.
  Properties are keyword/value pairs. If the :templated property is true, 
  the href must be minimally templated."
  [rel href & properties]
  {:post [(validate-link %)]} ; Should this be using preconditions?
  {(keyword rel) (apply hash-map :href href properties)})

;;; Creates a new curie
(defn new-curie 
  "Creates a new curie. Curies are a special form of links of the form
  {:curies {:name name :href href :templated true & properties}}.
  The properties :templated or :rel are fixed, and cannot be set."
  [name href & properties])

(defn add-link
  "Creates and adds a new link. If there already exists a link with the 
  specified rel, it will turn it into a multi-link and add the new link.
  Attempting to add a curie will cause an error."
  [resource rel href & properties])

;;; Takes multiple links
(defn add-links
  "Adds a variable number of links to the resource, merging links my rel. 
  Attempting to add a link with rel=\"curies\" will cause an error."
  [resource & links])

(defn add-curie
  "Creates and adds a new curie. Attempting to add a curie whose name already
  exists will cause an error."
  [resource name href & properties])

(defn add-curies
  "Adds multiple curies. Attempting to add a curie whose name already exists
  will cause an error."
  [resource & curies])

(defn add-property 
  "Adds a single property to the resource. If there already exists a property
  with name, will overwrite the existing property. Attempting to add the
  properties _links or _embedded will cause an error."
  [resource name value])

;;; Takes a collection of properties?
;; Properties are maps/2-tuples?
(defn add-properties 
  "Adds multiple properties to the resource. Existing properties sharing names
  with the new properties will be overwritten. Attempting to add the properties
  _links or _embedded will cause an error."
  [resource & properties])

(defn add-embedded-resource
  "Adds a single embedded resource mapped to the given rel in _embedded. If
  there already exists one or more embedded resources mapped to rel, converts
  the rel to a group of embedded resources and appends to it."
  [resource rel embedded-resource])

(defn add-embedded-resources 
  "Adds multiple embedded resources as members of a resource group mapped to
  rel. If there already exist resources mapped to rel, adds the new resources
  to the existing resource group."
  [resource rel & embedded-resources])

;;; Function to hoist embedded links
;;; TODO: Later.
(defn hoist-embedded-links [resource])

;;; Serializes to a json string
;;; TODO: Later.
(defn to-json [resource])