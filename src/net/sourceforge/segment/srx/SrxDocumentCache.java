package net.sourceforge.segment.srx;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for storing cached data.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {

	private Map<Class<?>, Map<Object, Object>> map;
	
	public SrxDocumentCache() {
		this.map = new HashMap<Class<?>, Map<Object, Object>>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> valueClass) {
		T value = null;
		Map<Object, Object> klassMap = map.get(valueClass);
		if (klassMap != null) {
			value = (T)klassMap.get(key);
		}
		return value;
	}

	public <T> void put(Object key, T value) {
		Map<Object, Object> klassMap = map.get(value.getClass());
		if (klassMap == null) {
			klassMap = new HashMap<Object, Object>();
			map.put(value.getClass(), klassMap);
		}
		klassMap.put(key, value);
	}
	
}
