package ch.sharedvd.tipi.engine.utils;

import org.junit.Assert;
import org.junit.Test;

public class QuantityFormatterTest {

    @Test
    public void formatMillis() {
        Assert.assertEquals("1[d]6[h]", QuantityFormatter.formatMillis(1000 * 3600 * 30 + 300456));
        Assert.assertEquals("13[h]20[m]", QuantityFormatter.formatMillis(1000 * 3600 * 13 + 1234000));
        Assert.assertEquals("20[m]12[s]", QuantityFormatter.formatMillis(20 * 60 * 1000 + 12050));
        //Assert.assertEquals("12[s]324[ms]", QuantityFormatter.formatMillis(12324));
        Assert.assertEquals("12.3[s]", QuantityFormatter.formatMillis(12324));
        Assert.assertEquals("123[ms]", QuantityFormatter.formatMillis(123));
    }

    @Test
    public void formatWithSignificant() {
        Assert.assertEquals("0.232", QuantityFormatter.formatWithThreeSign(0.23234));
        Assert.assertEquals("1.23", QuantityFormatter.formatWithThreeSign(1.23234));
        Assert.assertEquals("12.0", QuantityFormatter.formatWithThreeSign(12));
        Assert.assertEquals("12.0", QuantityFormatter.formatWithThreeSign(12.00012));
        Assert.assertEquals("12.1", QuantityFormatter.formatWithThreeSign(12.1));
        Assert.assertEquals("12.1", QuantityFormatter.formatWithThreeSign(12.123));
        Assert.assertEquals("99.9", QuantityFormatter.formatWithThreeSign(99.91));
        Assert.assertEquals("100.0", QuantityFormatter.formatWithThreeSign(99.99999));
        Assert.assertEquals("100", QuantityFormatter.formatWithThreeSign(100.111));
        Assert.assertEquals("999", QuantityFormatter.formatWithThreeSign(999.111));
        Assert.assertEquals("1023", QuantityFormatter.formatWithThreeSign(1023.8));

        try {
            QuantityFormatter.formatWithThreeSign(1024.1);
            Assert.fail("");
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void formatBytes() {
        Assert.assertEquals("123[b]", QuantityFormatter.formatBytes(123));
        Assert.assertEquals("1023[b]", QuantityFormatter.formatBytes(1023));
        Assert.assertEquals("1024[b]", QuantityFormatter.formatBytes(1024));
        Assert.assertEquals("1.00[Kb]", QuantityFormatter.formatBytes(1025));
        Assert.assertEquals("1.10[Kb]", QuantityFormatter.formatBytes(1130));
        Assert.assertEquals("1.17[Kb]", QuantityFormatter.formatBytes(1200));
        Assert.assertEquals("9.77[Kb]", QuantityFormatter.formatBytes(10000));
        Assert.assertEquals("10.7[Kb]", QuantityFormatter.formatBytes(11000));
        Assert.assertEquals("1023[Kb]", QuantityFormatter.formatBytes(1024 * 1023));
        Assert.assertEquals("1.01[Mb]", QuantityFormatter.formatBytes(1024 * 1030));
        Assert.assertEquals("1023[Mb]", QuantityFormatter.formatBytes(1024 * 1024 * 1023));
        Assert.assertEquals("1.32[Gb]", QuantityFormatter.formatBytes(1024L * 1024 * 450 * 3));
        Assert.assertEquals("1023[Gb]", QuantityFormatter.formatBytes(1024L * 1024 * 1024 * 1023));
        Assert.assertEquals("1024[Gb]", QuantityFormatter.formatBytes(1024L * 1024 * 1024 * 1024));
        Assert.assertEquals("1.00[Tb]", QuantityFormatter.formatBytes(1024L * 1024 * 1024 * 1025));

        try {
            QuantityFormatter.formatBytes(1024L * 1024 * 1024 * 1024 * 1025);
            Assert.fail("");
        } catch (Exception e) {
            // OK
        }
    }

}
