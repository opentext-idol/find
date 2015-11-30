package com.hp.autonomy.frontend.find.idol.view;

import com.hp.autonomy.frontend.view.ViewContentSecurityPolicy;
import com.hp.autonomy.frontend.view.idol.ViewServerCopyResponseProcessor;
import com.hp.autonomy.frontend.view.idol.ViewServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Controller
@RequestMapping("/api/public/view")
@Slf4j
public class IdolViewController {
    @Autowired
    private ViewServerService viewServerService;

    @RequestMapping(value = "/viewDocument", method = RequestMethod.GET)
    public void viewDocument(
            @RequestParam("reference") final String reference,
            @RequestParam("index") final String index,
            final HttpServletResponse response
    ) throws IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        ViewContentSecurityPolicy.addContentSecurityPolicy(response);
        viewServerService.viewDocument(reference, Collections.singletonList(index), new ViewServerCopyResponseProcessor(response.getOutputStream(), reference));
    }
}
