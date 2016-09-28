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
package org.jboss.tools.norestart.core.internal;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class NoRestartLaunchUtils {

	private NoRestartLaunchUtils() {
	}

	public static Optional<INoRestartLaunchDelegate> getNoRestartLaunchDelegate(ILaunchConfiguration launchConfig) throws CoreException {
		String norestarter = launchConfig.getAttribute(NoRestartConstants.NO_RESTART_AGENT_LAUNCHKEY, "");
		if (norestarter.isEmpty()) {
			return Optional.empty();
		}
		Optional<INoRestartLaunchDelegate> delegate = readNoRestartLaunchDelegates().filter(ld -> norestarter.equals(ld.getId()))
																					.findFirst();
		return delegate;
	}

	public static String getNoRestartVMArgs(ILaunchConfiguration launchConfig) throws CoreException {
		Optional<INoRestartLaunchDelegate> launchDelegate = getNoRestartLaunchDelegate(launchConfig);
		if (launchDelegate.isPresent()) {
			return launchDelegate.get().getNoRestartVMArgs(launchConfig);
		}
		return "";
	}

	public static Collection<IPackageFragment> getAllTopLevelPackages() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		Set<IPackageFragment> packages = new LinkedHashSet<>();
		for (IProject project : root.getProjects()) {
			if (project.isAccessible()){
				IJavaProject jp = JavaCore.create(project);
				if (jp != null) {
					packages.addAll(getTopLevelPackages(jp));
				}
			}
		}
		return packages;
	}

	public static Collection<IPackageFragment> getTopLevelPackages(IJavaProject javaProject) {
		Set<IPackageFragment> packages = new LinkedHashSet<>();
		try {
			if (javaProject == null || !javaProject.getProject().isAccessible()) {
				return getAllTopLevelPackages();
			}
			for (IPackageFragment f : javaProject.getPackageFragments()){
				if (f.getKind() != IPackageFragmentRoot.K_SOURCE || f.isDefaultPackage() ) {
				  continue;
				}
				if (f.containsJavaResources() || f.getChildren().length > 1) {
					packages.add(f);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return packages;
	}
	
	static Stream<INoRestartLaunchDelegate> readNoRestartLaunchDelegates() {
		Stream<INoRestartLaunchDelegate> result;
	    IExtensionRegistry registry = RegistryFactory.getRegistry();
	    IExtensionPoint extensionPoint = registry.getExtensionPoint("org.jboss.tools.norestart.core.noRestartLaunchDelegates");
	    if(extensionPoint == null) {
	    	result = Stream.empty();
	    } else {
	    	result = Stream.of(extensionPoint.getExtensions())
	    			.map(e -> e.getConfigurationElements())
	    			.flatMap(ces -> Stream.of(ces))
	    			.map(ce -> toDelegate(ce))
	    			.filter(d -> d != null);
	    }
	    return result;
	  }

	private static INoRestartLaunchDelegate toDelegate(IConfigurationElement ce) {
		try {
			return (INoRestartLaunchDelegate) ce.createExecutableExtension("class");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getVMArgs(ILaunchConfiguration launchConfig) throws CoreException {
		String arguments = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
		return arguments;
	}

	public static void addNoRestartVariable(ILaunchConfigurationWorkingCopy launchConfig) {
		String vmArgs;
		try {
			vmArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			if (!vmArgs.contains(NoRestartConstants.NO_RESTART_VMARGS_KEY)) {
				String key = getOrCreateKey(launchConfig);
				String var = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(NoRestartConstants.NO_RESTART_VMARGS_KEY, key);
				String newVmArgs = var+" "+vmArgs;
				launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, newVmArgs);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void removeNoRestartVariable(ILaunchConfigurationWorkingCopy launchConfig) {
		String vmArgs;
		try {
			vmArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			String keyPrefix = "${"+NoRestartConstants.NO_RESTART_VMARGS_KEY;
			int start = vmArgs.indexOf(keyPrefix);
			if (start > -1) {
				int end = vmArgs.indexOf("}", start);
				if (end < -1) {
					end = vmArgs.indexOf(" ", start);
				}
				if (end < -1) {
					end = vmArgs.length();
				}
				if (end < vmArgs.length()) {
					end++;
				}
				String newVmArgs = vmArgs.substring(0, start)+vmArgs.substring(end, vmArgs.length());
				launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, newVmArgs.trim());
			}		
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static String getOrCreateKey(ILaunchConfigurationWorkingCopy launchConfig) {
		String key = getKey(launchConfig);
		if (key == null) {
			key = UUID.randomUUID().toString();
			launchConfig.setAttribute(NoRestartConstants.NO_RESTART_CONFIG_KEY, key);
		}
		return key;
	}
	
	public static String getKey(ILaunchConfiguration launchConfig) {
		try {
			return launchConfig.getAttribute(NoRestartConstants.NO_RESTART_CONFIG_KEY, (String) null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isNoRestartEnabled(ILaunchConfiguration launchConfig) {
		try {
			String vmArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			return vmArgs.contains(NoRestartConstants.NO_RESTART_VMARGS_KEY);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
