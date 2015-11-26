package com.hp.autonomy.frontend.find.core.parametricFields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagCountInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/api/public/parametric")
public class ParametricValuesController<I extends Identifier, R extends ParametricRequest, T extends QueryTagInfo<C>, C extends QueryTagCountInfo, E extends Exception> {

    @Autowired
    private ParametricValuesService<R, T, C, E> parametricValuesService;

    @Autowired
    private ParametricRequestBuilder<R, I> parametricRequestBuilder;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<T> getParametricValues(
            @RequestParam("databases") final Set<I> databases,
            @RequestParam(value = "fieldNames", required = false) final Set<String> fieldNames,
            @RequestParam("queryText") final String queryText,
            @RequestParam("fieldText") final String fieldText
    ) throws E {
        final R parametricRequest = parametricRequestBuilder.buildRequest(databases, fieldNames, queryText, fieldText);
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }
}
