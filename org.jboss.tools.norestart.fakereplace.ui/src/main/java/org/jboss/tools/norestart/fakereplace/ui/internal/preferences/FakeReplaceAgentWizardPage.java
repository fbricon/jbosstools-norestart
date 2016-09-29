/*******************************************************************************
 * Copyright (c) 2014-2016 Takari, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Takari, Inc. - initial API and implementation for m2e
 *      Red Hat, Inc. - Copy/Modify wizard for FakeReplace agents
 *******************************************************************************/

package org.jboss.tools.norestart.fakereplace.ui.internal.preferences;

import java.io.File;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.norestart.fakereplace.internal.FakeReplaceCore;
import org.jboss.tools.norestart.fakereplace.internal.agent.FakeReplaceAgentManager;
import org.jboss.tools.norestart.fakereplace.internal.agent.IFakeReplaceAgent;
import org.jboss.tools.norestart.fakereplace.internal.agent.MavenPropertiesIdentifier;


public class FakeReplaceAgentWizardPage extends WizardPage {

  private Text location;

  private Text name;

  private IFakeReplaceAgent original;

  private Button btnDirectory;

  private Set<String> usedNames;

  private boolean editable;

  public FakeReplaceAgentWizardPage(IFakeReplaceAgent original, Set<String> usedNames) {
    super("Add/Edit FakeReplace agent");
    this.original = original;
    this.usedNames = usedNames;
    editable = original == null? true: !original.isReadOnly();

    setDescription(original == null ? "Add FakeReplace agent"
        : "Edit FakeReplace agent");
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    setControl(container);
    container.setLayout(new GridLayout(3, false));

    Composite composite = new Composite(container, SWT.NONE);
    RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
    rl_composite.fill = true;
    composite.setLayout(rl_composite);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3,
        1));

    Label lblInstallationLocation = new Label(container, SWT.NONE);
    lblInstallationLocation.setText("Home directory");

    location = new Text(container, SWT.BORDER);
    location.setEditable(editable);
    location.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateNameVersion(location.getText().trim());
        updateStatus();
      }
    });
    location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    btnDirectory = new Button(container, SWT.NONE);
    btnDirectory.setEnabled(editable);
    btnDirectory.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        selectLocationAction();
      }
    });
    btnDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    btnDirectory.setText("Browse...");

    Label lblInstallationName = new Label(container, SWT.NONE);
    lblInstallationName.setText("Name");

    name = new Text(container, SWT.BORDER);
    name.setEditable(editable);
    name.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateStatus();
      }
    });
    name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    if (original != null) {
      location.setText(original.getPath());
      name.setText(original.getName());
    }

    updateStatus();
  }

  protected void selectLocationAction() {
    FileDialog dlg = new FileDialog(getShell());
    dlg.setFilterPath(location.getText());
    dlg.setText("Select a FakeReplace distribution jar");
    String dir = dlg.open();
    if(dir == null) {
      return;
    }
    location.setText(dir);
    updateNameVersion(dir);
  }

  private void updateNameVersion(String dir) {
    File d = new File(dir);
    if (!d.exists() || !d.isFile()) {
      return;
    }
    String v = findVersion(d);
    String fileName = new File(dir).getName();
    int i = fileName.lastIndexOf(".jar");
    String n = fileName.substring(0, i)+" "+v;
    name.setText(n);
  }

  private String findVersion(File file) {
    try {
		return MavenPropertiesIdentifier.identifyVersion(file);
	} catch (CoreException e) {
		e.printStackTrace();
		return "Unknown";
	}
  }



  protected void updateStatus() {
    setPageComplete(false);

    if (location.getText().trim().isEmpty()) {
      setErrorMessage("Location must not be empty");
      return;
    }

    String n = name.getText().trim();
    if (n.isEmpty()) {
      setMessage("Name must not be empty");
      return;
    }

    if (usedNames != null && usedNames.contains(n)) {
      setErrorMessage(n + " already exists");
      return;
    }

    setMessage("Select a FakeReplace distribution jar");
    setErrorMessage(null);
    setPageComplete(true);
  }

  public IFakeReplaceAgent getResult() {
    return ((FakeReplaceAgentManager) FakeReplaceCore.getFakeReplaceAgentManager())
        .createExternalAgent(name.getText(), location.getText());
  }
}
