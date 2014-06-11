package huffman;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Takes an ascii file and encodes into a compact file
 * @author Garrett
 */
public class Encode {
    public static Map<Character, String> bitCodesMap;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String source = args[0];
        String target = args[1];
//        String source = "sample5.txt";
//        String target = "TestMe.huf";
        
        System.out.println("=======================================================");
        System.out.println("================== Reading the File ===================");
        System.out.println("=======================================================");
        
        Map<Character, Integer> charFreqMap = Utilities.readCharFreq(source);
        System.out.println("- File Read");
        
        System.out.println("");
        System.out.println("=======================================================");
        System.out.println("============== Creating the Huffman Tree ==============");
        System.out.println("=======================================================");
        
        // Put map into Arraylist for sorting
        ArrayList<Entry<Character, Integer>> charFreqArray = new ArrayList<>(charFreqMap.entrySet());
        
        // Sort the pairs into increasing & lexicographical order
        Collections.sort(charFreqArray, new Comparator<Entry<Character, Integer>>() {
            public int compare (Entry<Character, Integer> obj1, Entry<Character, Integer> obj2) {
                // First: Sort by lower frequency
                if (obj1.getValue() - obj2.getValue() != 0)
                    return ((int)obj1.getValue() - (int)obj2.getValue());
                // Second: Sort by smaller ASCII
                else
                    return obj1.getKey() - obj2.getKey();
            }
        });
        
        // Create the leaves of the encoding tree
        ArrayList<Node> currNodes = new ArrayList<>();
        for (Entry<Character, Integer> charFreq : charFreqArray) {
            Node node = new Node(charFreq.getKey(), charFreq.getValue());
            currNodes.add(node);
        }
        
        // Combine the two least frequently used nodes to make a parent
        while (currNodes.size() > 1) {
            // Swap two least nodes with a combined node
            Node node = new Node(currNodes.get(0), currNodes.get(1));
            currNodes.remove(0);
            currNodes.remove(0);
            currNodes.add(0, node);
            
            // Sort lowest frequency first
            
            Collections.sort(currNodes, new Comparator<Node>() {
                public int compare (Node obj1, Node obj2) {
                    return obj1.count - obj2.count;
                }
            });
        }
        System.out.println("- Huffman Tree Created");
        
        System.out.println("");
        System.out.println("=======================================================");
        System.out.println("================ Creating the BitCodes ================");
        System.out.println("=======================================================");
        
        bitCodesMap = new HashMap<>();
        iterateTree(currNodes.get(0), "");
        System.out.println("- BitCodes Created: " + bitCodesMap);
        
        System.out.println("");
        System.out.println("=======================================================");
        System.out.println("=============== Canonicalizing the Code ===============");
        System.out.println("=======================================================");
        
        ArrayList<Entry<Character, String>> bitCodesArray = new ArrayList<>(bitCodesMap.entrySet());
        
        // Sort it
        Collections.sort(bitCodesArray, new Comparator<Entry<Character, String>>() {
            public int compare (Entry<Character, String> obj1, Entry<Character, String> obj2) {
                // First: Sort by lower bit length
                if (obj1.getValue().length() - obj2.getValue().length() != 0)
                    return ((int)obj1.getValue().length() - (int)obj2.getValue().length());
                // Second: Sort by smaller ASCII
                else
                    return obj1.getKey() - obj2.getKey();
            }
        });
        
        int code = 0;
        for (int i = 0; i < bitCodesArray.size(); i++) {
            String codeString = Integer.toBinaryString(code);
            while (codeString.length() < bitCodesArray.get(i).getValue().length()) {
                codeString = "0" + codeString;
            }
            bitCodesMap.put(bitCodesArray.get(i).getKey(), codeString);
            if (i + 1 < bitCodesArray.size())
                code = (code + 1) << (bitCodesArray.get(i + 1).getValue().length() - bitCodesArray.get(i).getValue().length());
            else
                code = code + 1;
        }
        System.out.println("- BitCodes Canonicalized: " + bitCodesMap);
        
        System.out.println("");
        System.out.println("=======================================================");
        System.out.println("=================== Writing To File ===================");
        System.out.println("=======================================================");
        System.out.println("  1: Turning the Code into bytes...");
        ArrayList<Byte> writeBytes = new ArrayList<>();
        
        // WRITE ADD: The number of characters used into the code
        writeBytes.add(new Byte((byte) bitCodesMap.size()));
        
        // Sort byteCodesArray
        ArrayList<Entry<Character, String>> byteCodes = new ArrayList<>(bitCodesMap.entrySet());
        Collections.sort(byteCodes, new Comparator<Entry<Character, String>>() {
            public int compare (Entry<Character, String> obj1, Entry<Character, String> obj2) {
                return ((int)obj1.getKey() - (int)obj2.getKey());
            }
        });
        
        // WRITE ADD: The ASCII bitLength pair for each character
        for (Entry<Character, String> byteCode : byteCodes) {
            writeBytes.add(new Byte((byte) (int) byteCode.getKey()));
            writeBytes.add(new Byte((byte) (int) byteCode.getValue().length()));
        }
        
        System.out.println("  2: Turning file into bitString...");
        // Read the file line-by-line and turn it into into a giant bytestring
        String fileLine;
        String bitQueue = "";
        boolean firstIter = true;
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            while ((fileLine = br.readLine()) != null) {
                // Modify the file to have new lines where there should be
                if (firstIter)
                    firstIter = false;
                else
                    fileLine = "\n" + fileLine;
                // For each char in this fileLine, add char bits to queue
                for (char ch : fileLine.toCharArray()) {
                        bitQueue += bitCodesMap.get(ch);
                }
                // If you have 8 bits, change to a byte and remove
                while (bitQueue.length() >= 8) {
                    String s = bitQueue.substring(0, 8);
                    int value = Integer.parseInt(s, 2);
                    bitQueue = bitQueue.substring(8);
                    writeBytes.add(new Byte((byte)value));
                }
            }
        } catch (Exception e) {
            System.out.println("Z: Error ~ " + e);
        }
        bitQueue += bitCodesMap.get('\u0000');
        // If you have 8 bits, change to a byte and remove
        if (bitQueue.length() >= 8) {
            String s = bitQueue.substring(0, 8);
            int value = Integer.parseInt(s, 2);
            bitQueue = bitQueue.substring(8);
            writeBytes.add(new Byte((byte)value));
        }
        if (!bitQueue.isEmpty()) {
            while (bitQueue.length() < 8) {
                bitQueue += "0";
            }
            int value = Integer.parseInt(bitQueue, 2);
            writeBytes.add(new Byte((byte)value));
        }
        
        System.out.println("  3: Writing to file...");
        // Write to target file
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(target));
            for (byte encodedByte : writeBytes) {
                dos.writeByte(encodedByte);
            } 
        } catch (EOFException e) {}

        System.out.println("File '" + source + "' encoded to '" + target + "'. ");
    }
    
    /** Iterates through the huffman tree and writes the bit code to the bitCodesMap */
    public static void iterateTree(Node node, String bitString) {
        if (node.isLeaf()) {
            bitCodesMap.put(node.code, bitString);
        } else {
            iterateTree(node.leftChild, bitString + "0");
            iterateTree(node.rightChild, bitString + "1");
        }
    }
}