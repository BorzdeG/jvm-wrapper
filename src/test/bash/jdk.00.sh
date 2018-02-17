#!/usr/bin/env bash

CUR_DIR=$(dirname "$0")
source "${CUR_DIR}/_test_core.sh"

# Hack for code verification
TEST_FULL_VERSION=${TEST_FULL_VERSION:?}
#
before_test
export USE_SYSTEM_JDK=N
#
mkdir -p ./test
cp ../src/test/resources/Test.java ./test/

[[ -f "test/Test.java" ]] || die;

TEST_OUTPUT=$(./jdkw javac -d ./test test/Test.java 2>&1)
[[ -f "test/Test.class" ]] || die;

TEST_OUTPUT=$(./jdkw java -cp ./test/ Test 2>&1)
[[ "${TEST_OUTPUT}" == "${TEST_FULL_VERSION}" ]] || die
#
after_test