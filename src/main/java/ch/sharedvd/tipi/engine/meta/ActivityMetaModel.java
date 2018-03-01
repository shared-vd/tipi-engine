package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.utils.Assert;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityMetaModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> clazz;
    private final Set<String> usedConnections = new HashSet<String>();
    private String description;
    private List<VariableDescription> variablesDescription = null;

    public ActivityMetaModel(Class<?> clazz) {
        this(clazz, null, null, null);
    }
    public ActivityMetaModel(Class<?> clazz, VariableDescription[] variables) {
        this(clazz, Arrays.asList(variables), null, null);
    }
    public ActivityMetaModel(Class<?> clazz, String descr) {
        this(clazz, null, null, descr);
    }

    public ActivityMetaModel(Class<?> clazz, String[] aUsedConnections, String descr) {
        this(clazz, null, aUsedConnections, descr);
    }

    public ActivityMetaModel(Class<?> clazz, List<VariableDescription> variables, String[] aUsedConnections, String descr) {
        Assert.notNull(clazz);
        Assert.isTrue(Activity.class.isAssignableFrom(clazz));
        this.clazz = clazz;
        this.description = descr;
        this.variablesDescription = variables;

        // Every Activity uses the Database connection
        if (null != aUsedConnections) {
            for (String ct : aUsedConnections) {
                this.usedConnections.add(ct);
            }
        }

        init();

        // Verifie que on a bien ActivityMetaModel -> Activity
        //    ou alors SubProcessMetaModel -> SubProcess
        //    ou alors ProcessMetaModel -> Process
        {
            Assert.isTrue(Activity.class.isAssignableFrom(clazz));
            if (this instanceof TopProcessMetaModel) {
                Assert.isTrue(TopProcess.class.isAssignableFrom(clazz), "Class " + clazz + " n'est pas un Processus");
            } else if (this instanceof SubProcessMetaModel) {
                Assert.isTrue(SubProcess.class.isAssignableFrom(clazz), "Class " + clazz + " n'est pas un SubProcess");
                Assert.isFalse(TopProcess.class.isAssignableFrom(clazz), "Class " + clazz + " est un Processus");
            } else {
                Assert.isFalse(SubProcess.class.isAssignableFrom(clazz), "Class " + clazz + " est un SubProcess");
            }
        }
    }

    // A overrider
    protected void init() {
    }

    public List<VariableDescription> getVariables() {
        return variablesDescription;
    }
    public VariableDescription getVariable(String key) {
        if (variablesDescription != null) {
            return variablesDescription.stream().filter(vd -> vd.getName().equals(key)).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Retourne le Fully Qualified Name de la classe
     *
     * @return the FQN
     */
    public String getFQN() {
        return clazz.getName();
    }

    public Set<String> getUsedConnections() {
        return usedConnections;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getSimpleName() {
        return clazz.getSimpleName();
    }

    public Activity create() {
        try {
            return (Activity) clazz.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
