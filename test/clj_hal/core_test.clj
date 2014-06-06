(ns clj-hal.core-test
  (:require [clojure.test :refer :all]
            [clj-hal.core :refer :all]
            [cheshire.core :refer :all]))

(defn using-cheshire
  [resource]
  (parse-string (generate-string resource)))

(defn slurp-json
  [path]
  (parse-string (slurp path)))

;;;; Tests new-resource

(deftest new-resource-minimal
  (testing "Tests new-resource."
    (is (= (using-cheshire (new-resource "/test"))
           (slurp-json "resources/test/json/new-resource.json")))))

(deftest new-resource-blank
  (testing "Tests that new-resource disallows blank self urls."
    (is (thrown? java.lang.AssertionError (new-resource "")))))

(deftest new-resource-nil
  (testing "Tests that new-resource disallows nil self urls."
    (is (thrown? java.lang.AssertionError (new-resource nil)))))

;;;; Tests new-link

(deftest new-link-valid-minimal
  (testing "Tests valid new-link produces expected output."
    (is (= {:test {:href "test.com"}}
           (new-link :test "test.com")))))

(deftest new-link-valid
  (testing "Tests valid new-link produces expected output."
    (is (= {:test {:href "test/{t}" :templated true :type "type" 
                   :deprecation "deprecation" :name "name" :profile "profile" 
                   :title "title" :hreflang "hreflang"}}
           (new-link :test "test/{t}" :templated true :type "type" 
                     :deprecation "deprecation" :name "name" :profile "profile" 
                     :title "title" :hreflang "hreflang")))))

(deftest new-link-invalid-property
  (testing "Tests that invalid properties throw errors."
    (is (thrown? java.lang.AssertionError
      (new-link :valid-ref "valid-uri" :not-a-property "string")))))

(deftest new-link-templated-false
  (testing "Tests that templated false does not require templated href"
    (is (= {:xkcd {:href "xkcd.com" :templated false}}
           (new-link :xkcd "xkcd.com" :templated false)))))

(deftest new-link-templated-true
  (testing "Tests that templated true requires templated href"
    (is (thrown? java.lang.AssertionError
      (new-link :xkcd "xkcd.com" :templated true)))))

(deftest new-link-templated-true
  (testing "Tests that templated true passes if href is templated"
    (is (= {:what-if {:href "https://what-if.xkcd.com/{id}" :templated true}}
           (new-link :what-if "https://what-if.xkcd.com/{id}" 
                     :templated true)))))

(deftest new-link-curies
  (testing "Tests that it will not allow you to create rel=:curies"
    (is (thrown? java.lang.AssertionError
      (new-link :curies "/docs/{id}")))))

;;;; Tests new-curie

(deftest new-curie-valid-minimal
  (testing "Tests valid new-curie produces expected output."
    (is (= {:curies {:name "docs" :href "/docs/{id}" :templated true}}
           (new-curie "docs" "/docs/{id}")))))

(deftest new-curie-valid
  (testing "Tests valid new-curie produces expected output."
    (is (= {:curies {:name "docs" :href "/docs/{id}/" :templated true 
            :type "type" :deprecation "deprecation" :profile "profile" 
            :title "title" :hreflang "hreflang"}}
           (new-curie "docs" "/docs/{id}/" :type "type" 
                      :deprecation "deprecation" :profile "profile" 
                      :title "title" :hreflang "hreflang")))))

(deftest new-curie-keyword
  (testing "Tests that if you pass a keyword into name, coerces to string."
    (is (= {:curies {:name "docs" :href "/docs/{id}" :templated true}}
            (new-curie :docs "/docs/{id}")))))

(deftest new-curie-invalid-property
  (testing "Tests that if you add a non-property, throws error."
    (is (thrown? java.lang.AssertionError
      (new-curie "docs" "/docs/{id}" :foobar "baz")))))
    
(deftest new-curie-set-templated
  (testing "Tests that you cannot set templated"
    (is (thrown? java.lang.AssertionError
      (new-curie "docs" "/docs/{id}" :templated false)))))

