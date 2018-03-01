package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.UnknownProcess;
import ch.sharedvd.tipi.engine.client.*;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Class helper static
 *
 * @author jec
 */
public class MetaModelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaModelHelper.class);

    public static TopProcessMetaModel getTopProcessMeta(String fqn) {
        ActivityMetaModel amm = createActivityMetaModel(fqn);
        if (amm instanceof TopProcessMetaModel) {
            return (TopProcessMetaModel) amm;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static ActivityMetaModel createActivityMetaModel(String fqn) {
        try {
            Class<? extends Activity> activity = (Class<? extends Activity>) Class.forName(fqn);
            return createActivityMetaModel(activity);
        } catch (ClassNotFoundException e) {
            Class<? extends Activity> activity = UnknownProcess.class;
            return createActivityMetaModel(activity);
        } catch (RuntimeException ee) {
            throw ee;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ActivityMetaModel createActivityMetaModel(Class<? extends Activity> clazz) {
        if (clazz.getAnnotation(TipiUnknownActivity.class) != null) {
            return createUnkonwnProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiTopProcess.class) != null) {
            return createTopProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiSubProcess.class) != null) {
            return createSubProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiActivity.class) != null) {
            return createActivityMetaModelFromAnnotation(clazz);
        }
        // fallback to static field for old implementation
        return getMetaFromStaticField(clazz);
    }

    @Deprecated
    public static ActivityMetaModel getMetaFromStaticField(Class<?> c) {
        try {
            Field field = c.getField("META");
            return (ActivityMetaModel) field.get(null);
        } catch (NoSuchFieldException ex) {
            //LOGGER.debug("Class {} implements static field meta, but should implements META to be coding guidelines compliant.",c.getSimpleName());
            try {
                Field field = c.getField("meta");
                return (ActivityMetaModel) field.get(null);
            } catch (SecurityException e) {
                LOGGER.error("Property meta for class " + c.getName() + " is not public");
            } catch (NoSuchFieldException e) {
                LOGGER.trace("No meta property for class" + c.getName());
            } catch (IllegalAccessException e) {
                LOGGER.trace("meta property for class" + c.getName() + " has no public access");
            }
        } catch (Exception e) {
            // On ne fait rien
        }
        return null;
    }


    static TopProcessMetaModel createUnkonwnProcessMetaModel(Class<?> clazz) {
        final TopProcessMetaModel metaModel = new TopProcessMetaModel(clazz, 1, 10, 10, "Unknown");
        return metaModel;
    }

    public static TopProcessMetaModel createTopProcessMetaModel(Class<?> clazz) {
        final TipiTopProcess ann = clazz.getAnnotation(TipiTopProcess.class);
        Assert.notNull(ann, "No annotation found on "+clazz);

        final List<VariableDescription> varsDesc = getVariableDescriptions(ann);

        final TopProcessMetaModel metaModel = new TopProcessMetaModel(clazz, ann.priority(), ann.nbMaxTopConcurrent(), ann.nbMaxConcurrent(), ann.description());
        metaModel.setDeleteWhenFinished(ann.deleteWhenFinished());
        metaModel.setStartable(ann.startable());
        metaModel.setVariables(varsDesc);
        metaModel.setShownInUI(ann.showInUi());
        return metaModel;
    }

    static SubProcessMetaModel createSubProcessMetaModel(Class<?> clazz) {
        final TipiSubProcess ann = clazz.getAnnotation(TipiSubProcess.class);
        Assert.notNull(ann, "No annotation found on "+clazz);

        SubProcessMetaModel metaModel = new SubProcessMetaModel(clazz, ann.description());
        return metaModel;
    }

    static ActivityMetaModel createActivityMetaModelFromAnnotation(Class<?> clazz) {
        final TipiActivity ann = clazz.getAnnotation(TipiActivity.class);
        Assert.notNull(ann, "No annotation found on "+clazz);

        final ActivityMetaModel metaModel = new ActivityMetaModel(clazz, ann.description());
        return metaModel;
    }

    private static List<VariableDescription> getVariableDescriptions(TipiTopProcess topProcAnn) {
        if (topProcAnn.variables() != null && topProcAnn.variables().length > 0) {
            List<VariableDescription> varsDesc = new ArrayList<VariableDescription>();
            for (TipiVariable var : topProcAnn.variables()) {
                varsDesc.add(new VariableDescription(var.name(), var.type(), var.description(), StringUtils.trimToNull(var.defaultValue())));
            }
            return varsDesc;
        }
        return null;
    }
}
