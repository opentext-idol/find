package com.hp.autonomy.frontend.find.idol.answer;

import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.types.idol.responses.answer.Answer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class AnswerServerController {
    static final String BASE_PATH = "/api/public/answer";
    static final String ASK_PATH = "ask";
    static final String TEXT_PARAM = "text";
    static final String MAX_RESULTS_PARAM = "maxResults";

    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;

    @Autowired
    AnswerServerController(final AskAnswerServerService askAnswerServerService,
                                  final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory) {
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<Answer> ask(@RequestParam(TEXT_PARAM)
                            final String text,
                            @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                            final Integer maxResults) {
        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .build();

        return askAnswerServerService.ask(request);
    }
}
