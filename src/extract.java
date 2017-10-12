import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class extract {
    // case folding
    public static String caseFolding_numberRemoval(String term) {
        String removedSpecialChar_term = term.toLowerCase().replaceAll("[^\\w\\s]", " ");
        String[] splited_terms_collection = removedSpecialChar_term.split("\\s+");
        for(int i=0; i<splited_terms_collection.length; i++) {
            // apply number removal
            if (splited_terms_collection[i].matches("[-+]?\\d*\\.?\\d+")) {
                splited_terms_collection[i].trim();
                continue;
            }
            // remove string of length 1
            if (splited_terms_collection[i].length() == 1) {
                splited_terms_collection[i].trim();
                continue;
            }

            // remove strings that contain numbers
            if (splited_terms_collection[i].matches(".*\\d+.*")) {
                splited_terms_collection[i].trim();
                continue;
            }
            // remove empty strings
            if (splited_terms_collection[i].equals("")) {
                splited_terms_collection[i].trim();
                continue;
            }
            return splited_terms_collection[i];
        }
        return "";
    }

    // stop word removal
    public static Boolean stopWord(String term) throws FileNotFoundException {
        // read through stop word text file and add each word into a list
        Scanner scan_stop_words = new Scanner(new File("stopwords.txt"));
        List<String> stop_words_collection = new ArrayList<String>();
        while (scan_stop_words.hasNext()) {
            stop_words_collection.add(scan_stop_words.next());
        }
        // if term is a stop word then we don't add it to our collection
        if (stop_words_collection.contains(term)) {
            return true;
        }
        else {
            return false;
        }
    }

    // split sgm files into text document files
    public static void splitIntoDocument() throws FileNotFoundException, UnsupportedEncodingException {

        // split into multiple documents
        int doc_ID = 1;
        Scanner scan = new Scanner(new File("reu_docs.txt"));
        scan.useDelimiter(Pattern.compile("</REUTERS>"));
        while (scan.hasNext()) {
            String logicalLine = scan.next();
            PrintWriter writer = new PrintWriter("docs/" + doc_ID + "doc.txt");
            writer.print(logicalLine);
            writer.close();
            doc_ID++;
        }
    }

    // tokenize
    public static void splitDocIntoTokens() throws FileNotFoundException {
        System.out.println("splitDocIntoToken started...");
        File dir = new File("docs");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // data structure to store token pairs of each document
            int count = 0;
            int doc_id;
            Stack tokenCollection = new Stack();
            // go through each document
            for (File child : directoryListing) {
                if (count <= directoryListing.length) {
                    // read from each document
                    Scanner scan2 = new Scanner(new File("docs/" + child.getName()));
                    String tempTerm = "";
                    while (scan2.hasNext()) {
                        String term = scan2.next();
                        // apply case folding -remove special characters and make sting into lower case and apply number removal
                        String updated_term = caseFolding_numberRemoval(term);
                        if (!updated_term.equals("") || !updated_term.equals(null)) {
                            // apply stop word removal
                            if (stopWord(updated_term) == true) {
                                continue;
                            } else {
                                // remove same word back to back
                                if (tempTerm.equals(updated_term)) {
                                    continue;
                                } else {
                                    // retrieve docID
                                    doc_id = Integer.parseInt(directoryListing[count].toString().replaceAll("[^0-9]", ""));
                                    // add token pair to stack
                                    if (updated_term != ""){
                                        tokenCollection.push(updated_term + ":" + doc_id);
                                    }
                                }
                                tempTerm = updated_term;
                            }
                        }
                    }
                    count++;
                }
            }
            spmi(tokenCollection);
        }
    }

    // SPMI algorithm
    public static void spmi(Stack token_stream) throws FileNotFoundException {
        System.out.println("spmi started...");
        // dictionary / posting list
        Map<String, List<Integer>> block = new HashMap<String, List<Integer>>();
        int count = 0;
        // counter for max number of token in a single block
        int blockSize = 0;
        // create a block output file
        PrintWriter output_file = new PrintWriter("Blocks/" +count +"block.txt");
        // run until we have tokens in our stack
        while (!token_stream.empty()) {
            if (blockSize != 500000) {
                Object token = token_stream.pop();
                String pairToken[] = token.toString().split(":");
                String term = pairToken[0];
                int docID = Integer.parseInt(pairToken[1]);

                // if term is not in the current dictionary
                if (!(block.containsKey(term))) {
                    List<Integer> postingList = new ArrayList<>();
                    postingList.add(docID);
                    block.put(term, postingList);
                }
                // if term is already in the current dictionary only add it to the term's posting list
                else {
                    List<Integer> existingTermPostingList = block.get(term);
                    if (!existingTermPostingList.contains(docID)) {
                        existingTermPostingList.add(docID);
                    }
                }
                blockSize++;
                // dictionary is not full but token stream is empty
                if (token_stream.empty()) {
                    //sort
                    Map<String, List<Integer>> sortedBlock = new TreeMap<String, List<Integer>>(block);
                    Set<String> keys = sortedBlock.keySet();
                    for (String mapToken : keys){
                        List<Integer> sortedPostingList = sortedBlock.get(mapToken);
                        Collections.sort(sortedPostingList);
                        output_file.println(mapToken + ":" + sortedPostingList);
                    }
                }
            }
            // dictionary is full
            else {
                //sort
                Map<String, List<Integer>> sortedBlock = new TreeMap<String, List<Integer>>(block);
                Set<String> keys = sortedBlock.keySet();
                for (String mapToken : keys){
                    List<Integer> sortedPostingList = sortedBlock.get(mapToken);
                    Collections.sort(sortedPostingList);
                    output_file.println(mapToken + ":" + sortedPostingList);
                }
                output_file.close();
                count++;
                output_file = new PrintWriter("Blocks/" +count +"block.txt");
                block = new HashMap<String, List<Integer>>();
                blockSize = 0;
                count++;
            }
        }
        output_file.close();
    }

    // handles merging of blocks to produce an inverted index
    public static void mergeBlocks() throws IOException, FileNotFoundException{
        System.out.println("mergeBlocks started...");
        // will store the final inverted index
        Map<String, List<Integer>> finalBlock = new HashMap<String, List<Integer>>();
        // will store the current documents tokens
        Map<String, List<Integer>> currentBlock = new HashMap<String, List<Integer>>();
        // go through Blocks directory
        File dir = new File("Blocks");
        File[] directoryListing = dir.listFiles();
        int count = 0;
        if (directoryListing != null) {
            // go through each document
            for (File child : directoryListing) {
                // read from each document
                BufferedReader scan3 = new BufferedReader(new FileReader("Blocks/" + child.getName()));
                String line;
                while ((line = scan3.readLine()) != null) {
                    String pairToken[] = line.split(":");
                    String term = pairToken[0];
                    String documentPostingList[] = pairToken[1].split(",");
                    int docID;
                    List<Integer> finalPostingList = new ArrayList<>();
                    List<Integer> currentPostingList = new ArrayList<>();
                    for (int i = 0; i < documentPostingList.length; i++) {
                        docID = Integer.parseInt(documentPostingList[i].replace("[", "").replace("]", "").replace(" ", ""));
                        // map the first block data
                        if (count == 0) {
                            finalPostingList.add(docID);
                            finalBlock.put(term, finalPostingList);
                        }
                        // traversing through blocks map
                        else {
                            currentPostingList.add(docID);
                            currentBlock.put(term, currentPostingList);
                        }
                    }
                }
                // start the actual merge - go through currentBlock and add terms into the finalBlock appropriately
                for (String key : currentBlock.keySet()) {
                    // if finalBlock does not have the term, create term and add its posting list
                    if (!finalBlock.containsKey(key)) {
                        List<Integer> postingList = new ArrayList<>();
                        List<Integer> currentNewTermPostingList = currentBlock.get(key);
                        for (int i = 0; i < currentNewTermPostingList.size(); i++) {
                            postingList.add(currentNewTermPostingList.get(i));
                        }
                        finalBlock.put(key,postingList);
                    }
                    // already in the dictionary, add only to posting list of the term
                    else {
                        List<Integer> finalExistingTermPostingList = finalBlock.get(key);
                        List<Integer> currentExistingTermPostingList = currentBlock.get(key);
                        // merge existing posting list
                        for (int i = 0; i < currentExistingTermPostingList.size(); i++) {
                            if (!finalExistingTermPostingList.contains(currentExistingTermPostingList.get(i))) {
                                finalExistingTermPostingList.add(currentExistingTermPostingList.get(i));
                            }
                        }
                    }
                }
                count++;
            }
        }
        // write inverted index to disk
        writeToDiskInvertedIndex(finalBlock);
    }

    // handles writing inverted index to disk
    public static void writeToDiskInvertedIndex( Map<String, List<Integer>> invertedIndex) throws FileNotFoundException{
        // write our final index to text file
        PrintWriter output_file = new PrintWriter("FinalBlock/FinalIndexBlock.txt");
        Map<String, List<Integer>> sortedBlock = new TreeMap<String, List<Integer>>(invertedIndex);
        int numOfTerm = 0;
        int numOfPostings = 0;
        for (String key : sortedBlock.keySet()) {
            List<Integer> finalPostingList = sortedBlock.get(key);
            Collections.sort(finalPostingList);
            output_file.println(key + ":" + finalPostingList);
            numOfTerm++;
            numOfPostings += finalPostingList.size();
        }
        output_file.close();
//        System.out.println(numOfTerm + " " + numOfPostings);
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
        System.out.println("Enter -> 1 for single term query");
        System.out.println("Enter -> 2 for multiple term AND query");
        System.out.println("Enter -> 3 for multiple term OR query");
        System.out.println("Enter -> 4 to terminate");
        System.out.println("---------------------------------------------------");

        String selection = scanner_input.nextLine();

        if (!selection.equals("1") && !selection.equals("2") && !selection.equals("3") && !selection.equals("4")) {
            System.out.println("Invalid selection");
            querySearch();
        }

        // end program
        else if (selection.equals("4")) {
            System.out.println("Exiting...");
            System.exit(1);
        }
        else {
            // run search
            System.out.println("---------------------------------------------------");
            System.out.print("Please enter your query: ");
            String input = scanner_input.nextLine().toLowerCase();
            String inputCheckLength[] = input.split(" ");
            // single query term - should return all the docID's where this term occurs
            if (selection.equals("1") && inputCheckLength.length == 1) {
                singleQuery(input, invertedIndex);
            }
            // multiple AND query terms
            else if (selection.equals("2")) {
                multipleAndQuery(input, invertedIndex);
            } else if (selection.equals("3")) {
                multipleOrQuery(input, invertedIndex);
            } else {
                System.out.println("Invalid selection, please try again");
                querySearch();
            }
        }
    }

    // handles single query search
    public static  void singleQuery(String input, Map<String, List<Integer>> invertedIndex) throws IOException {
        // query
        String singleQuery = input;
        if (invertedIndex.containsKey(singleQuery)) {
            List<Integer> singleQueryPostingList = invertedIndex.get(singleQuery);
            // prints posting list
            System.out.println(singleQuery + " " + singleQueryPostingList);
            querySearch();
        }
        else{
            System.out.println("No match found");
            querySearch();
        }
    }

    // handles multiple query search - AND
    public static void multipleAndQuery(String input, Map<String, List<Integer>> invertedIndex) throws IOException {
        // AND query terms - should return docID's where both terms occur in the same document
        String andQuery = input;
        // split each query term
        String queryWordsCollection[] = andQuery.split(" ");
        // will store all the postings lists of the query terms
        List<List> postingListCollection = new ArrayList<>();
        // check if we have postings list for all the query terms
        Boolean hasMultipleQuery = false;
        Stack<String> queryStatus = new Stack<>();
        for (int i = 0; i < queryWordsCollection.length; i++) {
            postingListCollection.add(invertedIndex.get(queryWordsCollection[i]));
            if (invertedIndex.containsKey(queryWordsCollection[i])) {
                queryStatus.push("true");
            }
        }
        if (queryStatus.size() == queryWordsCollection.length) {
            hasMultipleQuery = true;
        }
        // if all query terms have a posting list
        if (hasMultipleQuery) {
            List<Integer> finalPostingList = new ArrayList<>();
            int count = 0;
            // build individual posting lists for intersection
            for (List<Integer> docIDS : postingListCollection) {
                List<Integer> comparingList = new ArrayList<>();
                for (int id : docIDS) {
                    // initialize final posting list
                    if (count == 0) {
                        finalPostingList.add(id);
                    }
                    // initialize comparing posting list
                    else {
                        comparingList.add(id);
                    }
                }
                // intersect
                if (count >= 1) {
                    finalPostingList.retainAll(comparingList);
                }
                count++;
            }
            if (finalPostingList.isEmpty()) {
                System.out.println("No match found");
                querySearch();
            }
            else {
                // print AND result
                System.out.println(andQuery + " " + finalPostingList);
                querySearch();
            }
        }
        // no postings found for parsed query
        else {
            System.out.println("No match found");
            querySearch();
        }
    }

    // handles multiple query search - OR
    public static void multipleOrQuery(String input, Map<String, List<Integer>> invertedIndex) throws IOException {
        // OR query terms - should return document frequency of query term
        String orQuery = input;
        // split each query term
        String queryWordsCollection[] = orQuery.split(" ");
        // will store all the postings lists of the query terms
        List<List> postingListCollection = new ArrayList<>();
        // check if we have postings list for all the query terms
        Stack<String> queryStatus = new Stack<>();
        for (int i = 0; i < queryWordsCollection.length; i++) {
            postingListCollection.add(invertedIndex.get(queryWordsCollection[i]));
            if (invertedIndex.containsKey(queryWordsCollection[i])) {
                queryStatus.push("true");
            }
        }
        // no postings found for parsed query
        if (postingListCollection.isEmpty()) {
            System.out.println("No match found");
            querySearch();
        }
        else {
            List<Integer> allPostings = new ArrayList<>();
            for (List<Integer> docIDS : postingListCollection) {
                for (int id : docIDS) {
                    allPostings.add(id);
                }
            }
            int defaultFrequency = 1;
            HashMap<Integer, Integer> queryFrequency = new HashMap<Integer, Integer>();
            for (int docID : allPostings) {
                if (!queryFrequency.containsKey(docID)) {
                    queryFrequency.put(docID, defaultFrequency);
                } else {
                    int currentFrequency = queryFrequency.get(docID);
                    queryFrequency.put(docID, currentFrequency + 1);
                }
            }
            Object[] a = queryFrequency.entrySet().toArray();
            Arrays.sort(a, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
                            ((Map.Entry<String, Integer>) o1).getValue());
                }
            });
            for (Object e : a) {
                System.out.println(((Map.Entry<Integer, Integer>) e).getKey() + " : "
                        + ((Map.Entry<String, Integer>) e).getValue());
            }
            querySearch();
        }
    }

    // Driver
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
//        splitIntoDocument();
//        splitDocIntoTokens();
//        mergeBlocks();
        querySearch();
    }
}