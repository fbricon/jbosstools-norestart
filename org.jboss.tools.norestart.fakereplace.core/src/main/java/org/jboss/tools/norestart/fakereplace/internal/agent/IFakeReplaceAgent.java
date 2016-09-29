package org.jboss.tools.norestart.fakereplace.internal.agent;

public interface IFakeReplaceAgent {

	String getName();
	
	String getPath();

	boolean isReadOnly();
}
