package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.utils.ArrayLong;

import java.io.Serializable;
import java.util.*;

public class VariableMap implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ARG_SEPARATOR = ",";
    private final Map<String, Serializable> variables = new HashMap<>();

    public VariableMap() {
    }

    /**
     * Construit une map de paramètre avec une clé et une valeur.
     *
     * @param key   une clé
     * @param value une valeur
     */
    public VariableMap(String key, Serializable value) {
        variables.put(key, value);
    }

    /**
     * Construit une map de paramètre avec deux paires clé-valeur.
     *
     * @param key1   la clé de la première paire
     * @param value1 la valeur de la première paire
     * @param key2   la clé de la seconde paire
     * @param value2 la valeur de la seconde paire
     */
    public VariableMap(String key1, Serializable value1, String key2, Serializable value2) {
        variables.put(key1, value1);
        variables.put(key2, value2);
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
            variables.put(key, arrayLong);
            return arrayLong;
        }
        return null;
    }

    public void put(String key, Serializable value) {
        variables.put(key, value);
    }

    public Set<String> keySet() {
        return variables.keySet();

    }

    public Serializable get(String key) {
        return variables.get(key);
    }
}
