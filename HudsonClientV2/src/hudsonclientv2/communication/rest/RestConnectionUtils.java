package hudsonclientv2.communication.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author libenzi Initialize a connection for interrogating a rest API with
 *         aceji servlet, read from the REST Api and serialize it in a String
 */
public class RestConnectionUtils {

	/**
	 * Utility class. Must not be instantiated
	 */
	private RestConnectionUtils() {

	}

	/**
	 * Make connection from an URL, read from it and resent a String from it
	 * 
	 * @param cookies
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public static String makeConnectionAndRead(List<String> cookies, URL url) throws IOException, ProtocolException {
		String myCookie = "";
		for (String string : cookies) {
			myCookie += string;
		}
		String urlParameters = "";
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;

		Long beginConn = System.currentTimeMillis();
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		Long endConn = System.currentTimeMillis();
		System.out.println("connection took: " + (endConn - beginConn) + " ms");
		conn.setDoOutput(true);
		conn.setRequestProperty("Cookie", myCookie);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(true);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

		wr.write(postData);

		wr.flush();

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String content = "";
		while ((line = reader.readLine()) != null) {
			content += "\n" + line;
		}

		wr.close();
		reader.close();
		return content;
	}

}
