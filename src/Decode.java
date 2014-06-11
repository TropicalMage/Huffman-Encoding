package huffman;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Takes an encoded file and decodes it into an output file (ascii only)
 * @author Garrett
 */
public class Decode {
    static byte MAX_BIT_LENGTH = 0;
    static byte numChars; // Number of unique chars used

    public static void main(String[] args) {
        String source = args[0];
        String target = args[1];
//        String source = "TestMe.huf";
//        String target = "TestMe.txt";
        
        System.out.println("=======================================================");
        System.out.println("================== Grabbing the Code ==================");
        System.out.println("=======================================================");
        
        Map<Character, Byte>  charCodeMap = grabCode(source);
        System.out.println("- Code retreived: " + charCodeMap);
        
        System.out.println();
        System.out.println("=======================================================");
        System.out.println("======== Reforming the Depth Code to a BitCode ========");
        System.out.println("=======================================================");
        Map<Character, String>  bitCodeMap = new HashMap<>();
        
        // Put map into Arraylist for sorting
        ArrayList<Map.Entry<Character, Byte>> charCodeArray = new ArrayList<>(charCodeMap.entrySet());
        
        // Sort the pairs into increasing & lexicographical order
        Collections.sort(charCodeArray, new Comparator<Map.Entry<Character, Byte>>() {
            public int compare (Map.Entry<Character, Byte> obj1, Map.Entry<Character, Byte> obj2) {
                // First: Sort by smaller bitLength
                if (obj1.getValue() - obj2.getValue() != 0)
                    return ((int)obj1.getValue() - (int)obj2.getValue());
                // Second: Sort by smaller ASCII
                else
                    return obj1.getKey() - obj2.getKey();
                }
        });
        
        // Canonicalize the Code
        int code = 0;
        for (int i = 0; i < charCodeArray.size(); i++) {
            String codeString = Integer.toBinaryString(code);
            while (codeString.length() < charCodeArray.get(i).getValue().intValue()) {
                codeString = "0" + codeString;
            }
            bitCodeMap.put(charCodeArray.get(i).getKey(), codeString);
            if (i + 1 < charCodeArray.size())
                code = (code + 1) << (charCodeArray.get(i + 1).getValue().intValue() - charCodeArray.get(i).getValue().intValue());
            else
                code = code + 1;
        }
        System.out.println("- BitCode Created: " + bitCodeMap);
        
        System.out.println();
        System.out.println("=======================================================");
        System.out.println("=================== Writing to File ===================");
        System.out.println("=======================================================");
        
        String bitQueue = "";
        
        // Read from the byteFile and write at the same time to prevent large buildups of data
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(target));) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(source))) {
                boolean finished = false;
                
                // Skip past the metaData
                Byte characterCount = dis.readByte();
                for (int i = 0; i < characterCount.intValue(); i++) {
                    dis.readByte();
                    dis.readByte();
                }
                
                // Read the data
                while (!finished) {
                    // If you're unsure if you can add an ascii char, add another byte into the queue
                    while (bitQueue.length() < MAX_BIT_LENGTH) {
                        byte echo = dis.readByte();
                        String bitString = String.format("%8s", Integer.toBinaryString(echo & 0xFF)).replace(' ', '0');
                        bitString = padLeft(bitString, 8);
                        bitQueue += bitString;
                    }
                    
                    // Find an ascii code representation
                    Character foundCode = null;
                    while (foundCode == null) {
                        String s = "";
                        for (int i = 0; i < MAX_BIT_LENGTH; i++) {
                            s += bitQueue.charAt(i);
                            // If you have the bitCode as a value...
                            if (bitCodeMap.containsValue(s)) {
                                for (Character c : bitCodeMap.keySet()) {
                                    // Find the ascii representation for said bitCode
                                    if (bitCodeMap.get(c).equals(s)) {
                                        foundCode = c;
                                    }
                                }
                                bitQueue = bitQueue.substring(s.length());
                                break;
                            }
                        }
                    }

                    // Writing the ascii character to the file
                    if (foundCode.equals('\u0000')) {
                        finished = true;
                    } else {
                        dos.writeByte(foundCode);
                    }
                }
            } catch (EOFException e) {
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("- File '" + source + "' successfully decoded to '" + target + "'. ");
        } catch (EOFException e) {
            // end of file
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    
    

    /** Reads the file and stores the Code as a Map  */
    private static Map<Character, Byte> grabCode(String source) {
        Map<Character, Byte> charCodeMap = new HashMap<>();

        try (DataInputStream dis = new DataInputStream(new FileInputStream(source))) {
            // First Byte: Number of Chars Used
            numChars = dis.readByte();
            for (byte i = 0; i < numChars; i++) {
                char asciiCode = (char) dis.readByte();
                byte bitLength = dis.readByte();
                if (bitLength > MAX_BIT_LENGTH) {
                    MAX_BIT_LENGTH = bitLength;
                }
                charCodeMap.put(asciiCode, bitLength);
            }
        } catch (Exception e) {
                System.out.println("Error ~ " + e);
        }
        return charCodeMap;
    }

    public static String padLeft(String original, int length) {
        String result = original;
        while (result.length() < length) {
            result = "0" + result;
        }
        return result;
    }
}
