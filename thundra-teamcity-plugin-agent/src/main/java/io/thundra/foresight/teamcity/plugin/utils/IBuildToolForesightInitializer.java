package io.thundra.foresight.teamcity.plugin.utils;

import jetbrains.buildServer.agent.BuildRunnerContext;
import org.springframework.stereotype.Component;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Map;

public interface IBuildToolForesightInitializer {

    void initialize(BuildRunnerContext runner, String agentPath);

    String getAgentPath(BuildRunnerContext runner, String agentPath);
}
