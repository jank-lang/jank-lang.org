with (import <nixpkgs> {});
mkShell
{
  buildInputs =
  [
    # HTML generation.
    leiningen
    clojure

    # Resource handling.
    #asciinema
    #termtosvg
    scour
    optipng
    jpegoptim
    nodejs

    # Linting
    shellcheck
  ];
  shellHook =
  ''
  function jank-generate-resources
  {
    npm install
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
    jank-generate-resources resources/src
    lein trampoline run build
    rsync -ra resources/generated/img/ build/img/
    jank-optimize-resources build
  }
  export -f jank-build

  function jank-deploy
  {
    set -e
      jank-build
      ./bin/deploy
    set +e
  }
  export -f jank-deploy
  '';
}
