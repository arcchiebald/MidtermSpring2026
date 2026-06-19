#!/usr/bin/env sh
set -eu

mvn -q exec:java -Dexec.args="$*"
