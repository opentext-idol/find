package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.parametricvalues.ParametricFieldName;
import com.hp.autonomy.parametricvalues.ParametricRequest;
import com.hp.autonomy.parametricvalues.ParametricValuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

@Controller
@RequestMapping("/api/public/parametric")
public class ParametricValuesController {

    @Autowired
    private ParametricValuesService parametricValuesService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<ParametricFieldName> getParametricValues(
            @RequestParam("databases") final Set<ResourceIdentifier> databases,
            @RequestParam("fieldNames") final Set<String> fieldNames,
            @RequestParam("queryText") final String queryText,
            @RequestParam("fieldText") final String fieldText
    ) throws HodErrorException {
        final ParametricRequest parametricRequest = new ParametricRequest.Builder()
                .setDatabases(databases).setFieldNames(fieldNames).setQuery(queryText).setFieldText(fieldText).build();

        return parametricValuesService.getAllParametricValues(parametricRequest);
    }
}
