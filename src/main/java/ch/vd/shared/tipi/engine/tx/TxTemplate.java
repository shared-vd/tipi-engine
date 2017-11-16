package ch.vd.shared.tipi.engine.tx;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Classe identique au Transactiontemplate de Spring mais permattant un traitement de TOUTES les exceptions
 * pas seulement les Runtime
 */
public class TxTemplate {

    private TransactionTemplate tmpl;

    public TxTemplate() {
    }

    public TxTemplate(PlatformTransactionManager txMgr) {
        tmpl = new TransactionTemplate(txMgr);
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

    public <T> T doWith(TxWith<T> callback) {
        tmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        return tmpl.execute((s) -> {
            try {
                return callback.execute();
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void doWithout(TxWithout callback) {
        tmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        tmpl.execute((s) -> {
            try {
                callback.execute();
            } catch (RuntimeException rte) {
                throw rte;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public PlatformTransactionManager getTransactionManager() {
        if (tmpl != null) {
            return tmpl.getTransactionManager();
        }
        return null;
    }
}
