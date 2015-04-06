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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class DBpediaEntityLookup {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    SimilarityClient similarityClient;

    public static class EntityFeed {

        @Key
        public ArrayList<DBpediaNamedEntity> results;
    }

    public static class DBpediaUrl extends GenericUrl {

        public DBpediaUrl(String encodedUrl) {
            super(encodedUrl);
        }

    }

    public DBpediaEntityLookup(SimilarityClient similarityClient) {
        this.similarityClient = similarityClient;
    }

    public ArrayList<DBpediaEntityLookupResult> lookup(String name, int maxHits) throws Exception {
        HttpRequestFactory requestFactory
                = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        DBpediaUrl url = new DBpediaUrl("http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?MaxHits=" + maxHits + "&QueryString=" + name);
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAccept("application/json");
        HttpResponse response = request.execute();

        EntityFeed resultList = response.parseAs(EntityFeed.class);
        ArrayList<DBpediaEntityLookupResult> res = new ArrayList<>();
        //WordNetSimilarityClient similarityClient = new WordNetSimilarityClient();
        for (DBpediaNamedEntity ne : resultList.results) {
            res.add(new DBpediaEntityLookupResult(ne, similarityClient.similarity(name, ne.getName())));
        }
        return res;
    }

    public JsonObject getEntityJsonByUri(String uri) {
        String urls = uri.replace("/resource/", "/data/") + ".json";
        urls = URI.create(urls).toASCIIString();
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        StringBuilder jsonSb = new StringBuilder();
        try {
            url = new URL(urls);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                jsonSb.append(line);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                return null;
            }
        }
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(jsonSb.toString());
        System.out.println("uri: " + uri);
        System.out.println("uri2: " + URI.create(uri).toASCIIString());
        JsonObject jo = je.getAsJsonObject().get(URI.create(uri).toASCIIString()).getAsJsonObject();
        return jo;
    }

    public DBpediaNamedEntity getEntityByUri(String uri) {
        JsonObject jo = getEntityJsonByUri(uri);
        return getEntityByJson(uri, jo);
    }

    public DBpediaNamedEntity getEntityByJson(String uri, JsonObject jo) {


        DBpediaNamedEntity res = new DBpediaNamedEntity();
        res.setUri(uri);
        
        try {
            JsonArray jda = jo.get("http://www.w3.org/2000/01/rdf-schema#comment").getAsJsonArray();
            for (JsonElement jde : jda) {
                JsonObject jdo = jde.getAsJsonObject();
                if (jdo.get("lang") != null && jdo.get("lang").getAsString().equals("en")) {
                    res.setDescription(jdo.get("value").getAsString());
                    break;
                }
            }
        } catch (Exception e) {
        }

        try {
            JsonArray jla = jo.get("http://www.w3.org/2000/01/rdf-schema#label").getAsJsonArray();
            for (JsonElement jle : jla) {
                JsonObject jlo = jle.getAsJsonObject();
                if (jlo.get("lang") != null && jlo.get("lang").getAsString().equals("en")) {
                    res.setLabel(jlo.get("value").getAsString());
                    break;
                }
            }
        } catch (Exception e) {
        }

        try {
            JsonArray jpa = jo.get("http://xmlns.com/foaf/0.1/isPrimaryTopicOf").getAsJsonArray();
            for (JsonElement jpe : jpa) {
                JsonObject jpo = jpe.getAsJsonObject();
                if (jpo.get("value") != null && jpo.get("value").getAsString().contains("en.wikipedia")) {
                    res.setPageUrl(jpo.get("value").getAsString());
                    break;
                }
            }
        } catch (Exception e) {
        }

        try {
            JsonArray jta = jo.get("http://dbpedia.org/ontology/thumbnail").getAsJsonArray();
            for (JsonElement jte : jta) {
                JsonObject jto = jte.getAsJsonObject();
                if (jto.get("value") != null) {
                    res.setThumbUrl(jto.get("value").getAsString());
                    break;
                }
            }
        } catch (Exception e) {
        }

        try {
            HashSet<DBpediaCategory> categories = new HashSet<>();
            JsonArray jta = jo.get(DBpediaOntology.TYPE_ATTRIBUTE).getAsJsonArray();
            for (JsonElement jte : jta) {
                JsonObject jto = jte.getAsJsonObject();
                if (jto.get("value") != null) {
                    String catUri = jto.get("value").getAsString();
                    DBpediaCategory cat = DBpediaOntology.getInstance().categoriesByUri.get(catUri);
                    if (cat != null) {
                        //if categories contains a subclass of cat, then cat is not added
                        //if categories contains a superclass of cat, then this class is removed
                        boolean addCat = true;
                        for (Iterator<DBpediaCategory> it = categories.iterator(); it.hasNext();) {
                            DBpediaCategory c = it.next();
                            if (cat.hasAncestor(c)) {
                                it.remove(); //don't break, there could be more than one ancestor
                            } else if (c.hasAncestor(cat)) {
                                addCat = false;
                                break;
                            }
                        }
                        if (addCat) {
                            categories.add(cat);
                        }
                    }
                }
            }
            res.classes.addAll(categories);
        } catch (Exception e) {
        }
        if (res.classes.isEmpty()) {
            res.classes.add(DBpediaOntology.thingCategory());
        }
        return res;
    }
    
}
