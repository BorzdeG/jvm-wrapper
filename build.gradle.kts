import com.gradle.scan.plugin.BuildScanExtension
import nebula.plugin.responsible.FacetDefinition
import nebula.plugin.responsible.NebulaFacetPlugin
import nebula.plugin.responsible.TestFacetDefinition
import io.gitlab.arturbosch.detekt.DetektCheckTask
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DEFAULT_PROFILE_NAME
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformPluginBase
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.time.LocalDateTime
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import java.time.format.DateTimeFormatter

tasks.withType<Wrapper> {
  distributionType = Wrapper.DistributionType.BIN
  gradleVersion = "4.7"
}

group = "ru.itbasis.jvm-wrapper"

buildscript {
  val kotlinVersion: String by extra
  dependencies {
    classpath(kotlin("gradle-plugin", kotlinVersion))
    classpath("gradle.plugin.io.gitlab.arturbosch.detekt:detekt-gradle-plugin:latest.release")
    classpath("com.netflix.nebula:nebula-project-plugin:latest.release")
  }
}

plugins {
  `build-scan`
}

apply {
  plugin<IdeaPlugin>()
}

configure<BuildScanExtension> {
  setTermsOfServiceUrl("https://gradle.com/terms-of-service")
  setTermsOfServiceAgree("yes")

  if (!System.getenv("CI").isNullOrEmpty()) {
    publishAlways()
    tag("CI")
  }
}

configure<IdeaModel> {
  project {
    vcs = "Git"
  }
}

allprojects {

  version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))

  apply {
    from("$rootDir/gradle/dependencies.gradle.kts")
    plugin<BasePlugin>()
    plugin<DetektPlugin>()
    plugin<NebulaFacetPlugin>()
  }

  configure<DetektExtension> {
    this.profile(DEFAULT_PROFILE_NAME, Action {
      config = rootDir.resolve("detekt-config.yml")
    })
  }

  afterEvaluate {
    plugins.withType(JavaBasePlugin::class.java) {
      configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
      }
      plugins.withType(NebulaFacetPlugin::class.java) {
        val facetIntegrationTest = TestFacetDefinition("integrationTest").also { extension.add(it) }
        tasks.withType(DetektCheckTask::class.java) {
          dependsOn(facetIntegrationTest.name)
          this.actions
          tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(this)
        }
      }
    }

    tasks.withType(KotlinCompile::class.java) {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
      }
    }

    plugins.withType(KotlinPlatformPluginBase::class.java) {
      configure<KotlinProjectExtension> {
        experimental.coroutines = Coroutines.ENABLE
      }
      dependencies {
        "compile"(kotlin("stdlib-jdk8"))

        arrayOf(kotlin("test"), kotlin("test-junit"), "io.kotlintest:kotlintest-runner-junit4").forEach {
          "testCompile"(it)
          "integrationTestCompile"(it)
        }
      }
    }

    tasks.withType(Test::class.java).all {
      testLogging {
        showStandardStreams = true
      }
    }
  }
}
