package ch.vd.shared.tipi.engine.model;

import ch.vd.shared.tipi.engine.AbstractTipiPersistenceTest;
import ch.vd.shared.tipi.engine.utils.ArrayLong;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class PersistenceTest extends AbstractTipiPersistenceTest {

    @Test
    public void processName_is_helpful_in_ui() throws Exception {

        final AtomicReference<Long> TOP_PROCESS = new AtomicReference<Long>(0L);
        final AtomicReference<Long> ACT_1 = new AtomicReference<Long>(0L);
        final AtomicReference<Long> ACT_2 = new AtomicReference<Long>(0L);

        txTemplate.doWithout(() -> {
            TopProcessModel parent = new TopProcessModel();
            parent.setFqn("Process1");
            em.persist(parent);
            TOP_PROCESS.set(parent.getId());

            ActivityModel child1;
            {
                child1 = new ActivityModel();
                child1.setFqn("act1");
                child1.setParent(parent);
                child1.setProcess(parent);
                em.persist(child1);
                ACT_1.set(child1.getId());
            }
            {
                ActivityModel child2 = new ActivityModel();
                child2.setFqn("act2");
                child2.setParent(parent);
                child2.setProcess(parent);
                child2.setPrevious(child1);
                em.persist(child2);
                ACT_2.set(child2.getId());
            }
        });

        txTemplate.doWithout(() -> {
            ActivityModel activite1 = activityModelRepository.findOne(ACT_1.get());
            Assert.assertEquals("Process1", activite1.getProcessName());

            ActivityModel activite2 = activityModelRepository.findOne(ACT_2.get());
            Assert.assertEquals("Process1", activite2.getProcessName());

            ActivityModel topProcess = activityModelRepository.findOne(TOP_PROCESS.get());
            Assert.assertEquals("Process1", topProcess.getProcessName());
        });
    }


    @Test
    public void callstack_2000() throws Exception {

        final AtomicReference<Long> ID = new AtomicReference<Long>(0L);

        // 2000 chars
        final StringBuilder callstack = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            callstack.append('C');
        }

        txTemplate.doWithout(() -> {
            ActivityModel act = new ActivityModel();
            act.setFqn("Bla");
            em.persist(act);
            ID.set(act.getId());

            act.setCallstack(callstack.toString());
        });

        txTemplate.doWithout(() -> {
            ActivityModel activite1 = activityModelRepository.findOne(ID.get());

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

        txTemplate.doWithout(() -> {
            ActivityModel act = new ActivityModel();
            act.setFqn("Bla");
            em.persist(act);
            ID.set(act.getId());

            // Truncate à 2000 chars
            act.setCallstack(callstack.toString());
        });

        txTemplate.doWithout(() -> {
            ActivityModel activite1 = activityModelRepository.findOne(ID.get());

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

        txTemplate.doWithout(() -> {
            TopProcessModel parent = new TopProcessModel();
            parent.setFqn("Process1");
            em.persist(parent);
            P_ID.set(parent.getId());

            ActivityModel child1;
            {
                child1 = new ActivityModel();
                child1.setFqn("act1");
                child1.setParent(parent);
                child1.setProcess(parent);
                em.persist(child1);
                C1_ID.set(child1.getId());
            }
            {
                ActivityModel child2 = new ActivityModel();
                child2.setFqn("act2");
                child2.setParent(parent);
                child2.setProcess(parent);
                child2.setPrevious(child1);
                em.persist(child2);
                C2_ID.set(child2.getId());
            }

            {
                TopProcessModel parent2 = new TopProcessModel();
                parent2.setFqn("Process1");
                parent2.setParent(parent);
                parent2.setProcess(parent);
                em.persist(parent2);
                P2_ID.set(parent2.getId());

                ActivityModel child2_1;
                {
                    child2_1 = new ActivityModel();
                    child2_1.setFqn("act2.1");
                    child2_1.setParent(parent2);
                    child2_1.setProcess(parent);
                    em.persist(child2_1);
                    C2_1_ID.set(child2_1.getId());
                }
            }
        });

        txTemplate.doWithout(() -> {
            TopProcessModel parent = topProcessModelRepository.findOne(P_ID.get());
            ActivityModel child1 = activityModelRepository.findOne(C1_ID.get());
            ActivityModel child2 = activityModelRepository.findOne(C2_ID.get());
            TopProcessModel parent2 = topProcessModelRepository.findOne(P2_ID.get());
            ActivityModel child2_1 = activityModelRepository.findOne(C2_1_ID.get());

            TopProcessModel process = parent;

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

        txTemplate.doWithout(() -> {
            ActivityModel act = new ActivityModel();
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

        txTemplate.doWithout(() -> {
            ActivityModel activite1 = activityModelRepository.findOne(ID.get());

            // RegDate
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_REGDATE"));
            Assert.assertEquals(20010203, activite1.getVariable("UNE_CLE_REGDATE"));
            // Array long
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_ARRAY_LONG"));
            ArrayLong al = (ArrayLong) activite1.getVariable("UNE_CLE_ARRAY_LONG");
            Assert.assertEquals(1L, al.get(0));
            Assert.assertEquals(2L, al.get(1));
            Assert.assertEquals(3L, al.get(2));
            //On vérifie la variable serialisée
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_SERIALIZABLE"));
            Exception exception = (Exception) activite1.getVariable("UNE_CLE_SERIALIZABLE");
            Assert.assertEquals("salut", exception.getMessage());
            //On vérifie la variable TimeStamp
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_TIMESTAMP"));
            Assert.assertEquals(DATE, activite1.getVariable("UNE_CLE_TIMESTAMP"));
            //On vérifie la variable String
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_STRING"));
            Assert.assertEquals("UN STRING", activite1.getVariable("UNE_CLE_STRING"));
            //On vérifie la variable Long
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_LONG"));
            Assert.assertEquals(1L, activite1.getVariable("UNE_CLE_LONG"));
            //On vérifie la variable Int
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_INT"));
            Assert.assertEquals(4, activite1.getVariable("UNE_CLE_INT"));

            //On vérifie la variable Boolean
            Assert.assertTrue(activite1.containsVariable("UNE_CLE_BOOLEAN"));
            Assert.assertEquals(Boolean.TRUE, activite1.getVariable("UNE_CLE_BOOLEAN"));
        });
    }

    @Test
    public void persistVariables() throws Exception {

        final AtomicReference<Long> P_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> A_ID = new AtomicReference<Long>(0L);

        txTemplate.doWithout(() -> {
            TopProcessModel p = new TopProcessModel();
            p.setFqn("Process1");
            em.persist(p);
            P_ID.set(p.getId());

            activityPersistenceService.putVariable(p, "var1", 12L);
            activityPersistenceService.putVariable(p, "var2", 1);
            activityPersistenceService.putVariable(p, "var3", "Bla");

            ActivityModel a1;
            {
                ActivityModel a = new ActivityModel();
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
                ActivityModel a = new ActivityModel();
                a.setFqn("act2");
                a.setParent(p);
                a.setPrevious(a1);
                em.persist(a);
            }
        });

        // re-get vars
        txTemplate.doWithout(() -> {
            TopProcessModel p = topProcessModelRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            Assert.assertEquals(12L, p.getVariable("var1"));
            Assert.assertEquals(1, p.getVariable("var2"));
            Assert.assertEquals("Bla", p.getVariable("var3"));
            Assert.assertNull(p.getVariable("var4"));

            ActivityModel a = activityModelRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);

            Assert.assertEquals(13L, a.getVariable("var1"));
            Assert.assertEquals(2, a.getVariable("var2"));
            Assert.assertEquals("Bli", a.getVariable("var3"));
            Assert.assertNull(a.getVariable("var4"));
        });

        // delete vars
        txTemplate.doWithout(() -> {
            TopProcessModel p = topProcessModelRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            p.removeVariable("var2");

            ActivityModel a = activityModelRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);
            a.removeVariable("var3");
        });

        // re-get vars
        txTemplate.doWithout(() -> {
            TopProcessModel p = topProcessModelRepository.findOne(P_ID.get());
            Assert.assertNotNull(p);
            Assert.assertEquals(12L, p.getVariable("var1"));
            Assert.assertNull(p.getVariable("var2"));
            Assert.assertEquals("Bla", p.getVariable("var3"));

            ActivityModel a = activityModelRepository.findOne(A_ID.get());
            Assert.assertNotNull(a);

            Assert.assertEquals(13L, a.getVariable("var1"));
            Assert.assertEquals(2, a.getVariable("var2"));
            Assert.assertNull(a.getVariable("var3"));
        });
    }

    @Test
    public void persistProcess() throws Exception {


        final long ID = txTemplate.doWith(() -> {
            TopProcessModel p = new TopProcessModel();
            p.setFqn("Process1");
            em.persist(p);

            ActivityModel a1;
            {
                ActivityModel a = new ActivityModel();
                a.setFqn("act1");
                a.setParent(p);
                em.persist(a);
                a1 = a;
            }
            {
                ActivityModel a = new ActivityModel();
                a.setFqn("act2");
                a.setParent(p);
                a.setPrevious(a1);
                em.persist(a);
            }
            return p.getId();
        });

        txTemplate.doWithout(() -> {
            TopProcessModel p = topProcessModelRepository.findOne(ID);
            Assert.assertNotNull(p);
        });
    }

}
