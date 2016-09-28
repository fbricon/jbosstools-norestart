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

public final class NoRestartConstants {
	
	public static final String NO_RESTART_AGENT_LAUNCHKEY = "org.jboss.tools.norestart.AGENT";
	
	public static final String NO_RESTART_VMARGS_KEY = "no_restart_vmargs";

	public static final String NO_RESTART_CONFIG_KEY = "org.jboss.tools.norestart.CONFIG";

	private NoRestartConstants(){
		//Don't instantiate
	}
}
