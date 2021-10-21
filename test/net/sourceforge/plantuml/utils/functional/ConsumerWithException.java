package net.sourceforge.plantuml.utils.functional;

public interface ConsumerWithException<T> {
	void accept(T t) throws Exception;
}
