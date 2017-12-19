package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.utils.ArrayLong;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

;

public class PersistenceTest extends AbstractTipiPersistenceTest {

    @Override
    public void onSetUp() throws Exception {
        super.onSetUp();
    }

    @Test
    public void processName_is_helpful_in_ui() {

        final AtomicReference<Long> TOP_PROCESS = new AtomicReference<>(0L);
        final AtomicReference<Long> ACT_1 = new AtomicReference<>(0L);
        final AtomicReference<Long> ACT_2 = new AtomicReference<>(0L);

        txTemplate.txWithout((s) -> {
            DbTopProcess parent = new DbTopProcess();
            parent.setFqn("Process1");
            em.persist(parent);
            TOP_PROCESS.set(parent.getId());

            DbActivity child1;
            {
                child1 = new DbActivity();
                child1.setFqn("act1");
                child1.setParent(parent);
                child1.setProcess(parent);
                em.persist(child1);
                ACT_1.set(child1.getId());
            }
            {
                DbActivity child2 = new DbActivity();
                child2.setFqn("act2");
                child2.setParent(parent);
                child2.setProcess(parent);
                child2.setPrevious(child1);
                em.persist(child2);
                ACT_2.set(child2.getId());
            }
        });

        txTemplate.txWithout((s) -> {
            DbActivity activite1 = activityRepository.findOne(ACT_1.get());
            Assert.assertEquals("Process1", activite1.getProcessName());

            DbActivity activite2 = activityRepository.findOne(ACT_2.get());
            Assert.assertEquals("Process1", activite2.getProcessName());

            DbActivity topProcess = activityRepository.findOne(TOP_PROCESS.get());
            Assert.assertEquals("Process1", topProcess.getProcessName());
        });
    }

    @Test
    public void callstack_2000() {

        final AtomicReference<Long> ID = new AtomicReference<>(0L);

        // 2000 chars
        final StringBuilder callstack = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            callstack.append('C');
        }

        txTemplate.txWithout((s) -> {
            DbActivity act = new DbActivity();
            act.setFqn("Bla");
            em.persist(act);
            ID.set(act.getId());

            act.setCallstack(callstack.toString());
        });

