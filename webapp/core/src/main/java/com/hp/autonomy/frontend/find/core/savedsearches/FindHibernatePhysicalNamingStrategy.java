package com.hp.autonomy.frontend.find.core.savedsearches;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class FindHibernatePhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalColumnName(final Identifier logicalName, final JdbcEnvironment jdbcEnvironment) {
        if (logicalName.isQuoted()) {
            return logicalName;
        } else {
            return super.toPhysicalColumnName(logicalName, jdbcEnvironment);
        }
    }

}
