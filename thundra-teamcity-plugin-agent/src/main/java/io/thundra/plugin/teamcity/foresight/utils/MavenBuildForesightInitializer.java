package io.thundra.plugin.teamcity.foresight.utils;

import io.thundra.plugin.maven.test.instrumentation.checker.FailsafeChecker;
import io.thundra.plugin.maven.test.instrumentation.checker.SurefireChecker;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
            parentPomPath = parentPomPath != null ? parentPomPath : "./pom.xml";
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
                surefireChecker.checkProfiles(logger, mavenReader, agentPath, pomFile, false);
                surefireChecker.checkPom(logger, mavenReader, agentPath, pomFile, false);
                surefireInstrumented.set(surefireChecker.instrumented.get() || surefireInstrumented.get());

                logger.debug(String.format("<Execute> Checking %s for Failsafe plugin", pomFile));
                // Check for Failsafe
                failsafeChecker.checkProfiles(logger, mavenReader, agentPath, pomFile, false);
                failsafeChecker.checkPom(logger, mavenReader, agentPath, pomFile, false);
                failsafeInstrumented.set(failsafeChecker.instrumented.get() || failsafeInstrumented.get());
            }

            if (!surefireInstrumented.get()) {
                logger.info("<Execute> Couldn't find any configuration for Surefire");
                logger.info("<Execute> Adding Surefire to parent pom manually ...");

                // If we couldn't instrument at all, add surefire plugin to the parent pom,
                // relative to the current directory (./pom.xml)
                File parentPom = new File(parentPomPath);
                if (parentPom.exists() && parentPom.isFile()) {
                    surefireChecker.checkPom(logger, mavenReader, agentPath, parentPomPath, true);
                    logger.info("<Execute> Parent pom is instrumented");
                } else {
                    logger.warn("<Execute> Couldn't find parent pom at " + parentPomPath);
                    logger.warn("<Execute> Instrumentation failed");
                }
            }

            logger.info("<Execute> Instrumentation is complete");
        } catch (IOException e) {

            logger.error("<Error> pom.xml couldn't find for instrumentation ...");
        }
}

    @Override
    public String getAgentPath(BuildRunnerContext runner, String agentPath) {
        String apiKey =
                ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(), THUNDRA_APIKEY);
        String projectId =
                ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                        THUNDRA_AGENT_TEST_PROJECT_ID);

        String thundraRestBaseUrl = ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                THUNDRA_REST_BASE_URL);
        String restBaseUrlParam = StringUtils.isNotEmpty(thundraRestBaseUrl) ? " -Dthundra.agent.report.rest.baseurl="
                + thundraRestBaseUrl : "";
        agentPath = agentPath + restBaseUrlParam;
        agentPath += (String.format(" -Dthundra.apiKey=%s -Dthundra.agent.test.project.id=%s" +
                " -Dthundra.agent.test.run.id=%s", apiKey, projectId, UUID.randomUUID().toString()));

        return agentPath;
    }

}
