package org.jboss.tools.norestart.fakereplace.internal.agent;

public class ExternalFakeReplaceAgent implements IFakeReplaceAgent {

	private String path;
	private String name;
	private boolean isReadOnly;

	public ExternalFakeReplaceAgent(String name, String path) {
		this.name = name;
		this.path = path;
	}

	@Override
	public String toString() {
		return getName() + " [path=" + getPath() + "]";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExternalFakeReplaceAgent other = (ExternalFakeReplaceAgent) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return isReadOnly;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

}
