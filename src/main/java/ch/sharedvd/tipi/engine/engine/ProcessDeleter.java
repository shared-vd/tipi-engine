package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDeleter.class);

    private DbTopProcess process;
    private HqlBuilderService hqlBuilder;
    private PersistenceContextService persist;

    public ProcessDeleter(DbTopProcess process, HqlBuilderService hqlBuilder, PersistenceContextService persist) {
        this.process = process;
        this.hqlBuilder = hqlBuilder;
        this.persist = persist;
    }

    public boolean delete() {
        final long pid = process.getId();

        DbActivityCriteria crit = new DbActivityCriteria();
        crit.addAndExpression(Expr.or(crit.id().eq(pid), crit.process__Id().eq(pid)));
        //Permet de ne pas supprimer les process non terminés
        crit.addAndExpression(crit.state().eq(ActivityState.EXECUTING));
        crit.activateRowCount();
        Long nbActiInExec = (Long) hqlBuilder.getSingleResult(crit);
        if (nbActiInExec > 0) {
            // Impossible d'effacer ce processus
            AppLog.error(LOGGER, "Impossible d'effacer le processus " + process + ". Il y a des activités EXECUTING");
            return false;
        }

        // Delete du process en cascade
        DbTopProcess p = hqlBuilder.getById(DbTopProcess.class, pid);
        AppLog.info(LOGGER, "Suppression du precessus " + p.getProcessName() + " [id:" + pid + "]");
        persist.remove(p);

        return true;
    }

}

