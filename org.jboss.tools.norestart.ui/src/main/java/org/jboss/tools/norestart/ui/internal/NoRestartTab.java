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
package org.jboss.tools.norestart.ui.internal;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jboss.tools.norestart.core.internal.NoRestartConstants;
import org.jboss.tools.norestart.core.internal.NoRestartLaunchUtils;

public class NoRestartTab extends AbstractLaunchConfigurationTab {

	private Button enableNoRestartButton;

	@Override
	public void createControl(Composite parent) {
	    Composite body = new Composite(parent, SWT.NONE);
	    GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(body);
	    GridDataFactory.fillDefaults().applyTo(body);
	    setControl(body);
	    
	    Group group = new Group(body, SWT.NONE);
	    GridLayoutFactory.fillDefaults().applyTo(group);
	    GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
	    
	    enableNoRestartButton = new Button(group, SWT.CHECK);
	    enableNoRestartButton.setText("Enable No-Restart");
	    enableNoRestartButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		setDirty(true);
	    		updateLaunchConfigurationDialog();
	    	}
		});
	    Link link = new Link(group, SWT.WRAP);
		link.setText("Injects the <a>Fakereplace</a> java agent to the VM arguments.");
		link.addSelectionListener(new OpenPreferencesListener());
	}

	@Override
	public String getName() {
		return "No-Restart";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration launchConfig) {
			boolean noRestartEnabled = NoRestartLaunchUtils.isNoRestartEnabled(launchConfig);
			enableNoRestartButton.setSelection(noRestartEnabled);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy launchConfig) {
		if (enableNoRestartButton.getSelection()) {
			NoRestartLaunchUtils.addNoRestartVariable(launchConfig);
			//TODO get implementor value 
			launchConfig.setAttribute(NoRestartConstants.NO_RESTART_AGENT_LAUNCHKEY, "fakereplace");
		} else {
			NoRestartLaunchUtils.removeNoRestartVariable(launchConfig);
			launchConfig.removeAttribute(NoRestartConstants.NO_RESTART_AGENT_LAUNCHKEY);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy launchCopy) {
		
	}

	@Override
	public Image getImage() {
		return Images.NORESTART_ICON;
	}
	
	private static class OpenPreferencesListener extends SelectionAdapter {

		private static final String PREF_PAGE_ID = "org.jboss.tools.norestart.ui.preferences.fakereplace.FakeReplaceAgentsPreferencePage";
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
							PREF_PAGE_ID,
							new String[] {PREF_PAGE_ID},
							null
							).open();
				}
			});
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	}
}
