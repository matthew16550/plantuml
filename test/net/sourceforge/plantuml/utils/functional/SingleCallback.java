package net.sourceforge.plantuml.utils.functional;

/**
 * Like {@link java.util.function.Consumer} but allowing exceptions
 */
public interface SingleCallback<T> {
	void call(T t) throws Exception;
}
