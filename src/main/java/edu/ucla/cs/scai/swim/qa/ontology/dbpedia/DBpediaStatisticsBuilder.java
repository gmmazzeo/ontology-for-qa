package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaAttribute;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaNamedEntity;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaStatisticsBuilder {

    static DBpediaOntology ontology = DBpediaOntology.getInstance();

    private static void processEntity(String uri, LinkedList<String> queue) {
        JsonObject entityJson = ontology.getEntityJsonByUri(uri);
        DBpediaNamedEntity entity = ontology.getEntityByJson(uri, entityJson);
        if (entity.classes.size() != 1) {
            System.out.println("Classes of " + uri + " are " + entity.classes);
            System.exit(0);
        }
        String domain = entity.classes.get(0).uri;
        ontology.updateEntity(uri, entity);
        //analyze the attributes of the entity
        System.out.println(uri);
        for (Map.Entry<String, JsonElement> e : entityJson.entrySet()) {
            String attributeUri = e.getKey();
            DBpediaAttribute attribute = ontology.getAttributeByUri(attributeUri);
            if (attribute != null) {
                JsonArray vals = e.getValue().getAsJsonArray();
                for (JsonElement jeVal : vals) {
                    attribute.triplesCount++;
                    Double dd = attribute.domainDistribution.get(domain);
                    if (dd == null) {
                        attribute.domainDistribution.put(domain, 1d);
                    } else {
                        attribute.domainDistribution.put(domain, 1 + dd);
                    }
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
                        System.out.println(joVal);
                        String uriVal = joVal.get("value").getAsString();
                        if (uriVal == null || !uriVal.startsWith("http://dbpedia.org/resource/")) {
                            continue;
                        }
                        DBpediaNamedEntity entityVal = (DBpediaNamedEntity) ontology.getEntityByUri(uriVal);
                        if (entityVal.classes.size() != 1) {
                            System.out.println("Classes of entityVal " + uriVal + " are " + entityVal.classes);
                            System.exit(0);
                        }
                        String classUri = entityVal.classes.get(0).uri;
                        Double rd = attribute.rangeDistribution.get(classUri);
                        if (rd == null) {
                            attribute.rangeDistribution.put(classUri, 1d);
                        } else {
                            attribute.rangeDistribution.put(classUri, 1 + rd);
                        }
                    } else {
                        System.out.println(attributeUri + "\n" + joVal);
                        System.exit(0);
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        LinkedList<String> queue = new LinkedList<>();
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

        while (!queue.isEmpty()) {
            String nextUri = queue.removeFirst();
            processEntity(nextUri, queue);
        }

    }

}