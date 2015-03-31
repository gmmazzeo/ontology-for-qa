/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TagMeClient {

    private static final String END_POINT = "http://tagme.di.unipi.it/tag";
    private static final String API_KEY = "mazzeo2015";

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static class AnnotationResults {

        @Key
        public ArrayList<AnnotationResult> annotations;
    }

    public static class AnnotationResult {

        @Key
        public String title;

        @Key
        public double rho;

        @Key
        public int start;

        @Key
        public int end;

        @Key
        public String spot;
    }

    public static class TagMeUrl extends GenericUrl {

        public TagMeUrl(String encodedUrl) {
            super(encodedUrl);
        }

    }

    public ArrayList<DBpediaEntityAnnotationResult> getTagMeResult(String sentence) throws Exception {
        HttpRequestFactory requestFactory
                = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        TagMeClient.TagMeUrl url = new TagMeClient.TagMeUrl(END_POINT + "?key=" + API_KEY + "&text=" + sentence);
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAccept("application/json");
        HttpResponse response = request.execute();

        TagMeClient.AnnotationResults result = response.parseAs(TagMeClient.AnnotationResults.class);
        ArrayList<DBpediaEntityAnnotationResult> res = new ArrayList<>();
        for (AnnotationResult ar : result.annotations) {
            DBpediaNamedEntity e = new DBpediaNamedEntity();
            e.setLabel(ar.title);
            e.setUri("http://dbpedia.org/resource/" + ar.title.replace(" ", "_"));
            res.add(new DBpediaEntityAnnotationResult(e, ar.rho, ar.start, ar.end, ar.spot));
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        TagMeClient tm = new TagMeClient();
        //String sentence="In which films directed by Garry Marshall was Julia Roberts starring?";
        String sentence="What is a film director?";
        for (DBpediaEntityAnnotationResult r : tm.getTagMeResult(sentence)) {
            System.out.println(r);
        }
    }

}
