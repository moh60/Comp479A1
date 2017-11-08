import java.io.*;
import java.util.*;

public class ranking {

    // calculates rsv for a given term in a document
    public static double getRSV(int docID, String term, Map docLengthMap, Map docTermFrequencyMap) throws IOException {
        rsv getRelevance = new rsv();
        double docFrequency = getRelevance.getDocFrequencyOfTerm(term,docTermFrequencyMap);
        double termFrequency = getRelevance.getTermFrequency(term, docID);
        double docLength = getRelevance.getLengthOfDoc(docID, docLengthMap);
        double relevance = getRelevance.calculateRSV(docFrequency,termFrequency,docLength);
        return relevance;
    }

    // query search functionality, which will return the docID's of matched query
    public static void querySearch() throws FileNotFoundException, IOException{
        // store inverted index to map
        Map<String, List<Integer>> invertedIndex = new HashMap<String, List<Integer>>();
        // read from final block text file
        BufferedReader scan4 = new BufferedReader(new FileReader("FinalBlock/FinalIndexBlock.txt" ));
        String line;
        while ((line = scan4.readLine()) != null) {
            String pairToken[] = line.split(":");
            String term = pairToken[0];
            String documentPostingList[] = pairToken[1].split(",");
            int docID;
            List<Integer> finalPostingList = new ArrayList<>();
            for (int i = 0; i < documentPostingList.length; i++) {
                docID = Integer.parseInt(documentPostingList[i].replace("[", "").replace("]", "").replace(" ", ""));
                finalPostingList.add(docID);
            }
            // map the dictionary term and its posting list
            invertedIndex.put(term, finalPostingList);
        }

        // generate Maps
        rsv RSVMap = new rsv();
        Map docLengthMap = RSVMap.LengthOfDocMap();
        Map docTermFrequencyMap = RSVMap.docFrequencyOfTermMap();

        // Prompt user to enter query term(s)
        Scanner scanner_input = new Scanner(System.in);
        System.out.println("---------------------------------------------------");
        System.out.println("Please select the type of query you are parsing");
        System.out.println("---------------------------------------------------");
        String selection = scanner_input.nextLine();
        // case folding and special character removal
        String input = selection.toLowerCase().replaceAll("[^\\w\\s]", "");
        String inputCheckLength[] = input.split(" ");
        StringBuilder finalInput = new StringBuilder("");
        // stop word removal
        for (int i = 0; i < inputCheckLength.length; i++) {
            if (!extract.stopWord(inputCheckLength[i])) {
                finalInput.append(inputCheckLength[i] + " ");
            }
        }
        // run OR search and ranking
        orQuery(finalInput.toString(), invertedIndex, docLengthMap, docTermFrequencyMap);
    }

    // handles multiple query search - OR
    public static void orQuery(String input, Map<String, List<Integer>> invertedIndex, Map docLengthMap, Map docTermFrequencyMap) throws IOException {
        // OR query terms - should return document frequency of query term
        String orQuery = input;
        // split each query term
        String queryWordsCollection[] = orQuery.split(" ");
        // will store all the postings lists of the query terms
        HashSet<Integer> postingListCollection = new HashSet<>();
        // check if we have postings list for all the query terms
        for (int i = 0; i < queryWordsCollection.length; i++) {
            for(int docID : invertedIndex.get(queryWordsCollection[i])) {
                postingListCollection.add(docID);
            }
        }
        System.out.println("Start Ranking");
        TreeMap<Integer,Double> rsvCollection = new TreeMap<>();
        // for each doc id get rsv for given query terms
        for (int id : postingListCollection) {
            double docRSVTotal = 0;
            for (int i = 0; i < queryWordsCollection.length; i++) {
                double docRSV = getRSV(id, queryWordsCollection[i],docLengthMap,docTermFrequencyMap);
                docRSVTotal += docRSV;
            }
            rsvCollection.put(id, docRSVTotal);
        }
        // Calling the method sortByvalues
        Map sortedMap = sortByValues(rsvCollection);
        // Get a set of the entries on the sorted map
        Set set = sortedMap.entrySet();
        // Get an iterator
        Iterator i = set.iterator();
        // sort rsv score by highest to lowest
        int k = 10;
        for (int j = 0; j < k; j++) {
            if (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                System.out.print("Document: " + me.getKey() + " ");
                System.out.println("Score: " + me.getValue());
            }
        }
    }

    // handles sorting
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    // Driver
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        querySearch();
    }
}