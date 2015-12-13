package hudsonclientv2.communication.rest.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities that offers the ability to look for words on a simple String
 * @author libenzi
 *
 */
public final class TxtReader {

	/**
	 * Hide utility class constructor
	 */
	private TxtReader() {
	}

	/**
	 * Are words present in a text not case sensitive
	 * @param txt the whole text
	 * @param words The words we're lookin for
	 * @return true if all words has been found, false else
	 */
	public static boolean isWordsPresentsInTxtNotCaseSensitive(final String txt, final String... words) {
		final List<String> wordsUpper = new ArrayList<String>();
		for (final String word : words) {
			wordsUpper.add(word.toUpperCase());
		}
		return isWordsPresentsInTxt(txt.toUpperCase(), (String[]) wordsUpper.toArray(new String[wordsUpper.size()]));
	}

	/**
	 * Check if words are in whole text, but case sensitive. USed by the method above
	 * @param txt The whole text
	 * @param words words to found
	 * @return true if all words have been found, false else
	 */
	public static boolean isWordsPresentsInTxt(final String txt, final String... words) {
		boolean isWordPresent = true;
		for (final String word : words) {
			if (!txt.contains(word)) {
				isWordPresent = false;
			}
		}
		return isWordPresent;
	}

}