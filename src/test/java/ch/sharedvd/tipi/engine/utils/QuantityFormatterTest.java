package ch.sharedvd.tipi.engine.utils;

import ch.vd.registre.base.AssertableBaseTest_ForSharedBase;
import org.junit.Test;

public class QuantityFormatterTest extends AssertableBaseTest_ForSharedBase {

	@Test
	public void formatMillis() {
		assertEquals("1[d]6[h]", QuantityFormatter.formatMillis(1000*3600*30+300456));
		assertEquals("13[h]20[m]", QuantityFormatter.formatMillis(1000*3600*13+1234000));
		assertEquals("20[m]12[s]", QuantityFormatter.formatMillis(20*60*1000+12050));
		//assertEquals("12[s]324[ms]", QuantityFormatter.formatMillis(12324));
		assertEquals("12.3[s]", QuantityFormatter.formatMillis(12324));
		assertEquals("123[ms]", QuantityFormatter.formatMillis(123));
	}

	@Test
	public void formatWithSignificant() {
		assertEquals("0.232", QuantityFormatter.formatWithThreeSign(0.23234));
		assertEquals("1.23", QuantityFormatter.formatWithThreeSign(1.23234));
		assertEquals("12.0", QuantityFormatter.formatWithThreeSign(12));
		assertEquals("12.0", QuantityFormatter.formatWithThreeSign(12.00012));
		assertEquals("12.1", QuantityFormatter.formatWithThreeSign(12.1));
		assertEquals("12.1", QuantityFormatter.formatWithThreeSign(12.123));
		assertEquals("99.9", QuantityFormatter.formatWithThreeSign(99.91));
		assertEquals("100.0", QuantityFormatter.formatWithThreeSign(99.99999));
		assertEquals("100", QuantityFormatter.formatWithThreeSign(100.111));
		assertEquals("999", QuantityFormatter.formatWithThreeSign(999.111));
		assertEquals("1023", QuantityFormatter.formatWithThreeSign(1023.8));

		try {
			QuantityFormatter.formatWithThreeSign(1024.1);
			fail();
		}
		catch (Exception e) {
			// OK
		}
	}

	@Test
	public void formatBytes() {
		assertEquals("123[b]", QuantityFormatter.formatBytes(123));
		assertEquals("1023[b]", QuantityFormatter.formatBytes(1023));
		assertEquals("1024[b]", QuantityFormatter.formatBytes(1024));
		assertEquals("1.00[Kb]", QuantityFormatter.formatBytes(1025));
		assertEquals("1.10[Kb]", QuantityFormatter.formatBytes(1130));
		assertEquals("1.17[Kb]", QuantityFormatter.formatBytes(1200));
		assertEquals("9.77[Kb]", QuantityFormatter.formatBytes(10000));
		assertEquals("10.7[Kb]", QuantityFormatter.formatBytes(11000));
		assertEquals("1023[Kb]", QuantityFormatter.formatBytes(1024*1023));
		assertEquals("1.01[Mb]", QuantityFormatter.formatBytes(1024*1030));
		assertEquals("1023[Mb]", QuantityFormatter.formatBytes(1024*1024*1023));
		assertEquals("1.32[Gb]", QuantityFormatter.formatBytes(1024L*1024*450*3));
		assertEquals("1023[Gb]", QuantityFormatter.formatBytes(1024L*1024*1024*1023));
		assertEquals("1024[Gb]", QuantityFormatter.formatBytes(1024L*1024*1024*1024));
		assertEquals("1.00[Tb]", QuantityFormatter.formatBytes(1024L*1024*1024*1025));

		try {
			QuantityFormatter.formatBytes(1024L*1024*1024*1024*1025);
			fail();
		}
		catch (Exception e) {
			// OK
		}
	}

}
