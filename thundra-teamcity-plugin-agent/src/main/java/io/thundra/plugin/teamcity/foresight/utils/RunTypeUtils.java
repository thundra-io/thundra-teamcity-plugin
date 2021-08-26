package io.thundra.plugin.teamcity.foresight.utils;

/**
 * @author yusuferdem
 */
public  class RunTypeUtils {
    public static final String GRADLE_RUNNER = "gradle-runner";
    public static final String MAVEN_RUNNER = "Maven2";

    private RunTypeUtils() {
    }

    public static boolean isGradleRunType(String runType) {
        return GRADLE_RUNNER.equals(runType);
    }

    public static boolean isMavenRunType(String runType) {
        return MAVEN_RUNNER.equals(runType);
    }
}
