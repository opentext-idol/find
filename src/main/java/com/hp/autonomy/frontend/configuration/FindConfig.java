package com.hp.autonomy.frontend.configuration;

import com.autonomy.frontend.configuration.AbstractConfig;
import com.autonomy.frontend.configuration.ConfigException;
import com.autonomy.frontend.configuration.Login;
import com.autonomy.frontend.configuration.LoginConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonDeserialize(builder = FindConfig.Builder.class)
@Getter
@EqualsAndHashCode(callSuper = false)
public class FindConfig extends AbstractConfig<FindConfig> implements LoginConfig<FindConfig> {

    private final Login login;
    private final IodConfig iod;

    private FindConfig(final Builder builder) {
        this.login = builder.login;
        this.iod = builder.iod;
    }

    @Override
    public FindConfig merge(final FindConfig config) {
        if(config != null) {
            final Builder builder = new Builder();

            builder.setLogin(this.login == null ? config.login : this.login.merge(config.login));
            builder.setIod(this.iod == null ? config.iod : this.iod.merge(config.iod));

            return builder.build();
        }
        else {
            return this;
        }
    }

    @Override
    public FindConfig withoutDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withoutDefaultLogin();

        return builder.build();
    }

    @Override
    public FindConfig generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.generateDefaultLogin();

        return builder.build();
    }

    public FindConfig withHashedPasswords() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withHashedPasswords();

        return builder.build();
    }

    @Override
    public FindConfig withEncryptedPasswords(final TextEncryptor textEncryptor) {
        return this;
    }

    @Override
    public FindConfig withDecryptedPasswords(final TextEncryptor textEncryptor) {
        return this;
    }

    @Override
    public void basicValidate() throws ConfigException {
        if(!this.login.getMethod().equalsIgnoreCase("default")){
            this.login.basicValidate();
        }
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private Login login;
        private IodConfig iod;

        public Builder() {}

        public Builder(final FindConfig config) {
            this.login = config.login;
            this.iod = config.iod;
        }

        public Builder setLogin(final Login login) {
            this.login = login;
            return this;
        }

        public Builder setIod(final IodConfig iod) {
            this.iod = iod;
            return this;
        }

        public FindConfig build() {
            return new FindConfig(this);
        }
    }

}
