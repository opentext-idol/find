/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.conversation;

import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreAddDataCommand;
import com.autonomy.nonaci.indexing.impl.DreSyncCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.xml.sax.SAXException;

import static com.hp.autonomy.frontend.find.idol.conversation.ConversationController.CONVERSATION_PATH;
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
    private final XPathExpression xAnswerText;
    private final String questionAnswerDatabaseMatch;
    private final String systemNames;
    private final ConfigService<IdolFindConfig> configService;

    @Value("${conversation.server.url}")
    private String url;

    private final ConversationContexts contexts;
    private final DocumentFieldsService documentFieldsService;
    private final DocumentBuilder documentBuilder;
    private final XPathExpression xAnswer;
    private final XPathExpression xEntityName;
    private final XPathExpression xPropertyName;

    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @Autowired
    public ConversationController(
            final ConversationContexts contexts,
            final DocumentFieldsService documentFieldsService,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ConfigService<IdolFindConfig> configService,
            @Value("${conversation.server.allowSelfSigned}") final boolean allowSelfSigned,
            @Value("${questionanswer.databaseMatch}") final String questionAnswerDatabaseMatch,
            @Value("${questionanswer.conversation.system.names}") final String systemNames
    ) {
        this.contexts = contexts;
        this.documentFieldsService = documentFieldsService;
        this.configService = configService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.questionAnswerDatabaseMatch = questionAnswerDatabaseMatch;
        this.systemNames = systemNames;

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
            xEntityName = xPath.compile("metadata/fact/@entity_name");
            xPropertyName = xPath.compile("metadata/fact/property/@name");
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
            contexts.put(newContextId, new ArrayList<>(Collections.singletonList(new Utterance(false, greeting))));

            return new Response(greeting, newContextId);
        }

        final List<Utterance> history = contexts.get(contextId);
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
            // Only using the first answer for now.
            final Node answer = (Node) xAnswer.evaluate(parse, XPathConstants.NODE);

            if (answer != null) {
                final String answerText = (String) xAnswerText.evaluate(answer, XPathConstants.STRING);
                final String entityName = (String) xEntityName.evaluate(answer , XPathConstants.STRING);
                final String propertyName = (String) xPropertyName.evaluate(answer , XPathConstants.STRING);

                if (isNotBlank(entityName) && isNotBlank(propertyName)) {
                    return respond(history, "The " + propertyName + " of " + entityName + " is " + answerText + ".", contextId);
                }

                return respond(history, answerText, contextId);
            }
        }
        catch(SAXException|XPathExpressionException e) {
            log.warn("Exception while parsing question answer response", e);
        }

        return null;
    }

    @RequestMapping(value = "history", method = RequestMethod.GET)
    @ResponseBody
    public List<Utterance> history(
            @RequestParam("contextId") final String contextId,
            Principal activeUser
    ) {
        final List<Utterance> utterances = contexts.get(contextId);
        if (utterances == null) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser.getName(), contextId);
            throw new IllegalArgumentException("Invalid context supplied");
        }

        return utterances;
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
        final List<Utterance> utterances = contexts.get(contextId);
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
