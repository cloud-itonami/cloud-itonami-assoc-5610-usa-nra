(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url"
             "url-provenance" "established-date" "retrieved-at"])
(def expected
  [{"id" "nra.who-we-are-our-history" "title" "Who We Are (Our History)"
    "association" "nra" "isic" "5610" "country" "USA" "kind" "governance-program"
    "url" "https://restaurant.org/about-us/who-we-are/our-history/"
    "url-provenance" "official-association-site" "established-date" "1919-03-13"
    "retrieved-at" "2026-07-15"}
   {"id" "nra.servsafe-about" "title" "ServSafe (About Us)"
    "association" "nra" "isic" "5610" "country" "USA" "kind" "best-practices-guide"
    "url" "https://www.servsafe.com/about-us" "url-provenance" "official-association-site"
    "established-date" nil "retrieved-at" "2026-07-15"}])
(deftest reference-preserves-fields-date-precision-and-topics
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "nra" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= ["1919-03-13" nil] (mapv #(present (call kir 'entry-field "nra" % "established-date")) [0 1])))
    (is (= ["governance" "food-safety"] (mapv #(present (call kir 'topic "nra" % 0)) [0 1])))
    (is (= "nra.servsafe-about" (present (call kir 'by-topic-id "nra" "food-safety" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "wef")))
      (is (nil? (present (call kir 'entry-field "nra" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "nra" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "nra" 1 "established-date"))))
      (is (nil? (present (call kir 'topic "nra" 0 1))))
      (is (zero? (call kir 'by-topic-count "nra" "labor")))
      (is (nil? (present (call kir 'by-topic-id "nra" "food-safety" 1)))))))
(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))
(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const run=x=>{"
                     "if(x['entry-count']('nra')!==2n||x['entry-field']('nra',0n,'established-date')[2]!=='1919-03-13'||x['entry-field']('nra',1n,'established-date')[1]!==false)throw Error('dates');"
                     "if(x['topic']('nra',0n,0n)[2]!=='governance'||x['topic']('nra',1n,0n)[2]!=='food-safety')throw Error('topics');"
                     "if(x['by-topic-id']('nra','food-safety',0n)[2]!=='nra.servsafe-about'||x['topic']('nra',0n,1n)[1]!==false)throw Error('query');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