(deftest new-curie-link-untemplated
  (testing "Tests that if the curie is untemplated, throws error."
    (is (thrown? java.lang.AssertionError
      (new-curie "docs" "/docs/13")))))

;;;; Tests add-link

(def info (new-resource "info.com"))

(deftest add-link-create-valid
  (testing "Tests valid link added to fresh resource"
    (is (= {:_links {:self {:href "info.com"} 
                     :data {:href "data.net" :hreflang "Logic"}}}
           (add-link info :data "data.net" :hreflang "Logic")))))

(deftest add-link-create-invalid
  (testing "Tests invalid link construction"
    (are [p v] (thrown? java.lang.AssertionError 
                 (add-link info :data "data.net" p v))
      :templated true
      :nosuch "property")))

(deftest add-link-valid-link
  (testing "Tests that it accepts a valid link."
    (is (= {:_links {:self {:href "info.com"} 
                     :data {:href "data.net" :hreflang "Logic"}}}
           (add-link info (new-link :data "data.net" :hreflang "Logic"))))))
      
(deftest add-link-invalid-link
  (testing "Tests that it rejects an invalid link."
    (is (thrown? java.lang.AssertionError
      (add-link info {:data {:hreg "typo!" :hreflang "Logic"}})))))

(deftest add-link-invalid-templated
  (testing "Tests that it rejects a link which is not propertly templated."
    (is (thrown? java.lang.AssertionError
      (add-link info {:data {:href "data.com" :templated true}})))))
      
(deftest add-link-existing
  (testing "Tests that existing rels added are combined into groups of links"
    (is (= {:_links {:self {:href "info.com"} 
                     :androids [{:href "data.net" :hreflang "Logic"}
                                {:href "lore.net" :hreflang "Emotions"}]}}
           (-> info
               (add-link :androids "data.net" :hreflang "Logic")
               (add-link :androids "lore.net" :hreflang "Emotions"))))))

(deftest add-link-create-rel-curies
  (testing "Tests that attempting to add reserved rel :curies will stop"
    (is (thrown? java.lang.AssertionError
      (add-link info (new-link :curies "explodes?"))))))

(deftest add-link-rel-curies
  (testing "Tests that attempting to add reserved rel :curies will stop"
    (is (thrown? java.lang.AssertionError
      (add-link info {:curies {:href "explodes?"}})))))

;;;; Tests add-links
(def tanks (new-link :tanks "tanks.com"))
(def planes (new-link :planes "planes.com"))
(def more-planes (new-link :planes "moreplanes.com"))

(deftest add-links-distinct-valid
  (testing "Tests that adding distinct links works."
    (is (= (-> info (add-link tanks) (add-link planes))
           (apply add-links info [tanks planes])))))

(deftest add-links-share-rel-valid
  (testing "Tests that adding shared rels works."
    (is (= (-> info (add-link planes) (add-link more-planes))
           (apply add-links info [planes more-planes])))))

(deftest add-links-existing-rel-valid
  (testing "Tests that adding shared rels works."
    (is (= (-> info (add-link planes) (add-link more-planes))
           (as-> info $i
                 (add-link $i planes)
                 (apply add-links $i [more-planes]))))))

(deftest add-links-contains-curie
  (testing "Tests that cannot add a link with reserved rel :curies"
    (is (thrown? java.lang.AssertionError
      (apply add-links info [(new-link :valid "valid.com") 
                             {:curies {:href "explodes?"}}])))))

(deftest add-links-malformed-link
  (testing "Tests that cannot add a link with reserved rel :curies"
    (is (thrown? java.lang.AssertionError
      (apply add-links info [(new-link :unvakud "valid.com") 
                             {:invalid {:hreg "explodes?"}}])))))

