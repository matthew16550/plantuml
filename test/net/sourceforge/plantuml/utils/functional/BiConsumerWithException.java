package net.sourceforge.plantuml.utils.functional;

public interface BiConsumerWithException<T, U> {
	void accept(T t, U u) throws Exception;
}
