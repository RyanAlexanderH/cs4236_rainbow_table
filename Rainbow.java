import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.security.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Rainbow {
  public byte[][] testInputs;

  private HashMap<String, byte[]> table;  // <Ending word, Starting word>
  private MessageDigest SHA;  // Digest size 160 bits
  private static final int CHAIN_LENGTH = 210;
  private static final int RAINBOWTABLE_SIZE = 47500;
  private static final String RAINBOWTABLE_FILE = "Rainbow_Table.ser";

  // ------ Constructor ---------------------------------------------
  public Rainbow() {
    table = new HashMap<String, byte[]>();
    testInputs = new byte[RAINBOWTABLE_SIZE][20];
    try {
      SHA = MessageDigest.getInstance("SHA1");
    } catch (Exception e) {
      System.out.println("Exception " + e);
    }
    
    // generateTable();
    readTable();
  }

  // ------ SHA1 Hash Function --------------------------------------
  public byte[] hash(byte[] plaintext) {
    byte digest[] = new byte[20];
    try {
      digest = SHA.digest(plaintext);
      SHA.reset();
    } catch (Exception e) {
      System.out.println("Exception " + e);
    }
    return digest;
  }

  // ------ Reduce Function -----------------------------------------
  public byte[] reduce(byte[] digest, int i) {
    byte[] word = new byte[3];
    byte[] i_byte = intToBytes(i);
    word[0] = (byte)(digest[0] + i_byte[0]);
    word[1] = (byte)(digest[1] + i_byte[1]);
    word[2] = (byte)(digest[2] + i_byte[2]);
    return word;
  }

  // ------ Generate Table Function ---------------------------------
  private void generateTable() {
    System.out.println("----- GENERATING TABLE ---------------------------");
    byte[] wordStart, wordEnd;
    String key;
    int success = 0, collisions = 0;

    // Random rand = new Random(0);
    int i = 0;
    while (table.size() < RAINBOWTABLE_SIZE) {
      wordStart = intToBytes(i);
      wordEnd = generateChain(wordStart, i);
      key = bytesToHex(wordEnd);
      if (!table.containsKey(key)) {
        table.put(key, wordStart);
        success++;
      } else {
        collisions++;
      }
      i++;
    }

    System.out.println("Table Size:\t\t" + RAINBOWTABLE_SIZE);
    System.out.println("Chain Length:\t\t" + CHAIN_LENGTH);
    System.out.println("Number of success:\t" + success);
    System.out.println("Number of collisions:\t" + collisions);
    writeTable();
  }

  private byte[] generateChain(byte[] wordStart, int index) {
    byte[] digest = new byte[20];
    byte[] word = wordStart;
    for (int i = 0; i < CHAIN_LENGTH; i++) {
      digest = hash(word);
      word = reduce(digest, i);
      if (index < RAINBOWTABLE_SIZE) {
        testInputs[index] = digest;
      }
    }
    return word;
  }

  //---- Invert Function --------------------------------------------
  public byte[] invert(byte[] digestSearch) {
    byte[] result = new byte[3];
    String key;
    for (int i = CHAIN_LENGTH - 1; i >= 0; i--) {
      key = invertHashReduce(digestSearch, i);
      if (table.containsKey(key)) {
        result = invertChain(digestSearch, table.get(key));
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private String invertHashReduce(byte[] digest, int start) {
    byte[] word = new byte[3];
    for (int i = start; i < CHAIN_LENGTH; i++) {
      word = reduce(digest, i);
      digest = hash(word);
    }
    return bytesToHex(word);
  }

  private byte[] invertChain(byte[] digestSearch, byte[] word) {
    byte[] digest;
    for (int i = 0; i < CHAIN_LENGTH; i++) {
      digest = hash(word);
      if (Arrays.equals(digestSearch, digest)) {
        return word;
      }
      word = reduce(digest, i);
    }
    return null;
  }

  // ------ Helper --------------------------------------------------
  public static String digestToKey(byte[] digest) {
    byte[] key = new byte[3];
    for (int i = 0; i < key.length; i++) {
      key[i] = digest[i];
    }
    return bytesToHex(key);
  }

  public static byte[] hexToBytes(String hexString) {
    HexBinaryAdapter adapter = new HexBinaryAdapter();
    byte[] bytes = adapter.unmarshal(hexString);
    return bytes;
  }

  public static String bytesToHex(byte[] bytes) {
    HexBinaryAdapter adapter = new HexBinaryAdapter();
    String str = adapter.marshal(bytes);
    return str;
  }

  public static byte[] intToBytes(int i) {
    byte result[] = new byte[3];
    result[0] = (byte) (i >> 16);
    result[1] = (byte) (i >> 8);
    result[2] = (byte) i;
    return result;
  }

  public static int bytesToInt(byte[] bytes) {
    return java.nio.ByteBuffer.wrap(bytes).getInt();
  }

  private void writeTable() {
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(RAINBOWTABLE_FILE));
      out.writeObject(table);
      out.close();
      System.out.println("Rainbow table has been saved to " + RAINBOWTABLE_FILE);
    } catch (Exception e) {
      System.out.println("Exception " + e);
    }
  }

  @SuppressWarnings("unchecked")
  private void readTable() {
    System.out.println("----- READING TABLE ------------------------------");
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(RAINBOWTABLE_FILE));
      table = (HashMap<String, byte[]>) in.readObject();
      in.close();
      System.out.println("Table Size:\t\t" + RAINBOWTABLE_SIZE);
      System.out.println("Chain Length:\t\t" + CHAIN_LENGTH);
      System.out.println("Rainbow table has been loaded from " + RAINBOWTABLE_FILE);
    } catch (Exception e) {
      System.out.println("Exception " + e);
    }
  }
}