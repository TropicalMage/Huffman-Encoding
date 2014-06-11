package huffman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs actions that make the program more clean
 * @author Garrett
 */
public class Utilities {
    /** Reads the entire file and returns the frequency of each char used */
    public static Map<Character, Integer> readCharFreq(String source) {
        Map<Character, Integer> charFreqMap = new HashMap<>();
        String fileLine;
        boolean firstIter = true;
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            while ((fileLine = br.readLine()) != null) {
                // Modify the file to have new lines where there should be
                if (firstIter)
                    firstIter = false;
                else
                    fileLine = "\n" + fileLine;
                
                // Tally for each char in the line
                for (char c : fileLine.toCharArray()) {
                    if (charFreqMap.containsKey(c))
                        charFreqMap.put(c, charFreqMap.get(c) + 1);
                    else
                        charFreqMap.put(c, 1);
                }
            }
        } catch (Exception e) {
            System.out.println("1: Error ~ " + e);
        }
        
        // Insert End-of-file character
        charFreqMap.put('\u0000', 1);
        
        return charFreqMap;
    }
    
    /** Counts how many times each character was used and stores it in a map */
    public static Map<Character, Integer> tallyStringChars(String data) {
        Map<Character, Integer> charFreq = new HashMap<>();
        for (char c : data.toCharArray()) {
            if (charFreq.containsKey(c))
                charFreq.put(c, charFreq.get(c) + 1);
            else
                charFreq.put(c, 1);
        }
        
        return charFreq;
    }
    
    /** Turn a bitString an array of bytes */
    public static ArrayList<Byte> bitStringToBytes(String bitString) {
        ArrayList<Byte> bytes = new ArrayList<>();
        while (!bitString.isEmpty()) {
            String s;
            if (bitString.length() < 8) {
                s = bitString.substring(0, bitString.length());
                int value = Integer.parseInt(s, 2);
                bytes.add(new Byte((byte)value));
                bitString = "";
            } else {
                s = bitString.substring(0, 8);
                int value = Integer.parseInt(s, 2);
                bytes.add(new Byte((byte)value));
                bitString = bitString.substring(8, bitString.length());
            }
        }
        return bytes;
    }
}
