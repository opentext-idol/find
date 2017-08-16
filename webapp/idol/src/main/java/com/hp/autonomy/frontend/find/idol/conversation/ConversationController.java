/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.conversation;

import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.aci.client.util.ActionParameters;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreAddDataCommand;
import com.autonomy.nonaci.indexing.impl.DreSyncCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.idol.answer.AnswerFilter;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.conversation.ConversationContexts.ConversationContext;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.CategoryHit;
import com.hp.autonomy.types.idol.responses.SuggestOnTextWithPathResponseData;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static com.hp.autonomy.frontend.find.idol.conversation.ConversationController.CONVERSATION_PATH;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Controller
@RequestMapping(CONVERSATION_PATH)
class ConversationController {
    static final String CONVERSATION_PATH = "/api/public/conversation";

    private static final String USER_AGENT = "Find";
    // Special response from conversation server if a session doesn't exist.
    private static final String NO_SUCH_INSTANCE = "Error: no such instance";
    private static final String errorResponse = "Sorry, there's a problem with the conversation server at the moment, please try again later.";

    private final CloseableHttpClient httpClient;
    private final String questionAnswerDatabaseMatch;
    private final String systemNames;
    private final ConfigService<IdolFindConfig> configService;
    private final Processor<SuggestOnTextWithPathResponseData> suggestProcessor;
    private final int maxDisambiguationQualifierValues;
    private final AnswerFilter answerFilter;
    private final boolean filterByDocumentSecurity;

    @Value("${conversation.server.url}")
    private String url;

    private final ConversationContexts contexts;
    private final DocumentFieldsService documentFieldsService;
    private final DocumentBuilder documentBuilder;
    private final XPathExpression xAnswer;
    private final XPathExpression xAnswerText;
    private final XPathExpression xSource;
    private final XPathExpression xSystemName;
    private final XPathExpression xEntityName;
    private final XPathExpression xPropertyName;
    private final XPathExpression xQualifierName;
    private final XPathExpression xQualifierValue;

    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    private static final List<Expert> experts = Arrays.asList(
        new Expert("Eric Champod", "eric.champod@credit-suisse.com", "Payments"),
        new Expert("Martin Keller", "martin.keller@credit-suisse.com", "Precious Metals"),
        new Expert("Vikash Kumar", "vikash.kumar@credit-suisse.com", "Securities"),
        new Expert("Choki Lirgyatsang", "choki.lirgyatsang.2@credit-suisse.com", "Payments"),
        new Expert("René Nussbaum", "rene.nussbaum@credit-suisse.com", "Payments"),
        new Expert("Anton Schnider", "anton.schnider@credit-suisse.com", "Payments"),
        new Expert("Jeannette Zimmermann", "jeannette.zimmermann@credit-suisse.com", "Payments")
    );

    @Autowired
    public ConversationController(
            final ConversationContexts contexts,
            final DocumentFieldsService documentFieldsService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ConfigService<IdolFindConfig> configService,
            final ProcessorFactory processorFactory,
            final AnswerFilter answerFilter,
            @Value("${conversation.server.allowSelfSigned}") final boolean allowSelfSigned,
            @Value("${questionanswer.databaseMatch}") final String questionAnswerDatabaseMatch,
            @Value("${questionanswer.conversation.system.names}") final String systemNames,
            @Value("${questionanswer.disambiguation.maxQualifierValues}") final int maxDisambiguationQualifierValues,
            @Value("${questionanswer.documentSecurity.filter}") final boolean filterByDocumentSecurity
    ) {
        this.contexts = contexts;
        this.documentFieldsService = documentFieldsService;
        this.configService = configService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.questionAnswerDatabaseMatch = questionAnswerDatabaseMatch;
        this.systemNames = systemNames;
        this.suggestProcessor = processorFactory.getResponseDataProcessor(SuggestOnTextWithPathResponseData.class);
        this.maxDisambiguationQualifierValues = maxDisambiguationQualifierValues;
        this.answerFilter = answerFilter;
        this.filterByDocumentSecurity = filterByDocumentSecurity;

        try {
            final SSLConnectionSocketFactory sslSocketFactory = allowSelfSigned
                    ? new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build(), NoopHostnameVerifier.INSTANCE)
                    : SSLConnectionSocketFactory.getSocketFactory();

            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory).build();

