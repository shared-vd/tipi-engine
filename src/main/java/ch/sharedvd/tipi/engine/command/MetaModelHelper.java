package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.UnknownProcess;
import ch.sharedvd.tipi.engine.client.*;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
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

    public static DbActivity createModelFromMeta(final ActivityMetaModel meta, final boolean isProcess,
                                                 final VariableMap vars, ActivityPersisterService aHelperService) {
        Assert.notNull(meta);

        DbActivity a = null;
        if (isProcess) {
            a = new DbTopProcess();
        } else if (meta instanceof SubProcessMetaModel) {
            a = new DbSubProcess();
        } else if (meta instanceof ActivityMetaModel) {
            a = new DbActivity();
        } else {
            Assert.fail("Activity type not supported");
        }
        a.setFqn(meta.getFQN());
        aHelperService.putVariables(a, vars);

        return a;
    }

    public static TopProcessMetaModel getTopProcessMeta(String fqn) {
        ActivityMetaModel amm = getMeta(fqn);
        if (amm != null && amm instanceof TopProcessMetaModel) {
            return (TopProcessMetaModel) amm;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static ActivityMetaModel getMeta(String fqn) {
        try {
            Class<? extends Activity> activity = (Class<? extends Activity>) Class.forName(fqn);
            return getActivityMetaModel(activity);
        } catch (ClassNotFoundException e) {
            Class<? extends Activity> activity = UnknownProcess.class;
            return getActivityMetaModel(activity);
        } catch (RuntimeException ee) {
            throw ee;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ActivityMetaModel getActivityMetaModel(Class<? extends Activity> clazz) {
        if (clazz.getAnnotation(TipiUnknownActivity.class) != null) {
            return getUnkonwnProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiTopProcess.class) != null) {
            return getTopProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiSubProcess.class) != null) {
            return getSubProcessMetaModel(clazz);
        } else if (clazz.getAnnotation(TipiActivity.class) != null) {
            return getActivityMetaModelFromAnnotation(clazz);
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


    public static TopProcessMetaModel getUnkonwnProcessMetaModel(Class<?> clazz) {
//		TopProcessMetaModel metaModel = getTopProcessMetaModel(clazz);
//		metaModel.setShownInUI(true);

        TopProcessMetaModel metaModel = new TopProcessMetaModel(clazz, 1, 10, 10, "Unknown");
        metaModel.setShownInUI(true);
        return metaModel;
    }

    public static TopProcessMetaModel getTopProcessMetaModel(Class<?> clazz) {
        TipiTopProcess ann = clazz.getAnnotation(TipiTopProcess.class);

        List<VariableDescription> varsDesc = getVariableDescriptions(ann);

        TopProcessMetaModel metaModel = new TopProcessMetaModel(clazz, ann.priority(), ann.nbMaxTopConcurrent(), ann.nbMaxConcurrent(),
                ann.description());
        metaModel.setDeleteWhenFinished(ann.deleteWhenFinished());
        metaModel.setStartable(ann.startable());
        metaModel.setVariablesDescription(varsDesc);
        metaModel.setShownInUI(ann.showInUi());
        return metaModel;
    }

    public static SubProcessMetaModel getSubProcessMetaModel(Class<?> clazz) {
        TipiSubProcess ann = clazz.getAnnotation(TipiSubProcess.class);

        SubProcessMetaModel metaModel = new SubProcessMetaModel(clazz);
        metaModel.setDescription(ann.description());
        return metaModel;
    }

    public static ActivityMetaModel getActivityMetaModelFromAnnotation(Class<?> clazz) {
        TipiActivity ann = clazz.getAnnotation(TipiActivity.class);

        ActivityMetaModel metaModel = new ActivityMetaModel(clazz);
        metaModel.setDescription(ann.description());
        return metaModel;
    }

    private static List<VariableDescription> getVariableDescriptions(TipiTopProcess topProcAnn) {
        if (topProcAnn.variables() != null && topProcAnn.variables().length > 0) {
            List<VariableDescription> varsDesc = new ArrayList<VariableDescription>();
            for (TipiVariable var : topProcAnn.variables()) {
                varsDesc.add(new VariableDescription(var.name(), var.type(), var.description(), var.testingOnly(), StringUtils.trimToNull(var.defaultValue())));
            }
            return varsDesc;
        }
        return null;
    }
}
