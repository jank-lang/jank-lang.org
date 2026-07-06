(ns my.app.filesystem
  (:include "boost/filesystem.hpp")
  (:refer-global :rename {boost.filesystem.file_size file-size}))

(defn file-info [file-path]
  (try
    (let [bytes (file-size (cpp/cast std.string file-path))]
      {:path file-path
       :size bytes})
    (catch boost.filesystem.filesystem_error e
      {:path file-path
       :error (.what e)})))

(file-info "/etc/passwd")
; {:path "/etc/passwd", :size 4025}

(file-info "/root/.bash_history")
; {:path "/root/.bash_history"
;  :error "boost::filesystem::file_size: Permission denied [system:13]: \"/root/.bash_history\""}
