/*******************************************************************************
 * Copyright (c) 2008-2016 Sonatype, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *      Anton Tanasenko - Refactor marker resolutions and quick fixes (Bug #484359)
 *******************************************************************************/
package org.jboss.tools.norestart.ui.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * copied from org.eclipse.m2e.editor.xml.MvnImages
 * @author Eugene Kuleshov
 */
public class Images {

	public static Image NORESTART_ICON = createImage("norestart.gif");

	private static ImageDescriptor create(String key) {
		try {
			ImageDescriptor imageDescriptor = createDescriptor(key);
			ImageRegistry imageRegistry = getImageRegistry();
			if (imageRegistry != null) {
				imageRegistry.put(key, imageDescriptor);
			}
			return imageDescriptor;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static Image createImage(String key) {
		create(key);
		ImageRegistry imageRegistry = getImageRegistry();
		if (imageRegistry == null)
			return null;
		Image img = imageRegistry.get(key);
		if (img == null) {
			create(key);
		}
		return imageRegistry.get(key);
	}

	private static ImageRegistry getImageRegistry() {
		UIActivator plugin = UIActivator.getInstance();
		return plugin == null ? null : plugin.getImageRegistry();
	}

	private static ImageDescriptor createDescriptor(String image) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(UIActivator.PLUGIN_ID, "icons/" + image); //$NON-NLS-1$
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		Image image = Custom.images.get(imageDescriptor);
		if (image == null) {
			synchronized (Custom.images) {
				image = Custom.images.get(imageDescriptor);
				if (image == null) {
					image = imageDescriptor.createImage();
					if (image != null) {
						Custom.images.put(imageDescriptor, image);
					}
				}
			}
		}
		return image;
	}

	static class Custom {
		static final Map<ImageDescriptor, Image> images = new ConcurrentHashMap<>();

		static void dispose() {
			for (Image img : images.values()) {
				img.dispose();
			}
			images.clear();
		}

	}
}
