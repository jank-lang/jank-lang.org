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
    go
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
    lein run build
    jank-optimize-resources build
  }
  export -f jank-build

  # TODO: Get this via nix.
  go install github.com/juliusmh/snippit@latest
  go env
  export PATH="~/go:''${PATH}"
  '';
}
