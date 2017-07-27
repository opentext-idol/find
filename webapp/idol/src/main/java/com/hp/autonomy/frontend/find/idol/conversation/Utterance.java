package com.hp.autonomy.frontend.find.idol.conversation;

/*
 * $Id:$
 *
 * Copyright (c) 2017, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$ 
 */

import lombok.Data;

@Data
public class Utterance {
    private final boolean user;
    private final String text;
}
