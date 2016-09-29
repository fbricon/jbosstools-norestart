package org.jboss.tools.norestart.ui.internal.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NoRestartMainPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  protected void createFieldEditors() {
    setDescription("expand the tree to set No-Restart preferences");
  }

}
