package org.jboss.tools.norestart.fakereplace.internal;

import org.jboss.tools.norestart.fakereplace.internal.agent.IFakeReplaceAgentManager;

public class FakeReplaceCore {

	public static IFakeReplaceAgentManager getFakeReplaceAgentManager() {
		return FakeReplaceCorePluginActivator.getDefault().getFakeReplaceAgentManager();
	}
}
