package com.hp.autonomy.frontend.find.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(IdolCondition.class)
@ComponentScan("com.hp.autonomy.frontend.find.idol")
public class IdolConfigurationLoader {
}
