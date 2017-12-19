package ch.vd.registre.tipi.command;

import ch.sharedvd.tipi.engine.action.parentChild.TstParentProcess;
import ch.sharedvd.tipi.engine.command.annotated.AnnotatedSubProcess;
import ch.sharedvd.tipi.engine.command.annotated.AnnotatedTopProcess;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetaModelHelperTest {

    @Test
    public void testGetTopProcessMetaFromStaticField() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(TstParentProcess.class.getName());
        assertNotNull(meta);
        assertEquals("Bla bla", meta.getDescription());
        assertEquals(6, meta.getPriority());
        assertEquals(-1, meta.getNbMaxTopConcurrent());
        assertEquals(20, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetTopProcessMetaFromAnnotationWithClassName() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(AnnotatedTopProcess.class.getName());
        assertNotNull(meta);
        assertEquals("Test TopProcess", meta.getDescription());
        assertEquals(100, meta.getPriority());
        assertEquals(-1, meta.getNbMaxTopConcurrent());
        assertEquals(-1, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetTopProcessMetaFromAnnotationWithClass() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMetaModel(AnnotatedTopProcess.class);
        assertNotNull(meta);
        assertEquals("Test TopProcess", meta.getDescription());
        assertEquals(100, meta.getPriority());
        assertEquals(-1, meta.getNbMaxTopConcurrent());
        assertEquals(-1, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetSubProcessMetaFromAnnotationWithClass() {
        SubProcessMetaModel meta = MetaModelHelper.getSubProcessMetaModel(AnnotatedSubProcess.class);
        assertNotNull(meta);
        assertEquals("Test SubProcess", meta.getDescription());
    }

    @Test
    public void testGetActivityMetaModel() {

        // With static field
        ActivityMetaModel meta = MetaModelHelper.getActivityMetaModel(TstParentProcess.class);
        assertNotNull(meta);
        assertTrue(TopProcessMetaModel.class.isAssignableFrom(meta.getClass()));

        // With annotation
        meta = MetaModelHelper.getActivityMetaModel(AnnotatedTopProcess.class);
        assertNotNull(meta);
        assertTrue(TopProcessMetaModel.class.isAssignableFrom(meta.getClass()));

        // With annotation subprocess
        meta = MetaModelHelper.getActivityMetaModel(AnnotatedSubProcess.class);
        assertNotNull(meta);
        assertTrue(SubProcessMetaModel.class.isAssignableFrom(meta.getClass()));
    }
}
