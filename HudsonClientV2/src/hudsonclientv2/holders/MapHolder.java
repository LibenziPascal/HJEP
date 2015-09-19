package hudsonclientv2.holders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapHolder {
	private MapHolder() {

	}

	private final static Map<String, SimpleUser> USER_BY_URL = new HashMap<String, SimpleUser>();

	public static void putEntry(String url, String username, String password) {
		USER_BY_URL.put(url, new SimpleUser(username, password));
	}

	public static SimpleUser getEntry(String url) {
		return USER_BY_URL.get(url);
	}

	public static Set<String> getUrls() {
		return USER_BY_URL.keySet();
	}

	public static void save() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File("usersByUrl.csv")));
			for (Entry<String, SimpleUser> ubu : USER_BY_URL.entrySet()) {
				writer.write(ubu.getKey() + "," + ubu.getValue().getUsername() + "," + ubu.getValue().getPassword());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void init() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("usersByUrl.csv")));
			String r = null;
			while ((r = reader.readLine()) != null) {
				String[] values = r.split(",");
				USER_BY_URL.put(values[0], new SimpleUser(values[1], values[2]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
