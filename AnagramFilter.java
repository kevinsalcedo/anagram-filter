import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class AnagramFilter {
    public static void main(String[] args) throws Exception {
        List<String> commons = readFile(args[1]);
        filterList(args[0], commons);
    }

    // Read the common eight letter words file, and return list of them
    public static List<String> readFile(String fileName) {
        List<String> thing = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(word -> {
                thing.add(word);
            });
        } catch (Exception ex) {
            System.out.println("Error reading file: " + ex.getMessage());
        }
        return thing;
    }

    // Read the anagram data file and filter it basedo n the common list
    public static void filterList(String fileName, List<String> filter) throws Exception {
        File output = new File(fileName.substring(0, fileName.indexOf('.')) + "-filtered.csv");
        FileWriter writer = new FileWriter(output);
        writer.write("WORD,ALPHA,LETTER,INDEX" + "\n");
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(word -> {
                // Get the actual word
                String[] row = word.split(",");
                String key = row[0];

                //  Check if the common list contains the word
                if (filter.contains(key)) {
                    try {
                        writer.write(word + "\n");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        } catch (Exception ex) {
            System.out.println("Error reading file: " + ex.getMessage());
        }
        writer.close();
    }
}