package hudsonclientv2.communication.rest.reader;

import java.util.ArrayList;
import java.util.List;

public class TxtReader {

	private TxtReader() {
	}

	public static boolean isWordsPresentsInTxtNotCaseSensitive(String txt, String... words) {
		List<String> wordsUpper = new ArrayList<String>();
		for (String word : words) {
			wordsUpper.add(word.toUpperCase());
		}
		return isWordsPresentsInTxt(txt.toUpperCase(), (String[]) wordsUpper.toArray(new String[wordsUpper.size()]));
	}

	public static boolean isWordsPresentsInTxt(String txt, String... words) {
		boolean isWordPresent = true;
		for (String word : words) {
			if (!txt.contains(word)) {
				isWordPresent = false;
			}
		}
		return isWordPresent;
	}

}