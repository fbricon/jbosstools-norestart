package org.jboss.tools.norestart.fakereplace.internal;

import org.eclipse.core.runtime.Plugin;
import org.jboss.tools.norestart.fakereplace.internal.agent.FakeReplaceAgentManager;
import org.jboss.tools.norestart.fakereplace.internal.agent.IFakeReplaceAgentManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FakeReplaceCorePluginActivator extends Plugin {

	private IFakeReplaceAgentManager agentManager;
	
	// The shared instance
	private static FakeReplaceCorePluginActivator plugin;
	
	@Override
  public void start(BundleContext context) throws Exception {
		super.start(context);
		agentManager = new FakeReplaceAgentManager();
		plugin  = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
  public void stop(BundleContext context) throws Exception {
		plugin = null;
		agentManager = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */;
	public static FakeReplaceCorePluginActivator getDefault() {
		return plugin;
	}

	IFakeReplaceAgentManager getFakeReplaceAgentManager() {
		return agentManager;
	}
}
