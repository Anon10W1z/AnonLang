package io.github.anon10w1z.anonlang;

public class AnonVariable<T> {
	private T value;

	public static <T> AnonVariable<T> of(T t) {
		AnonVariable<T> field = new AnonVariable<>();
		field.setValue(t);
		return field;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
