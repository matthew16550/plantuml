package net.sourceforge.plantuml.utils.functional;

/**
 * Like {@link java.util.function.BiConsumer} but allowing exceptions
 */
public interface BiCallback<T, U> {
	void call(T t, U u) throws Exception;
}
