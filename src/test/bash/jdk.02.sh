#!/usr/bin/env bash

source "${PWD}/src/test/bash/_test_core.sh"

# Hack for code verification
TEST_JVM_HOME=${TEST_JVM_HOME:?}
TEST_FULL_VERSION=${TEST_FULL_VERSION:?}
#
before_gradle_test
export USE_SYSTEM_JVM=N
export JVMW_DEBUG=N
#
cp -R src.origin/test/resources/gradle/* ./

TEST_OUTPUT=$(./jvmw ./gradlew clean build 2>&1)
[[ "${TEST_OUTPUT}" == *"${TEST_JVM_HOME}"* ]] || die
[[ -f "build/libs/test.jar" ]] || die;

TEST_OUTPUT=$(./jvmw java -jar build/libs/test.jar 2>&1)
[[ "${TEST_OUTPUT}" == "${TEST_FULL_VERSION}" ]] || die
#
after_test
