import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.GregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class wyomingDownload {
	static PrintWriter ind;
	static String directory = "C:\\Users\\Crae\\Dropbox\\GROSS2012\\sites\\GROSSJAVA\\WyomingParse\\out\\";
	static String linkPREFIX = "http://weather.uwyo.edu/cgi-bin/sounding?";
	static String region = "region=europe";
	static String typeText = "&TYPE=TEXT%3ALIST";
	// varied
	static String yr = "&YEAR=";
	static String mon = "&MONTH=";
	static String from = "&FROM=all";
	static String SN = "&STNM=";
	// resolution that files split into - the commands are: year, month, 12hr
	static String mode = "12hr";

	public static void main(String args[]) {
		String[] stationNum = { "10868", "06260", "16622", "16245", "8190" };
		String[] stations = { "10868Muenchen-OberschlssheimObservations",
				"06260EHDBDeBiltObservations",
				"16622LGTSThessaloniki(Airport)Observations",
				"16245LIREPraticaDiMareObservations", "8190Observations" };
		
		String[] years = { "2009", "2010", "2011" };
		String[] months = { "01", "02", "03", "04", "05", "06", "07", "08",
				"09", "10", "11", "12" };
		// Loop over the stations once you have em
		for (int k = 0; k < stationNum.length; k++) {
			System.out.println(k);
			for (int i = 0; i < years.length; i++) {
				for (int j = 0; j < months.length; j++) {
					// Assembling link
					String linkGen = linkPREFIX + region + typeText + yr
							+ years[i] + mon + months[j] + from + SN
							+ stationNum[k];
					wyomingSite(linkGen, years[i], months[j], stationNum[k],
							stations[k]);
				}
			}
		}
	}

	public static void wyomingSite(String link, String years, String months,
			String stationNum, String stations) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(link);
			HttpResponse response = client.execute(get);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			if (mode.equals("month")) {
				ind = new PrintWriter(new FileWriter(directory + stationNum
						+ "_" + years + months + ".txt"));
				while ((line = in.readLine()) != null) {

					boolean cL = containsLetter(line);
					if (cL == true | line.contains("--") | line.isEmpty()) {
					} else {

						ind.println(line);
					}
				}
				in.close();
				ind.close();
			} else if (mode.equals("12hr")) {
				while ((line = in.readLine()) != null) {
					if (line.replaceAll(" ", "").contains(stations)) {
						String timestamp = line;
						String[] timestamp_arr = timestamp.split(" ");
						int ts_len = timestamp_arr.length;
						int time_index = ts_len - 4;
						int day_index = ts_len - 3;

						String time = timestamp_arr[time_index];
						String day = timestamp_arr[day_index];

						// ind = new PrintWriter(new FileWriter(directory
						// + stations + years + months + day + time
						// + ".txt"));

						String jd = Day2julianDay(day, months, years);
						ind = new PrintWriter(new FileWriter(directory
								+ stations + "." + years + jd + "."
								+ time.replaceAll("Z", "00Z") + ".txt"));
					} else if (line
							.contains("Station information and sounding indices")) {
						ind.close();
					}

					boolean cL = containsLetter(line);
					if (cL == true | line.contains("--") | line.isEmpty()) {
					} else {
						ind.println(line);
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean containsLetter(String s) {
		if (s == null)
			return false;
		boolean letterFound = false;
		for (int i = 0; !letterFound && i < s.length(); i++)
			letterFound = letterFound || Character.isLetter(s.charAt(i));
		return letterFound;
	}

	public static String Day2julianDay(String DOM, String MON, String YEAR) {

		int intDOM = Integer.parseInt(DOM);
		int intMON = Integer.parseInt(MON) - 1; // -1 because months start at 0;
		int intYEAR = Integer.parseInt(YEAR);

		GregorianCalendar gc = new GregorianCalendar();
		gc.set(GregorianCalendar.DAY_OF_MONTH, intDOM);
		gc.set(GregorianCalendar.MONTH, intMON);
		gc.set(GregorianCalendar.YEAR, intYEAR);
		int intDOY = gc.get(GregorianCalendar.DAY_OF_YEAR);

		return String.format("%03d", intDOY);
	}
}
