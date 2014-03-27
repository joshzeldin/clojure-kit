(ns io.prismic.render
  (:require [clojure.string :as str]
            [io.prismic.structured :as structured])
  (:use [clojure.core.match :only (match)]))

; private utilities

(defn- parse-date [d] (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd") d))
(defn- format-date [d pattern] (.format (java.text.SimpleDateFormat. pattern) d))
(defn- span [class content] (str "<span class=\"" class "\">" content "</span>"))
(defn- ddd [x] (prn x) x)

; public API

(declare fragment)

(defn text [f] (span "text" (:value f)))

(defn number [f] (span "number" (:value f)))

(defn color [f] (span "color" (:value f)))

(defn date
  ([f pattern] (span "date" (format-date (parse-date (:value f)) pattern)))
  ([f] (date f "yyyy/MM/dd")))

(defn image [f]
  (let [main (-> f :value :main) size (:dimensions main)]
    (str "<img src=\"" (:url main) "\" width=\"" (:width size) "\" height=\"" (:height size) "\" />")))

(defn web-link [f] (str "<a href=\"" (-> f :value :url) "\">" (-> f :value :url) "</a>"))

(defn file-link [f] (str "<a href=\"" (-> f :value :file :url) "\">" (-> f :value :file :name) "</a>"))

(defn document-link [f resolver]
  (str "<a href=\"" (resolver f) "\">" (-> f :value :document :slug) "</a>"))

(defn fragments [fs resolver]
  (str/join "\n" (for [[k f] fs]
                   (str "<section data-field=\"" (name k) "\">" (fragment f resolver) "</section>"))))

(defn document [doc resolver] (fragments (:fragments doc) resolver))

(defn embed [f]
  (let [url (-> f :value :oembed :embed_url)
        type (str/lower-case (-> f :value :oembed :type))
        provider (str/lower-case (-> f :value :oembed :provider_name))
        html (-> f :value :oembed :html)]
    (str "<div data-oembed=\"" url "\" data-oembed-type=\"" type "\" data-oembed-provider=\"" provider "\">" html "</div>")))

(defn group [f resolver] (str/join "\n" (for [g (:value f)] (fragments g resolver))))

(defn fragment [f resolver]
  (case (:type f)
    "Text" (text f)
    "Select" (text f)
    "Number" (number f)
    "Color" (color f)
    "Date" (date f)
    "Image" (image f)
    "Embed" (embed f)
    "Link.web" (web-link f)
    "Link.file" (file-link f)
    "Link.document" (document-link f resolver)
    "Group" (group f resolver)
    "StructuredText" (structured/render f resolver)
    ""))
