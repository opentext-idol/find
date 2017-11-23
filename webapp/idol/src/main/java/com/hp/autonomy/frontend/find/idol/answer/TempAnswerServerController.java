/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.impl.DocumentProcessor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.MAX_RESULTS_PARAM;
import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.TEXT_PARAM;

// This is a temporary controller, long term we'd use AnswerServerController, but the demo network has an older build
//   of answer server at the moment so we need custom parsing.
@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class TempAnswerServerController {

    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final AciService aciService;


    private final XPathExpression xAnswer;
    private final XPathExpression xAnswerText;
    private final XPathExpression xScore;
    private final XPathExpression xSource;
    private final XPathExpression xSystemName;

    @Autowired
    TempAnswerServerController(final AskAnswerServerService askAnswerServerService,
                               final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory, final AciService aciService) {
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.aciService = aciService;


        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        try {
            xAnswer = xPath.compile("/autnresponse/responsedata/answers/answer");
            xAnswerText = xPath.compile("text");
            xScore = xPath.compile("score");
            xSource = xPath.compile("source");
            xSystemName = xPath.compile("@system_name");
        }
        catch(XPathExpressionException e) {
            throw new Error("Invalid XPaths", e);
        }

    }

    @RequestMapping(value = "ask-demo", method = RequestMethod.GET)
    public List<AskAnswer> ask(
            @RequestParam(TEXT_PARAM) final String text,
           @Value("${temp.answerserver.host}") final String host,
           @Value("${temp.answerserver.port}") final int port,
            @RequestParam(value = MAX_RESULTS_PARAM, required = false)
            final Integer maxResults
    ) throws XPathExpressionException {
        final ArrayList<AskAnswer> toReturn = new ArrayList<AskAnswer>();

        final AciServerDetails details = new AciServerDetails();
        details.setHost(host);
        details.setPort(port);

        final AciParameters params = new AciParameters("ask");
        params.add("sort", "confidence");
        params.add("text", text);

        final Document parse = aciService.executeAction(details, params, new DocumentProcessor());

        final NodeList nodes = (NodeList) xAnswer.evaluate(parse, XPathConstants.NODESET);

        for (int ii = 0; ii < nodes.getLength(); ++ii) {
            final Node answer = nodes.item(ii);

            if (maxResults != null && toReturn.size() >= maxResults) {
                break;
            }

            final AskAnswer toAdd = new AskAnswer();
            toAdd.setText((String) xAnswerText.evaluate(answer, XPathConstants.STRING));
            toAdd.setSource((String) xSource.evaluate(answer, XPathConstants.STRING));
            toAdd.setScore(NumberUtils.toDouble((String) xScore.evaluate(answer, XPathConstants.STRING)));
            toAdd.setSystemName((String) xSystemName.evaluate(answer, XPathConstants.STRING));
            toReturn.add(toAdd);
        }

        return toReturn;
    }
}
