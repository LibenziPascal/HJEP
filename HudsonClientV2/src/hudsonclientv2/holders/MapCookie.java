package hudsonclientv2.holders;

import hudsonclientv2.communication.rest.RestHudsonClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapCookie {
	private MapCookie() {

	}

	private final static Map<String, List<String>> COOKIE_BY_URL = new HashMap<String, List<String>>();

	public static List<String> getEntryNG(String url, String username, String password) throws IOException {
		if (COOKIE_BY_URL.get(url) == null) {
			COOKIE_BY_URL.put(url, RestHudsonClient.init(url, username, password));
		}
		return COOKIE_BY_URL.get(url);
	}
}
