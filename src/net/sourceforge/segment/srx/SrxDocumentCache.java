package net.sourceforge.segment.srx;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents SRX document cache.
 * Responsible for managing cached data. It can store more than one object 
 * under one key as long as value class is different.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {

	private Map<Class<?>, Map<Object, Object>> map;
	
	public SrxDocumentCache() {
		this.map = new HashMap<Class<?>, Map<Object, Object>>();
	}
	
	/**
	 * Retrieves object from cache. 
	 * @param <T> value object type
	 * @param key
	 * @param valueClass class of value object
	 * @return value object
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> valueClass) {
		T value = null;
		Map<Object, Object> klassMap = map.get(valueClass);
		if (klassMap != null) {
			value = (T)klassMap.get(key);
		}
		return value;
	}

	/**
	 * Puts an object in cache.
	 * @param <T> value object type
	 * @param key
	 * @param value value object
	 */
	public <T> void put(Object key, T value) {
		Map<Object, Object> klassMap = map.get(value.getClass());
		if (klassMap == null) {
			klassMap = new HashMap<Object, Object>();
			map.put(value.getClass(), klassMap);
		}
		klassMap.put(key, value);
	}
	
}
