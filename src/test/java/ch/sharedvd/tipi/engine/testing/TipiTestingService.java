package ch.sharedvd.tipi.engine.testing;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.hibernate.SessionException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TipiTestingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TipiTestingService.class);

    @Autowired
    private TopProcessRepository topProcessRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private TxTemplate txTemplate;
    @Autowired
    private CommandConsumer commandConsumer;

    public List<TipiTopProcessInfos> getAllProcesses(final Class<? extends TopProcess> clazz) {
        final List<TipiTopProcessInfos> list = new ArrayList<>();

        txTemplate.txWithout((s) -> {
            final List<DbTopProcess> results = topProcessRepository.findProcessesByFqn(clazz.getName());
            for (DbTopProcess am : results) {
                final TipiTopProcessInfos infos = new TipiTopProcessInfos(am, false);
                list.add(infos);
            }
        });
        return list;
    }

    public void waitEndAllActivitiesNoAssertIfError() {
        waitEndAllActivities(false);
    }

    public void waitEndAllActivitiesAndAssertIfError() {
        waitEndAllActivities(true);
    }

    /**
     * Attends que toutes les activités et processes soient terminés. Ca veut dire qu'il n'y en a plus en executing
     * Les activités peuvent être soit en
     * - FINISHED
     * - ERROR
     * - SUSPENDED
     *
     * @param assertThatThereIsNoError
     */
    private void waitEndAllActivities(boolean assertThatThereIsNoError) {
        boolean end = false;
        while (!end) {
            try {
                // Assert que plus aucune activité n'est potentiellement démarrable.
                final List<DbActivity> acts = activityRepository.findByStateOrRequestEndExecution(ActivityState.EXECUTING, true);
                end = ((null == acts) || acts.isEmpty());
                if (end) {
                    // Contrôle en plus qu'il n'y a plus de commande dans la queue.
                    end = !commandConsumer.hasCommandPending();
                }
            } catch (SessionException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException("Echec de la session hibernate", e);
            } catch (Throwable e) {
                end = false;
            }
        }

        if (assertThatThereIsNoError) {
            List<DbActivity> acts = activityRepository.findAll();
            for (DbActivity am : acts) {
                Assert.assertTrue("State: " + am.getState(), ActivityState.ERROR != am.getState());
            }
        }
    }

}
