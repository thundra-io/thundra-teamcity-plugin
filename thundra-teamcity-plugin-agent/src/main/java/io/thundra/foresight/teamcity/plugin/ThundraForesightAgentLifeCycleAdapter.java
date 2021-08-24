package io.thundra.foresight.teamcity.plugin;

import io.thundra.foresight.teamcity.plugin.utils.IBuildToolForesightInitializer;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.EventDispatcher;
import org.apache.commons.lang3.ObjectUtils;
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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static io.thundra.foresight.teamcity.plugin.utils.ThundraUtils.LATEST;
import static io.thundra.foresight.teamcity.plugin.utils.ThundraUtils.THUNDRA_AGENT_BOOTSTRAP_JAR;
import static io.thundra.foresight.teamcity.plugin.utils.ThundraUtils.THUNDRA_AGENT_METADATA;
import static io.thundra.foresight.teamcity.plugin.utils.ThundraUtils.THUNDRA_AGENT_TEST_PROJECT_ID;
import static io.thundra.foresight.teamcity.plugin.utils.ThundraUtils.THUNDRA_APIKEY;

/**
 * @author yusuferdem
 */
public class ThundraForesightAgentLifeCycleAdapter extends AgentLifeCycleAdapter {
    private static final Logger log = LogManager.getLogger(ThundraForesightAgentLifeCycleAdapter.class);

    private final ExtensionHolder extensionHolder;
    private final BuildToolForesightInitializerFactory buildToolForesightInitializerFactory;

    private String agentPath;

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        String thundraApikey = runner.getBuildParameters().getEnvironmentVariables().get(THUNDRA_APIKEY);
        String projectId =
                runner.getBuildParameters().getEnvironmentVariables().get(THUNDRA_AGENT_TEST_PROJECT_ID);
        if (StringUtils.isEmpty(thundraApikey) || StringUtils.isEmpty(projectId)) {
            return;
        }
        IBuildToolForesightInitializer initializer =
                buildToolForesightInitializerFactory.getInitializer(runner.getRunType());
        if (ObjectUtils.isNotEmpty(initializer)) {
            try {
                File workingDirectory = runner.getWorkingDirectory();
                getThundraAgent(workingDirectory.getAbsolutePath());

                agentPath = (initializer.getAgentPath(runner, agentPath));
                initializer.initialize(runner, agentPath);
            } catch (IOException | XMLStreamException e) {
                e.printStackTrace();
            }

        }
        super.beforeRunnerStart(runner);
    }

    public ThundraForesightAgentLifeCycleAdapter(@NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                                                 @NotNull final ExtensionHolder extensionHolder,
                                                 BuildToolForesightInitializerFactory
                                                         buildToolForesightInitializerFactory) {

        this.extensionHolder = extensionHolder;
        this.buildToolForesightInitializerFactory = buildToolForesightInitializerFactory;
        dispatcher.addListener(this);
    }

    @Override
    public void buildStarted(@NotNull AgentRunningBuild build) {
    }



    protected void getThundraAgent(String agentDirPath) throws IOException, XMLStreamException {
        String latestAgentVersion = "";
        BufferedInputStream in = new BufferedInputStream(new URL(THUNDRA_AGENT_METADATA).openStream());
        XMLStreamReader reader1 = XMLInputFactory.newInstance().createXMLStreamReader(in);
        while (reader1.hasNext()) {
            if (reader1.next() == XMLStreamConstants.START_ELEMENT && reader1.getLocalName().equals(LATEST)) {
                latestAgentVersion = reader1.getElementText();
                break;
            }
        }
        log.info("Latest Agent Version : " + latestAgentVersion);
        if (StringUtils.isNotEmpty(latestAgentVersion)) {
            BufferedInputStream agentStream = new BufferedInputStream(
                    new URL(String.format("https://repo.thundra.io/service/local/repositories/thundra-releases" +
                            "/content/io/thundra/agent/thundra-agent-bootstrap/%s/thundra-agent-bootstrap-%s.jar",
                            latestAgentVersion, latestAgentVersion)).openStream());
            String jarPath = agentDirPath + THUNDRA_AGENT_BOOTSTRAP_JAR;
            File file = new File(jarPath);
            Files.copy(agentStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.agentPath = jarPath;
        }
        log.info("Thundra Foresight Instrumentation started");

    }
}
