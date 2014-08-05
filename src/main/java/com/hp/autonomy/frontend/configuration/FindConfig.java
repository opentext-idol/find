package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.frontend.configuration.AbstractConfig;
import com.autonomy.frontend.configuration.ConfigException;
import com.autonomy.frontend.configuration.Login;
import com.autonomy.frontend.configuration.LoginConfig;
import com.autonomy.frontend.configuration.PasswordsConfig;
import com.autonomy.frontend.configuration.database.Postgres;
import com.autonomy.user.admin.UserAdminConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jasypt.util.text.TextEncryptor;

@JsonDeserialize(builder = FindConfig.Builder.class)
@Getter
@EqualsAndHashCode(callSuper = false)
public class FindConfig extends AbstractConfig<FindConfig> implements LoginConfig<FindConfig>, PasswordsConfig<FindConfig>, UserAdminConfig {

    private final Locale locale;
    private final Login login;
    private final Postgres postgres;

    private FindConfig(final Builder builder) {
        this.locale = builder.locale;
        this.login = builder.login;
        this.postgres = builder.postgres;
    }

    @Override
    public FindConfig merge(final FindConfig config) {
        if(config != null) {
            final Builder builder = new Builder();

            builder.setLocale(this.locale == null ? config.locale : this.locale);
            builder.setLogin(this.login == null ? config.login : this.login.merge(config.login));
            builder.setPostgres(this.postgres == null ? config.postgres : this.postgres.merge(config.postgres));

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
    public FindConfig withoutPasswords() {
        final Builder builder = new Builder(this);

        builder.postgres = builder.postgres.withoutPassword();

        return builder.build();
    }

    @Override
    public FindConfig generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.generateDefaultLogin();

        return builder.build();
    }

    @Override
    public FindConfig withHashedPasswords() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withHashedPasswords();

        return builder.build();
    }

    @Override
    public FindConfig withEncryptedPasswords(final TextEncryptor textEncryptor) {
        final Builder builder = new Builder(this);

        builder.postgres = builder.postgres.withEncryptedPassword(textEncryptor);

        return builder.build();
    }

    @Override
    public FindConfig withDecryptedPasswords(final TextEncryptor textEncryptor) {
        final Builder builder = new Builder(this);

        builder.postgres = builder.postgres.withDecryptedPassword(textEncryptor);

        return builder.build();
    }

    @Override
    public void basicValidate() throws ConfigException {
        if(!this.login.getMethod().equalsIgnoreCase("default")){
            this.login.basicValidate();
        }
    }

    @Override
    @JsonIgnore
    public AciServerDetails getCommunityDetails() {
        return this.getLogin().getCommunity().toAciServerDetails();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private Locale locale;
        private Login login;
        private Postgres postgres;

        public Builder() {}

        public Builder(final FindConfig config) {
            this.locale = config.locale;
            this.login = config.login;
            this.postgres = config.postgres;
        }

        public Builder setLogin(final Login login) {
            this.login = login;
            return this;
        }

        public Builder setPostgres(final Postgres postgres) {
            this.postgres = postgres;
            return this;
        }

        public Builder setLocale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        public FindConfig build() {
            return new FindConfig(this);
        }
    }

}
