#!/bin/bash

set -e

# cd to project root directory
cd "$(dirname "$(dirname "$0")")"

docker build --target base -t genomealmanac/chipseq-xcor-base .

docker run --name chipseq-xcor-base --rm -i -t -d \
    -v /tmp/chipseq-test:/tmp/chipseq-test \
    genomealmanac/chipseq-xcor-base /bin/sh