/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.ast.DecisionNode;
import org.kie.dmn.api.core.event.*;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CustomDMNListener implements DMNRuntimeEventListener {


    private String elasticSearchUrl = System.getProperty("org.jbpm.event.emitters.elasticsearch.url", "http://localhost:9200");
    private static String elasticSearchUser = System.getProperty("org.jbpm.event.emitters.elasticsearch.user","elastic");
    private static String elasticSearchPassword = System.getProperty("org.jbpm.event.emitters.elasticsearch.password","cvwgzp9vf8gkjhfzbz5srrjq");

    private ObjectMapper mapper = new ObjectMapper();
    private final String[] labels;
    private static ExecutorService executor;

    private static CloseableHttpClient httpclient;

    static {
        executor = buildExecutorService();
        try {
            httpclient = buildClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CustomDMNListener(String... labels) {
        this.labels = labels;
    }

    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent e) {
    }

    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent e) {

     StringBuilder content = new StringBuilder();
     DecisionNode decisionNode = e.getDecision();
     String decisionNodeName = decisionNode.getName();
     DMNDecisionResult result = e.getResult().getDecisionResultByName(decisionNodeName);
     AuditObject auditObject = new AuditObject();
     auditObject.setRuleName(decisionNodeName);
     auditObject.setRuleFiredAt(new Date());
     auditObject.setStatus(result.getEvaluationStatus().name());
     Map<String, Object> mapInputs = e.getResult().getContext().getAll();
     Map<String, Object> auditMap = new HashMap<>();
     for (String str : mapInputs.keySet()) {
        if (null != mapInputs.get(str) && !"org.kie.dmn.core.ast.DMNFunctionDefinitionEvaluator$DMNFunction".equals(mapInputs.get(str).getClass().getName())) {
                System.out.println(mapInputs.get(str).getClass());
                auditMap.put(str, String.valueOf(mapInputs.get(str)));
         }
     }
     auditObject.setInputs(auditMap);
     auditObject.setResult(String.valueOf(result.getResult()) );
     System.out.println(auditMap);

     String json = new Gson().toJson(auditObject);
     System.out.println(json);



     System.out.println(new StringEntity(json, "UTF-8"));

     HttpPost httpPut = new HttpPost(elasticSearchUrl + "/cust/cust");
     httpPut.setEntity(new StringEntity(json, "UTF-8"));

     System.out.println("Executing request " + httpPut.getRequestLine());
     httpPut.setHeader("Content-Type", "application/json");
     httpPut.setHeader("Accept", "application/json");


     ResponseHandler<String> responseHandler = response -> {
     int status = response.getStatusLine().getStatusCode();
     if (status >= 200 && status < 300) {
        HttpEntity entity = response.getEntity();
        return entity != null ? EntityUtils.toString(entity) : null;
     } else {
        throw new ClientProtocolException("Unexpected response status: " + status);
     }
     };
     String responseBody = null;
     try {
        responseBody = httpclient.execute(httpPut, responseHandler);
     } catch (IOException e1) {
     e1.printStackTrace();
     }

     System.out.println("Elastic search response '{}'" + responseBody);


    }


    protected static ExecutorService buildExecutorService() {

        return Executors.newCachedThreadPool();
    }


    protected static CloseableHttpClient buildClient() throws Exception{

        HttpClientBuilder builder = HttpClients.custom();

        if (elasticSearchUser != null && elasticSearchPassword != null) {
            SSLContextBuilder builder1 = new SSLContextBuilder();
            builder1.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(elasticSearchUser, elasticSearchPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder1.build(),  new NoopHostnameVerifier());
            builder.setDefaultCredentialsProvider(provider);
            builder.setSSLSocketFactory(sslConnectionSocketFactory).build();
        }

        return builder.build();
    }

    @Override
    public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {
    }

    @Override
    public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {
    }

    @Override
    public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {
    }

    @Override
    public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {
    }

    @Override
    public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {
    }

    @Override
    public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
    }

    @Override
    public void beforeEvaluateDecisionService(BeforeEvaluateDecisionServiceEvent event) {
    }

    @Override
    public void afterEvaluateDecisionService(AfterEvaluateDecisionServiceEvent event) {
    }
}
