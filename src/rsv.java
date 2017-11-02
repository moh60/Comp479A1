import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Stack;

public class rsv {
    int numOfDocsInCorpus = 21578;
    double avgDocLengthCorpus = 154;
    double k1 = 0.9;
    double b = 0.1;
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

    // return length of a document by docID
    public long getLengthOfDoc(int docID) throws IOException {
        Scanner scanner = new Scanner(new File("docLength.txt"));
        long docLength = 0;
        while (scanner.hasNext()) {
            String docLine = scanner.next();
            String docPair[]= docLine.split(":");
            if (docID == Integer.parseInt(docPair[0])) {
                docLength = Long.parseLong(docPair[1]);
                break;
            }
        }
        return docLength;
    }

    // return document frequency of a term
    public long getDocFrequencyOfTerm(String term) throws IOException {
        Scanner scanner = new Scanner(new File("PostingsList\\0postingsListBlock.txt"));
        long docFrequency = 0;
        while (scanner.hasNext()) {
            String docLine = scanner.nextLine();
            String pairToken[] = docLine.split(":");
            String docTerm = pairToken[0];
            if (term.equals(docTerm)) {
                String documentPostingList[] = pairToken[1].split(",");
                docFrequency = documentPostingList.length;
                break;
            }
        }
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
        System.out.println("df " + docFrequency);
        System.out.println("tf " + termFrequency);
        System.out.println("Ld" + docLength);
        System.out.println("L" + avgDocLengthCorpus);
        System.out.println("N " + numOfDocsInCorpus);
        System.out.println("k1 " + k1);
        System.out.println("b " + b);

        double NmodDF = Math.log(numOfDocsInCorpus/docFrequency);
        System.out.println("NmodDF " + NmodDF);

        double top = (k1+1)*termFrequency;
        double bottom = (k1 * (1-b) + b * (docLength/avgDocLengthCorpus)) + termFrequency;
        double topBottom = top/bottom;
        System.out.println("top : " + top + " bottom: " + bottom);
        double finalResult = NmodDF * topBottom;
        System.out.println("Final " + finalResult);

        rsv = Math.log(numOfDocsInCorpus/docFrequency) * (((k1+1)*termFrequency)/((k1*(1-b)+b*(docLength/avgDocLengthCorpus))+termFrequency));
        System.out.println(rsv);
        return rsv;
    }

    // Driver
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
//        calculateAverageDocLength();
    }
}
