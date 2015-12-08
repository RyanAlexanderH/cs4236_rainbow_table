import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class Crack {
  public static Rainbow table = new Rainbow();
  public static byte[][] digests;
  public static byte[][] messages;

  public static void main(String[] args) {
    System.out.println("\n----- TESTING ------------------------------------");
    // System.out.println("Using the tail");
    // invertDigests(table.testInputs);
    System.out.println("Using random words");
    invertDigests(generateRandomWords(1000));

    processFile();
    doHashes();
  }

  // ------ Process File Function -------------------------------------------
  public static void processFile() {
    String f = "SAMPLE_INPUT.data";
    try {
      BufferedReader br = new BufferedReader(new FileReader(f));
      FileWriter wr = new FileWriter("output.data");
      String line;
      int index = 0;

      digests = new byte[1000][20];
      messages = new byte[1000][3];

      wr.write("S T A R T\n\n");
      System.out.println("\n----- READING FILE -------------------------------");
      while ((line = br.readLine()) != null) {
        String hex = "";
        for (String retval: line.trim().split("\\s+", 8)) {
          while (retval.length() != 8) {
            retval = "0" + retval;
          }
          hex += retval;
        }
        digests[index] = Rainbow.hexToBytes(hex);
        index++;
      }
      System.out.println(index + " digests has been read");
      
      wr.write("READ DONE\n");

      System.out.println("\n----- INVERTING ----------------------------------");
      int success = invertDigests(digests);
      
      System.out.println("\n----- WRITING FILE -------------------------------");
      for (int i = 0; i < messages.length; i++) {
        if (messages[i] == null) {
          wr.write("     0\n");
        } else {
          wr.write(Rainbow.bytesToHex(messages[i]) + "\n");
        }
      }
      wr.write("The total number of messages found is: " + success + "\n");
      wr.close();
      br.close();
    } catch (Exception e) {
      System.out.println("Exception " + e);
    }
  }

  // ------ Invert Messages Function --------------------------------------
  public static int invertDigests(byte[][] digests) {
    byte[] digest, result;
    int success = 0;
    long start, end, t = 0;

    for (int i = 0; i < digests.length; i++) {
      digest = digests[i];
      start = System.currentTimeMillis();
      result = table.invert(digest);
      end = System.currentTimeMillis();
      if (messages != null) {
        messages[i] = result;
      }
      if (result != null) {
        success++;
      }
      t += (end - start);
    }

    System.out.println("Time taken by invert, t:\t" + t / 1000.0);
    System.out.println("Number of success:\t\t" + success + " / " + digests.length);
    double ratio = (double)success / digests.length;
    DecimalFormat df = new DecimalFormat("##.####");
    System.out.println("Percentage of words found, C:\t" + df.format((ratio * 100.0)) + "%");

    return success;
  }

  // ------ Process File Function -------------------------------------------
  public static void doHashes() {
    long start, end;
    byte[] digest;

    start = System.currentTimeMillis();
    for (int i = 0; i < 8388608; i++) {
      digest = table.hash(Rainbow.intToBytes(i));
    }
    end = System.currentTimeMillis();

    System.out.println("Time taken to compute 2^23 SHA1 hashes, T: " + ((end - start)/1000.0));
  }

  // 
  public static byte[][] generateRandomWords(int size) {
    byte[][] digests = new byte[size][20];

    Random rand = new Random();
    for (int i = 0; i < digests.length; i++) {
      digests[i] = table.hash(Rainbow.intToBytes(rand.nextInt()));
    }
    return digests;
  }
}