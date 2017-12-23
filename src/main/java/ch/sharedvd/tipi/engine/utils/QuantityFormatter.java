package ch.sharedvd.tipi.engine.utils;


/**
 * Classe statique permettant de formatter des quantités
 * Supporte le <i>temps</i> et les <i>bytes</i>
 *
 * Exemple:
 *  QuantityFormatter.formatMillis(1234123) -> 20[m]34[s]
 *  QuantityFormatter.formatBytes(1234123) -> 1.18[mb]
 *
 */
public class QuantityFormatter {

	private static long BYTES_KB = 1024;
	private static long BYTES_MB = BYTES_KB*1024;
	private static long BYTES_GB = BYTES_MB*1024;
	private static long BYTES_TB = BYTES_GB*1024;
	private static long BYTES_PB = BYTES_TB*1024;

	private static long SEC_IN_MILLIS = 1000;
	private static long MIN_IN_MILLIS = 60 * SEC_IN_MILLIS;
	private static long HOUR_IN_MILLIS = MIN_IN_MILLIS * 60;
	private static long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

	public static String formatMillis(final long milliseconds) {

		final String str;
		if (milliseconds > DAY_IN_MILLIS) {
			long days = milliseconds / DAY_IN_MILLIS;
			long daysInMillis = days * DAY_IN_MILLIS;

			long hours = (milliseconds - daysInMillis) / HOUR_IN_MILLIS;

			str = ""+days+"[d]"+hours+"[h]";
		}
		else if (milliseconds > HOUR_IN_MILLIS) {
			long hours = milliseconds / HOUR_IN_MILLIS;
			long hoursInMillis = hours * HOUR_IN_MILLIS;

			long mins = (milliseconds - hoursInMillis) / MIN_IN_MILLIS;

			str = ""+hours+"[h]"+mins+"[m]";
		}
		else if (milliseconds > MIN_IN_MILLIS) {
			long mins = milliseconds / MIN_IN_MILLIS;
			long minsInMillis = mins * MIN_IN_MILLIS;

			long secs = (milliseconds - minsInMillis) / SEC_IN_MILLIS;

			str = ""+mins+"[m]"+secs+"[s]";
		}
		else if (milliseconds > SEC_IN_MILLIS) {
			// Formattage x.yy[s]
			str = formatWithThreeSign(milliseconds / 1.0 / SEC_IN_MILLIS)+"[s]";
		}
		else {
			str = formatWithThreeSign(milliseconds)+"[ms]";
		}
		return str;
	}

	public static String formatBytes(final long bytes) {
		Assert.isTrue(bytes < BYTES_PB, "Bytes : [" + bytes +"]");

		final String str;

		if (bytes > BYTES_TB) {
			double gb = (bytes / 1.0 / BYTES_TB);
			String value = formatWithThreeSign(gb);
			str = value+"[Tb]";
		}
		else if (bytes > BYTES_GB) {
			double gb = (bytes / 1.0 / BYTES_GB);
			String value = formatWithThreeSign(gb);
			str = value+"[Gb]";
		}
		else if (bytes > BYTES_MB) {
			double mb = (bytes / 1.0 / BYTES_MB);
			String value = formatWithThreeSign(mb);
			str = value+"[Mb]";
		}
		else if (bytes > BYTES_KB) {
			double kb = (bytes / 1.0 / BYTES_KB);
			String value = formatWithThreeSign(kb);
			str = value+"[Kb]";
		}
		else {
			// Bytes
			String value = formatWithThreeSign(bytes);
			str = value+"[b]";
		}
		return str;
	}

	public static String formatWithThreeSign(double value) {
		Assert.isTrue(value <= 1024.0);

		final String str;
		if (value > 100.0) {
			str = String.format("%d", (long)value);
		}
		else if (value > 10.0) {
			str = String.format("%.1f", value);
		}
		else if (value > 1.0) {
			str = String.format("%.2f", value);
		}
		else {
			str = String.format("%.3f", value);
		}
		return str;
	}
}
