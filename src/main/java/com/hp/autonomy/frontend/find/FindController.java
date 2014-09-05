package com.hp.autonomy.frontend.find;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Controller
public class FindController {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @RequestMapping("/")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if(LoginTypes.DEFAULT.equals(configService.getConfig().getAuthentication().getMethod())) {
            response.sendRedirect(contextPath + "/loginPage");
        }
        else {
            response.sendRedirect(contextPath + "/public/");
        }
    }

}
