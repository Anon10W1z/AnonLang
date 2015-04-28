public class AnonField<T> {
	private T value;

	public static <T> AnonField<T> of(T t) {
		AnonField<T> field = new AnonField<>();
		field.setValue(t);
		return field;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Class getClassType() {
		return value.getClass();
	}
}
