package io.thundra.plugin.teamcity.foresight.utils;

import jetbrains.buildServer.agent.BuildRunnerContext;

/**
 * @author yusuferdem
 */
public interface IBuildToolForesightInitializer {

    void initialize(BuildRunnerContext runner, String agentPath);

    String getAgentPath(BuildRunnerContext runner, String agentPath);
}
