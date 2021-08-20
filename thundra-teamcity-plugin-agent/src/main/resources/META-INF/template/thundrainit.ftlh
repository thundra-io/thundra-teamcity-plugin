initscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath 'io.thundra.agent:thundra-gradle-test-plugin:${thundraGradlePluginVersion}'
    }
}

allprojects {
    apply plugin: io.thundra.agent.gradle.ThundraTestPlugin

    repositories {
        mavenLocal()
    }

    thundra {

    jvmArgsParam = [
            "-javaagent:${thundraAgentPath}"
            ]
        }
}