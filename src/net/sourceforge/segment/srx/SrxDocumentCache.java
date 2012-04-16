package net.sourceforge.segment.srx;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents SRX document cache.
 * Responsible for managing cached data. It can store more than one object 
 * under one key as long as value class is different.
 * 
 * @author loomchild
 */
public class SrxDocumentCache {

	private Map<Object, Object> map;
	
	public SrxDocumentCache() {
		this.map = new ConcurrentHashMap<Object, Object>();
	}
	
	/**
	 * Retrieves object from cache. 
	 * @param <T> value object type
	 * @param key
	 * @param valueClass class of value object
	 * @return value object
	 */
	public <T> T get(Class<T> valueClass, Object... key) {
		@SuppressWarnings("unchecked")
		T value = (T)map.get(getKey(valueClass, key));
		return value;
	}

	/**
	 * Puts an object in cache.
	 * @param <T> value object type
	 * @param key
	 * @param value value object
	 */
	public <T> void put(T value, Class<T> valueClass, Object... key) {
		map.put(getKey(valueClass, key), value);
	}
	
	private Object getKey(Class<?> valueClass, Object... key) {
		Object[] fullKey = Arrays.copyOf(key, key.length + 1);
		fullKey[key.length] = valueClass;
		return fullKey;
	}
	
}
