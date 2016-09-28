package org.jboss.tools.norestart.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

public abstract class AbstractNoReplaceLaunchDelegate implements INoRestartLaunchDelegate, IExecutableExtension{

	protected String id;
	
	protected String name;
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		id = config.getAttribute("id");
		name = config.getAttribute("name");
	}
	
	@Override
	public String getName() {
		return name == null?getId():name;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
