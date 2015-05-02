package io.github.anon10w1z.anonlang;

/**
 * A variable created in an AnonLang program
 *
 * @param <T> The type of value this is
 */
public final class AnonVariable<T> {
	private T value;

	private AnonVariable(T value) {
		this.value = value;
	}

	public static <T> AnonVariable<T> of(T value) {
		return new AnonVariable<>(value);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Class getType() {
		return value.getClass();
	}
}
