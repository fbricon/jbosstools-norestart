/*****************************************************************************
 * Copyright (c) 2008-2014 Sonatype, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation for m2e
 *                                   (http://eclipse.org/m2e)
 *      Fred Bricon (Red Hat, Inc) - Adapted m2e preference page
 *                                   for Vert.X agents
 *****************************************************************************/

package org.jboss.tools.norestart.fakereplace.ui.internal.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.norestart.fakereplace.internal.FakeReplaceCore;
import org.jboss.tools.norestart.fakereplace.internal.agent.FakeReplaceAgentManager;
import org.jboss.tools.norestart.fakereplace.internal.agent.IFakeReplaceAgent;


/**
 * FakeReplace agents preference page
 *
 * @author Eugene Kuleshov
 * @author Fred Bricon
 */
public class FakeReplaceAgentsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private final FakeReplaceAgentManager agentManager;

  private String                    defaultAgent;

  private List<IFakeReplaceAgent>       agents;

  private CheckboxTableViewer       agentsViewer;

  public FakeReplaceAgentsPreferencePage() {
    setTitle("FakeReplace Agents");
    this.agentManager = (FakeReplaceAgentManager) FakeReplaceCore.getFakeReplaceAgentManager();
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  protected void performDefaults() {
    loadAgents();
    super.performDefaults();
  }

  protected void loadAgents() {
    IFakeReplaceAgent agent = agentManager.getDefaultAgent();
    defaultAgent = agent == null ? "" : agent.getName();
    agents = new ArrayList<IFakeReplaceAgent>(agentManager.getFakeReplaceAgents());
    agentsViewer.setInput(agents);
    refreshAgentsViewer();
  }

  @Override
  public boolean performOk() {
    agentManager.setAgents(agents);
    agentManager.setDefaultAgent(getDefaultAgent());
    return true;
  }

  @Override
  protected Control createContents(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout(3, false);
    gridLayout.marginBottom = 5;
    gridLayout.marginRight = 5;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    composite.setLayout(gridLayout);

    Label link = new Label(composite, SWT.NONE);
    link.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));
    link.setText("Add, Remove and select default FakeReplace agents");

    createTable(composite);
    new Label(composite, SWT.NONE);

    loadAgents();
    return composite;
  }

  private IFakeReplaceAgent getDefaultAgent() {
    if (defaultAgent != null && !defaultAgent.trim().isEmpty()) {
      for (IFakeReplaceAgent agent : agents) {
        if (defaultAgent.equals(agent.getName())) {
          return agent;
        }
      }
    }
    return agents.isEmpty() ? null : agents.get(0);
  }

  protected void refreshAgentsViewer() {
    agentsViewer.refresh(); // should listen on property changes instead?

    Object[] checkedElements = agentsViewer.getCheckedElements();
    if(checkedElements == null || checkedElements.length == 0) {
      IFakeReplaceAgent agent = getDefaultAgent();
      if (agent != null) {
        agentsViewer.setChecked(agent, true);
        defaultAgent = agent.getName();
      }
    }

    for(TableColumn column : agentsViewer.getTable().getColumns()) {
      column.pack();
    }
  }

  protected IFakeReplaceAgent getSelectedFakeReplaceAgent() {
    IStructuredSelection sel = (IStructuredSelection) agentsViewer.getSelection();
    return (IFakeReplaceAgent) sel.getFirstElement();
  }

  private void createTable(Composite composite) {
    agentsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);

    agentsViewer.setLabelProvider(new AgentsLabelProvider());

    agentsViewer.setContentProvider(new IStructuredContentProvider() {

      @Override
      public Object[] getElements(Object input) {
        if(input instanceof List<?>) {
          List<?> list = (List<?>) input;
          if(!list.isEmpty()) {
            return list.toArray(new IFakeReplaceAgent[list.size()]);
          }
        }
        return new Object[0];
      }

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }

      @Override
      public void dispose() {
      }

    });

    Table table = agentsViewer.getTable();
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3);
    gd_table.heightHint = 151;
    gd_table.widthHint = 333;
    table.setLayoutData(gd_table);

    TableColumn tblclmnName = new TableColumn(table, SWT.NONE);
    tblclmnName.setWidth(100);
    tblclmnName.setText("Name");

    TableColumn tblclmnHome = new TableColumn(table, SWT.NONE);
    tblclmnHome.setWidth(120);
    tblclmnHome.setText("Path");

    Button addButton = new Button(composite, SWT.NONE);
    addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    addButton.setText("Add...");
    addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        FakeReplaceAgentWizard wizard = new FakeReplaceAgentWizard(getForbiddenNames(null));
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        if(dialog.open() == Window.OK) {
          agents.add(wizard.getResult());
          refreshAgentsViewer();
        }
      }
    });

    final Button editButton = new Button(composite, SWT.NONE);
    editButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    editButton.setEnabled(false);
    editButton.setText("Edit...");
    editButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IFakeReplaceAgent agent = getSelectedFakeReplaceAgent();
        FakeReplaceAgentWizard wizard = new FakeReplaceAgentWizard(agent, getForbiddenNames(agent));
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        if(dialog.open() == Window.OK) {
          IFakeReplaceAgent updatedAgent = wizard.getResult();
          for(int i = 0; i < agents.size(); i++ ) {
            if(agent == agents.get(i)) {
              agents.set(i, updatedAgent);
              break;
            }
          }
          refreshAgentsViewer();
        }
      }
    });

    final Button removeButton = new Button(composite, SWT.NONE);
    removeButton.setEnabled(false);
    removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    removeButton.setText("Remove");
    removeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IFakeReplaceAgent agent = getSelectedFakeReplaceAgent();
        if (!agent.isReadOnly()) {
        	agents.remove(agent);
        	refreshAgentsViewer();
        }
      }
    });

    agentsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        if(agentsViewer.getSelection() instanceof IStructuredSelection) {
          IFakeReplaceAgent agent = getSelectedFakeReplaceAgent();
          removeButton.setEnabled(agent != null && !agent.isReadOnly());
          editButton.setEnabled(agent != null);
        }
      }
    });

    agentsViewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        setCheckedAgent((IFakeReplaceAgent) event.getElement());
      }
    });
  }

  protected Set<String> getForbiddenNames(IFakeReplaceAgent agent) {
    Set<String> names = new HashSet<>(agents.size());
    for(IFakeReplaceAgent other : agents) {
      if(other != agent) {
        names.add(other.getName());
      }
    }
    return names;
  }

  protected void setCheckedAgent(IFakeReplaceAgent agent) {
    agentsViewer.setAllChecked(false);
    if (agent == null /* || !agent.isAvailable() */) {
      agent = getDefaultAgent();
    } else {
      defaultAgent = agent.getName();
    }
    agentsViewer.setChecked(agent, true);
  }

  static class AgentsLabelProvider implements ITableLabelProvider, IColorProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
      IFakeReplaceAgent agent = (IFakeReplaceAgent) element;
      switch(columnIndex) {
        case 0:
          return agent.getName();
        case 1:
          return agent.getPath();
      }
      return null;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public Color getBackground(Object element) {
      return null;
    }

    @Override
    public Color getForeground(Object element) {
      return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }
  }

}
