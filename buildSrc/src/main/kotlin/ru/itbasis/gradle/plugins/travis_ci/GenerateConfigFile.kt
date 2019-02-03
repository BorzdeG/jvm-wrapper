@file:Suppress("UnstableApiUsage")

package ru.itbasis.gradle.plugins.travis_ci

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.intellij.lang.annotations.Language

open class GenerateConfigFile : DefaultTask(), Runnable {
  @OutputFile
  var outputFile = project.objects.fileProperty()

  @Suppress("NOTHING_TO_INLINE")
  private inline fun RegularFileProperty.writeText(text: String) = get().asFile.writeText(text)

  @Suppress("NOTHING_TO_INLINE")
  private inline fun RegularFileProperty.appendText(text: String) = get().asFile.appendText(text)

  @TaskAction
  override fun run() {
    blockHead()
    blockInstall()
    blockBeforeScript()
    blockJobsStages()
  }

  private fun blockHead() {
    outputFile.writeText(
      """
sudo: required
language: generic

env:
  global:
    - JVM_WRAPPER_VERSION="`date +%Y%m%d_%H%M%S`"

"""
    )
  }

  private fun blockBeforeScript() {
    outputFile.appendText(
      """
before_script:
  - while IFS='' read -r line || [[ -n "${'$'}{line}" ]]; do eval "export ${'$'}{line}"; done < "samples.properties/${'$'}{ENV_TEST_FILE}.env"
  - env
  - bats -v
  - shellcheck -V
"""
    )
  }

  private fun blockInstall() {
    outputFile.appendText(
      """
install:
    """
    )
    blockInstallOSX()
    blockInstallLinux()
  }

  private fun blockJobsStages() {
    outputFile.appendText(
      """
jobs:
  include:
"""
    )
    blockJobsStageTest()
  }

  private fun blockJobsStageTest() {
    val dockerImages = arrayOf(
      "centos:centos6", "centos:centos7", "debian:wheezy", "debian:jessie", "ubuntu:trusty", "opensuse:latest", "base/archlinux:latest"
    )
    val envTestFiles = arrayOf(
      "openjdk-13", "openjdk-12", "openjdk-11", "openjdk-10", "openjdk-9", "openjdk-8", "openjdk-7",
      //
      "oracle-11", "oracle-8"
    )

    arrayListOf("osx", "linux").forEach { os ->
      envTestFiles.forEach { envTestFile ->
        outputFile.appendText(
          """
    - stage: test
      os: $os
      env:
        - ENV_TEST_FILE="$envTestFile"
      script:
        - cd ./wrapper-sh/
        - ./src/test/bash/test_suite.sh
        - cd ${'$'}TRAVIS_BUILD_DIR
        """
        )
      }
    }
    dockerImages.forEach { dockerImage ->
      envTestFiles.forEach { envTestFile ->
        outputFile.appendText(
          """
    - stage: test
      os: linux
      services:
        - docker
      env:
        - ENV_TEST_FILE="$envTestFile"
        - DOCKER_IMAGE="$dockerImage"
      script:
        - cd ./wrapper-sh/
        - ./src/test/bash/test_docker_suite.sh
        - cd ${'$'}TRAVIS_BUILD_DIR
        """
        )
      }
    }
  }

  private fun blockInstallLinux() {
    @Language("Bash") val cmds = arrayOf(
      "docker --version",
      "sudo add-apt-repository -y ppa:duggan/bats",
      "sudo apt-get update",
      "sudo apt-get install -y shellcheck bats",
      "apt-cache search oracle-"
    )
    cmds.forEach { cmd ->
      outputFile.appendText(
        """
  - if [[ "${'$'}TRAVIS_OS_NAME" == "linux" ]]; then $cmd; fi
      """
      )
    }
  }

  private fun blockInstallOSX() {
    @Language("Bash") val cmds = arrayOf(
      "brew update", "brew reinstall shellcheck bats-core", "brew cask uninstall java", "brew search java"
    )
    cmds.forEach { cmd ->
      outputFile.appendText(
        """
  - if [[ "${'$'}TRAVIS_OS_NAME" == "osx" ]]; then $cmd; fi
      """
      )
    }
  }

  companion object {
    const val TASK_NAME = "generateTravisConfig"
  }
}
