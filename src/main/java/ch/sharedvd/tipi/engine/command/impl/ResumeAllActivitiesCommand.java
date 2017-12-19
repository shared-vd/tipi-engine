package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.engine.ActivityStateChangeService;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.utils.DialectToSqlHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ResumeAllActivitiesCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumeAllActivitiesCommand.class);

    @Autowired
    @Qualifier("hibernateDialect")
    private String dialect;
    @Autowired
    private EntityManager em;

    private ActivityState state;
    private String groupName;

    public ResumeAllActivitiesCommand(ActivityState state) {
        this.state = state;
    }

    public ResumeAllActivitiesCommand(ActivityState state, String gname) {
        this.state = state;
        this.groupName = gname;
    }

    @SuppressWarnings("unchecked")
    public List<DbActivity> getActivities() {

        final DialectToSqlHelper helper = new DialectToSqlHelper(dialect);

        final String HQL;
        final List<DbActivity> models;
        if (StringUtils.isNotBlank(groupName)) {
            HQL =
                    "select a from DbActivity a join a.process p " +
                            "where p.id in (select pi.id from DbTopProcess pi where pi.fqn = :group) " +
                            "  and a.requestEndExecution = " + helper.formatBoolean(false) +
                            "  and a.state = :state";
            final Query q = em.createQuery(HQL);
            q.setParameter("group", groupName);
            q.setParameter("state", state);
            models = q.getResultList();
        } else {
            HQL =
                    "select a from DbActivity a " +
                            "where a.requestEndExecution = " + helper.formatBoolean(false) +
                            "  and a.state = ?1";
            final Query q = em.createQuery(HQL);
            q.setParameter("state", state);
            models = q.getResultList();
        }
        return models;
    }

    @Override
    public void execute() {
        final List<DbActivity> models = getActivities();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trouvé " + models.size() + " activités a resumer");
        }
        for (DbActivity acti : models) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resuming activity " + acti.getId() + " / " + acti.getFqn());
            }
            ActivityStateChangeService.resuming(acti);
            runActivity(acti);
        }
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
