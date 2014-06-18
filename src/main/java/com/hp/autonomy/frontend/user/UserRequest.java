package com.hp.autonomy.frontend.user;

/*
 * $Id: $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */

import lombok.Getter;

import java.util.List;

@Getter
public class UserRequest {
    private String username;
    private String password;
    private List<String> roles;
}