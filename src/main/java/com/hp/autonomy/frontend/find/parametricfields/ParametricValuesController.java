package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.frontend.find.FindQueryProfileService;
import com.hp.autonomy.frontend.find.beanconfiguration.HodCondition;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.parametricvalues.ParametricFieldName;
import com.hp.autonomy.parametricvalues.ParametricRequest;
import com.hp.autonomy.parametricvalues.ParametricValuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

@Controller
@RequestMapping("/api/public/parametric")
@Conditional(HodCondition.class)
public class ParametricValuesController {

    @Autowired
    private ParametricValuesService parametricValuesService;

    @Autowired
    private FindQueryProfileService findQueryProfileService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<ParametricFieldName> getParametricValues(
            @RequestParam("databases") final Set<ResourceIdentifier> databases,
            @RequestParam(value = "fieldNames", required = false) final Set<String> fieldNames,
            @RequestParam("queryText") final String queryText,
            @RequestParam("fieldText") final String fieldText
    ) throws HodErrorException {
        final ParametricRequest parametricRequest = new ParametricRequest.Builder()
                .setQueryProfile(findQueryProfileService.getQueryProfile())
                .setDatabases(databases)
                .setFieldNames(fieldNames)
                .setQuery(queryText)
                .setFieldText(fieldText)
                .build();

        return parametricValuesService.getAllParametricValues(parametricRequest);
    }
}
