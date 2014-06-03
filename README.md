# clj-hal

WIP. API for easily creating [hal] representations in Clojure.

## API?

```
;;; Creates a new resource
(defn new-resource [self])

;;; Creates a new link
(defn new-link [rel href & properties])

;;; Creates a new curie
(defn new-curie [name href & properties])

;;; Takes one link + properties
;;; Rolls new-link and add-link into one function
(defn add-link [resource rel href & properties])

;;; Takes multiple links
(defn add-links [resource & links])

;;; Takes one curie + properties
;;; Rolls new-curie and add-curie into one function
(defn add-curie [resource name href & properties])

;;; Takes multiple curies
(defn add-curies [resource & curies])

;;; Takes a single property
(defn add-property [resource name value])

;;; Takes a collection of properties?
;; Properties are maps/2-tuples?
(defn add-properties [resource & properties])

;;; Takes a single resource
;;; We can't roll new-resource into this, can we?
(defn add-embedded-resource [resource rel embedded-resource])

;;; Takes a collection of resources
;;; This is trickier...
(defn add-embedded-resources [resource rel & embedded-resources])

;;; Function to hoist embedded links
(defn hoist-embedded-links [resource])
```

When taking multiple values to add (for example, in add-embedded-resources) should the functions take:

* A collection of items to add
* An arbitrary number of item arguments to add

Basically, should it be:

```
(defn add-embedded-resources [resource rel embedded-resources])
```

or

```
(defn add-embedded-resources [resource rel & embedded-resources])
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[hal]:http://stateless.co/hal_specification.html