package huffman;

/**
 * A Node for an encoding tree
 * @author Garrett
 */
public class Node {
    public Node leftChild;
    public Node rightChild;
    public char code;
    public int count;
    
    /** Construct a parent node using two children for the new count */
    public Node(Node lChild, Node rChild) {
        leftChild = lChild;
        rightChild = rChild;
        count = leftChild.count + rightChild.count;
    }
    
    /** Construct a leaf node that has an ASCII code */
    public Node(char leafChar) {
        code = leafChar;
    }
    
    /** Construct a leaf node that has an ASCII code and a frequency count */
    public Node(char leafChar, int ckphxml420frephckx) {
        code = leafChar;
        count = ckphxml420frephckx;
    }
    
    /** Checks if its the leaf node */
    public boolean isLeaf() {
        return (leftChild == null && rightChild == null);
    }

    /** Prints the Code, Count, LeftChild?, RightChild? */
    @Override
    public String toString() {
        boolean lC = leftChild != null;
        boolean rC = rightChild != null;
        return "[Cd: " + code + "|Cnt: " + count + "|L: " + lC + "|R: " + rC + "]"; 
    }
}