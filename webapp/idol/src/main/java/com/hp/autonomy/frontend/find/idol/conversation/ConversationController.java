/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.conversation;

import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreAddDataCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import java.io.IOException;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

import static com.hp.autonomy.frontend.find.idol.conversation.ConversationController.CONVERSATION_PATH;

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

    @Value("${conversation.server.url}")
    private String url;

    private final ConversationContexts contexts;
    private final DocumentFieldsService documentFieldsService;

    @Autowired
    public ConversationController(
            final ConversationContexts contexts,
            final DocumentFieldsService documentFieldsService,
            @Value("${conversation.server.allowSelfSigned}") final boolean allowSelfSigned
    ) {
        this.contexts = contexts;
        this.documentFieldsService = documentFieldsService;

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
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser, contextId);
        }

        if (contextId == null || illegalContextId) {
            final HttpPost post = new HttpPost(this.url + "nadia/engine/dialog");
            post.setHeader("User-Agent", USER_AGENT);
            final HttpResponse resp = httpClient.execute(post);
            final String greeting = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");

            final int code = resp.getStatusLine().getStatusCode();
            if(code == 429) {
                // license limit for concurrent sessions has expired
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

    @RequestMapping(value = "history", method = RequestMethod.GET)
    @ResponseBody
    public List<Utterance> history(
            @RequestParam("contextId") final String contextId,
            Principal activeUser
    ) {
        final List<Utterance> utterances = contexts.get(contextId);
        if (utterances == null) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", activeUser, contextId);
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
            @Value("${conversation.rating.field}") final String ratingField,
            @Value("${content.index.port}") final int indexPort
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

        if (rating >= 0) {
            // Check that the field is editable
            final Set<String> idolFields = documentFieldsService.getEditableIdolFields(ratingField);

            if (idolFields != null) {
                for(final String field : idolFields) {
                    idx.append("#DREFIELD ").append(field).append("=\"").append(rating).append("\"\n");
                }
            }
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
        command.put("CreateDatabase", "true");
        command.setPostData(idx.toString());
        final HttpClient client = new HttpClientFactory().createInstance();
        final int indexId = new IndexingServiceImpl(new ServerDetails(indexHost, indexPort), client).executeCommand(command);

        final HashMap<String, Object> map = new HashMap<>();
        map.put("reference", ref);
        map.put("indexId", indexId);
        return map;
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
