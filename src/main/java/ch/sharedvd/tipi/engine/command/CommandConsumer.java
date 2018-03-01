package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.command.impl.ColdRestartCommand;
import ch.sharedvd.tipi.engine.command.impl.RunExecutingActivitiesCommand;
import ch.sharedvd.tipi.engine.command.impl.StopConsumerCommand;
import ch.sharedvd.tipi.engine.utils.BeanAutowirer;
import ch.sharedvd.tipi.engine.utils.QuantityFormatter;
import ch.sharedvd.tipi.engine.utils.Startable;
import ch.sharedvd.tipi.engine.utils.TixTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommandConsumer implements Startable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandConsumer.class);

    @Autowired
    private BeanAutowirer autowirer;

    @Autowired
    private TixTemplate tt;

    private boolean resumeTipiAtBoot = true;

    private boolean commandEnCours = false;
    private BlockingQueue<CommandWrapper> queue;
    private Thread consumationThread;
    private boolean stopped = true; // par défaut, le commande consumer n'est *pas* démarré

    @Override
    public void start() throws Exception {
        LOGGER.info("Start called");

        Assert.isTrue(stopped);

        stopped = false;

        queue = new ArrayBlockingQueue<CommandWrapper>(100000);

        // Thread de consommation
        consumationThread = new Thread(this);
        consumationThread.setDaemon(true); // so the whole system stop when main() exits
        consumationThread.setName("TiPi-Consumer");
        consumationThread.setPriority(Thread.NORM_PRIORITY + 1);
        LOGGER.info("Démarrage du Thread de CommandConsumer ...");
        consumationThread.start();

        if (resumeTipiAtBoot) {
            // Reveille les taches tout de suite
            LOGGER.info("Cold restart TiPi ...");
            addCommand(new ColdRestartCommand());
        } else {
            LOGGER.info("Pas de Cold restart de TiPi");
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public void destroy() throws Exception {
        destroy(false);
    }

    public void destroy(boolean calledFromStart) throws Exception {
        if (!stopped) {
            if (calledFromStart) {
                LOGGER.info("Destroy called from Start");
            } else {
                LOGGER.info("Destroy called");
            }
        }

        stopped = true;

        if (consumationThread != null) {
            addCommand(new StopConsumerCommand()); // Va débloquer le Thread de consommation
            queue = null; // Ne permet plus de mettre des nouveaux trucs dedans
            int cnt = 0;
            while (consumationThread != null && cnt < 50) {
                if (cnt == 2) {
                    LOGGER.debug("Waiting for the thread to stop (max 5 sec) ...");
                }
                Thread.sleep(100);
                cnt++;
            }
            if (consumationThread != null && consumationThread.isAlive() && !consumationThread.isInterrupted()) {
                LOGGER.info("Thread not stopped by itself. Interrupting ...");
                consumationThread.interrupt();
                consumationThread.join();
            }
            Assert.isNull(consumationThread);
        }
        queue = null;
    }

    public int getPendingCommandCount() {
        if (queue != null) {
            return queue.size();
        }
        return 0;
    }

    public boolean hasCommandPending() {
        if (queue != null) {
            boolean has = (queue.size() > 0) || commandEnCours;
            return has;
        }
        return commandEnCours;
    }

    public void removeCommandOfClass(Class<? extends Command> clazz) {
        removeCommandOfClass(clazz, false);
    }

    private void removeCommandOfClass(Class<? extends Command> clazz, boolean aNotTheFirst) {
        Assert.notNull(clazz);
        if (queue != null) {
            int count = 0;
            Iterator<CommandWrapper> iter = queue.iterator();
            boolean first = aNotTheFirst;
            while (iter.hasNext()) {
                CommandWrapper c = iter.next();
                if (c != null) {
                    Assert.notNull(c.getCommand());
                    if (clazz.isAssignableFrom(c.getCommand().getClass())) {
                        // On laisse la première, on supprime les suivantes (il ne doit en rester plus que une en queue)
                        if (!first) {
                            iter.remove();
                            count++;
                        }
                        first = false;
                    }
                }
            }
            if (count > 0) {
                LOGGER.debug("Suppression de " + count + " Command du type " + clazz.getSimpleName());
            }
        }
    }

    @Override
    public void run() {
        boolean loopActive = true;
        while (loopActive) {
            try {
                if (queue == null) {
                    // Est-ce seulement possible ?
                    loopActive = false;
                } else {
                    commandEnCours = false;

                    final CommandWrapper currentCommand = queue.take();
                    Assert.notNull(currentCommand, "currentCommand can't ne null. Either a command or an InterruptedException");

                    commandEnCours = true;

                    // On peut être en destroy => faire le minimum (en tout cas pas l'autowire!)
                    if (currentCommand.getCommand() instanceof StopConsumerCommand) {
                        loopActive = false;
                    } else {

                        // On le fait très vite pour en profiter dans les logs...
                        autowirer.autowire(currentCommand.getCommand());

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Begin command: " + currentCommand.getCommand());
                        }

                        // Temps d'attente de la commande
                        if (currentCommand.getElapsedTimeMillis() > 10000) {
                            LOGGER.warn("The command: " + currentCommand.getCommand() + " waited for " + QuantityFormatter.formatMillis(currentCommand.getElapsedTimeMillis()));
                        } else if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("The command: " + currentCommand.getCommand() + " waited for " + QuantityFormatter.formatMillis(currentCommand.getElapsedTimeMillis()));
                        }

                        // Lancement de la commande
                        final long begin = System.currentTimeMillis();
                        {
                            try {
                                if (currentCommand.getCommand().needTransaction()) {
                                    tt.txWithout((status) -> {
                                        currentCommand.getCommand().execute();
                                    });
                                } else {
                                    currentCommand.getCommand().execute();
                                }
                            } catch (Exception e) {
                                LOGGER.error("Exception pendant le traitement de la commande (retry=" + currentCommand.getNbRetry()
                                        + " command=" + currentCommand.getCommand() + ") : " + e, e);

                                // Erreur -> ajoute la commande a la fin de la queue si on a pas deja essayé trop de fois
                                if (currentCommand.getNbRetry() <= 5) {
                                    currentCommand.incNbRetry();
                                    queue.add(currentCommand);
                                    Thread.sleep(2000); // On attends un peu pour pas bourrer trop la base quand il y a une erreur
                                }
                            }
                        }
                        // Temps de processing de la command
                        final long diff = System.currentTimeMillis() - begin;
                        if (diff > 3000) {
                            LOGGER.warn("The command: " + currentCommand.getCommand() + " has taken " + QuantityFormatter.formatMillis(diff));
                        } else if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("The command: " + currentCommand.getCommand() + " has taken " + QuantityFormatter.formatMillis(diff));
                        }

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("End command: " + currentCommand.getCommand());
                        }

                        // On calcule les stats après la command.
                        // On pénalise moins le système vu qu'on vient de démarrer plein d'activités
                        //engineInterceptor.afterConsumerStartActivity();

                    }
                }
            } catch (InterruptedException inter) {
                // On ne fait rien
                Assert.isTrue(stopped);
                loopActive = false;
            } catch (Throwable e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("CommandConsumer thread (" + consumationThread.getName() + ") stopped");
        commandEnCours = false;
        consumationThread = null;
    }

    public void addCommand(Command c) {
        if (queue != null) {
            try {
                boolean result = queue.offer(new CommandWrapper(c), 10, TimeUnit.SECONDS);
                if (result) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Ajout de '" + c + "' Nombre de commandes dans la queue après: " + queue.size());
                    }
                } else {
                    LOGGER.error("Impossible d'ajouter la commande '" + c + "' dans la queue! Size=" + queue.size() + " Command=" + c.getClass().getSimpleName());
                    for (CommandWrapper w : queue) {
                        LOGGER.error(w.toString());
                    }
                }
                // Supprime les commandes inutiles pour limiter la taille de la queue.
                if (c instanceof RunExecutingActivitiesCommand) {
                    removeCommandOfClass(c.getClass(), true);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setResumeTipiAtBoot(boolean startTipi) {
        this.resumeTipiAtBoot = startTipi;
    }
}
