package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaAttribute;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaNamedEntity;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaStatisticsBuilder {

    static DBpediaOntology ontology = DBpediaOntology.getInstance();
    static HashSet<String> processed = new HashSet<>();
    static HashSet<String> enqueued = new HashSet<>();
    static int maxEntities = Integer.MAX_VALUE;

    private static void processEntity(String uri, LinkedList<String> queue) {
        JsonObject entityJson = ontology.getEntityJsonByUri(uri);
        DBpediaNamedEntity entity = ontology.getEntityByJson(uri, entityJson);
        double numberOfEntityClasses = entity.classes.size();
        if (numberOfEntityClasses == 0) {
            System.out.println("Classes of " + uri + " are " + entity.classes);
            System.exit(0);
        }

        ontology.updateEntity(uri, entity);
        //analyze the attributes of the entity
        System.out.println(uri);
        for (Map.Entry<String, JsonElement> e : entityJson.entrySet()) {
            String attributeUri = e.getKey();
            DBpediaAttribute attribute = ontology.getAttributeByUri(attributeUri);
            if (attribute != null) {
                JsonArray vals = e.getValue().getAsJsonArray();
                int nVal = vals.size();
                attribute.triplesCount += nVal;
                for (DBpediaCategory domCat : entity.classes) {
                    String domain = domCat.uri;
                    Double dd = attribute.domainDistribution.get(domain);
                    if (dd == null) {
                        attribute.domainDistribution.put(domain, nVal / numberOfEntityClasses);
                    } else {
                        attribute.domainDistribution.put(domain, nVal / numberOfEntityClasses + dd);
                    }
                }
                for (JsonElement jeVal : vals) {
                    JsonObject joVal = jeVal.getAsJsonObject();
                    String type = joVal.get("type").getAsString();
                    if (type.equals("literal")) {
                        JsonElement jeValDatatype = joVal.get("datatype");
                        String datatype;
                        if (jeValDatatype == null) {
                            datatype = "http://www.w3.org/2001/XMLSchema#string";
                        } else {
                            datatype = jeValDatatype.getAsString();
                        }
                        Double rd = attribute.rangeDistribution.get(datatype);
                        if (rd == null) {
                            attribute.rangeDistribution.put(datatype, 1d);
                        } else {
                            attribute.rangeDistribution.put(datatype, 1 + rd);
                        }
                    } else if (type.equals("uri")) {
                        String uriVal = joVal.get("value").getAsString();
                        if (uriVal == null || !uriVal.startsWith("http://dbpedia.org/resource/")) {
                            //it's a url that does not represent a dbpedia resorce, and we handle it as a string
                            Double rd = attribute.rangeDistribution.get("http://www.w3.org/2001/XMLSchema#string");
                            if (rd == null) {
                                attribute.rangeDistribution.put("http://www.w3.org/2001/XMLSchema#string", 1d);
                            } else {
                                attribute.rangeDistribution.put("http://www.w3.org/2001/XMLSchema#string", 1 + rd);
                            }
                            continue;
                        }
                        DBpediaNamedEntity entityVal = (DBpediaNamedEntity) ontology.getEntityByUri(uriVal);
                        if (entityVal.classes.isEmpty()) {
                            System.out.println("Classes of entityVal " + uriVal + " are " + entityVal.classes);
                            System.exit(0);
                        }
                        for (DBpediaCategory catVal : entityVal.classes) { //in case of more than 1 class, the weight of the triple is equally partitioned among the different classes
                            String classUri = catVal.uri;
                            Double rd = attribute.rangeDistribution.get(classUri);
                            if (rd == null) {
                                attribute.rangeDistribution.put(classUri, 1d / entityVal.classes.size());
                            } else {
                                attribute.rangeDistribution.put(classUri, 1d / entityVal.classes.size() + rd);
                            }
                        }
                        if (!processed.contains(uriVal) && !enqueued.contains(uriVal) && processed.size() + enqueued.size() < maxEntities) {
                            queue.addLast(uriVal);
                            enqueued.add(uriVal);
                        }
                    } else {
                        System.out.println(attributeUri + "\n" + joVal);
                        System.exit(0);
                    }
                }

            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        BufferedReader in = new BufferedReader(new FileReader("/home/massimo/attributestats.json"));
        String l = in.readLine();
        String json = "";
        while (l != null) {
            json += l;
            l = in.readLine();
        }
        in.close();
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, DBpediaAttribute>>() {
        }.getType();
        HashMap<String, DBpediaAttribute> stats = gson.fromJson(json, type);
        for (String attUri : stats.keySet()) {
            DBpediaAttribute cachedAtt = stats.get(attUri);
            DBpediaAttribute ontoAtt = ontology.attributesByUri.get(attUri);
            ontoAtt.domainDistribution = cachedAtt.domainDistribution;
            ontoAtt.rangeDistribution = cachedAtt.rangeDistribution;
            ontoAtt.triplesCount = cachedAtt.triplesCount;
        }
        int totalTriples=0;
        for (String attUri : ontology.attributesByUri.keySet()) {
            DBpediaAttribute ontoAtt=ontology.attributesByUri.get(attUri);
            System.out.println(attUri+" "+ontoAtt.triplesCount+" triples");
            if (ontoAtt.triplesCount==0) {
                System.out.println("========================>> NOT FOUND <<=============================");
            } else {
                System.out.println("Domain: "+ontoAtt.domainUri);
                totalTriples+=ontoAtt.triplesCount;
                for (Map.Entry<String, Double> e:ontoAtt.domainDistribution.entrySet()) {
                    System.out.println("\t"+e.getKey()+" -> "+(e.getValue()/ontoAtt.triplesCount));
                }
                System.out.println("Range: "+ontoAtt.rangeUri);
                for (Map.Entry<String, Double> e:ontoAtt.rangeDistribution.entrySet()) {
                    System.out.println("\t"+e.getKey()+" -> "+(e.getValue()/ontoAtt.triplesCount));
                }
            }
        }
        System.out.println("Total triples found: "+totalTriples);

        in = new BufferedReader(new FileReader("/home/massimo/queue.json"));
        l = in.readLine();
        json = "";
        while (l != null) {
            json += l;
            l = in.readLine();
        }
        in.close();
        type = new TypeToken<LinkedList<String>>() {
        }.getType();
        LinkedList<String> queue = gson.fromJson(json, type);

        if (queue.isEmpty()) {
            queue = new LinkedList<>();
            queue.addLast("http://dbpedia.org/resource/%C3%81ngel_Gim%C3%A9nez");
            queue.addLast("http://dbpedia.org/resource/007:_Licence_to_Kill");
            queue.addLast("http://dbpedia.org/resource/2000_series_(Chicago_'L')");
            queue.addLast("http://dbpedia.org/resource/5172_Yoshiyuki");
            queue.addLast("http://dbpedia.org/resource/5676_Voltaire");
            queue.addLast("http://dbpedia.org/resource/6_Squadron_SAAF");
            queue.addLast("http://dbpedia.org/resource/Aalsum,_Friesland");
            queue.addLast("http://dbpedia.org/resource/Aalsum,_Groningen");
            queue.addLast("http://dbpedia.org/resource/Aaron_Lines");
            queue.addLast("http://dbpedia.org/resource/Abel_Lafleur");
            queue.addLast("http://dbpedia.org/resource/Measles");
        }

        in = new BufferedReader(new FileReader("/home/massimo/processed.json"));
        l = in.readLine();
        json = "";
        while (l != null) {
            json += l;
            l = in.readLine();
        }
        in.close();
        type = new TypeToken<HashSet<String>>() {
        }.getType();
        processed = gson.fromJson(json, type);

        System.out.println("Entity processed: "+processed.size());

        int duplicates = 0;
        for (Iterator<String> it = queue.iterator(); it.hasNext();) {
            String uri = it.next();
            if (enqueued.contains(uri) || processed.contains(uri)) {
                it.remove();
                duplicates++;
            } else {
                enqueued.add(uri);
            }
        }

        System.out.println(duplicates + " duplicates removed");

        while (!queue.isEmpty() && processed.size() < maxEntities) {
            String nextUri = queue.removeFirst();
            if (processed.contains(nextUri)) {
                continue;
            }
            processed.add(nextUri);
            processEntity(nextUri, queue);
            if (processed.size() % 100 == 0) {
                System.out.println(processed.size() + " entities processed so far");
                String s = gson.toJson(ontology.attributesByUri);
                PrintWriter out = new PrintWriter(new FileOutputStream("/home/massimo/attributestats.json"), true);
                out.print(s);
                out.close();
                s = gson.toJson(queue);
                out = new PrintWriter(new FileOutputStream("/home/massimo/queue.json"), true);
                out.print(s);
                out.close();
                s = gson.toJson(processed);
                out = new PrintWriter(new FileOutputStream("/home/massimo/processed.json"), true);
                out.print(s);
                out.close();
            }
        }

        for (DBpediaAttribute a : ontology.attributesByUri.values()) {
            if (a.triplesCount == 0) {
                continue;
            }
            System.out.println("Attribute " + a.getUri());
            System.out.println("Number of triples: " + a.triplesCount);
            System.out.println("Domain distribution");
            double totVal=0;
            for (Map.Entry<String, Double> e : a.domainDistribution.entrySet()) {
                totVal+=e.getValue();
            }
            for (Map.Entry<String, Double> e : a.domainDistribution.entrySet()) {
                e.setValue(e.getValue() / totVal);
                System.out.println("\t" + e.getKey() + " -> " + e.getValue());
            }
            System.out.println("Range distribution");
            totVal=0;
            for (Map.Entry<String, Double> e : a.rangeDistribution.entrySet()) {
                totVal+=e.getValue();
            }
            for (Map.Entry<String, Double> e : a.rangeDistribution.entrySet()) {
                e.setValue(e.getValue() / totVal);
                System.out.println("\t" + e.getKey() + " -> " + e.getValue());
            }
        }

        String s = gson.toJson(ontology.attributesByUri);
        PrintWriter out = new PrintWriter(new FileOutputStream("/home/massimo/attributestats.json"), true);
        out.print(s);
        out.close();
        s = gson.toJson(queue);
        out = new PrintWriter(new FileOutputStream("/home/massimo/queue.json"), true);
        out.print(s);
        out.close();
        s = gson.toJson(processed);
        out = new PrintWriter(new FileOutputStream("/home/massimo/processed.json"), true);
        out.print(s);
        out.close();

    }

}
