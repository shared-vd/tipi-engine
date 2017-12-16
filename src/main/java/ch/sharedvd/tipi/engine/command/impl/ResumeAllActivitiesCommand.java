package ch.sharedvd.tipi.engine.command.impl;

import ch.vd.registre.tipi.command.Command;
import ch.vd.registre.tipi.engine.ActivityStateChangeService;
import ch.vd.registre.tipi.model.ActivityState;
import ch.vd.registre.tipi.model.DbActivity;
import ch.vd.shared.hibernate.sql.DialectToSqlHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class ResumeAllActivitiesCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(ResumeAllActivitiesCommand.class);

	@Autowired
	@Qualifier("hibernateDialect")
	private String dialect;

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

		final Object[] params;
		final String HQL;
		if (StringUtils.isNotBlank(groupName)) {
			HQL =
					"select a from DbActivity a join a.process p " +
					"where p.id in (select pi.id from DbTopProcess pi where pi.fqn = ?1) " +
					"  and a.requestEndExecution = "+ helper.formatBoolean(false) +
					"  and a.state = ?2";
			params = new Object[] {groupName, state};
		}
		else {
			HQL =
					"select a from DbActivity a " +
					"where a.requestEndExecution = "+ helper.formatBoolean(false) +
					"  and a.state = ?1";
			params = new Object[] {state};
		}
		final List<DbActivity> models = persist.list(HQL, params);
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
