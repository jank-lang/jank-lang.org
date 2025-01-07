(defn create-vertex-shader! []
  (c++/glCreateShader c++/GL_VERTEX_SHADER))

(defn set-shader-source! [shader source]
  (let [shader (int shader)
        source (str source)]
    (c++/glShaderSource shader 1 source c++/nullptr)))

(defn compile-shader! [shader]
  (c++/glCompileShader (int shader)))
