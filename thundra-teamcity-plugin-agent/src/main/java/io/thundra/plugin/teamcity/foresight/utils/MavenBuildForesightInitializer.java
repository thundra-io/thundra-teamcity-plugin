package io.thundra.plugin.teamcity.foresight.utils;

import io.thundra.plugin.maven.test.instrumentation.checker.FailsafeChecker;
import io.thundra.plugin.maven.test.instrumentation.checker.SurefireChecker;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_AGENT_TEST_PROJECT_ID;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_APIKEY;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_REST_BASE_URL;

/**
 * @author yusuferdem
 */
public class MavenBuildForesightInitializer implements IBuildToolForesightInitializer {
    private static final Logger logger = LogManager.getLogger(MavenBuildForesightInitializer.class);

    @Override
    public void initialize(BuildRunnerContext runner, String agentPath) {
        File[] matchingFiles = runner.getWorkingDirectory().listFiles((dir, name) -> name.equals("pom.xml"));
        if (matchingFiles == null) {
            logger.error("<Error> pom.xml couldn't find for instrumentation ...");
            return;
        }
        String[] pomFiles = Arrays.stream(matchingFiles).map(File::getAbsolutePath).toArray(String[]::new);

        logger.info("<Execute> Executing maven instrumentation ...");
        logger.info(String.format("<Execute> Found %s pom.xml files", pomFiles.length));

        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        SurefireChecker surefireChecker = new SurefireChecker();
        FailsafeChecker failsafeChecker = new FailsafeChecker();

        // A toggle to check once the loop ends
        AtomicBoolean surefireInstrumented = new AtomicBoolean();
        AtomicBoolean failsafeInstrumented = new AtomicBoolean();

        logger.info("<Execute> Processing the pom files");

        for (String pomFile : pomFiles) {
            logger.debug(String.format("<Execute> Processing %s", pomFile));

            // Start checking and processing the pom files
            logger.debug(String.format("<Execute> Checking %s for Surefire plugin", pomFile));
            // Check for Surefire
            surefireChecker.checkProfiles(logger, mavenReader, agentPath, pomFile, true);
            surefireChecker.checkPom(logger, mavenReader, agentPath, pomFile, true);
            surefireInstrumented.set(surefireChecker.instrumented.get() || surefireInstrumented.get());

            logger.debug(String.format("<Execute> Checking %s for Failsafe plugin", pomFile));
            // Check for Failsafe
            failsafeChecker.checkProfiles(logger, mavenReader, agentPath, pomFile, true);
            failsafeChecker.checkPom(logger, mavenReader, agentPath, pomFile, true);
            failsafeInstrumented.set(failsafeChecker.instrumented.get() || failsafeInstrumented.get());
        }

        logger.info("<Execute> Instrumentation is complete");
    }

    @Override
    public String getAgentPath(BuildRunnerContext runner, String agentPath) {
        String projectId = runner.getBuildParameters().getEnvironmentVariables().get(THUNDRA_AGENT_TEST_PROJECT_ID);
        String apiKey = runner.getBuildParameters().getEnvironmentVariables().get(THUNDRA_APIKEY);
        String thundraRestBaseUrl = runner.getBuildParameters().getEnvironmentVariables().get(THUNDRA_REST_BASE_URL);
        String restBaseUrlParam = StringUtils.isNotEmpty(thundraRestBaseUrl) ? " -Dthundra.agent.report.rest.baseurl="
                + thundraRestBaseUrl : "";
        agentPath = agentPath + restBaseUrlParam;
        agentPath += (String.format(" -Dthundra.apiKey=%s -Dthundra.agent.test.project.id=%s", apiKey, projectId));

        return agentPath;
    }

}
