initscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'io.thundra.agent:thundra-gradle-test-plugin:${THUNDRA_GRADLE_PLUGIN_VERSION}'
    }
}

allprojects {
    apply plugin: io.thundra.agent.gradle.ThundraTestPlugin

    repositories {
        mavenCentral()
    }

    thundra {
        agentPath = "${THUNDRA_AGENT_PATH}"
    }
}