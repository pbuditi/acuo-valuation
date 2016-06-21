package com.acuo.valuation.markit.requests;

public class Key<T> {

	private final String identifier;
	private final Class<T> type;

	private Key(String identifier, Class<T> type) {
		this.identifier = identifier;
		this.type = type;
	}

	public static <T> Key<T> key(String identifier, Class<T> type) {
		return new Key<>(identifier, type);
	}

	public String identifier() {
		return identifier;
	}

	public Class<T> type() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key<?> other = (Key<?>) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Key [identifier=" + identifier + ", type=" + type + "]";
	}
}