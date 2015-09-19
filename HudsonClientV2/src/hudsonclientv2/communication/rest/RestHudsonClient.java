package hudsonclientv2.communication.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Initialize connection fron acegi. Offer the ability to retrieve cookies from it to interrogate the server with cookies after
 * @author libenzi
 *
 */
public class RestHudsonClient {

	public static List<String> init(String urlRepo, String username, String password) throws IOException {
		String urlParameters = "j_username=" + username + "&j_password=" + password + "&rememberMe=false";
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		String request = urlRepo + "/j_acegi_security_check";
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String headerName;
		List<String> cookies = new ArrayList<String>();
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equals("Set-Cookie")) {
				cookies.add(conn.getHeaderField(i));
			}
		}

		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		return cookies;
	}

}
