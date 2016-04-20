package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.Map;

@Controller
public class HodFindController extends FindController {

    @Override
    protected Map<String, Object> getPublicConfig() {
        return Collections.emptyMap();
    }
}
