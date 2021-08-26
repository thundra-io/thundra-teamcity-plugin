package io.thundra.plugin.teamcity.foresight;

import io.thundra.plugin.teamcity.foresight.utils.GradleBuildForesightInitializer;
import io.thundra.plugin.teamcity.foresight.utils.IBuildToolForesightInitializer;
import io.thundra.plugin.teamcity.foresight.utils.MavenBuildForesightInitializer;
import io.thundra.plugin.teamcity.foresight.utils.RunTypeUtils;

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
