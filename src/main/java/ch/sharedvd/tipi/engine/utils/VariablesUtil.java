package ch.sharedvd.tipi.engine.utils;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VariableUtil permet de transformer des variables String en provenance
 * du web ou de cmd dans le bon type de variable.
 * Exemple : Si le TopProcessMetaModel défini une variable "ind" de type ArrayLong, la clé-valeur "ind:306606" créera une
 * variable ind de type ArrayLong avec la valeur 306606 dedans.
 *
 * @author dgo
 */
public class VariablesUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(VariablesUtil.class);

    public static VariableMap buildVariableMapFromString(TopProcessMetaModel model, Map<String, Object> aParams, String separator) {

        final VariableMap vars = new VariableMap();

        // on traite tous les paramètres renseignés explicitement
        for (Map.Entry<String, Object> entry : aParams.entrySet()) {

            final String key = entry.getKey();
            final Object val = entry.getValue();

            if (val instanceof String) {
                VariableDescription variableDescription = isKnownVariable(model.getVariablesDescription(), key);
                if (variableDescription != null) {
                    Serializable value = parseValue((String) val, variableDescription.getVariableType(), separator);
                    if (value != null) {
                        vars.put(key, value);
                    }
                }
            } else if (val instanceof Serializable) {
                vars.put(key, (Serializable) val);
            } else {
                LOGGER.warn("VariableType [" + key + "] non serializable");
            }
        }

        // on ajoute toutes les valeurs par défaut des paramètres non-renseignés explicitement
        addDefaultVariables(model, separator, vars);

        return vars;
    }

    public static VariableMap buildDefaultVariablesMap(TopProcessMetaModel model) {

        final VariableMap vars = new VariableMap();
        addDefaultVariables(model, ",", vars);

        return vars;
    }

    private static void addDefaultVariables(TopProcessMetaModel model, String separator, VariableMap vars) {
        // on ajoute toutes les valeurs par défaut des paramètres non-renseignés explicitement
        final List<VariableDescription> list = model.getVariablesDescription();
        if (list != null) {
            for (VariableDescription description : list) {
                if (vars.get(description.getName()) == null && description.getDefaultValue() != null) {
                    Serializable value = parseValue(description.getDefaultValue(), description.getVariableType(), separator);
                    if (value != null) {
                        vars.put(description.getName(), value);
                    }
                }
            }
        }
    }

    private static Serializable parseValue(String valueAsString, VariableType type, String separator) {

        Serializable value = null;
        switch (type) {
            case ArrayLong:
                List<Long> longs = new ArrayList<Long>();
                for (String var : valueAsString.split(separator)) {
                    Long varLong = Long.parseLong(var);
                    longs.add(varLong);
                }
                value = new ArrayLong(longs);
                break;
            case String:
                value = valueAsString;
                break;
            case LocalDate:
                try {
                    int i = Integer.parseInt(valueAsString);
                    Assert.isTrue(false);
                    //value = LocalDate.parse(i);
                } catch (NumberFormatException e) {
                    LOGGER.error("Impossible de parser [" + valueAsString + "] au format RegDate");
                }
                break;
            case Long:
                try {
                    value = Long.parseLong(valueAsString);
                } catch (NumberFormatException e) {
                    LOGGER.error("Impossible de parser [" + valueAsString + "] au format Long");
                }
                break;
            default:
                LOGGER.warn("VariableType [" + type.toString() + "] non implémenté");
        }
        return value;
    }

    private static VariableDescription isKnownVariable(List<VariableDescription> availableVariables, String variable) {
        if (availableVariables != null) {
            for (VariableDescription vd : availableVariables) {
                if (vd.getName().equals(variable)) {
                    return vd;
                }
            }
        }
        return null;
    }
}
