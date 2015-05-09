
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author peterhuang
 */
public class DBpediaDomainRangeFinder {

    boolean printDomainRangeCount = false;
    boolean printRangesPerAttribute = false;
    double THRESHOLD = 0.1;
    DBpediaOntology ontology = DBpediaOntology.getInstance();
    String attrPrefix = "<ont/";
    String ontNamespace = "http://dbpedia.org/ontology/";
    String ontPrefix = "<ont/";
    String resNamespace = "http://dbpedia.org/resource/";
    String resPrefix = "<res/";
    String thingPrefix = "http://www.w3.org/2002/07/";
    String thingSuffux = "owl#Thing";
    HashMap<String, ArrayList<String>> types = new HashMap<>();
    HashMap<String, String> proplabels = new HashMap<>();
    HashMap<String, String> domains = new HashMap<>();
    HashMap<String, String> ranges = new HashMap<>();

    public static void main(String[] args) throws Exception {
        DBpediaDomainRangeFinder typesFinder = new DBpediaDomainRangeFinder();
        //Either calculateTypes and saveTypes, or loadTypes
        //typesFinder.calculateTypes("/Volumes/Untitled/dbpedia/processed/instance_types_en_owl.nt");
        //typesFinder.saveTypestoFile("/Volumes/Untitled/dbpedia/processed/instance_types_lowest");
        typesFinder.loadPropertyLabels("/Users/peterhuang/Downloads/dbpedia/properties_labels");
        typesFinder.loadTypes("/Users/peterhuang/Downloads/dbpedia/instance_types_lowest");
        typesFinder.calculateMappingDomainRange("/Volumes/Untitled/dbpedia/processed/mappingbased_properties_cleaned_en.nt",
                "/Users/peterhuang/Downloads/dbpedia/mappings");
        //typesFinder.calculateMappingDomainRange("/Volumes/Untitled/dbpedia/processed/infobox_properties_en.nt",
        //        "/Users/peterhuang/Downloads/dbpedia/properties");
    }

    //Recursively look for paths through parents
    public ArrayList<ArrayList<DBpediaCategory>> parentBFS (ArrayList<DBpediaCategory> path) {
        ArrayList<ArrayList<DBpediaCategory>> paths = new ArrayList<>();
        DBpediaCategory cat = path.get(path.size() - 1);
        if (cat.getParents().isEmpty()) {
            paths.add(path);
            return paths;
        }
        for (DBpediaCategory parCat : cat.getParents()) {
            ArrayList<DBpediaCategory> copy = new ArrayList<>(path);
            copy.add(parCat);
            paths.addAll(parentBFS(copy));
        }
        return paths;
    }

