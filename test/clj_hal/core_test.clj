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
    (is (thrown? Exception (new-resource "")))))

(deftest new-resource-nil
  (testing "Tests that new-resource disallows nil self urls."
    (is (thrown? Exception (new-resource nil)))))

;;;; Tests new-link

#_(defn new-link
  "Creates a new link. Links are maps of the form {rel href & properties}.
  Properties are keyword/value pairs. If the :templated property is true, 
  the href must be minimally templated."
  [rel href & properties]
  {:post [(validate-link %)]} ; Should this be using preconditions?
  {(keyword rel) (apply hash-map :href href properties)})

(deftest new-link-valid-minimal
  (testing "Tests valid new-link produces expected output."
    (is (= {:test {:href "test.com"}}
           (new-link :test "test.com")))))

; {:href :templated :type :deprecation :name :profile :title :hreflang}
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