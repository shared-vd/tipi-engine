package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.base.jpa.PersistenceContextService;
import ch.vd.registre.base.tx.TxCallback;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import ch.vd.registre.base.tx.TxTemplate;
import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.tipi.AppLog;
import ch.vd.registre.tipi.action.*;
import ch.vd.registre.tipi.client.AbortException;
import ch.vd.registre.tipi.command.CommandHelperService;
import ch.vd.registre.tipi.command.CommandService;
import ch.vd.registre.tipi.command.impl.EndActivityCommand;
import ch.vd.registre.tipi.command.impl.RunExecutingActivitiesCommand;
import ch.vd.registre.tipi.engine.stats.TipiThreadStats;
import ch.vd.registre.tipi.interceptor.TipiEngineInterceptorFacade;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import ch.vd.registre.tipi.model.ActivityState;
import ch.vd.registre.tipi.model.DbActivity;
import ch.vd.registre.tipi.model.DbSubProcess;
import ch.vd.registre.tipi.retry.RetryContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class ActivityRunner implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityRunner.class);

	private final TipiEngineInterceptorFacade engineInterceptor;
	private final PersistenceContextService persist;
	private final CommandHelperService commandHelperService;
	private final CommandService commandService;
	private final TopProcessGroupLauncher groupLauncher;

	private final long activityId;
	private final ActivityMetaModel meta;
	private TxTemplate tt;

	public ActivityRunner(ActivityRunnerContext context, long actiId, ActivityMetaModel meta, TipiEngineInterceptorFacade audit) {
		this.engineInterceptor = audit;
		Assert.notNull(audit);
		this.activityId = actiId;
		Assert.isTrue(this.activityId > 0);
		this.meta = meta;
		Assert.notNull(meta);

		Assert.notNull(context.commandService);
		Assert.notNull(context.persist);
		Assert.notNull(context.commandHelperService);
		Assert.notNull(context.launcher);
		Assert.notNull(context.txManager);

		this.commandService = context.commandService;
		this.persist = context.persist;
		this.commandHelperService = context.commandHelperService;
		this.groupLauncher = context.launcher;
		this.tt = new TxTemplate(context.txManager);
	}

	public long getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return meta.getFQN();
	}

	public boolean isTopActivity() {
		return meta instanceof TopProcessMetaModel;
	}

	@Override
	public void run() {

		try {
			groupLauncher.initInfosForThread(this);

			engineInterceptor.onStartActivity(getActivityId(), getActivityName());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ActivityRunner begin. Id: " + activityId);
			}

			executeAndRetry();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ActivityRunner ended. Id: " + activityId);
			}
		}
		catch (Throwable e) {
			AppLog.error(LOGGER, "Activity " + activityId + " ended poorly: " + e, e);
			// Si on a une exception ici, c'est qu'on n'a pas bien trappé l'exception
			// Il faut donc remover l'ID de cette activité de la liste des running
			groupLauncher.removeRunning(activityId);
		}
		finally {
			engineInterceptor.onEndActivity(getActivityId(), getActivityName());
		}
	}

	private void executeAndRetry() {

		final long begin = System.currentTimeMillis();

		try {
			executeActivity();
		}
		catch (Throwable t) {

			final long diff = System.currentTimeMillis() - begin;

			boolean aborted = isAborted(t);
			boolean interrupted = isInterrupted(t);

			final boolean runExecuting;
			int nbRetry = 0;
			if (!aborted && !interrupted && groupLauncher.isStarted()) {

				nbRetry = incrementNbRetries();
				final String baseMessage = "Activity id=" + activityId + " retry="+nbRetry+" (" + getActivityName() + ") : rollback done. Retrying? Msg='" + t.getMessage()+ "'";
				// On log tjrs le callstack, sinon on sait pas ce qu'il s'est passé.
				// Le callstack enregistré dans le DbActivity est trop court...
				final String msgWithCallstack = "\n" + ExceptionUtils.getStackTrace(t);

				final RetryContext retryContext = new RetryContext(nbRetry, t, diff);
				final boolean canRetry = meta.getRetryPolicy().canRetry(retryContext);
				if (!canRetry) {
					// On log en ERROR parce que c'est terminé -> Error
					LOGGER.error(msgWithCallstack);

					// Si on a une erreur
					treatErrorCase(t, nbRetry);
					// Pas besoin de relancer, l'activité est terminée (max retry atteint)
					runExecuting = false;
				}
				else {
					// On log en ERROR juste le message
					LOGGER.error(baseMessage);
					// On log en debug parce qu'on va retrying
					LOGGER.debug(msgWithCallstack);

					// Il faut essayer de relancer
					runExecuting = true;
				}
			}
			else {
				// Pas besoin de relancer, l'activité est ABORTED
				runExecuting = false;
			}

			if (interrupted) {
				groupLauncher.setStatusForThread(TipiThreadStats.STATUS_INTERRUPTED);
			}
			else if (aborted) {
				groupLauncher.setStatusForThread(TipiThreadStats.STATUS_ABORTED);
			}
			else {
				groupLauncher.setStatusForThread(TipiThreadStats.STATUS_EXCEPTION + ":" + nbRetry);
			}
			groupLauncher.removeRunning(activityId);

			// On doit réessayer de lancer cette activité APRES l'avoir supprimé de
			// la liste des RUNNING sinon elle va pas pouvoir être relancée
			if (runExecuting) {
				commandService.sendCommand(new RunExecutingActivitiesCommand());
			}

		} // try - catch : executeActivity
		finally {
			SecurityContextHolder.clearContext();
		}
	}

	private boolean isAborted(Throwable t) {
		AbortException ae = null;
		while ((null == ae) && (null != t)) {
			if (t instanceof AbortException) {
				ae = (AbortException) t;
			}
			t = t.getCause();
		}
		return (null != ae) && ae.getAbortType() == AbortException.AbortType.ABORTED;
	}

	private boolean isInterrupted(Throwable t) {
		boolean interrupted = Thread.currentThread().isInterrupted();
		while (!interrupted && (null != t)) {
			if (t instanceof InterruptedException) {
				interrupted = true;
			}
			else if (t instanceof AbortException) {
				interrupted = ((AbortException) t).getAbortType() == AbortException.AbortType.INTERRUPTED;
			}
			t = t.getCause();
		}
		return interrupted;
	}

	private void treatErrorCase(final Throwable err, final int nbRetry) {
		tt.doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				final String msg = "Activity " + activityId + " (" + getActivityName() + ") : rollback done. Retrying? Msg='"
						+ err.getMessage() + "'";
				// On log en ERROR parce que l'activité va VRAIMENT passer en erreur
				AppLog.error(LOGGER, msg + "\n" + ExceptionUtils.getStackTrace(err));

				try {
					onError(err);
				}
				catch (Exception ignored) {
					// On ne fait rien avec cette exception
					AppLog.error(LOGGER, ignored.getMessage(), ignored);
				}

				DbActivity model = persist.get(DbActivity.class, activityId);
				if (model != null) { // model peut etre null si le process a été supprimé
					Assert.isEqual(ActivityState.EXECUTING, model.getState(), "L'etat du process Id: " + model.getId()
							+ " est impossible: " + model.getState());
					model.setNbRetryDone(nbRetry);
					// On met le call stack dans l'activité
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(bos);

					Throwable th = err;
					while (th != null) {
						th.printStackTrace(pw);
						th = th.getCause();
					}
					pw.flush();
					pw.close();
					model.setCallstack(bos.toString());

					ErrorActivityResultContext resultContext = new ErrorActivityResultContext("Type: " + err.getClass().getName()
							+ ". Message: " + err.getMessage());
					ActivityStateChangeService.runnerFinished(model, resultContext);

					// On a eu une erreur mais l'activité est terminée -> runner les autres
					commandService.sendCommand(new EndActivityCommand(activityId));
				}
				else {
					AppLog.error(LOGGER, "L'activité " + activityId + " n'existe plus en DB");
				}
			}
		});
	}

	private void onError(Throwable exception) {
		Activity activityService = commandHelperService.createActivity(activityId);
		Assert.notNull(activityService);
		activityService.onError(exception);
		engineInterceptor.onErrorActivity(getActivityId(), getActivityName(), exception);
	}

	private int incrementNbRetries() {

		// On incrémente le nb de retry
		try {
			return tt.doInTransaction(new TxCallback<Integer>() {
				@Override
				public Integer execute(TransactionStatus status) throws Exception {
					DbActivity activity = persist.get(DbActivity.class, activityId);
					if (activity != null) {
						activity.setNbRetryDone(activity.getNbRetryDone() + 1);
						return activity.getNbRetryDone();
					}
					return 0;
				}
			});
		}
		catch (Throwable er) {
			//Si on ne peut pas incrémenter on fait un retry sans pouvoir mettre en base le nombre de retry
			AppLog.error(LOGGER, "Erreur lors de la tentative d'incrémentation du nb de retry pour l'activité " + activityId, er);
		}
		return -1;
	}

	private void executeActivity() {

		final long begin = System.currentTimeMillis();
		final AtomicLong timeBeforeCommit = new AtomicLong();

		final Activity finishedActivity = tt.doInTransaction(new TxCallback<Activity>() {
			@Override
			public Activity execute(TransactionStatus status) throws Exception {
				DbActivity model = persist.get(DbActivity.class, activityId);
				Assert.notNull(model, "Impossible de récupérer le model pour l'activité " + activityId);

				final Activity activity;

				// On a été ABORTED -> termine vite fait
				if (!ActivityState.ABORTED.equals(model.getState())
						&& (model.getProcess() == null || !ActivityState.ABORTED.equals(model.getProcess().getState()))) {

					Assert.isEqual(ActivityState.EXECUTING, model.getState(), "L'etat de l'activity Id: " + model.getId()
							+ " est impossible: " + model.getState());

					activity = commandHelperService.createActivity(activityId);
					if (null == activity) {
						StringBuffer msg = new StringBuffer();
						msg.append("No ActivityServiceFound in registered activities map: Activity info: Nom: ").append(model.getFqn())
								.append(". Id: ").append(model.getId());
						AppLog.error(LOGGER, msg.toString());
						throw new RuntimeException(msg.toString());
					}

					final ActivityResultContext resultContext;
					if ((model instanceof DbSubProcess) && ((DbSubProcess) model).isExecuted()) {

						final DbSubProcess subProcModel = (DbSubProcess) model;
						// SubProcess dans sa phase terminate()
						SubProcess sub = (SubProcess) activity;

						final Date dateStart = new Date();
						try {

							// On appelle flush pour qu'on ne dépende pas de l'état après le run de l'activité
							persist.flush(false); // Pas de log, c'est un flush() fonctionnel indispensable

							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Calling terminate()");
							}
							resultContext = sub.doTerminate();

						}
						finally {
							//On met la date du début du terminate()
							subProcModel.setDateStartTerminate(dateStart);
							//On met la date de la fin du terminate()
							subProcModel.setDateEndTerminate(new Date());
						}
					}
					else {

						final Date dateStart = new Date();
						try {
							// On appelle flush pour qu'on ne dépende pas de l'état après le run de l'activité
							persist.flush(false); // Pas de log, c'est un flush() fonctionnel indispensable

							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Calling execute()");
							}
							resultContext = activity.doExecute();
						}
						finally {
							//On met la date du début de l'execute()
							model.setDateStartExecute(dateStart);
							//On met la date de la fin de l'execute()
							model.setDateEndExecute(new Date());
						}
					}
					Assert.notNull(resultContext, "Le retour de l'activité ne peut pas etre null");

					if (Thread.currentThread().isInterrupted()) {
						throw new AbortException(AbortException.AbortType.INTERRUPTED);
					}

					// On récupère le model après le terminate() de l'activité
					model = persist.get(DbActivity.class, activityId);

					ActivityStateChangeService.runnerFinished(model, resultContext);

					groupLauncher.setStatusForThread(TipiThreadStats.STATUS_COMMIT);
				}
				else {
					AppLog.info(LOGGER, "Activité ou process (" + getActivityId() + ") ABORTED -> pas de run");
					// ABORTED -> on met l'activité en ABORTED
					ActivityStateChangeService.aborted(model);

					groupLauncher.setStatusForThread(TipiThreadStats.STATUS_ABORTED);

					// Activité non créée
					activity = null;
				}
				commandService.sendCommand(new EndActivityCommand(activityId));

				timeBeforeCommit.set(System.currentTimeMillis());

				return activity;
			}
		});

		try {
			if (finishedActivity != null) {
				// La facade est obsolete parce qu'on est hors TX
				finishedActivity.setFacade(null);
				finishedActivity.onAfterCommit();
			}
		}
		catch (Throwable t) {
			// On s'en tape de cette exception, le process s'est terminé correctement
			LOGGER.error(t.getMessage());
		}

		final long end = System.currentTimeMillis();
		final long diffCommit = end - timeBeforeCommit.get();
		final long diffTotal = end - begin;
		// Si on met plus de 10s pour committer, on log WARN
		final String message = String.format("ActivityRunner(id=%d/%s) terminé: total=%.3f[s] dont commit=%.3f[s]", getActivityId(),
				getActivityName(), (diffTotal / 1000.0), (diffCommit / 1000.0));
		if (diffCommit > 10000) {
			AppLog.warn(LOGGER, message);
		}
		else if (diffTotal > 60000) {
			LOGGER.warn(message);
		}
		else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(message);
			}
		}
	}

}
