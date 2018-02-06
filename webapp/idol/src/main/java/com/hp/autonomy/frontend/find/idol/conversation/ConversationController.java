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
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.idol.conversation.ConversationContexts.ConversationContext;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
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
import java.util.regex.Pattern;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
    private static final ContentType UTF8_TEXT = ContentType.create("text/plain", "UTF-8");

    private final String conversationModuleName;

    static final String CONVERSATION_PATH = "/api/public/conversation";

    private static final String errorResponse = "Sorry, there's a problem with the conversation server at the moment, please try again later.";

    private final CloseableHttpClient httpClient;
    private final Processor<SuggestOnTextWithPathResponseData> suggestProcessor;

    @Value("${conversation.server.url}")
    private String url;

    private final ConversationContexts contexts;
    private final DocumentFieldsService documentFieldsService;
    private final DocumentBuilder documentBuilder;

    private final XPathExpression xPrompts;
    private final XPathExpression xPrompt;
    private final XPathExpression xChoices;
    private final XPathExpression xSuggestions;
    private final XPathExpression xSessionId;

    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    private static final List<Expert> experts = Arrays.asList(
        new Expert("John Smith", "john.smith@example.com", "Interbank Conventions"),
        new Expert("Jane Doe", "jane.doe@example.com", "Online Payment"),
        new Expert("Richard Roe", "richard.roe@example.com", "Payment processes"),
        new Expert("Signor Rossi", "signor.rossi@example.com", "Securities"),
        new Expert("Ashok Kumar", "ashok.kumar@example.com", "International Payments")
    );

    @Autowired
    public ConversationController(
            final ConversationContexts contexts,
            final DocumentFieldsService documentFieldsService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ProcessorFactory processorFactory,
            @Value("${conversation.server.allowSelfSigned}") final boolean allowSelfSigned,
            @Value("${questionanswer.conversation.module.name}") final String conversationModuleName
            ) {
        this.contexts = contexts;
        this.documentFieldsService = documentFieldsService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.suggestProcessor = processorFactory.getResponseDataProcessor(SuggestOnTextWithPathResponseData.class);
        this.conversationModuleName = conversationModuleName;

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

            xPrompts = xPath.compile("/autnresponse/responsedata/prompts");
            xPrompt = xPath.compile("prompt");
            xChoices = xPath.compile("valid_choices/valid_choice");
            xSuggestions = xPath.compile("suggestions/suggestion");
            xSessionId = xPath.compile("/autnresponse/responsedata/result/managed_resources/id");
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
            final String newContextId;

            try {
                {
                    final HttpPost post = new HttpPost(this.url);

                    post.setEntity(MultipartEntityBuilder.create()
                            .addTextBody("action", "manageresources", UTF8_TEXT)
                            .addTextBody("systemname", conversationModuleName, UTF8_TEXT)
                            .addTextBody("data", "{\"operation\":\"add\",\"type\":\"conversation_session\"}", ContentType.APPLICATION_JSON)
                            .build());

                    final HttpResponse resp = httpClient.execute(post);

                    if(resp.getStatusLine().getStatusCode() != 200) {
                        return new Response(errorResponse);
                    }

                    final Document doc = documentBuilder.parse(resp.getEntity().getContent());
                    newContextId = (String) xSessionId.evaluate(doc, XPathConstants.STRING);
                }

                {
                    // curl "localhost:12000/a=converse&systemName=Conversation&sessionId=123456789123456789”
                    final HttpPost post = new HttpPost(this.url);
                    post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("action", "converse"),
                        new BasicNameValuePair("systemname", conversationModuleName),
                        new BasicNameValuePair("sessionId", newContextId),
                        new BasicNameValuePair("text", query)
                    ), "UTF-8"));

                    final HttpResponse resp = httpClient.execute(post);

                    if (resp.getStatusLine().getStatusCode() != 200) {
                        return new Response(errorResponse);
                    }

                    final Document doc = documentBuilder.parse(resp.getEntity().getContent());
                    final String greeting = parseResponse(doc);

                    final ConversationContext context = new ConversationContext();
                    contexts.put(newContextId, context);

                    return respond(context, replaceUsername(greeting), newContextId);
                }
            }
            catch(SAXException | IOException | XPathExpressionException e) {
                return new Response(errorResponse);
            }
        }

        final ConversationContext context = contexts.get(contextId);
        final List<Utterance> history = context.getHistory();
        history.add(new Utterance(true, query));

        try {
            {
                // curl "localhost:12000/a=converse&systemName=Conversation&sessionId=123456789123456789”
                final HttpPost post = new HttpPost(this.url);
                post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                        new BasicNameValuePair("action", "converse"),
                        new BasicNameValuePair("systemname", conversationModuleName),
                        new BasicNameValuePair("sessionId", contextId),
                        new BasicNameValuePair("text", query)
                ), "UTF-8"));

                final HttpResponse resp = httpClient.execute(post);

                if (resp.getStatusLine().getStatusCode() != 200) {
                    return new Response(errorResponse);
                }

                final Document doc = documentBuilder.parse(resp.getEntity().getContent());
                final String response = parseResponse(doc);

                return respond(context, replaceUsername(response), contextId);
            }
        }
        catch(SAXException|XPathExpressionException e) {
            return new Response(errorResponse);
        }
    }

    protected String parseResponse(final Document doc) throws XPathExpressionException {
        final NodeList prompts = (NodeList) xPrompts.evaluate(doc, XPathConstants.NODESET);

        final StringBuilder builder = new StringBuilder();
        for (int ii = 0; ii < prompts.getLength(); ++ii) {
            if (ii > 0) {
                builder.append("\n");
            }
            final Node prompt = prompts.item(ii);

            builder.append(xPrompt.evaluate(prompt, XPathConstants.STRING));

            List<String> choices = getOptions(prompt, xChoices);
            List<String> suggestions = getOptions(prompt, xSuggestions);

            if(!choices.isEmpty()) {
                builder.append("\nChoose from: ");
                for(String choice : choices) {
                    builder.append("\n").append("<suggest query=\"")
                            .append(escapeHtml4(choice))
                            .append("\" label=\"")
                            .append(escapeHtml4(choice))
                            .append("\"/>");
                }
            }

            if(!suggestions.isEmpty()) {
                builder.append("\nSuggestions: ");
                for(String suggestion : suggestions) {
                    builder.append("\n").append("<suggest query=\"")
                            .append(escapeHtml4(suggestion))
                            .append("\" label=\"")
                            .append(escapeHtml4(suggestion))
                            .append("\"/>");
                }
            }
        }

        return builder.toString();
    }

    protected List<String> getOptions(final Node prompt, final XPathExpression xpath) throws XPathExpressionException {
        final NodeList validChoices = (NodeList) xpath.evaluate(prompt, XPathConstants.NODESET);
        final List<String> opts = new ArrayList<>();
        for (int jj = 0; jj < validChoices.getLength(); ++jj) {
            opts.add(validChoices.item(jj).getTextContent());
        }
        return opts;
    }

    private String replaceUsername(final String str){
        final String TOKEN = "<findUser>";
        final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
        final String userName = principal.getName();

        final Map<String, String> fields = principal.getFields();

        String firstName = null;

        if (fields != null) {
            firstName = fields.get("givenname");
        }
        if (isNotBlank(firstName)) {
            // If we have a first name, replace the username with the name
            return str.replace(TOKEN, firstName);
        }
        else {
            // If no first name, the username may be something silly like A568612, so we should just discard it.
            // May be more sensible to use the name generally; but we know it's safe to remove for this demo's dialogue.
            return str.replaceAll(" ?" + Pattern.quote(TOKEN), "");
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

    private Response respond(final ConversationContext context, final String message, final String contextId) {
        return respond(context == null ? null : context.getHistory(), message, contextId);
    }

    private Response respond(final List<Utterance> history, final String message, final String contextId) {
        if (history != null) {
            history.add(new Utterance(false, message));
        }
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
