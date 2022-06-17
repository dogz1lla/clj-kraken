# clj-kraken

This is a simple CLI utility to access the 
[kraken crypto exchange API](https://docs.kraken.com/rest/).

## Installation

Clone this repo to a local directory.

## Usage

Run the utility using the clojure CLI:

    $clj -M -m kraken-api.clj-kraken Time
    {error [], result {unixtime 1637529415, rfc1123 Sun, 21 Nov 21 21:16:55 +0000}}

Run the utility with `-h` (or `--help`) flags to see a help string describing 
possible parameters and options.

    $clj -M -m kraken-api.clj-kraken -h

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build an uberjar:

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/clj-kraken-0.1.0-SNAPSHOT.jar

If you remove `version` from `build.clj`, the uberjar will become `target/clj-kraken-standalone.jar`.

### Not tested

Any private endpoints besides `Balance` are untested!

### Appendix

Have a look at the [little article](https://dogz1lla.xyz/kraken_api_client) i wrote about this repo!

## License

Copyright © 2021 dogz1lla

Distributed under the GNU GENERAL PUBLIC LICENSE Version 3.
