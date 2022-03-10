package io.thundra.plugin.teamcity.foresight.utils;

import io.thundra.plugin.maven.test.instrumentation.MavenInstrumentationProcessor;
import io.thundra.plugin.maven.test.instrumentation.model.AgentParameter;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_AGENT_TEST_PROJECT_ID;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_APIKEY;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_REST_BASE_URL;

/**
 * @author yusuferdem
 */
public class MavenBuildForesightInitializer implements IBuildToolForesightInitializer {
    private static final Logger logger = LogManager.getLogger(MavenBuildForesightInitializer.class);
    public static final String THUNDRA_AGENT_DEBUG_ENABLE = "THUNDRA_AGENT_DEBUG_ENABLE";

    @Override
    public void initialize(BuildRunnerContext runner, String agentPath) {
        try {
            List<String> pomFiles = null;
            try(Stream<Path> paths = Files.walk(Paths.get(runner.getWorkingDirectory().getAbsolutePath()))){
                pomFiles = paths.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().equals("pom.xml")).
                            map(Path::toString).collect(Collectors.toList());}
            logger.info("<Execute> Executing maven instrumentation ...");
            logger.info(String.format("<Execute> Found %s pom.xml files", pomFiles.size()));
            if (pomFiles == null) {
                logger.error("<Error> pom.xml couldn't find for instrumentation ...");
                return;
            }
            String parentPomPath =
                    ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(), "THUNDRA_MAVEN_INSTRUMENTATION_PARENT_POM");

            MavenInstrumentationProcessor.getInstance().process(agentPath, getAgentParameters(runner),
                    pomFiles.toArray(new String[pomFiles.size()]), parentPomPath);

            logger.info("<Execute> Instrumentation is complete");
        } catch (IOException e) {

            logger.error("<Error> pom.xml couldn't find for instrumentation ...");
        }
}

    private List<AgentParameter> getAgentParameters(BuildRunnerContext runner) {
        ArrayList<AgentParameter> agentParameters = new ArrayList<>();
        String apiKeyParam =
                ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(), THUNDRA_APIKEY);
        String projectIdParam =
                ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                        THUNDRA_AGENT_TEST_PROJECT_ID);

        String thundraRestBaseUrlParam = ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                THUNDRA_REST_BASE_URL);

        String thundraAgentDebugEnableParam = ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                THUNDRA_AGENT_DEBUG_ENABLE);
        agentParameters.add(new AgentParameter("-Dthundra.apiKey", apiKeyParam));
        agentParameters.add(new AgentParameter("-Dthundra.agent.test.project.id", projectIdParam));
        if (StringUtils.isNotEmpty(thundraRestBaseUrlParam)) {
            agentParameters.add(new AgentParameter("-Dthundra.agent.report.rest.baseurl", thundraRestBaseUrlParam));
        }
        if (StringUtils.isNotEmpty(thundraAgentDebugEnableParam)) {
            agentParameters.add(new AgentParameter("-Dthundra.agent.debug.enable", thundraAgentDebugEnableParam));
        }
        return agentParameters;
    }

}
