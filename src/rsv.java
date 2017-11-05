import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class rsv {
    int numOfDocsInCorpus = 21578;
    double avgDocLengthCorpus = 154;
    double k1 = 0.5;
    double b = 0.5;
    double rsv;

    public rsv(){

    }
    // return average document length of entire corpus
    public static long calculateAverageDocLength() throws IOException {
        Scanner scanner = new Scanner(new File("docLength.txt"));
        Stack<String> docLengths = new Stack<>();
        int sumOfAllDocLengths = 0;
        int numOfDocuments = 0;
        long averageDocLength = 0;
        while (scanner.hasNext()) {
            String docLine = scanner.next();
            String docPair[]= docLine.split(":");
            docLengths.push(docPair[1]);
            sumOfAllDocLengths += Integer.parseInt(docPair[1]);
            numOfDocuments++;
        }
        averageDocLength = sumOfAllDocLengths/numOfDocuments;
        System.out.println(averageDocLength);
        return averageDocLength;
    }

    // generate a Map for doc id - doc length pair
    public static Map LengthOfDocMap() throws IOException {
        Scanner scanner = new Scanner(new File("docLength.txt"));
        Map<Integer,Integer> docsCollectionMap = new HashMap<>();
        while (scanner.hasNext()) {
            String docLine = scanner.next();
            String docPair[]= docLine.split(":");
            docsCollectionMap.put(Integer.parseInt(docPair[0]), Integer.parseInt(docPair[1]));
        }
        return docsCollectionMap;
    }
    // return length of a document by docID
    public static long getLengthOfDoc(int docID, Map docsCollectionMap) {
        long docLength = (Integer)docsCollectionMap.get(docID);
        return docLength;
    }

    // generates a Map for term and postings list
    public static Map docFrequencyOfTermMap() throws IOException {
        Scanner scanner = new Scanner(new File("PostingsList\\0postingsListBlock.txt"));
        Map<String,String> docsCollectionMap = new HashMap<>();
        while (scanner.hasNext()) {
            String docLine = scanner.nextLine();
            String pairToken[] = docLine.split(":");
            docsCollectionMap.put(pairToken[0],pairToken[1]);
        }
        return docsCollectionMap;
    }
    // return document frequency of a term
    public static long getDocFrequencyOfTerm(String term, Map docsCollectionMap) {
        Map<String,String> docCollection = docsCollectionMap;
        String documentPostingList[] = docCollection.get(term).split(",");
        long docFrequency = documentPostingList.length;
        return docFrequency;
    }

    // return term frequency in a document
    public long getTermFrequency(String term, int docID) throws IOException{
        // read from each document
        Scanner doc = new Scanner(new File("docs\\" + docID + "doc.txt"));
        long termFrequency = 0;
        String scanningTempTerm = "";
        while (doc.hasNext()) {
            String scanningTerm = doc.next();
            // apply case folding -remove special characters and make sting into lower case and apply number removal
            String scanningUpdatedTerm = extract.caseFolding_numberRemoval(scanningTerm);
            if (!scanningUpdatedTerm.equals("") || !scanningUpdatedTerm.equals(null)) {
                // remove same word back to back
                if (scanningTempTerm.equals(scanningUpdatedTerm)) {
                    continue;
                } else {
                    if (scanningUpdatedTerm.equals(term)){
                        termFrequency++;
                    }
                }
                scanningTempTerm = scanningUpdatedTerm;
            }
        }
        return termFrequency;
    }

    // calculate rsv using formula from chapter 11 pg 233
    public double calculateRSV(double docFrequency, double termFrequency, double docLength) {
        rsv = Math.log10(numOfDocsInCorpus/docFrequency) * (((k1+1)*termFrequency)/((k1*(1-b)+b*(docLength/avgDocLengthCorpus))+termFrequency));
        return rsv;
    }

    // Driver
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
//        calculateAverageDocLength(); return 154

//        Map m = docFrequencyOfTermMap();
//        getDocFrequencyOfTerm("workweek",m); return 3

//        Map l = LengthOfDocMap();
//        getLengthOfDoc(11651,l); //return 73

    }
}
