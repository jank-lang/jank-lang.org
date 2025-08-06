(defn create-vertex-shader! []
  (cpp/glCreateShader cpp/GL_VERTEX_SHADER))

(defn set-shader-source! [shader source]
  (let [shader (cpp/int shader)
        source (str source)]
    (cpp/glShaderSource shader 1 source cpp/nullptr)))

(defn compile-shader! [shader]
  (cpp/glCompileShader (cpp/int shader)))
