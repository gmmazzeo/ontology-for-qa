/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
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
import java.io.IOException;
import java.util.ArrayList;

public class DBpediaEntityLookup {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static class EntityFeed {

        @Key
        public ArrayList<DBpediaNamedEntity> results;
    }

    public static class DBpediaUrl extends GenericUrl {

        public DBpediaUrl(String encodedUrl) {
            super(encodedUrl);
        }

    }

    public ArrayList<DBpediaEntityLookupResult> lookup(String name, int maxHits) throws IOException {
        HttpRequestFactory requestFactory
                = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        DBpediaUrl url = new DBpediaUrl("http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?MaxHits="+maxHits+"&QueryString="+name);
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAccept("application/json");
        HttpResponse response = request.execute();

        EntityFeed resultList = response.parseAs(EntityFeed.class);
        ArrayList<DBpediaEntityLookupResult> res=new ArrayList<>();
        WordNetSimilarityClient wnc=new WordNetSimilarityClient();
        for (DBpediaNamedEntity ne:resultList.results) {
            res.add(new DBpediaEntityLookupResult(ne, wnc.similarity(name, ne.getName())));
        }
        return res;
    }
}
