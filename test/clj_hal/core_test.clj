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
           (new-curie "docs" "/docs/{id}" :type "type" 
                      :deprecation "deprecation" :profile "profile" 
                      :title "title" :hreflang "hreflang")))))

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