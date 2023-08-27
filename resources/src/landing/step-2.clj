(defn create-vertex-shader! []
  (native/raw "__value = make_box(glCreateShader(GL_VERTEX_SHADER));"))

(defn set-shader-source! [shader source]
  (native/raw "auto const s(detail::to_string(~{ source }));
               glShaderSource(detail::to_int(~{ shader }), 1, &s.data, nullptr);"))

(defn compile-shader! [shader]
  (native/raw "glCompileShader(detail::to_int(~{ shader }));"))
