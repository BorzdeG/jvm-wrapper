#!/usr/bin/env bash

TEST_JVM_VERSION=${TEST_JVM_VERSION:?}
TEST_JVM_VENDOR=${TEST_JVM_VENDOR:?}
TEST_TYPE=${TEST_TYPE:?}
TEST_JVM_TYPE=${TEST_JVM_TYPE:?}

# Hack for code verification
USE_SYSTEM_JDK=${USE_SYSTEM_JDK}
JVMW_DEBUG=${JVMW_DEBUG}
REQUIRED_UPDATE=${REQUIRED_UPDATE}
TEST_OUTPUT=${TEST_OUTPUT}
JVM_VERSION=${JVM_VERSION}
JVM_VENDOR=${JVM_VENDOR}
#
# shellcheck disable=SC2034
export TEST_JVM_HOME="${HOME}/.jvm/${TEST_JVM_VENDOR}-${TEST_JVM_TYPE}-${TEST_JVM_VERSION}/"

function before_test() {
	rm -Rf "${HOME}"/.jvm/oracle-jdk-*
	rm -Rf "${HOME}"/.jvm/oracle-jre-*
	cp ../jdkw ./
	cp "../samples.properties/${TEST_JVM_VENDOR}-${TEST_JVM_VERSION}.properties" ./jvmw.properties
}

function after_test() {
	unset USE_SYSTEM_JDK JVMW_DEBUG REQUIRED_UPDATE
	for env_test in $(env | grep TEST_); do
		unset "${env_test%%=*}"
	done
}

function die() {
	echo '----- TEST .JVM directory :: begin -----'
	ls  -la "${HOME}/.jvm/"
	echo '----- TEST .JVM directory :: end -----'
	echo '----- TEST content "*.last_update" :: begin -----'
	tail  "${HOME}/.jvm/*.last_update"
	echo '----- TEST content "*.last_update" :: end -----'
	echo '----- TEST ENVIRONMENTS :: begin -----'
	env | grep TEST_
	echo '----- TEST ENVIRONMENTS :: end -----'
	echo '----- SYSTEM JVM :: begin -----'
	java -fullversion
	echo '----- SYSTEM JVM :: end -----'
	echo '----- TEST CONFIGURATION FILE :: begin -----'
	cat jvmw.properties
	echo
	echo "env.USE_SYSTEM_JDK=${USE_SYSTEM_JDK}"
	echo "env.JVMW_DEBUG=${JVMW_DEBUG}"
	echo "env.REQUIRED_UPDATE=${REQUIRED_UPDATE}"
	echo "env.JVM_VERSION=${JVM_VERSION}"
	echo "env.JVM_VENDOR=${JVM_VENDOR}"
	echo '----- TEST CONFIGURATION FILE :: end -----'
	echo '----- OUTPUT :: begin -----'
	echo "${TEST_OUTPUT}"
	echo '----- OUTPUT :: end -----'
	echo "error line: ${BASH_LINENO[0]}"
	after_test
	exit 1
}