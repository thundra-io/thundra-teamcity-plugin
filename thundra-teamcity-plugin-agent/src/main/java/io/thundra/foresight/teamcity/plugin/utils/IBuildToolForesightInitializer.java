package io.thundra.foresight.teamcity.plugin.utils;

import jetbrains.buildServer.agent.BuildRunnerContext;

/**
 * @author yusuferdem
 */
public interface IBuildToolForesightInitializer {

    void initialize(BuildRunnerContext runner, String agentPath);

    String getAgentPath(BuildRunnerContext runner, String agentPath);
}
