/*************************************************************************************
 * Copyright (c) 2012-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.norestart.fakereplace.internal.agent;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;

public class MavenPropertiesIdentifier {

	private MavenPropertiesIdentifier() {
		//no instanciation
	}
	public static String identifyVersion(File file) throws CoreException {
		try (ZipFile jar = new ZipFile(file)){
			return getVersionFromMetaInf(jar);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("nls")
	protected static String getVersionFromMetaInf(ZipFile jar) throws IOException {
		ZipEntry mavenEntry = jar.getEntry("META-INF/maven");//$NON-NLS-1$
		if (mavenEntry == null) {
			return null;
		}
		String entryName = mavenEntry.getName();
		Enumeration<? extends ZipEntry> zipEntries = jar.entries();
		String version = null;
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();
			if (zipEntry.getName().endsWith("pom.properties")
					&& zipEntry.getName().startsWith(entryName)) {
				Properties props = new Properties();
				props.load(jar.getInputStream(zipEntry));
				version = props.getProperty("version");
				if (version != null) {
					break;
				}
			}
		}
		return version;
	}
}
