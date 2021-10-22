package net.sourceforge.plantuml.test.functional;

@FunctionalInterface
public interface ConsumerWithException<T> {
	void accept(T t) throws Exception;
}
