package io.thundra.plugin.teamcity.foresight.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.GRADLE_CMD_PARAMS;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.GRADLE_PLUGIN_METADATA;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRAINIT_FTLH;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRAINIT_GRADLE_FILE;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_AGENT_PATH;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_AGENT_TEST_PROJECT_ID;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_APIKEY;
import static io.thundra.plugin.teamcity.foresight.utils.ThundraUtils.THUNDRA_GRADLE_PLUGIN_VERSION;

/**
 * @author yusuferdem
 */
public class GradleBuildForesightInitializer implements IBuildToolForesightInitializer{
    private static final Logger logger = LogManager.getLogger(GradleBuildForesightInitializer.class);

    @Override
    public void initialize(BuildRunnerContext runner, String agentPath) {
        try {
            String thundraApikey =
                    ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(), THUNDRA_APIKEY);
            String projectId =
                    ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                            THUNDRA_AGENT_TEST_PROJECT_ID);
            if (StringUtils.isEmpty(thundraApikey) || StringUtils.isEmpty(projectId)) {
                return;
            }
            File workingDirectory = runner.getWorkingDirectory();

            final String thundraGradlePluginVersion = getPluginVersion();
            final String initScriptPath = workingDirectory.getAbsolutePath();

            final Configuration cfg = getFreemarkerConfiguration();

            final Map<String, String> root = new HashMap<>();
            String gradleVersionFromEnv = ThundraUtils.getEnvVar(runner.getBuildParameters().getEnvironmentVariables(),
                    THUNDRA_GRADLE_PLUGIN_VERSION);
            root.put(THUNDRA_GRADLE_PLUGIN_VERSION,
                    StringUtils.isEmpty(gradleVersionFromEnv) ? thundraGradlePluginVersion : gradleVersionFromEnv);
            root.put(THUNDRA_AGENT_PATH, agentPath);

            final Template template = cfg.getTemplate(THUNDRAINIT_FTLH);
            String initScriptName = initScriptPath + File.separator
                    +  THUNDRAINIT_GRADLE_FILE;
            final Writer fileOut = new FileWriter(initScriptName);
            template.process(root, fileOut);
            addInitScriptParameters(runner, initScriptName);
        } catch (Exception ex) {
            logger.info("Thundra foresight gradle initialization failed", ex);
        }

    }

    @NotNull
    private Configuration getFreemarkerConfiguration() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(this.getClass(), "/META-INF/template");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        return cfg;
    }

    private String getPluginVersion() throws XMLStreamException, IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(GRADLE_PLUGIN_METADATA).openStream());
        XMLStreamReader reader1 = XMLInputFactory.newInstance().createXMLStreamReader(in);
        String latestPluginVersion = "";
        while (reader1.hasNext()) {
            if (reader1.next() == XMLStreamConstants.START_ELEMENT && reader1.getLocalName().equals("latest")) {
                latestPluginVersion = reader1.getElementText();
                break;
            }
        }
        logger.info("Latest Plugin Version : " + latestPluginVersion);
        return latestPluginVersion;
    }

    private void addInitScriptParameters(BuildRunnerContext runner, String initScriptPath) {
        if (StringUtils.isNotEmpty(initScriptPath)) {
            String params = runner.getRunnerParameters().getOrDefault(GRADLE_CMD_PARAMS, "");
            String initScriptParams = "--init-script " + initScriptPath;

            if (!params.contains(initScriptParams)) {
                runner.addRunnerParameter(GRADLE_CMD_PARAMS, initScriptParams + " " + params);
            }
        }
    }
}
