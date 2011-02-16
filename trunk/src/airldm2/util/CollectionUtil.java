package airldm2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtil {
	public static <T> List<T> makeList() {
		return new ArrayList<T>();
	}
	
	public static <T> Set<T> makeSet() {
		return new HashSet<T>();
	}
	
	public static <K,V> Map<K,V> makeMap() {
		return new HashMap<K,V>();
	}

	public static <T> Set<T> asSet(T... a) {
		return new HashSet<T>(Arrays.asList(a));
	}
}