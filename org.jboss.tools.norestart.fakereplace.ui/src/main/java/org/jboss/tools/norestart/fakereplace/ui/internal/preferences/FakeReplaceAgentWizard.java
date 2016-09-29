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

import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.jboss.tools.norestart.fakereplace.internal.agent.IFakeReplaceAgent;

public class FakeReplaceAgentWizard extends Wizard {

  private final FakeReplaceAgentWizardPage agentPage;

  private IFakeReplaceAgent                     result;

  public FakeReplaceAgentWizard(Set<String> names) {
    this(null, names);
  }

  public FakeReplaceAgentWizard(IFakeReplaceAgent original, Set<String> names) {
    this.agentPage = new FakeReplaceAgentWizardPage(original, names);
    setHelpAvailable(false);
    setWindowTitle(((original == null) ? "Add" : "Edit") + " FakeReplace agent");
  }

  @Override
  public void addPages() {
    addPage(agentPage);
  }

  @Override
  public boolean performFinish() {
    result = agentPage.getResult();
    return true;
  }

  public IFakeReplaceAgent getResult() {
    return result;
  }

}
