package hudsonclientv2.communication.rest;

import java.util.List;

public class CookieForUrl {
	final List<String> cookies;
	final String url;

	public CookieForUrl(List<String> cookies, String url) {
		super();
		this.cookies = cookies;
		this.url = url;
	}

	public List<String> getCookies() {
		return cookies;
	}

	public String getUrl() {
		return url;
	}
}