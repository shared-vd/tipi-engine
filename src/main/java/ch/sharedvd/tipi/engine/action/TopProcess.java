package ch.sharedvd.tipi.engine.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TopProcess extends SubProcess {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopProcess.class);

//	@Autowired
//	private TipiFacade tipiFacade;
//
//	/**
//	 * Supprime les processus du même nom
//	 */
//	protected void deleteOldSameProcesses() {
//		TipiCriteria criteria = new TipiCriteria();
//		criteria.setNameOrProcessName(facade.getName());
//		ResultListWithCount<TipiActivityInfos> list = tipiFacade.searchActivities(criteria, -1);
//		if (!list.getResult().isEmpty()) {
//			LOGGER.info("Trouvé "+list.getResult().size()+" processus avec le nom "+facade.getName()+" ...");
//
//			for (TipiActivityInfos i : list.getResult()) {
//				// Si c'est pas moi ...
//				if (i.getId() != getActivityId()) {
//					// ... on efface!
//					tipiFacade.abortProcess(i.getId(), true);
//				}
//			}
//		}
//	}

}
