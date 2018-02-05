#!/usr/bin/env bash

# shellcheck disable=SC2086
docker run \
--rm \
-it \
-e "ORACLE_USER=${ORACLE_USER}" \
-e "ORACLE_PASSWORD=${ORACLE_PASSWORD}" \
-v "$(pwd)/jdkw":/root/jdkw \
-v "$(pwd)/tests/":/root/tests/ \
-v "$(pwd)/samples.properties/":/root/samples.properties/ \
-v "$(pwd)/tests.sh":/root/tests.sh \
 $DOCKER_IMAGE:$DOCKER_TAG bash -c "cd /root && ./tests.sh 2>&1"