    //Calculates lowest common ancestor with graph rooted at #Thing
    public String commonAncestor(DBpediaCategory cat1, DBpediaCategory cat2) {
        ArrayList<ArrayList<DBpediaCategory>> paths1 = new ArrayList<>();
        ArrayList<ArrayList<DBpediaCategory>> paths2 = new ArrayList<>();

        paths1.addAll(parentBFS(new ArrayList<>(Arrays.asList(cat1))));
        paths2.addAll(parentBFS(new ArrayList<>(Arrays.asList(cat2))));

        for (ArrayList path : paths1) {
            Collections.reverse(path);
        }
        for (ArrayList path : paths2) {
            Collections.reverse(path);
        }

        String common = "";
        int lowest = -1;
        for (ArrayList<DBpediaCategory> path1 : paths1) {
            for (ArrayList<DBpediaCategory> path2 : paths2) {
                for (int k = 0; k < Math.min(path1.size(), path2.size()); k++) {
                    if (path1.get(k).equals(path2.get(k))) {
                        if (k > lowest) {
                            common = path1.get(k).getUri();
                            lowest = k;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        return common;
    }

    //Increment the count for the key in the map
    public void incrementCount(HashMap<String, Integer> map, String key) {
        if (map.get(key) == null) {
            map.put(key, 0);
        }
        map.put(key, map.get(key) + 1);
    }

    /*line format
        Assumes file is sorted by the second column
        <res/Entity> <ont/Attribute> <res/Entity>|"*".
        <res/%C3%87a_plane_pour_moi> <ont/aSide> "\"\"".
        <res/Anything_Is_Possible_(Debbie_Gibson_song)> <ont/aSide> "(7\" Remix/3:30)".
        <res/(101429)_1998_VF31> <ont/absoluteMagnitude> "17.4"^^<XS#double> .
        <res/Adolfas_Jucys> <ont/academicAdvisor> <res/Douglas_Hartree> .
    */
    public void calculateMappingDomainRange(String mappingsPath, String outPath) throws IOException {
        HashMap<String, Integer> domainCount = new HashMap<>();
        HashMap<String, Integer> rangeCount = new HashMap<>();
        HashMap<String, Integer> tempCount = new HashMap<>();
        String attr = "";
        try(BufferedReader in = new BufferedReader(new FileReader(mappingsPath))) {
            System.out.println("starting mapping");
            String l;
            int counter = 0;
            while (true) {
                l = in.readLine();
                if (l == null) {
                    break;
                }
                counter++;
                if (counter % 100_000 == 0) {
                    System.out.println(counter);
                }
                String[] s = l.substring(0, l.length() - 1).trim().split("> (?=<|\")", 3);
                if (!s[0].startsWith(resPrefix) || !s[1].startsWith(attrPrefix)) {
                    System.out.println("Invalid triple: " + l);
                    continue;
                }

                String subject = s[0].substring(resPrefix.length(), s[0].length());
                String attribute = s[1].substring(attrPrefix.length(), s[1].length());
                if (!attribute.equals(attr)) {
                    clearTemp(attr, tempCount);
                    attr = attribute;
                }

                ArrayList<String> subjectList = types.get(subject);
                if (subjectList == null) {
                    //System.out.println("subject types missing: " + subject);
                    continue;
                } else {
                    DBpediaCategory newCat = (DBpediaCategory) ontology.getCategoryByUri(ontNamespace + subjectList.get(0));
                    if (newCat == null) {
                        System.out.println("subject type invalid");
                        continue;
                    }
                    incrementCount(domainCount, newCat.getUri());
                    String common  = domains.get(attribute);
                    if (common == null) {
                        domains.put(attribute, newCat.getUri());
                    } else {
                        domains.put(attribute, commonAncestor((DBpediaCategory) ontology.getCategoryByUri(common), newCat));
                    }
                }

                if (s[2].startsWith(resPrefix)) {
                    String value = s[2].substring(resPrefix.length(), s[2].indexOf(">"));
                    if (subject.equals(value)) {
                        //System.out.println("subject value error: " + subject);
                        continue;
                    }
                    ArrayList<String> valueList = types.get(value);
                    if (valueList == null) {
                        //System.out.println("value types missing: " + value);
                        incrementCount(rangeCount, DBpediaOntology.THING_URI);
                        incrementCount(tempCount, DBpediaOntology.THING_URI);
                    } else {
                        DBpediaCategory newCat = (DBpediaCategory) ontology.getCategoryByUri(ontNamespace + valueList.get(0));
                        if (newCat == null) {
                            System.out.println("value type invalid");
                        } else {
                            incrementCount(rangeCount, newCat.getUri());
                            incrementCount(tempCount, newCat.getUri());
                        }
                    }
                } else if (s[2].contains("<http://dbpedia.org/datatype/")) {
                    String type = s[2].substring(s[2].indexOf("<http://dbpedia.org/datatype/") + 1, s[2].indexOf(">"));
                    incrementCount(rangeCount, type);
                    incrementCount(tempCount, type);
                } else if (s[2].contains("<XS#")) {
                    String type = s[2].substring(s[2].indexOf("<XS#") + 1, s[2].indexOf(">"));
                    incrementCount(rangeCount, type);
                    incrementCount(tempCount, type);
                } else if (s[2].startsWith("\"\\\"") && s[2].endsWith("\\\"\"")) {
                    incrementCount(rangeCount, "XS#string");
                    incrementCount(tempCount, "XS#string");
                } else if (s[2].startsWith("\"") && s[2].endsWith("\"")) {
                    incrementCount(rangeCount, "XS#string");
                    incrementCount(tempCount, "XS#string");
                } else if (s[2].startsWith("\"") && (s[2].endsWith("\"@en") || s[2].endsWith("\"@ja"))) {
                    incrementCount(rangeCount, "NS#langString");
                    incrementCount(tempCount, "NS#langString");
                } else if (s[2].startsWith("<") && s[2].endsWith(">")) {
                } else {
                    System.out.println("unknown range type: " + s[2]);
                    System.out.println(l);
                }
            }
            clearTemp(attr, tempCount);

            System.out.println("done");
            System.out.println(counter);
            System.out.println("\ndomains/ranges: " + domains.size());
            PrintWriter out = new PrintWriter(new FileOutputStream(outPath, false), true);
            for (String s : new TreeMap<>(domains).keySet()) {
                String domain = domains.get(s);
                if (domain.startsWith(thingSuffux)) {
                    domain = DBpediaOntology.THING_URI;
                } else if (domain.startsWith("XS#")) {
                    domain = domain.replace("XS#", "http://www.w3.org/2001/XMLSchema#");
                } else {
                    domain = ontNamespace + domain;
                }
                String range = ranges.get(s);
                if (range.startsWith(thingSuffux)) {
                    range = DBpediaOntology.THING_URI;
                } else if (range.contains("NS#langString")) {
                    range = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
                } else if (range.startsWith("XS#")) {
                    range = range.replace("XS#", "http://www.w3.org/2001/XMLSchema#");
                } else if (range.startsWith("http://dbpedia.org/datatype/")) {
                } else {
                    range = ontNamespace + range;
                }

                if (attrPrefix.equals("<prop/")) {
                    String propLabel = proplabels.get(s);
                    if (propLabel == null && !s.endsWith("_")) {
                        System.out.println("property label missing: " + s);
                        continue;
                    } else if (s.endsWith("_")) {
                        String temp = s.substring(0, s.length() - 1);
                        temp = temp.replace("^'+", "");
                        temp = temp.replace("'+$", "");
                        propLabel = proplabels.get(temp);
                        if (propLabel == null) {
                            System.out.println("property label missing: " + s);
                            continue;
                        }
                    }
                    out.println(s + " : " + proplabels.get(s) + " : <" + domain + ", " + range + ">");
                } else if (attrPrefix.equals(ontPrefix)) {
                    DBpediaAttribute dbpAttr = ontology.getAttributeByUri(ontNamespace + s);
                    if (dbpAttr == null) {
                        System.out.println("Error getting attribute from ontology: " + s);
                        continue;
                    }
                    out.println(s + " : " + dbpAttr.getLabel() + " : <" + domain + ", " + range + ">");
                }
            }
            if (printDomainRangeCount) {
                System.out.println("\ndomainCount: " + domainCount.size());
                for (String s : new TreeMap<>(domainCount).keySet()) {
                    System.out.println(s + " : " + domainCount.get(s));
                }
                System.out.println("\nrangeCount: " + rangeCount.size());
                for (String s : new TreeMap<>(rangeCount).keySet()) {
                    System.out.println(s + " : " + rangeCount.get(s));
                }
            }
        }
    }

    //Clear the temp hashmap once a new attribute is found
    public void clearTemp(String attr, HashMap tempCount) {
        if (attr.length() == 0) {
            return;
        }
        String domain = domains.get(attr);
        if (domain == null) {
            domain = DBpediaOntology.THING_URI;
        }
        if (domain.startsWith(ontNamespace)) {
            domain = domain.substring(ontNamespace.length());
        } else if (domain.startsWith(thingPrefix)) {
            domain = domain.substring(thingPrefix.length());
        }
        domains.put(attr, domain);

        if (tempCount.isEmpty()) {
            incrementCount(tempCount, DBpediaOntology.THING_URI);
        }
        ValueComparator bvc =  new ValueComparator(tempCount);
        TreeMap<String,Integer> sortedTemp = new TreeMap<>(bvc);
        sortedTemp.putAll(tempCount);
        tempCount.clear();

        String common = sortedTemp.firstEntry().getKey();
        int highestCount = sortedTemp.firstEntry().getValue();
        for (Map.Entry<String, Integer> entry : sortedTemp.entrySet()) {
            if (entry.getKey().startsWith(ontNamespace) && entry.getValue() > highestCount * THRESHOLD) {
                if (!common.startsWith(ontNamespace)) {
                    //common = ontology.getCategoryByUri(entry.getKey()).getUri();
                    //continue;
                    break;
                }
                common = commonAncestor((DBpediaCategory) ontology.getCategoryByUri(common), (DBpediaCategory) ontology.getCategoryByUri(entry.getKey()));
            }
        }

        if (common.startsWith(ontNamespace)) {
            common = common.substring(ontNamespace.length());
        } else if (common.startsWith(thingPrefix)) {
            common = common.substring(thingPrefix.length());
        }
        ranges.put(attr, common);

        if (printRangesPerAttribute) {
            System.out.println(attr + " : <" + domains.get(attr) + ", " + ranges.get(attr) + ">");
            for (Map.Entry entry : sortedTemp.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        }
    }

    /*line format:
        <Entity> <Category>
        <UCLA_Henry_Samueli_School_of_Engineering_and_Applied_Science> <University>
    */
    public void loadTypes(String typesPath) throws IOException {
        try(BufferedReader in = new BufferedReader(new FileReader(typesPath))) {
            System.out.println("starting loading types");
            System.out.println("== -> 100000");
            System.out.println("====================| -> 1000000");
            String l;
            int counter = 0;
            while (true) {
                l = in.readLine();
                if (l == null) {
                    break;
                }
                counter++;
                if (counter % 50_000 == 0) {
                    System.out.print("=");
                    if (counter % 1_000_000 == 0) {
                        System.out.println(">");
                    }
                }
                String[] s = l.split(" ");

                String entity = s[0].substring(1, s[0].length() - 1);
                String type = s[1].substring(1, s[1].length() - 1);
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(type);
                types.put(entity, arrayList);
            }
            System.out.println("done");
            System.out.println(types.size());
        }
    }

    //Load the labels for the properties from the properties labels file
    /*line format:
        <property> "label"@en .
        <barPassRate> "bar pass rate"@en .
    */
    public void loadPropertyLabels(String labelsPath) throws IOException {
        try(BufferedReader in = new BufferedReader(new FileReader(labelsPath))) {
            System.out.println("starting loading property labels");
            String l;
            while (true) {
                l = in.readLine();
                if (l == null) {
                    break;
                }
                String[] s = l.split("> (?=\")");

                String property = s[0].substring(1, s[0].length());
                String label = s[1].substring(1, s[1].length() - 6);
                if (!s[1].endsWith("\"@en .")) {
                    System.out.println("label error:" + l);
                    continue;
                }
                if (proplabels.get(property) != null) {
                    System.out.println("label already exists:" + property + " " + label);
                }
                proplabels.put(property, label);
            }
            System.out.println("done");
            System.out.println(proplabels.size());
        }
    }

    //Calculate the lowest type for each entity
    /*line format:
        <res/Entity> <ont/Category>
        <res/Apache_Software_Foundation> <ont/Agent> .
        <res/Apache_Software_Foundation> <ont/Non-ProfitOrganisation> .
        <res/Apache_Software_Foundation> <ont/Organisation> .
    */
    public void calculateTypes(String typesPath) throws IOException {
        try(BufferedReader in = new BufferedReader(new FileReader(typesPath))) {
            System.out.println("starting types");
            String l;
            int counter = 0;
            while (true) {
                l = in.readLine();
                if (l == null) {
                    break;
                }
                counter++;
                if (counter % 100_000 == 0) {
                    System.out.println(counter);
                }
                //System.out.println(l);
                String[] s = l.split(" ");
                if (s.length != 3 ||! s[0].startsWith(resPrefix) || ! s[1].startsWith(ontPrefix)) {
                    System.out.println("Invalid triple");
                    continue;
                }
                String entity = s[0].substring(5, s[0].length() - 1);
                String type = s[1].substring(5, s[1].length() - 1);
                if (type.equals("Wikidata:Q532") || type.equals("Wikidata:Q11424")) {
                    continue;
                }
                //System.out.println(entity + ' ' + type);
                if (types.get(entity) == null) {
                    DBpediaCategory newCat = (DBpediaCategory) ontology.getCategoryByUri(ontNamespace + type);
                    if (newCat == null) {
                        System.out.println("new cat invalid: " + type);
                    } else {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(type);
                        types.put(entity, arrayList);
                    }
                } else {
                    DBpediaCategory newCat = (DBpediaCategory) ontology.getCategoryByUri(ontNamespace + type);
                    if (newCat == null) {
                        System.out.println("new cat invalid: " + type);
                        continue;
                    }

                    boolean found = false;
                    ArrayList<String> arrayList = types.get(entity);
                    ArrayList<String> toRemove = new ArrayList<>();
                    ArrayList<String> toAdd = new ArrayList<>();
                    for (String string : arrayList) {
                        DBpediaCategory oldCat = (DBpediaCategory) ontology.getCategoryByUri(ontNamespace + string);
                        if (newCat.hasAncestor(oldCat)) {
                            toRemove.add(string);
                            toAdd.add(type);
                            found = true;
                            //System.out.println("found lower type");
                        } else if (oldCat.hasAncestor(newCat)) {
                            found = true;
                            //System.out.println("found higher type");
                        }
                    }
                    for (String string : toRemove) {
                        arrayList.remove(string);
                    }
                    for (String string : toAdd) {
                        if (!arrayList.contains(string)) {
                            arrayList.add(string);
                        }
                    }
                    if (!found) {
                        arrayList.add(type);
                        //System.out.println("types not in the same branch");
                    }
                }
            }
            System.out.println("done");
            System.out.println(types.size());
        }
    }

    //Run after calculateTypes to store the types hashmap to be loaded quicker later
    public void saveTypestoFile(String filePath) throws IOException {
        PrintWriter out = new PrintWriter(new FileOutputStream(filePath, false), true);
        for (String s : new TreeMap<>(types).keySet()) {
            if (types.get(s).size() > 1 || types.get(s).get(0).equals("Agent")) {
                System.out.println(s + " : " + types.get(s));
            }
            if (types.get(s).size() == 1) {
                    out.println("<" + s + "> <" + types.get(s).get(0) + ">");
            }
        }
    }

    /*
        Value comparator for sorted maps
        ValueComparator vc =  new ValueComparator(inputMap);
        TreeMap<String,Integer> sortedMap = new TreeMap<>(vc);
        sortedMap.putAll(inputMap);
    */
    class ValueComparator implements Comparator<String> {
        Map<String, Integer> map;
        ValueComparator(Map<String, Integer> map) {
            this.map = map;
        }

        @Override
        public int compare(String a, String b) {
            if (map.get(a) > map.get(b)) {
                return -1;
            } else if (map.get(a) < map.get(b)) {
                return 1;
            } else {
                if (a.compareTo(b) <= 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

}
