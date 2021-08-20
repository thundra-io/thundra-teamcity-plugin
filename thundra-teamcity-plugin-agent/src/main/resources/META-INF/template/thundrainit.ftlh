initscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'io.thundra.agent:thundra-gradle-test-plugin:${thundraGradlePluginVersion}'
    }
}

allprojects {
    apply plugin: io.thundra.agent.gradle.ThundraTestPlugin

    repositories {
        mavenCentral()
    }

thundra {
    agentPath = "${thundraAgentPath}"
    }
}