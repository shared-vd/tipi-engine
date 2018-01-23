package ch.sharedvd.tipi.engine.utils;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;


/**
 * Classe identique au Transactiontemplate de Spring mais permettant un traitement
 * de TOUTES les exceptions pas seulement les Runtime
 */
public class TixTemplate {

    private final PlatformTransactionManager transactionManager;

    public TixTemplate(PlatformTransactionManager txMgr) {
        Assert.notNull(txMgr);
        transactionManager = txMgr;
    }

    /**
     * Vérifie qu'une transaction est ouverte
     * Correspond au @Transaction(REQUIRED)
     */
    public void assertExistingTransaction() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);

        // Va envoyer une Exception s'il n'y a pas de Transaction ouverte
        getTransactionManager().getTransaction(def);
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Permet d'executer un callback dans un contexte transactionnel
     * Permet d'utiliser des lambda Java8
     *
     * txWithout() veut dire que la méthode ne renvoie pas de valeur de retour
     *
     * @param callback
     */
    public void txWithout(final TxWithout callback) {
        final TransactionTemplate tmpl = new TransactionTemplate(getTransactionManager());
        tmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        tmpl.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    callback.execute(status);
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new TxCallbackException(e);
                }
            }
        });
    }

    /**
     * Permet d'executer un callback dans un contexte transactionnel
     * Permet d'utiliser des lambda Java8
     *
     * txWith() veut dire que la méthode *renvoie* une valeur de retour
     *
     * @param callback
     */
    public <T> T txWith(final TxWith<T> callback) {
        final TransactionTemplate tmpl = new TransactionTemplate(getTransactionManager());
        tmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        return tmpl.execute(status -> {
            try {
                return callback.execute(status);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new TxCallbackException(e);
            }
        });
    }

    @FunctionalInterface
    public interface TxWithout {
        void execute(TransactionStatus status) throws Exception;
    }

    @FunctionalInterface
    public interface TxWith<T> {
        T execute(TransactionStatus status) throws Exception;
    }
}
