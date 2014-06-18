package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.frontend.configuration.AbstractConfigFileService;
import com.autonomy.login.role.Role;
import com.autonomy.login.role.Roles;
import com.autonomy.user.admin.UserAdmin;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class FindConfigFileService extends AbstractConfigFileService<FindConfig> {
    @Autowired
    private SessionLocaleResolver localeResolver;

    @Autowired
    private AciService testAciService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserAdmin userAdmin;

    @Autowired
    private Roles roles;

    private PostgresBoneCPDatabaseService databaseService;


    @Override
    public FindConfig preUpdate(final FindConfig config) {
        return config;
    }

    @Override
    public void postUpdate(final FindConfig config) throws Exception {
        localeResolver.setDefaultLocale(this.getConfig().getLocale());
        databaseService.updateDataSource();

        if (config.getLogin().getCommunity().validate(testAciService, null).isValid()) {
            this.initialiseCommunity();
        }
    }

    @Override
    public void postInitialise(final FindConfig config) throws Exception {
        // break circular dependency
        databaseService = applicationContext.getBean(PostgresBoneCPDatabaseService.class);

        postUpdate(config);
    }

    @Override
    public Class<FindConfig> getConfigClass() {
        return FindConfig.class;
    }

    @Override
    public FindConfig getEmptyConfig() {
        return new FindConfig.Builder().build();
    }

    private void initialiseCommunity(){
        final List<String> roleList = userAdmin.getRoles();

        for(final Role role : roles.getRoles()) {
            if(!roleList.contains(role.getName())){
                userAdmin.addRole(role.getName());
            }
        }
    }
}