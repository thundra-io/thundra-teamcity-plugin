package io.thundra.foresight.teamcity.plugin;

import io.thundra.foresight.teamcity.plugin.utils.GradleBuildForesightInitializer;
import io.thundra.foresight.teamcity.plugin.utils.IBuildToolForesightInitializer;
import io.thundra.foresight.teamcity.plugin.utils.MavenBuildForesightInitializer;
import io.thundra.foresight.teamcity.plugin.utils.RunTypeUtils;

/**
 * @author yusuferdem
 */
public class BuildToolForesightInitializerFactory {
    private final MavenBuildForesightInitializer mavenBuildFileModifier;
    private final GradleBuildForesightInitializer gradleBuildForesightInitializer;

    public BuildToolForesightInitializerFactory(MavenBuildForesightInitializer mavenBuildFileModifier, GradleBuildForesightInitializer gradleBuildForesightInitializer) {
        this.mavenBuildFileModifier = mavenBuildFileModifier;
        this.gradleBuildForesightInitializer = gradleBuildForesightInitializer;
    }

    public IBuildToolForesightInitializer getInitializer(String buildTool) {
        if (RunTypeUtils.isMavenRunType(buildTool)) {
            return mavenBuildFileModifier;
        }
        if (RunTypeUtils.isGradleRunType(buildTool)) {
            return gradleBuildForesightInitializer;
        }
        return null;
    }
}
