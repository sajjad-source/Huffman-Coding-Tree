import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Sajjad C Kareem
 * @author Josue Godeme
 *
 */
public class HuffmanEncoding implements Huffman
{

    Map<Character, String> codeMap = new HashMap<>(); //the codeMap that holds the character and its bit path in 1 and 0

    /**
     * Read file provided in pathName and count how many times each character appears
     *
     * @param pathName - path to a file to read
     * @return - Map with a character as a key and the number of times the character appears in the file as value
     * @throws IOException
     */
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {

        HashMap<Character,Long> frequencyMap = new HashMap<>(); //map that has character as key frequency of it as value
        BufferedReader input = new BufferedReader(new FileReader(pathName)); //read into the file

        int charNum; //character number in the file
        char character; //the character itself in the value

        try { //try to read input
            while((charNum = input.read()) != -1) //while there is still a character to read
            {
                character = (char) charNum; //get the character

                if (frequencyMap.containsKey(character)) //if the character is in the map
                {
                    frequencyMap.put(character, frequencyMap.get(character) + 1); //then just increment its frequency by 1
                } else { //else if not in map
                    frequencyMap.put(character, (long) 1); //then insert the character in map with value of 1
                }
            }
        }
        catch(Exception e) //exception catch
        {
            System.out.println("Couldn't read the file: " + e);
        }
        finally {
            return frequencyMap; //finally, return the frequencyMap
        }

    }



    /**
     * Construct a code tree from a map of frequency counts. Note: this code should handle the special
     * cases of empty files or files with a single character.
     *
     * @param frequencies a map of Characters with their frequency counts from countFrequencies
     * @return the code tree.
     */

    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        class TreeComparator implements Comparator<BinaryTree<CodeTreeElement>>
        {
            //comparator method comparing the frequency counts in the root nodes of two trees
            public int compare(BinaryTree<CodeTreeElement> N1, BinaryTree<CodeTreeElement> N2) { //takes into two nodes
                if (N1.getData().getFrequency() < N2.getData().getFrequency()) //if the frequency of N1 smaller than N2
                    return -1; //return -1
                else if (N1.getData().getFrequency() == N2.getData().getFrequency()) //if equal frequency
                    return 0; //return 0
                else //if N1 greater frequency
                    return 1; //return 1
            }
        }

        Comparator<BinaryTree<CodeTreeElement>> freqCompare = new TreeComparator(); //instantiate the comparator object

        //create a priority queue to hold trees and pass it the comparator
        PriorityQueue<BinaryTree<CodeTreeElement>> priorityQueue = new PriorityQueue<>(freqCompare);

        for (Map.Entry<Character, Long> map : frequencies.entrySet()) //iterate through the map
        {
            //create a CodeTreeElement: param1 = frequency (value of map) param2 = character (key of map)
            CodeTreeElement codeTreeElement = new CodeTreeElement(map.getValue(), map.getKey());

            BinaryTree<CodeTreeElement> initialTree = new BinaryTree<>(codeTreeElement); //create an initial tree for each character with data as codeTreeElement

            priorityQueue.add(initialTree); //add all the initial trees to the priority queue
        }

        while (priorityQueue.size() > 1) //while there is more than tree object left in queue
        {
            BinaryTree<CodeTreeElement> T1 = priorityQueue.remove(); //extract the first tree
            BinaryTree<CodeTreeElement> T2 = priorityQueue.remove(); //extract the second tree

            long frequencySum = T1.getData().getFrequency() + T2.getData().getFrequency(); //get the sum of frequencies of both

            //create a new tree with T1 as left, T2 as right, and its data being the sum of the frequencies
            BinaryTree<CodeTreeElement> T = new BinaryTree<>(new CodeTreeElement(frequencySum, null), T1, T2);
            priorityQueue.add(T); //add the new tree to the priorityQueue
        }

