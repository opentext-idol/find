package com.autonomy.abc.selenium.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = HostAndPorts.Builder.class)

public class HostAndPorts {

	private final String protocol;
	private final String host;
	private final int port;
	private final String name;

	private HostAndPorts(final Builder builder) {
		this.protocol = builder.protocol;
		this.host = builder.host;
		this.port = builder.port;
		this.name = builder.name;
	}

	public String getUrl() {
		return protocol + "://" + host + ':' + port;
	}

	public int getPortNumber() {
		return port;
	}

	public String getHostName() {
		return host;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getName() {
		return name;
	}

	@JsonPOJOBuilder(withPrefix = "set")
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Builder {

		private String protocol;
		private String host;
		private int port;
		private String name;

		public Builder() {
		}

		public Builder setProtocol(final String protocol) {
			this.protocol = protocol;
			return this;
		}

		public Builder setHost(final String host) {
			this.host = host;
			return this;
		}

		public Builder setPort(final int port) {
			this.port = port;
			return this;
		}

		public Builder setCommonName(final String name) {
			this.name = name;
			return this;
		}

		public HostAndPorts build() {
			return new HostAndPorts(this);
		}
	}
}

