package com.hp.autonomy.frontend.authentication;

import com.autonomy.frontend.configuration.ConfigService;
import com.autonomy.frontend.configuration.LoginConfig;
import java.io.IOException;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.autonomy.login.LoginControllerCore;
import com.autonomy.login.filter.LoginFilter;
import com.autonomy.login.role.Roles;
import com.autonomy.login.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * $Id: //depot/products/frontend/site-admin/trunk/webapp/src/main/java/com/autonomy/controlcentre/authentication/LoginController.java#11 $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: luca.mandrioli $ on $Date: 2014/01/06 $
 */
@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
    private UserService userService;

    @Autowired
    private ConfigService<LoginConfig<?>> configService;

    @Autowired
    private LoginFilter loginFilter;

    @Autowired
    @Qualifier("roles")
    private Roles roles;

    @Resource(name = "roleAdminPrivileges")
    private Set<String> loginPrivileges;

    private LoginControllerCore loginControllerCore;

    @PostConstruct
    public void init(){
        loginControllerCore = new LoginControllerCore(userService, configService, loginFilter, roles, loginPrivileges);
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    @ResponseBody
    public void login(
        @RequestParam("username") final String username,
        @RequestParam("password") final String password,
        final HttpServletRequest request,
        final HttpServletResponse response,
        final HttpSession session
    ) throws IOException {
        this.loginControllerCore.login(username, password, request, response, session);
    }

    @RequestMapping(value = "/authenticated/session")
    @ResponseBody
    public void authenticatedLogin(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final HttpSession session
    ) throws IOException {
        this.loginControllerCore.authenticatedLogin(request, response, session);
    }
}