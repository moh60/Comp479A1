import java.io.*;
import java.util.*;

public class ranking {

    // calculates rsv for a given term in a document
    public static double getRSV(int docID, String term) throws IOException {
        rsv getRelevance = new rsv();
        double docFrequency = getRelevance.getDocFrequencyOfTerm(term);
        double termFrequency = getRelevance.getTermFrequency(term, docID);
        double docLength = getRelevance.getLengthOfDoc(docID);
        double relevance = getRelevance.calculateRSV(docFrequency,termFrequency,docLength);
//        System.out.println(relevance);
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
        // Prompt user to enter query term(s)
        Scanner scanner_input = new Scanner(System.in);
        System.out.println("---------------------------------------------------");
        System.out.println("Please select the type of query you are parsing");
        System.out.println("---------------------------------------------------");
        String selection = scanner_input.nextLine();
        // run OR search
        String input = selection.toLowerCase();
        String inputCheckLength[] = input.split(" ");
        orQuery(input, invertedIndex);
    }

    // handles multiple query search - OR
    public static void orQuery(String input, Map<String, List<Integer>> invertedIndex) throws IOException {
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
        Map<Integer,Double> rsvCollection = new HashMap<Integer, Double>();
        // for each doc id get rsv for given query terms
        for (int id : postingListCollection) {
            if (id == 8072) {
                double docRSVTotal = 0;
                System.out.println("Document ID: " +id);
                for (int i = 0; i < queryWordsCollection.length; i++) {
                    System.out.println("term " + queryWordsCollection[i]);
                    double docRSV = getRSV(id, queryWordsCollection[i]);
                    docRSVTotal += docRSV;
                }
                rsvCollection.put(id, docRSVTotal);
                break;
            }
        }
        // sort rsv score by highest to lowest
        System.out.println("Printing");
        List<Double> c = new ArrayList<Double>(rsvCollection.values());
        Collections.sort(c);
        for(int i=0 ; i< c.size(); ++i) {
            System.out.println(i + " rank is " + c.get(i));
        }
    }

    // Driver
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        querySearch();
    }
}
