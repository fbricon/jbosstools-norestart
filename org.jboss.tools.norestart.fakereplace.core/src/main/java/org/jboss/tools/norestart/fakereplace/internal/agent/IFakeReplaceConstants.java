package org.jboss.tools.norestart.fakereplace.internal.agent;

public interface IFakeReplaceConstants {

  String PLUGIN_ID        = "org.jboss.tools.norestart.fakereplace.core";

  String PREFERENCE_PREFIX     = PLUGIN_ID + ".preferences";

  String ALL_AGENTS_KEY      = PREFERENCE_PREFIX + ".agents";

  String ALL_AGENTS_NODE_KEY = ALL_AGENTS_KEY + ".node";

  String DEFAULT_RUNTIME_KEY   = ALL_AGENTS_KEY + ".default";

}
