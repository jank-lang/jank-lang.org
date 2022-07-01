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
  export -f jank-generate-resources
  function jank-optimize-resources
  {
    ./bin/optimize-resources $@
  }
  export -f jank-optimize-resources
  function jank-build
  {
    jank-generate-resources
    lein trampoline run build
    jank-optimize-resources build
  }
  export -f jank-build

  function jank-deploy
  {
    jank-build
    ./bin/deploy
  }
  export -f jank-deploy

  # TODO: Get this via nix.
  npm install
  '';
}