            httpClient = HttpClientBuilder.create()
                    .setConnectionManager(new PoolingHttpClientConnectionManager(registry))
                    .disableRedirectHandling()
                    .build();
        }
        catch(NoSuchAlgorithmException|KeyManagementException|KeyStoreException e) {
            throw new Error("Unable to initialize conversation controller", e);
        }

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            documentBuilder = factory.newDocumentBuilder();
            final XPathFactory xPathFactory = XPathFactory.newInstance();
            final XPath xPath = xPathFactory.newXPath();
            xAnswer = xPath.compile("/autnresponse/responsedata/answers/answer");
            xAnswerText = xPath.compile("text");
            xSource = xPath.compile("source");
            xSystemName = xPath.compile("@system_name");
            xEntityName = xPath.compile("metadata/fact/@entity_name");
            xPropertyName = xPath.compile("metadata/fact/property/@name");
            xQualifierName = xPath.compile("metadata/fact/property/qualifier/@name");
            xQualifierValue = xPath.compile("metadata/fact/property/qualifier/@value");
        }
        catch(ParserConfigurationException|XPathExpressionException e) {
            throw new Error("Unable to initialize conversation controller XML parser", e);
        }
    }

    @RequestMapping(value = "chat", method = RequestMethod.POST)
    @ResponseBody
    public Response converse(
            @RequestParam(value = "query", defaultValue = "") final String query,
            @RequestParam(value = "contextId", required = false) final String contextId,
            Principal activeUser
    ) throws IOException, AciHttpException {
        final boolean illegalContextId = contextId != null && !contexts.containsKey(contextId);
        if (illegalContextId) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser.getName(), contextId);
        }

        if (contextId == null || illegalContextId) {
            final HttpPost post = new HttpPost(this.url + "nadia/engine/dialog");
            post.setHeader("User-Agent", USER_AGENT);
            final HttpResponse resp = httpClient.execute(post);
            final String greeting = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");

            final int code = resp.getStatusLine().getStatusCode();
            if(code == 429) {
                // license limit for concurrent sessions has expired
                log.warn("Too many concurrent sessions for the conversation server license; new conversation blocked");
                return new Response("There are too many concurrent sessions; please try again later.");
            }
            else if (code != 201) {
                return new Response(errorResponse);
            }

            final String newContextId = resp.getFirstHeader("Location").getValue().replaceFirst(".*/", "");
            final ConversationContext context = new ConversationContext();
            context.getHistory().add(new Utterance(false, greeting));
            contexts.put(newContextId, context);

            return new Response(greeting, newContextId);
        }

        final ConversationContext context = contexts.get(contextId);
        final List<Utterance> history = context.getHistory();
        history.add(new Utterance(true, query));

        final Response qaResponse = askQAServer(history, contextId, query);
        if (qaResponse != null) {
            return qaResponse;
        }

        final HttpPost post = new HttpPost(this.url + "nadia/engine/dialog/" + contextId);
        post.setHeader("User-Agent", USER_AGENT);
        post.setEntity(new UrlEncodedFormEntity(Collections.singletonList(new BasicNameValuePair("userUtterance", query)), "UTF-8"));
        final HttpResponse resp = httpClient.execute(post);
        final String answer = IOUtils.toString(resp.getEntity().getContent(), "utf-8");

        if (resp.getStatusLine().getStatusCode() != 200) {
            if(NO_SUCH_INSTANCE.equals(answer)) {
                // the session has expired or the server was restarted; clear the context
                contexts.remove(contextId);
                return respond(history, errorResponse, null);
            }
            return respond(history, errorResponse, contextId);
        }

        return respond(history, answer, contextId);
    }


    private Response askQAServer(final List<Utterance> history, final String contextId, final String query) throws IOException {
        final AnswerServerConfig answerServer = configService.getConfig().getAnswerServer();
        if (!answerServer.getEnabled()) {
            return null;
        }

        final ServerConfig sc = answerServer.getServer();
        final String qaURL = sc.getProtocol() + "://" + sc.getHost() + ":" + sc.getPort() + "/";

        final HttpPost post = new HttpPost(qaURL + "a=ask");

        final ArrayList<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("text", query));

        if(isNotBlank(systemNames)) {
            params.add(new BasicNameValuePair("systemNames", systemNames));
        }

        final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
        final String securityInfo = principal.getSecurityInfo();

        if (isNotBlank(securityInfo)) {
            params.add(new BasicNameValuePair("securityInfo", securityInfo));
        }

        if (isNotBlank(questionAnswerDatabaseMatch)) {
            params.add(new BasicNameValuePair("databaseMatch", questionAnswerDatabaseMatch));
        }

        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        final HttpResponse resp = httpClient.execute(post);

        if (resp.getStatusLine().getStatusCode() != 200) {
            log.warn("Answer server returned error code {}", resp.getStatusLine());
            return respond(history, errorResponse, contextId);
        }

        try {
            final Document parse = documentBuilder.parse(resp.getEntity().getContent());

            final List<Answer> unfiltered = new ArrayList<>();

            final NodeList nodes = (NodeList) xAnswer.evaluate(parse, XPathConstants.NODESET);

            for (int ii = 0; ii < nodes.getLength(); ++ii) {
                final Node answer = nodes.item(ii);
                final Answer current = new Answer(
                    (String) xSystemName.evaluate(answer, XPathConstants.STRING),
                    (String) xSource.evaluate(answer, XPathConstants.STRING),
                    (String) xAnswerText.evaluate(answer, XPathConstants.STRING),
                    (String) xEntityName.evaluate(answer, XPathConstants.STRING),
                    (String) xPropertyName.evaluate(answer, XPathConstants.STRING),
                    (String) xQualifierName.evaluate(answer, XPathConstants.STRING),
                    (String) xQualifierValue.evaluate(answer, XPathConstants.STRING)
                );

                unfiltered.add(current);
            }

            final List<String> refsToCheck = unfiltered.stream().map(Answer::getSource).filter(source ->
                    isNotBlank(source) && !source.equalsIgnoreCase("SQLDB")).collect(Collectors.toList());

            final ArrayList<Answer> answers = new ArrayList<>();

            if (!refsToCheck.isEmpty()) {
                final HashMap<String, String> urls = answerFilter.resolveUrls(refsToCheck);

                for(final Answer answer : unfiltered) {
                    final String source = answer.getSource();

                    if (!refsToCheck.contains(source)) {
                        answers.add(answer);
                    }
                    else {
                        // Empty string / actual URL is a URL, null means document not found and should be filtered out
                        final String url = urls.get(answer.getSource());

                        if (url != null || !filterByDocumentSecurity) {
                            answers.add(answer);
                        }

                        if (isNotBlank(url)) {
                            answer.setUrl(url);
                        }
                    }
                }
            }
            else {
                answers.addAll(unfiltered);
            }

            if (answers.size() > 1) {
                final Answer first = answers.get(0);
                for (int ii = answers.size() - 1; ii >= 1; --ii) {
                    if (!answers.get(ii).isSameEntityPropertyAndQualifier(first)) {
                        answers.remove(ii);
                    }
                }
            }

            if (!answers.isEmpty()) {
                final Answer answer = answers.get(0);
                final String answerText = answer.getAnswerText();
                final String entityName = answer.getEntityName();
                final String propertyName = answer.getPropertyName();
                final String qualifierName = answer.getQualifierName();
                final String qualifierValue = answer.getQualifierValue();
                final String url = answer.getUrl();
                final String answerLink = isBlank(url) ? answerText
                    : "<a href='"+ escapeHtml4(url)+"' target='_blank'>"+ escapeHtml4(answerText)+"</a>";

                if (isNotBlank(entityName) && isNotBlank(propertyName)) {
                    final StringBuilder response = new StringBuilder("The " + propertyName + " of " + entityName);

                    if (isNotBlank(qualifierName) && isNotBlank(qualifierValue)) {
                        response.append(" in ").append(qualifierValue);
                    }

                    response.append(" is ").append(answerLink).append(".");

                    if (answers.size() > 1) {
                        // There are multiple answers, we need to format it
                        response.append("\nWe also have data for");

                        for (int ii = 1, max = Math.min(1 + maxDisambiguationQualifierValues, answers.size()); ii < max; ++ii) {
                            final Answer suggest = answers.get(ii);
                            final String suggestedValue = suggest.getQualifierValue();
                            response.append(" <suggest query=\"")
                                .append(escapeHtml4("what is the " + propertyName + " of " + entityName + " in " + suggestedValue))
                                .append("\" label=\"")
                                .append(escapeHtml4(suggestedValue))
                                .append("\"/>");
                        }

                        if (answers.size() > maxDisambiguationQualifierValues + 1) {
                            response.append("…");
                        }

                        response.append(".");
                    }

                    return respond(history, response.toString(), contextId);
                }

                return respond(history, answerLink, contextId);
            }
        }
        catch(SAXException|XPathExpressionException e) {
            log.warn("Exception while parsing question answer response", e);
        }

        return null;
    }

    @Data
    public static class Answer {
        private final String systemName, source, answerText, entityName, propertyName, qualifierName, qualifierValue;
        private String url;

        public boolean isSameEntityPropertyAndQualifier(final Answer other){
            return StringUtils.equals(systemName, other.getSystemName())
                && StringUtils.equals(entityName, other.getEntityName())
                && StringUtils.equals(propertyName, other.getPropertyName())
                && StringUtils.equals(qualifierName, other.getQualifierName());

        }
    }

    @RequestMapping(value = "history", method = RequestMethod.GET)
    @ResponseBody
    public List<Utterance> history(
            @RequestParam("contextId") final String contextId,
            Principal activeUser
    ) {
        final List<Utterance> utterances = contexts.get(contextId).getHistory();
        if (utterances == null) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser.getName(), contextId);
            throw new IllegalArgumentException("Invalid context supplied");
        }

        return utterances;
    }

    @RequestMapping(value = "help", method = RequestMethod.POST)
    @ResponseBody
    public List<Expert> help(
            @RequestParam("contextId") final String contextId,
            @RequestParam(value = "topic", required = false) final String topic,
            Principal activeUser,
            @Value("${category.server.host}") final String categoryHost,
            @Value("${category.server.port}") final int categoryPort,
            @Value("${conversation.help.context.count}") final int helpContext
    ) {
        if (isNotBlank(topic)) {
            final List<Expert> relevant = experts.stream().filter(expert -> expert.getArea().equalsIgnoreCase(topic)).collect(Collectors.toList());

            if (!relevant.isEmpty()) {
                return relevant;
            }
        }

        final List<Utterance> utterances = contexts.get(contextId).getHistory();
        if (utterances == null) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser.getName(), contextId);
            throw new IllegalArgumentException("Invalid context supplied");
        }

        final StringBuilder queryText = new StringBuilder();

        // Classify based on the last 3 things they said
        for (int ii = utterances.size() - 1, count = 0; ii >= 0; --ii) {
            final Utterance utterance = utterances.get(ii);
            if (utterance.isUser()) {
                queryText.append(utterance.getText()).append("\n");
                count++;

                if (count >= helpContext) {
                    break;
                }
            }
        }


        // Contact category server.
        final AciHttpClientImpl client = new AciHttpClientImpl(new HttpClientFactory().createInstance());
        final AciServiceImpl service = new AciServiceImpl(client, new AciServerDetails(categoryHost, categoryPort));

        final ActionParameters params = new ActionParameters("categorysuggestfromtext");
        params.add("querytext", queryText);
        params.add("showcategorypath", true);

        final SuggestOnTextWithPathResponseData suggested = service.executeAction(params, suggestProcessor);

        final List<Expert> toReturn = new ArrayList<>();

        for(CategoryHit hit : suggested.getHits()) {
            // The server response looks like this:
            //  <autn:title>FX Specific</autn:title>
            //  <autn:path>Payments</autn:path>
            //  <autn:path>Payments/Payment Methods</autn:path>
            //  <autn:path>Payments/Payment Methods/FX Specific</autn:path>
            // so we can ignore the title, since it's included in the path.
            // We want to search going up the tree, from leaf ('FX Specific') to root ('Payments').
            final ArrayList<String> toSearch = new ArrayList<>();
            String previousPath = null;
            for(final String path : hit.getPath()) {
                if (previousPath == null) {
                    toSearch.add(path);
                }
                else {
                    toSearch.add(path.substring(previousPath.length() + 1));
                }
                previousPath = path;
            }
            Collections.reverse(toSearch);

            for(String area : toSearch) {
                for(Expert expert : experts) {
                    if (expert.getArea().equalsIgnoreCase(area)) {
                        toReturn.add(expert);
                    }
                }

                if (!toReturn.isEmpty()) {
                    return toReturn;
                }
            }
        }

        if (toReturn.isEmpty()) {
            toReturn.addAll(experts);
        }

        return toReturn;
    }

    @Data
    public static class Expert {
        private final String name;
        private final String email;
        private final String area;
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, ?> save(
            @RequestParam("contextId") final String contextId,
            @RequestParam(value = "rating", defaultValue = "-1") final int rating,
            Principal activeUser,
            @Value("${content.index.host}") final String indexHost,
            @Value("${conversation.index.database}") final String database,
            @Value("${conversation.index.database.create}") final boolean createDatabase,
            @Value("${conversation.index.security.field}") final String securityField,
            @Value("${conversation.index.security.type}") final String securityType,
            @Value("${conversation.index.security.aclField}") final String aclField,
            @Value("${conversation.index.security.userPrefix}") final String userPrefix,
            @Value("${conversation.index.security.adminGroup}") final String adminGroup,
            @Value("${conversation.rating.field}") final String ratingField,
            @Value("${conversation.user.contentField}") final String userField,
            @Value("${content.index.port}") final int indexPort,
            @Value("${conversation.index.DRESYNC}") final boolean DRESYNC
    ) {
        final List<Utterance> utterances = contexts.get(contextId).getHistory();
        if (utterances == null) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser, contextId);
            throw new IllegalArgumentException("Invalid context supplied");
        }

        final String ref = contextId + "_" + activeUser.getName();

        final StringBuilder idx = new StringBuilder("#DREREFERENCE " + ref + '\n');

        idx.append("#DRETITLE ").append("Conversation with ").append(activeUser.getName()).append('\n');

        if (isNotBlank(userField)) {
            idx.append("#DREFIELD ").append(userField).append("=\"").append(activeUser.getName()).append("\"\n");
        }

        if (rating >= 0) {
            // Check that the field is editable
            final Set<String> idolFields = documentFieldsService.getEditableIdolFields(ratingField);

            if (idolFields != null) {
                for(final String field : idolFields) {
                    idx.append("#DREFIELD ").append(field).append("=\"").append(rating).append("\"\n");
                }
            }
        }

        if(isNotBlank(securityField) && isNotBlank(securityType) && isNotBlank(aclField)) {
            idx.append("#DREFIELD ").append(securityField).append("=\"").append(securityType).append("\"\n");
            idx.append("#DREFIELD ").append(aclField).append("=\"")
                    .append("0:") // disable world read
                    .append("U:").append(obfuscate(defaultString(userPrefix) + activeUser.getName())).append(":")
                    .append("NU::")
                    .append("G:").append(obfuscate(defaultString(adminGroup))).append(":")
                    .append("NG:")
                    .append("\"\n");
        }

        idx.append("#DRECONTENT\n");

        for(final Utterance utterance : utterances) {
            idx.append(utterance.isUser() ? "U" : "S").append(": ").append(utterance.getText().replaceAll("#DREENDDOC", "DREENDDOC")).append("\n");
        }

        idx.append("\n#DREENDDOC\n");
        idx.append("#DREENDDATANOOP\n\n");

        final DreAddDataCommand command = new DreAddDataCommand();
        command.setDreDbName(database);
        command.setKillDuplicates("reference");
        command.put("CreateDatabase", Boolean.toString(createDatabase));
        command.setPostData(idx.toString());
        final HttpClient client = new HttpClientFactory().createInstance();
        final IndexingServiceImpl indexingService = new IndexingServiceImpl(new ServerDetails(indexHost, indexPort), client);
        final int indexId = indexingService.executeCommand(command);

        if (DRESYNC) {
            indexingService.executeCommand(new DreSyncCommand());
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put("reference", ref);
        map.put("indexId", indexId);
        return map;
    }

    private static String obfuscate(final String str) {
        if(isBlank(str)) {
            return str;
        }
        try {
            final byte[] bytes = ("[" + str + "]").getBytes("UTF-8");

            for (int ii = 0; ii < bytes.length; ++ii) {
                bytes[ii] ^= 173;
            }

            return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(bytes)).replaceFirst("=*$", "");
        } catch (final UnsupportedEncodingException uee) {
            throw new Error("should never happen", uee);
        }
    }

    private Response respond(final List<Utterance> history, final String message, final String contextId) {
        history.add(new Utterance(false, message));
        return new Response(message, contextId);
    }

    @Data
    public static class Response {
        private final String contextId;
        private final String response;

        public Response(final String response, final String contextId) {
            this.contextId = contextId;
            this.response = response;
        }

        public Response(final String response) {
            this(response, null);
        }
    }
}
