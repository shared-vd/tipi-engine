package ch.vd.registre.tipi.command;

import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.vd.registre.tipi.action.parentChild.TstParentProcess;
import ch.vd.registre.tipi.command.annotated.AnnotatedSubProcess;
import ch.vd.registre.tipi.command.annotated.AnnotatedTopProcess;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

;

public class MetaModelHelperTest {

    @Test
    public void testGetTopProcessMetaFromStaticField() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(TstParentProcess.class.getName());
        assertNotNull(meta);
        Assert.assertEquals("Bla bla", meta.getDescription());
        Assert.assertEquals(6, meta.getPriority());
        Assert.assertEquals(-1, meta.getNbMaxTopConcurrent());
        Assert.assertEquals(20, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetTopProcessMetaFromAnnotationWithClassName() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(AnnotatedTopProcess.class.getName());
        assertNotNull(meta);
        Assert.assertEquals("Test TopProcess", meta.getDescription());
        Assert.assertEquals(100, meta.getPriority());
        Assert.assertEquals(-1, meta.getNbMaxTopConcurrent());
        Assert.assertEquals(-1, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetTopProcessMetaFromAnnotationWithClass() {
        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMetaModel(AnnotatedTopProcess.class);
        assertNotNull(meta);
        Assert.assertEquals("Test TopProcess", meta.getDescription());
        Assert.assertEquals(100, meta.getPriority());
        Assert.assertEquals(-1, meta.getNbMaxTopConcurrent());
        Assert.assertEquals(-1, meta.getNbMaxConcurrent());
    }

    @Test
    public void testGetSubProcessMetaFromAnnotationWithClass() {
        SubProcessMetaModel meta = MetaModelHelper.getSubProcessMetaModel(AnnotatedSubProcess.class);
        assertNotNull(meta);
        Assert.assertEquals("Test SubProcess", meta.getDescription());
    }

    @Test
    public void testGetActivityMetaModel() {

        // With static field
        ActivityMetaModel meta = MetaModelHelper.getActivityMetaModel(TstParentProcess.class);
        assertNotNull(meta);
        Assert.assertTrue(TopProcessMetaModel.class.isAssignableFrom(meta.getClass()));

        // With annotation
        meta = MetaModelHelper.getActivityMetaModel(AnnotatedTopProcess.class);
        assertNotNull(meta);
        Assert.assertTrue(TopProcessMetaModel.class.isAssignableFrom(meta.getClass()));

        // With annotation subprocess
        meta = MetaModelHelper.getActivityMetaModel(AnnotatedSubProcess.class);
        assertNotNull(meta);
        Assert.assertTrue(SubProcessMetaModel.class.isAssignableFrom(meta.getClass()));
    }
}
