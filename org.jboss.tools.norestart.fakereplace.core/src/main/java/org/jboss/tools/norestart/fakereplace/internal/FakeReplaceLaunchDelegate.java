/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.norestart.fakereplace.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.launching.JavaRuntime;
import org.jboss.tools.norestart.core.internal.AbstractNoReplaceLaunchDelegate;
import org.jboss.tools.norestart.core.internal.NoRestartLaunchUtils;

public class FakeReplaceLaunchDelegate extends AbstractNoReplaceLaunchDelegate {

	@Override
	public String getNoRestartVMArgs(ILaunchConfiguration launchConfig) throws CoreException {
		String javaAgentUrl = FakeReplaceCore.getFakeReplaceAgentManager().getDefaultAgent().getPath();
		StringBuilder noRestartVMArgs = new StringBuilder()
				.append(" -Xbootclasspath/a:\"")
				.append(javaAgentUrl)
				.append("\" -javaagent:\"")
				.append(javaAgentUrl)
				.append("=log=info")
				.append(",packages=")
				.append(getPackages(launchConfig))
				.append(",index-file=")
				.append(getFakereplaceIndex(launchConfig))
				.append("\"");
		
		if ("org.eclipse.pde.ui.RuntimeWorkbench".equals(launchConfig.getType().getIdentifier())) {
			noRestartVMArgs.append(" -Dorg.osgi.framework.bootdelegation=org.fakereplace.*");	
		}
		
		return noRestartVMArgs.toString();
	}

	private String getPackages(ILaunchConfiguration launchConfig) throws CoreException {
		IJavaProject javaProject = JavaRuntime.getJavaProject(launchConfig);
		return NoRestartLaunchUtils.getTopLevelPackages(javaProject)
				.stream()
				.map(IPackageFragment::getElementName)
				.collect(Collectors.joining(";"));
	}

	private String getFakereplaceIndex(ILaunchConfiguration launchConfig) {
		Path parent = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString(),
				".metadata",
				".fakereplace",
				launchConfig.getName().replaceAll(" ", "_"))
				.toAbsolutePath();

		if (Files.notExists(parent)) {
			try {
				Files.createDirectories(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return parent.resolve("fakereplace.index").toString();
	}

}
