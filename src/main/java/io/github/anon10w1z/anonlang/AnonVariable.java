package io.github.anon10w1z.anonlang;

/**
 * A variable created by an AnonLang program
 */
public final class AnonVariable {
	/**
	 * The value of this variable
	 */
	private Object value;

	private AnonVariable(Object value) {
		this.value = value;
	}

	/**
	 * Creates a new AnonVariable with the specified value
	 *
	 * @param value The value to assign to this variable
	 *
	 * @return A new AnonVariable with the specified value
	 */
	public static AnonVariable of(Object value) {
		return new AnonVariable(value);
	}

	/**
	 * Returns the value of this variable
	 *
	 * @return The value of this variable
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value of this variable to the specified object
	 *
	 * @param value The new value of this variable
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the class type of this variable's value
	 *
	 * @return The class type of this variable's value
	 */
	public Class getType() {
		return value.getClass();
	}
}