        return priorityQueue.remove(); //return the final tree remaining in the queue
    }


    /**
     * Helper method for computing codeMap
     * @param codeTree
     * @param path
     * @return codeMap
     */
    public Map<Character, String> computeCodesHelper(BinaryTree<CodeTreeElement> codeTree, String path)
    {

        if (codeTree.isLeaf()) //if it is a leaf node
        {
            codeMap.put(codeTree.data.getChar(), path); //put the character in the map as the key and its value as the path in bits
        }else{ //else if not leaf node
            if (codeTree.hasLeft()) //if it has a left
            {
                computeCodesHelper(codeTree.getLeft(), path + "0"); //recurse with the left and add '0' to the path
            }
            if (codeTree.hasRight()) //if it has a right
            {
                computeCodesHelper(codeTree.getRight(), path + "1"); //recurse with the right and add '1' to the path
            }
        }

        return codeMap; //return the code map path
    }

    /**
     * Computes the code for all characters in the tree and enters them
     * into a map where the key is a character and the value is the code of 1's and 0's representing
     * that character.
     *
     * @param codeTree the tree for encoding characters produced by makeCodeTree
     * @return the map from characters to codes
     */
    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree)
    {
        String path = ""; //instantiate an empty path
        return computeCodesHelper(codeTree, path); //call the helper and return the code map
    }


    /**
     * Compress the file pathName and store compressed representation in compressedPathName.
     *
     * @param codeMap            - Map of characters to codes produced by computeCodes
     * @param pathName           - File to compress
     * @param compressedPathName - Store the compressed data in this file
     * @throws IOException
     */

    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {

        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName); //bit writer
        BufferedReader input = new BufferedReader(new FileReader(pathName)); //read into file

        int charNum; //char num in the file
        char character; //the character itself

        try //try reading the file
        {
            while((charNum = input.read()) != -1) //while there is still a character to read
            {
                character = (char) charNum; //get the character
                String code = codeMap.get(character); //get the path of the character in bits

                for (int i = 0; i < code.length(); i++) //for each bit in the path
                {

                    if (code.charAt(i) == '0') //if the bit is 0 (left)
                    {
                        bitOutput.writeBit(false); //then write a false bit to the compression
                    } else { //else if the bit is 1 (right)
                        bitOutput.writeBit(true); //then write a true bit to the compression
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error reading the file: " + e);
        }
        finally { //finally
            bitOutput.close(); //close the bit writer
            input.close(); //close the file reader
        }

    }

    /**
     * Decompress file compressedPathName and store plain text in decompressedPathName.
     *
     * @param compressedPathName   - file created by compressFile
     * @param decompressedPathName - store the decompressed text in this file, contents should match the original file before compressFile
     * @param codeTree             - Tree mapping compressed data to characters
     * @throws IOException
     */

    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {

        BufferedBitReader bitInput = new BufferedBitReader(compressedPathName); //bit reader
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName)); //file writer

        BinaryTree<CodeTreeElement> root = codeTree; //make a copy of the codeTree

        try
        {
            while (bitInput.hasNext()) //while there is a bit to read
            {
                boolean bit = bitInput.readBit(); //get the bit (true or false)

                if (!bit) //if false ('0')
                {
                    codeTree = codeTree.getLeft(); //then go left
                } else { //else if true ('1')
                    codeTree = codeTree.getRight(); //then go right
                }
                if (codeTree.isLeaf()) //if it is a leaf
                {
                    output.write(codeTree.getData().getChar()); //then write out the character
                    codeTree = root; //go back to the root

                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception caught: " + e);
        }
        finally {
            bitInput.close(); //close the bit read
            output.close(); //close the file write
        }

    }

    public static void main(String[] args) throws IOException
    {

        try
        {
            HuffmanEncoding test = new HuffmanEncoding();
            Map<Character, Long> map = test.countFrequencies("inputs/USConstitution.txt");
            BinaryTree<CodeTreeElement> tree = test.makeCodeTree(map);
            Map<Character, String> codeMap = test.computeCodes(tree);

            test.compressFile(codeMap,"inputs/USConstitution.txt", "inputs/compressed");
            test.decompressFile("inputs/compressed","inputs/decompressed", tree);
        }
        catch (Exception e)
        {
            System.out.println("Exception caught: " + e);
        }

    }
}
