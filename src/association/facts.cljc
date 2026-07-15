(ns association.facts
  "Industry rule/policy-statement catalog for the National Restaurant
  Association (NRA, Wikidata Q6978094) -- a 25th industry-association-
  level source (see cloud-itonami-assoc-6419-jpn-zenginkyo, -6512-jpn-sonpo,
  -6612-jpn-jsda, -6419-deu-bankenverband, -6612-usa-finra, -6512-usa-naic,
  -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf, -6511-jpn-seiho,
  -6910-jpn-nichibenren, -6810-jpn-recaj, -6411-jpn-boj, -6120-usa-ctia,
  -5110-usa-a4a, -3510-usa-eei, -2910-deu-vda, -5510-usa-ahla,
  -2100-usa-phrma, -4719-usa-nrf, -4100-usa-agc, -6020-usa-nab,
  -3600-usa-awwa, -4923-usa-ata for the first twenty-four) per
  ADR-2607141700 (cloud-itonami-compliance-fact-federation). The FIRST
  entry aligned to ISIC 5610 (restaurants and mobile food service
  activities) -- a new industry code for this family. A rule not in
  this table has NO spec-basis, full stop; extend `catalog`, do not
  invent an id/url/date.

  NOTE: 'NRA' here is the National Restaurant Association, NOT the
  National Rifle Association -- both share the initialism, disambiguated
  explicitly in this docstring and in organization.edn.

  The founding date (March 13, 1919, Kansas City) is directly confirmed
  on restaurant.org's own 'Who We Are / Our History' page. The second
  entry, ServSafe (the NRA's food-safety training and certification
  program, delivered via servsafe.com), does NOT state its own launch
  date anywhere on its official 'About Us' page (only secondary sources
  place it at 1990) -- :association-rule/established-date is
  deliberately omitted here rather than fabricated from an unconfirmed
  secondary date.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"nra"
   [{:association-rule/id "nra.who-we-are-our-history"
     :association-rule/title "Who We Are (Our History)"
     :association-rule/association "nra"
     :association-rule/isic "5610"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://restaurant.org/about-us/who-we-are/our-history/"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1919-03-13"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:governance}}
    {:association-rule/id "nra.servsafe-about"
     :association-rule/title "ServSafe (About Us)"
     :association-rule/association "nra"
     :association-rule/isic "5610"
     :association-rule/country "USA"
     :association-rule/kind :best-practices-guide
     :association-rule/url "https://www.servsafe.com/about-us"
     :association-rule/url-provenance :official-association-site
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:food-safety}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-5610-usa-nra Wave 0 (ADR-2607141700): "
                 (count (get catalog "nra")) " nra entries seeded with an "
                 "official restaurant.org/servsafe.com citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
