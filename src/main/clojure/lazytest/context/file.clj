(ns lazytest.context.file
  (:use [lazytest.context :only (Context setup teardown)]
	[lazytest.context.stateful :only (stateful)]
	[clojure.java.io :only (file)])
  (:import (java.io File)))

;; You don't create instances of this; use the temp-file function.
(deftype TempFileContext [name suffix dir ^{:tag File :unsynchronized-mutable true} f]
  Context
  (setup [this] (set! f (File/createTempFile name suffix dir)))
  (teardown [this] (.delete f)))

(defn temp-file
  "Returns a stateful context that creates a temporary file.  After
  setup, an empty file named with the given prefix and suffix (both
  optional) will exist.  The state of the context is the java.io.File.
  The file will be deleted during teardown.

  If suffix is a file extension, it should include the leading period,
  e.g., \".txt\"

  If dir is specified (String or File), the temporary file will be
  created in that directory; otherwise it will be in the
  system-default temporary directory."
  ([]
     (temp-file "temp" nil nil))
  ([prefix]
     (temp-file prefix nil nil))
  ([prefix suffix]
     (temp-file prefix suffix nil))
  ([prefix suffix dir]
     {:pre [(string? prefix)
	    (or (nil? suffix) (string? suffix))]}
     (let [dir (if (nil? dir) dir (file dir))]
       (stateful (TempFileContext. prefix suffix dir nil)))))

;; You don't create instances of this; use the temp-dir function.
(deftype TempDirContext [name dir ^{:tag File :unsynchronized-mutable true} f]
  Context
  (setup [this]
	 (set! f (File/createTempFile name "" dir))
	 (assert (.delete f))
	 (assert (.mkdirs f))
	 f)
  (teardown [this]
	    (doseq [x (reverse (file-seq f))]
	      (.delete x))))

(defn temp-dir
  "Returns a stateful context that creates a temporary directory.
  After setup, an empty directory named with the given
  prefix (optional) will exist.  The state of the context is the
  java.io.File.  The directory and all its contents will be
  recursively deleted during teardown.

  If dir is specified (String or File), the temporary directory will
  be created in that directory; otherwise it will be in the
  system-default temporary directory."
  ([]
     (temp-dir "temp" nil))
  ([prefix]
     (temp-dir prefix nil))
  ([prefix dir]
     {:pre [(string? prefix)]}
     (let [dir (if (nil? dir) dir (file dir))]
       (stateful (TempDirContext. prefix dir nil)))))
