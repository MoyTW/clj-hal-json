(ns clj-hal.core)

;;;;   You can technically have resources without self links! However, this
;;;; implementation enforces self links in all resources.
;;;;   Resources are plain Clojure data structures which map directly to json.

(defn templated-href-valid?
  "Ensures that a link is minimally templated; full documentation is here:
  http://tools.ietf.org/html/rfc6570"
  [link]
  (if (:templated (second (first link)))
      (re-find #"\{.+\}" (:href (second (first link))))
      true))

(def link-properties
  #{:href :templated :type :deprecation :name :profile :title :hreflang})
(def reserved-resource-properties
  #{:_links :_embedded})

(defn is-resource
  [resource]
  (-> resource :_links :self :href))

(defn new-resource
  "Creates a new resource. Resources are maps with reserved keys _links and
  _embedded, as well as a mandatory :self link."
  [self]
  {:pre [self
         (not (empty? self))]}
  {:_links {:self {:href self}}})

(defn new-link
  "Creates a new link. Links are maps of the form {rel href & properties}.
  Properties are keyword/value pairs. If the :templated property is true, 
  the href must be minimally templated."
  [rel href & properties]
  {:post [(not= (keyword (ffirst %)) :curies)
          (every? link-properties (keys (second (first %))))
          (templated-href-valid? %)]}
  {(keyword rel) (apply hash-map :href href properties)})

;;; Creates a new curie
(defn new-curie 
  "Creates a new curie. Curies are a special form of links of the form
  {:curies {:name name :href href :templated true & properties}}.
  The properties :templated or :rel are fixed, and cannot be set."
  [name-value href & properties]
  {:pre [(not-any? #(= :rel (keyword %)) (take-nth 2 properties))
         (not-any? #(= :templated (keyword %)) (take-nth 2 properties))]
   :post [(every? link-properties (keys (second (first %))))
          (templated-href-valid? %)]}
  {:curies (apply hash-map :name (name name-value)
                           :href href 
                           :templated true 
                           properties)})

(defn add-link
  "Adds a new link, optionally creating. If there already exists in links the
  specified rel, it will turn it into a multi-link and add the new link.
  Attempting to add a curie will cause an error."
  ([resource link]
  {:pre [(not= :curies (keyword (ffirst link)))
         (every? link-properties (keys (second (first link))))
         (templated-href-valid? link)]}
  (update-in resource 
             [:_links (ffirst link)]
             #(let [contents (second (first link))]
               (cond
                 (nil? %) contents
                 (map? %) (conj [] % contents)
                 :else (conj % contents)))))
  ([resource rel href & properties] 
  (add-link resource (apply new-link rel href properties))))

;;; Takes multiple links
(defn add-links
  "Adds a variable number of links to the resource, merging links into rel. 
  Attempting to add a link with rel=\"curies\" will cause an error."
  [resource & links]
  (if (and (= 1 (count links)) (not (map? (first links))))
      (reduce add-link resource (first links))
      (reduce add-link resource links)))

(defn add-curie
  "Creates and adds a new curie. Attempting to add a curie whose name already
  exists will cause an error."
  ([resource curie]
  {:pre [(= (ffirst curie) :curies)
         (every? link-properties (keys (second (first curie))))
         (:templated (second (first curie)))
         (templated-href-valid? curie)
         (not-any? #(= (:name (second (first curie))) (:name %))
                   (-> resource :_links :curies))]}
  (update-in resource [:_links :curies] 
                      #((fnil conj []) % (second (first curie)))))
  ([resource name href & properties]
  (add-curie resource (apply new-curie name href properties))))

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