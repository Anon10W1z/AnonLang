package io.github.anon10w1z.anonlang;

/**
 * A variable created in an AnonLang program
 */
public final class AnonVariable {
	private Object value;

	private AnonVariable(Object value) {
		this.value = value;
	}

	public static AnonVariable of(Object value) {
		return new AnonVariable(value);
	}

	public Object getValue() {
		return value;
	}

	public Class getType() {
		return value.getClass();
	}
}
