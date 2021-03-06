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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
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
        int topRefCount = (!resultList.results.isEmpty()) ? resultList.results.get(0).getRefCount() : 0;
        ArrayList<DBpediaEntityLookupResult> res = new ArrayList<>();
        ArrayList<String> visitedUrl = new ArrayList<>();
        for (DBpediaNamedEntity ne : resultList.results) {
            URLConnection con = new URL(URI.create(ne.getUri()).toASCIIString()).openConnection();
            con.connect();
            try {
                InputStream is = con.getInputStream();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String redirect = con.getURL().toString().replace("http://dbpedia.org/page/", "http://dbpedia.org/resource/");
            if (!ne.getUri().equals(redirect)) {
                ne.setUri(redirect);
            }
            if (visitedUrl.contains(redirect)) {
                continue;
            } else {
                visitedUrl.add(redirect);
            }
            res.add(new DBpediaEntityLookupResult(ne, Math.max(ne.getRefCount() / topRefCount, similarityClient.similarity(name, ne.getName()))));
        }
        return res;
    }

    public JsonObject getEntityJsonByUri(String uri) {
        String urls = uri.replace("/resource/", "/data/") + ".json";
        urls = URI.create(urls).toASCIIString();
        String line;
        StringBuilder jsonSb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader("/Users/peterhuang/NetBeansProjects/ontology-for-qa/json/" + URLEncoder.encode(urls, "UTF-8")))) {
            while ((line = in.readLine()) != null) {
                jsonSb.append(line);
            }
            in.close();
        } catch (Exception ex) {
        }

        if (jsonSb.toString().isEmpty()) {
            BufferedReader br;
            try {
                URL url = new URL(urls);
                InputStream is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    jsonSb.append(line);
                }
                is.close();
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            } finally {
                try (PrintWriter out = new PrintWriter("/Users/peterhuang/NetBeansProjects/ontology-for-qa/json/" + URLEncoder.encode(urls, "UTF-8"))) {
                    out.println(jsonSb.toString());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(jsonSb.toString());
        JsonObject jo = je.getAsJsonObject();
        return jo;
    }

    public DBpediaNamedEntity getEntityByUri(String uri) {
        JsonObject jo = getEntityJsonByUri(uri);
        return getEntityByJson(uri, jo);
    }

    public DBpediaNamedEntity getEntityByJson(String uri, JsonObject jsonObj) {

        DBpediaNamedEntity res = new DBpediaNamedEntity();
        res.setUri(uri);

        String uri2 = URI.create(uri).toASCIIString();
//        System.out.println("uri: " + uri);
//        System.out.println("uri2: " + uri2);
        JsonElement je = jsonObj.getAsJsonObject().get(uri2);
        if (je == null) {
            return null;
        }
        JsonObject jo = je.getAsJsonObject();

        for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
            String jre = e.getKey();
            if (jre.contains("http://dbpedia.org/resource")) {
                if (jre.equals(uri)) {
                    continue;
                }
                JsonObject o = e.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> v : o.entrySet()) {
                    String attr = v.getKey();
                    if (!(attr.contains("http://dbpedia.org/ontology") || attr.contains("http://dbpedia.org/property"))) {
                        continue;
                    }

                    String value = o.get(attr).getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                    if (value.equals(uri)) {
                        res.rangeOfAttributes.add(attr);
                    }
                }
            }
        }

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
            for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
                String attr = e.getKey();
                if (attr.contains("http://dbpedia.org/ontology") || attr.contains("http://dbpedia.org/property")) {
                    res.domainOfAttributes.add(attr);
                }
            }

            HashSet<DBpediaCategory> categories = new HashSet<>();
            JsonArray jta = jo.get(DBpediaOntology.TYPE_ATTRIBUTE).getAsJsonArray();
            for (JsonElement jte : jta) {
                JsonObject jto = jte.getAsJsonObject();
                JsonElement jval = jto.get("value");
                if (jval != null) {
                    String catUri = jval.getAsString();
                    if (!catUri.contains("http://dbpedia.org/ontology")) {
                        continue;
                    }
                    DBpediaCategory cat = DBpediaOntology.getInstance().categoriesByUri.get(catUri);
                    if (cat != null) {
                        //keep only the lowest subclasses categories
                        //if categories c contains a subclass of cat, then superclass c is removed
                        //if categories c contains a superclass of cat, then cat is not added
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
