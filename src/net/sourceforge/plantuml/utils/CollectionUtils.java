package net.sourceforge.plantuml.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

public class CollectionUtils {

	@SafeVarargs
	public static <E> List<E> unmodifiableListOf(E... elements) {
		return unmodifiableList(asList(elements));
	}
}
