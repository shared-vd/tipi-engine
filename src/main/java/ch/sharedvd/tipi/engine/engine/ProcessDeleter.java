package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ProcessDeleter {

    private static final Logger log = LoggerFactory.getLogger(ProcessDeleter.class);

    private DbTopProcess process;
    private EntityManager em;
    private TopProcessRepository topProcessRepository;

    public ProcessDeleter(DbTopProcess process, EntityManager em, TopProcessRepository r) {
        this.process = process;
        this.em = em;
        topProcessRepository = r;
    }

    public boolean delete() {
        final long pid = process.getId();

        final String hql =
                "select count(*) from DbActivity a " +
                        "where (a.process.id = :pid or a.id = :pid)" +
                        "and a.state = :state";
        final Query q = em.createQuery(hql);
        q.setParameter("pid", pid);
        q.setParameter("state", ActivityState.EXECUTING);
        int nbActiInExec = q.getFirstResult();
        if (nbActiInExec > 0) {
            // Impossible d'effacer ce processus
            log.error("Impossible d'effacer le processus " + process + ". Il y a des activités EXECUTING");
            return false;
        }

        // Delete du process en cascade
        DbTopProcess p = topProcessRepository.findOne(pid);
        log.info("Suppression du precessus " + p.getProcessName() + " [id:" + pid + "]");
        topProcessRepository.delete(p);

        return true;
    }

}

