package dev.jorel.commandapi.test;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A mutable wrapper of an object, as a list
 *
 * @param <T>
 */
public class Mut<T> {
	
	public static <T> Mut<T> of() {
		return new Mut<T>();
	}

	public static <T> Mut<T> of(T obj) {
		return new Mut<T>(obj);
	}
	
	private final Deque<Optional<T>> value;
	
	private Mut() {
		value = new ArrayDeque<>();
	}
	
	private Mut(T obj) {
		this();
		set(obj);
	}
	
	public void set(T obj) {
		this.value.add(Optional.ofNullable(obj));
	}
	
	public T get() {
		if(this.value.size() == 0) {
			throw new NoSuchElementException();
		} else {
			return this.value.remove().orElse(null);
		}
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
}
