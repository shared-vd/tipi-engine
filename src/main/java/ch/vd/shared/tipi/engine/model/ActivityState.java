package ch.vd.shared.tipi.engine.model;

public enum ActivityState {

	// ---------------------------------
	// - Cas d'une activité standard
	// INITIAL(requestEnd=false) - RunExecutingActivitiesCommand - EXECUTING(requestEnd=false)
	// 		Execution du run() - FINISHED(requestEnd=true) - EndActivityCommand - FINISHED(requestEnd=false)
	//
	// - Cas d'un sub-process
	// INITIAL(requestEnd=false) - RunExecutingActivitiesCommand - EXECUTING(requestEnd=false)
	//		Execution du run() - WAIT_ON_CHILDREN(requestEnd=true) - EndActivityCommand - WAIT_ON_CHILDREN(requestEnd=false)
	//		EndActivityCommand - EXECUTING_AFTER(requestEnd=false) -Execution du terminate() - FINISHED(requestEnd=true)
	//		EndActivityCommand - FINISHED(requestEnd=false)

	/**
	 * État initial lorsque l'activité est créée
	 */
	INITIAL,
	/**
	 * L'activité est en cours d'exécution ou de terminate
	 */
	EXECUTING,
	/**
	 * L'activité a été suspendue car elle est en attente de données
	 */
	SUSPENDED,
	/**
	 * L'activité doit attendre que les enfants soient finis
	 */
	WAIT_ON_CHILDREN,

	/**
	 * L'activité a terminé sans erreur
	 */
	FINISHED,
	/**
	 * L'activité a terminé en erreur
	 */
	ERROR,
	/**
	 * L'activité a été annulée
	 */
	ABORTED
}