(deftest add-links-takes-vec-list
  (testing "Tests that add-links can take a list or vector directly."
    (are [c] (= (-> info (add-link planes) (add-link tanks)) c)
      (add-links info [planes tanks])
      (add-links info `(~planes ~tanks)))))

;;;; Tests add-curie

(deftest add-curie-valid-minimal
  (testing "Tests that a valid curie will be properly added."
    (are [c] (= c {:_links {:self {:href "info.com"}
                            :curies [{:name "valid" 
                                      :href "valid/{temp}" 
                                      :templated true}]}})
      (add-curie info :valid "valid/{temp}")
      (add-curie info "valid" "valid/{temp}")
      (add-curie info (new-curie "valid" "valid/{temp}")))))

(deftest add-curie-valid
  (testing "Tests that a valid curie will be properly added."
    (is (= {:_links {:self {:href "info.com"}
                     :curies [{:name "valid" :href "valid/{temp}" 
                               :templated true :deprecation "deprecation"
                               :hreflang "hreflang"}]}}
           (add-curie info "valid" "valid/{temp}" :deprecation "deprecation" 
                      :hreflang "hreflang")))))

(deftest add-curie-malformed
  (testing "Tests that a malformed curie will be rejected."
    (are [c] (thrown? java.lang.AssertionError (add-curie info c))
      {:not-curies {:href "lol/{template}" :name "lol" :templated true}}
      {:curies {:href "lol/{template}" :name "lol"}}
      {:curies {:hreg "lol/{template}" :name "lol" :templated true}})))

(deftest add-curie-untemplated
  (testing "Tests that untemplated fails."
    (is (thrown? java.lang.AssertionError
      (add-curie info "explodes?" "explodes!")))))

(deftest add-curie-already-exists
  (testing "Tests that it does not double up on names"
    (is (thrown? java.lang.AssertionError
      (-> info (add-curie "double" "double/{toil}") 
               (add-curie "double" "and/{trouble}"))))))

;;;; Tests add-curies
(deftest add-curies-valid
  (testing "Tests that valid curies are added."
    (are [r] (= r (-> info (add-curie :v1 "v1/{t}") (add-curie :v2 "v2/{t}")))
      (add-curies info (new-curie :v1 "v1/{t}") (new-curie :v2 "v2/{t}"))
      (add-curies info [(new-curie :v1 "v1/{t}") (new-curie :v2 "v2/{t}")])
      (add-curies info `(~(new-curie :v1 "v1/{t}") 
                         ~(new-curie :v2 "v2/{t}"))))))

(deftest add-curies-malformed
  (testing "Tests that malformed curies will be rejected."
    (are [c] (thrown? java.lang.AssertionError (add-curies info c))
      {:not-curies {:href "lol/{template}" :name "lol" :templated true}}
      {:curies {:href "lol/{template}" :name "lol"}}
      {:curies {:hreg "lol/{template}" :name "lol" :templated true}}
      [(new-curie :valid "valid/{t}")
       {:not-curies {:href "lol/{template}" :name "lol" :templated true}}]
      [(new-curie :valid "valid/{t}")
       {:curies {:hreg "lol/{template}" :name "lol" :templated true}}])))

(deftest add-curies-link-untemplated
  (testing "Tests that untemplated fails."
    (is (thrown? java.lang.AssertionError
      (add-curies info {:curies {:href "notemplate}" 
                                 :name "explodes"
                                 :templated true}})))))

(deftest add-curies-already-exists
  (testing "Tests that you can't double-add."
    (is (thrown? java.lang.AssertionError
      (add-curies info (new-curie "double" "double/{toil}") 
                       (new-curie "double" "and/{trouble}"))))))

;;;; Tests add-property
#_(deftest add-property-valid)
#_(deftest add-property-invalid-key)
#_(deftest add-property-exists)
#_(deftest add-property-reserved)

;;;; Tests add-properties
#_(deftest add-properties-valid-single)
#_(deftest add-properties-valid-multiple)
#_(deftest add-properties-exists)
#_(deftest add-properties-not-distinct)
#_(deftest add-properties-reserved)
#_(deftest add-properties-bad-input)

;;;; Tests add-embedded-resource
#_(deftest add-embedded-resource-new)
#_(deftest add-embedded-resource-exists-rel)
#_(deftest add-embedded-resource-not-resource)

;;;; Tests add-embedded-resources
#_(deftest add-embedded-resources-single)
#_(deftest add-embedded-resources-multiple)
#_(deftest add-embedded-resources-exists-rel)
#_(deftest add-embedded-resources-one-not-resource)