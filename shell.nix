with (import <nixpkgs> {});
mkShell
{
  buildInputs =
  [
    # HTML generation.
    leiningen
    clojure

    # Resource handling.
    asciinema
    termtosvg
    # TODO: snippet
    scour
    optipng
    jpegoptim
    nodejs
  ];
  shellHook =
  ''
  function jank-generate-resources
  {
    ./bin/generate-resources $@
  }
  export jank-generate-resources
  function jank-optimize-resources
  {
    ./bin/optimize-resources $@
  }
  export jank-optimize-resources
  function jank-build
  {
    jank-generate-resources
    lein run build
    jank-optimize-resources build
  }
  export jank-build
  '';
}
