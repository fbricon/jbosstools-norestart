/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. Corporation and others.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.norestart.core.internal;

import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

public class NoRestartVariableResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null || argument.trim().isEmpty()) {
			return "";
		}
		
		ILaunchConfiguration[] launchConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
		ILaunchConfiguration launchConfig = Stream.of(launchConfigurations)
																		.filter(lc -> matches(lc, argument))
																		.findFirst().orElse(null);
		String noRestartArgs = NoRestartLaunchUtils.getNoRestartVMArgs(launchConfig);
		System.err.println(noRestartArgs);
		return noRestartArgs;
	}

	private boolean matches(ILaunchConfiguration lc, String argument) {
		String key;
		try {
			key = lc.getAttribute(NoRestartConstants.NO_RESTART_CONFIG_KEY, "");
			return argument.equals(key);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

}
