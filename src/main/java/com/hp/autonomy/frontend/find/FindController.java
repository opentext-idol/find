package com.hp.autonomy.frontend.find;

import com.autonomy.frontend.configuration.ConfigService;
import com.autonomy.frontend.configuration.LoginConfig;
import com.autonomy.frontend.configuration.LoginTypes;
import com.autonomy.login.filter.LoginFilter;
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
    private ConfigService<? extends LoginConfig<?>> configService;

    @Autowired
    private LoginFilter loginFilter;

    @RequestMapping("index")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if(LoginTypes.DEFAULT.equals(configService.getConfig().getLogin().getMethod())) {
            response.sendRedirect(contextPath + loginFilter.getLoginPageUrl());
        }
        else {
            response.sendRedirect(contextPath + "/public");
        }
    }

}
