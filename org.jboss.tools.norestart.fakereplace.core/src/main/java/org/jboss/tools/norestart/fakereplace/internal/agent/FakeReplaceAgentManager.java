package org.jboss.tools.norestart.fakereplace.internal.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.jboss.tools.norestart.core.internal.ResourceUtils;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class FakeReplaceAgentManager implements IFakeReplaceAgentManager {

  private final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];

  private final IPreferencesService   preferenceStore;

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("/agents");
	
  private static final String FAKEREPLACE_FILENAME = BUNDLE.getString("fakereplace");
	
  private IFakeReplaceAgent embeddedAgent;
  
  public FakeReplaceAgentManager() {
    this.preferenceStore = Platform.getPreferencesService();

    this.preferencesLookup[0] = InstanceScope.INSTANCE
        .getNode(IFakeReplaceConstants.PLUGIN_ID);
    this.preferencesLookup[1] = DefaultScope.INSTANCE
        .getNode(IFakeReplaceConstants.PLUGIN_ID);
    
    embeddedAgent = createEmbeddedAgent();
  }


  @Override
  public List<IFakeReplaceAgent> getFakeReplaceAgents() {
    return Collections
        .unmodifiableList(new ArrayList<>(
            getAgents().values()));
  }

  @Override
  public IFakeReplaceAgent getDefaultAgent() {
    String name = preferenceStore.get(IFakeReplaceConstants.DEFAULT_RUNTIME_KEY,
        null, preferencesLookup);
    IFakeReplaceAgent agent = getAgents().get(name);
    if (agent == null) {
      Collection<IFakeReplaceAgent> agents = getAgents().values();
      agent = agents.isEmpty() ? null : agents.iterator().next();
    }
    return agent;
  }

  public void setDefaultAgent(IFakeReplaceAgent runtime) {
    if (runtime == null) {
      preferencesLookup[0].remove(IFakeReplaceConstants.DEFAULT_RUNTIME_KEY);
    } else {
      preferencesLookup[0].put(IFakeReplaceConstants.DEFAULT_RUNTIME_KEY,
          runtime.getName());
    }
    flush();
  }

  @Override
  public IFakeReplaceAgent getAgent(String name) {
    if (name != null) {
      return getAgents().get(name);
    }
    return null;
  }

  public IFakeReplaceAgent createExternalAgent(String name,
      String location)
          throws FakeReplaceAgentValidationException {
    if (name == null) {
      throw new FakeReplaceAgentValidationException(
          "Fakereplace name can't be null");
    }
    if (location == null) {
      throw new FakeReplaceAgentValidationException("Location can't be null");
    }
    IFakeReplaceAgent runtime = new ExternalFakeReplaceAgent(name, location);
    return runtime;
  }

  public IFakeReplaceAgent createEmbeddedAgent()
	          throws FakeReplaceAgentValidationException {
		try {
			String path = ResourceUtils.getEmbeddedFileUrl("org.jboss.tools.norestart.fakereplace.core", FAKEREPLACE_FILENAME);
			String version = MavenPropertiesIdentifier.identifyVersion(new File(path));
			ExternalFakeReplaceAgent agent = new ExternalFakeReplaceAgent("Embedded FakeReplace "+version, path);
			agent.setReadOnly(true);
			return agent;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
}
  
  
  
  public void setAgents(List<IFakeReplaceAgent> agents) {
    removeAgentPreferences();
    Set<String> uniqueNames = new HashSet<>();
    StringBuilder sb = new StringBuilder();
    for (IFakeReplaceAgent agent : agents) {
      if (agent.equals(embeddedAgent)) {
    	  continue;
      }
      String name = agent.getName();
      if (!uniqueNames.add(name)) {
        throw new FakeReplaceAgentValidationException(
            "Fakereplace name must be unique : " + name);
      }
      if (sb.length() > 0) {
        sb.append('|');
      }
      sb.append(name);

      Preferences agentNode = getAgentPreferences(name, true);
      agentNode.put("fakereplace.path", agent.getPath());
    }
    preferencesLookup[0].put(IFakeReplaceConstants.ALL_AGENTS_KEY, sb.toString());
    flush();
  }

  private void removeAgentPreferences() {
    try {
      if (preferencesLookup[0]
          .nodeExists(IFakeReplaceConstants.ALL_AGENTS_NODE_KEY)) {
        preferencesLookup[0].node(IFakeReplaceConstants.ALL_AGENTS_NODE_KEY)
        .removeNode();
      }
    } catch (BackingStoreException ex) {
      // assume the node does not exist
    }
  }

  private Map<String, IFakeReplaceAgent> getAgents() {
    String agentPreference = preferenceStore.get(
        IFakeReplaceConstants.ALL_AGENTS_KEY, null, preferencesLookup);
    Map<String, IFakeReplaceAgent> agents = new LinkedHashMap<>();
    agents.put(embeddedAgent.getName(), embeddedAgent);
    if (agentPreference != null && !agentPreference.isEmpty()) {
      IFakeReplaceAgent agent;
      Preferences preferences;
      String[] names = agentPreference.split("\\|");
      for (String name : names) {
        preferences = getAgentPreferences(name, false);
        if (preferences != null) {
          agent = createAgent(name, preferences);
          if (agent != null) {
            agents.put(agent.getName(), agent);
          }
        }
      }
    }
    return agents;
  }


  private Preferences getAgentPreferences(String name, boolean create) {
    Preferences agentsNode = preferencesLookup[0]
        .node(IFakeReplaceConstants.ALL_AGENTS_KEY);
    try {
      if (agentsNode.nodeExists(name) || create) {
        return agentsNode.node(name);
      }
    } catch (BackingStoreException ex) {
      // assume the node does not exist
    }
    return null;
  }

  private IFakeReplaceAgent createAgent(String name, Preferences preferences) {
    String location = preferences.get("fakereplace.path", null);
    if (location != null) {
      return createExternalAgent(name, location);
    }
    // TODO handle other kind of agents
    return null;
  }

  private void flush() {
    try {
      preferencesLookup[0].flush();
    } catch (BackingStoreException ex) {
      // TODO do nothing
    }
  }
}