        txTemplate.txWithout((s) -> {
            DbActivity activite1 = activityRepository.findOne(ID.get());

            Assert.assertEquals(callstack.length(), activite1.getCallstack().length());
            Assert.assertEquals(callstack.toString(), activite1.getCallstack());
        });
    }

    @Test
    public void callstack_4059_truncated() throws Exception {

        final AtomicReference<Long> ID = new AtomicReference<Long>(0L);

        // 4000 chars
        final StringBuilder callstack = new StringBuilder();
        for (int i = 0; i < 4059; i++) {
            callstack.append('C');
        }

        txTemplate.txWithout((s) -> {
            DbActivity act = new DbActivity();
            act.setFqn("Bla");
            em.persist(act);
            ID.set(act.getId());

            // Truncate à 2000 chars
            act.setCallstack(callstack.toString());
        });

        txTemplate.txWithout((s) -> {
            DbActivity activite1 = activityRepository.findOne(ID.get());

            Assert.assertEquals(2000, activite1.getCallstack().length());
        });
    }

    @Test
    public void processParentChildPrevious() throws Exception {

        final AtomicReference<Long> P_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> C1_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> C2_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> P2_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> C2_1_ID = new AtomicReference<Long>(0L);

        txTemplate.txWithout((s) -> {
            DbTopProcess parent = new DbTopProcess();
            parent.setFqn("Process1");
            em.persist(parent);
            P_ID.set(parent.getId());

            DbActivity child1;
            {
                child1 = new DbActivity();
                child1.setFqn("act1");
                child1.setParent(parent);
                child1.setProcess(parent);
                em.persist(child1);
                C1_ID.set(child1.getId());
            }
            {
                DbActivity child2 = new DbActivity();
                child2.setFqn("act2");
                child2.setParent(parent);
                child2.setProcess(parent);
                child2.setPrevious(child1);
                em.persist(child2);
                C2_ID.set(child2.getId());
            }

            {
                DbTopProcess parent2 = new DbTopProcess();
                parent2.setFqn("Process1");
                parent2.setParent(parent);
                parent2.setProcess(parent);
                em.persist(parent2);
                P2_ID.set(parent2.getId());

                DbActivity child2_1;
                {
                    child2_1 = new DbActivity();
                    child2_1.setFqn("act2.1");
                    child2_1.setParent(parent2);
                    child2_1.setProcess(parent);
                    em.persist(child2_1);
                    C2_1_ID.set(child2_1.getId());
                }
            }
        });

        txTemplate.txWithout((s) -> {
            DbTopProcess parent = topProcessRepository.findOne(P_ID.get());
            DbActivity child1 = activityRepository.findOne(C1_ID.get());
            DbActivity child2 = activityRepository.findOne(C2_ID.get());
            DbTopProcess parent2 = topProcessRepository.findOne(P2_ID.get());
            DbActivity child2_1 = activityRepository.findOne(C2_1_ID.get());

            DbTopProcess process = parent;

            // Parent1
            Assert.assertEquals(P_ID.get(), parent.getId());
            // Child1
            Assert.assertEquals(C1_ID.get(), child1.getId());
            Assert.assertEquals(parent.getId(), child1.getParent().getId());
            Assert.assertEquals(process.getId(), child1.getProcess().getId());
            // Child2
            Assert.assertEquals(C2_ID.get(), child2.getId());
            Assert.assertEquals(parent.getId(), child2.getParent().getId());
            Assert.assertEquals(process.getId(), child2.getProcess().getId());
            // Child1 -> Child2
            Assert.assertEquals(child1.getId(), child2.getPrevious().getId());

            // Parent2
            Assert.assertEquals(P2_ID.get(), parent2.getId());
            Assert.assertEquals(parent.getId(), parent2.getParent().getId());
            Assert.assertEquals(process.getId(), parent2.getProcess().getId());
            // Child2_1
            Assert.assertEquals(C2_1_ID.get(), child2_1.getId());
            Assert.assertEquals(parent2.getId(), child2_1.getParent().getId());
            Assert.assertEquals(process.getId(), child2_1.getProcess().getId());
        });
    }

    @Test
    public void putGetVariable() throws Exception {

        final AtomicReference<Long> ID = new AtomicReference<Long>(-1L);
        final Date DATE = new Date();

        txTemplate.txWithout((s) -> {
            DbActivity act = new DbActivity();
            act.setFqn("Bla");
            em.persist(act);
            ID.set(act.getId());

            activityPersistenceService.putVariable(act, "UNE_CLE_REGDATE", LocalDate.of(2001, 2, 3));
            activityPersistenceService.putVariable(act, "UNE_CLE_ARRAY_LONG", new ArrayLong(1L, 2L, 3L));
            activityPersistenceService.putVariable(act, "UNE_CLE_SERIALIZABLE", new Exception("salut"));
            activityPersistenceService.putVariable(act, "UNE_CLE_TIMESTAMP", DATE);
            activityPersistenceService.putVariable(act, "UNE_CLE_STRING", "UN STRING");
            activityPersistenceService.putVariable(act, "UNE_CLE_LONG", 1L);
            activityPersistenceService.putVariable(act, "UNE_CLE_INT", 4);
            activityPersistenceService.putVariable(act, "UNE_CLE_BOOLEAN", Boolean.TRUE);
        });

        txTemplate.txWithout((s) -> {
            DbActivity activite1 = activityRepository.findOne(ID.get());

            // RegDate
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_REGDATE"));
            Assert.assertEquals(20010203, activite1.getVariable("UNE_CLE_REGDATE"));
            // Array long
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_ARRAY_LONG"));
            ArrayLong al = (ArrayLong) activite1.getVariable("UNE_CLE_ARRAY_LONG");
            Assert.assertEquals(1L, al.get(0));
            Assert.assertEquals(2L, al.get(1));
            Assert.assertEquals(3L, al.get(2));
            //On vérifie la variable serialisée
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_SERIALIZABLE"));
            Exception exception = (Exception) activite1.getVariable("UNE_CLE_SERIALIZABLE");
            Assert.assertEquals("salut", exception.getMessage());
            //On vérifie la variable TimeStamp
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_TIMESTAMP"));
            Assert.assertEquals(DATE, activite1.getVariable("UNE_CLE_TIMESTAMP"));
            //On vérifie la variable String
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_STRING"));
            Assert.assertEquals("UN STRING", activite1.getVariable("UNE_CLE_STRING"));
            //On vérifie la variable Long
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_LONG"));
            Assert.assertEquals(1L, activite1.getVariable("UNE_CLE_LONG"));
            //On vérifie la variable Int
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_INT"));
            Assert.assertEquals(4, activite1.getVariable("UNE_CLE_INT"));

            //On vérifie la variable Boolean
            Assert.Assert.assertTrue(activite1.containsVariable("UNE_CLE_BOOLEAN"));
            Assert.assertEquals(Boolean.TRUE, activite1.getVariable("UNE_CLE_BOOLEAN"));
        });
    }

    @Test
    public void persistVariables() throws Exception {

        final AtomicReference<Long> P_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> A_ID = new AtomicReference<Long>(0L);

        txTemplate.txWithout((s) -> {
            DbTopProcess p = new DbTopProcess();
            p.setFqn("Process1");
            em.persist(p);
            P_ID.set(p.getId());

            activityPersistenceService.putVariable(p, "var1", 12L);
            activityPersistenceService.putVariable(p, "var2", 1);
            activityPersistenceService.putVariable(p, "var3", "Bla");

            DbActivity a1;
            {
                DbActivity a = new DbActivity();
                a.setFqn("act1");
                a.setParent(p);
                em.persist(a);
                a1 = a;

                activityPersistenceService.putVariable(a, "var1", 13L);
                activityPersistenceService.putVariable(a, "var2", 2);
                activityPersistenceService.putVariable(a, "var3", "Bli");
            }
            A_ID.set(a1.getId());
            {
                DbActivity a = new DbActivity();
                a.setFqn("act2");
                a.setParent(p);
                a.setPrevious(a1);
                em.persist(a);
            }
        });

        // re-get vars
        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            Assert.assertEquals(12L, p.getVariable("var1"));
            Assert.assertEquals(1, p.getVariable("var2"));
            Assert.assertEquals("Bla", p.getVariable("var3"));
            Assert.assertNull(p.getVariable("var4"));

            DbActivity a = activityRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);

            Assert.assertEquals(13L, a.getVariable("var1"));
            Assert.assertEquals(2, a.getVariable("var2"));
            Assert.assertEquals("Bli", a.getVariable("var3"));
            Assert.assertNull(a.getVariable("var4"));
        });

        // delete vars
        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            p.removeVariable("var2");

            DbActivity a = activityRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);
            a.removeVariable("var3");
        });

        // re-get vars
        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            Assert.assertEquals(12L, p.getVariable("var1"));
            Assert.assertNull(p.getVariable("var2"));
            Assert.assertEquals("Bla", p.getVariable("var3"));

            DbActivity a = activityRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);

            Assert.assertEquals(13L, a.getVariable("var1"));
            Assert.assertEquals(2, a.getVariable("var2"));
            Assert.assertNull(a.getVariable("var3"));
        });
    }

    @Test
    public void persistProcess() {


        final long ID = txTemplate.txWith((s) -> {
            DbTopProcess p = new DbTopProcess();
            p.setFqn("Process1");
            em.persist(p);

            DbActivity a1;
            {
                DbActivity a = new DbActivity();
                a.setFqn("act1");
                a.setParent(p);
                em.persist(a);
                a1 = a;
            }
            {
                DbActivity a = new DbActivity();
                a.setFqn("act2");
                a.setParent(p);
                a.setPrevious(a1);
                em.persist(a);
            }
            return p.getId();
        });

        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(ID);
            Assert.assertNotNull(p);
        });
    }

}
