package com.autonomy.abc.selenium.util;

import com.autonomy.aci.client.util.AciURLCodec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlParams {

	private static final AciURLCodec codec = AciURLCodec.getInstance();

	public static Map<String, String> parse(final String params) {
		return parse(params, false);
	}

	public static Map<String, String> toLowerCase(final Map<String, String> in) {
		final Map<String, String> out = new LinkedHashMap<>();

		for (final String key : in.keySet()) {
			out.put(key.toLowerCase(), in.get(key));
		}

		return out;
	}

	public static String serialise(final Map<String, String> params) {
		if (params.isEmpty()) return "";
		final StringBuilder partialOutput = new StringBuilder();

		for (final String key : params.keySet()) {
			partialOutput.append('&').append(codec.encode(key)).append('=').append(codec.encode(params.get(key)));
		}

		partialOutput.deleteCharAt(0);
		return partialOutput.toString();
	}

	public static Map<String, String> parseLowerCase(final String query) {
		return parse(query, true);
	}

	private static Map<String, String> parse(final String query, final boolean toLowerCase) {
		final Map<String, String> output = new HashMap<>();
		if (query.isEmpty()) return output;
		final String[] paramsArray = query.split("&");

		for (final String param : paramsArray) {
			final String[] keyAndVal = param.split("=", 2);
			final String key = codec.decode(keyAndVal[0]);
			output.put(toLowerCase ? key.toLowerCase() : key, codec.decode(keyAndVal[1]));
		}

		return output;
	}
}
