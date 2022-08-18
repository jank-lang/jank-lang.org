# jank-lang.org
Here lies the website for the [jank programming language](https://github.com/jank-lang/jank). View the website here https://jank-lang.org

## Build instructions
Nix provides everything, so first just enter a Nix shell.

```bash
$ nix-shell
```

From there, you can build the full site to the `build` directory.

```bash
$ jank-build
```

To build and deploy, use the following.

```bash
$ jank-deploy
```
