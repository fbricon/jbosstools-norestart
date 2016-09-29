package org.jboss.tools.norestart.fakereplace.internal.agent;

import java.util.List;


public interface IFakeReplaceAgentManager {

  IFakeReplaceAgent getDefaultAgent();

  List<IFakeReplaceAgent> getFakeReplaceAgents();

  IFakeReplaceAgent getAgent(String name);

}
