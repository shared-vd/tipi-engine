package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.utils.ArrayLong;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Cette classe est juste plus simple a écrire que VariableMap ...
public class VariableMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public static final String ARG_SEPARATOR = ",";

	public VariableMap() {
	}

	/**
	 * Construit une map de paramètre avec une clé et une valeur.
	 *
	 * @param key   une clé
	 * @param value une valeur
	 */
	public VariableMap(String key, Object value) {
		put(key, value);
	}

	/**
	 * Construit une map de paramètre avec deux paires clé-valeur.
	 *
	 * @param key1   la clé de la première paire
	 * @param value1 la valeur de la première paire
	 * @param key2   la clé de la seconde paire
	 * @param value2 la valeur de la seconde paire
	 */
	public VariableMap(String key1, Object value1, String key2, Object value2) {
		put(key1, value1);
		put(key2, value2);
	}

	/**
	 * Prend un String avec une virgule comme séparateur et crée un ArrayLong avec les valeurs
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public Serializable putStringAsArrayLong(String key, String value) {
		return putStringAsArrayLong(key, value, ARG_SEPARATOR);
	}

	public Serializable putStringAsArrayLong(String key, String value, String separator) {
		if (value != null) {
			List<Long> listLong = new ArrayList<Long>();
			String[] values = value.split(separator);
			for (String stringValue : values) {
				Long longValue = Long.parseLong(stringValue);
				listLong.add(longValue);
			}
			ArrayLong arrayLong = new ArrayLong(listLong);
			put(key, arrayLong);
			return arrayLong;
		}
		return null;
	}

}
