import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
 
public class AnagramFilter {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        
        List<String> commons = readFile(args[1]);
        List<String> theList = readFile(args[0]);
        Collections.shuffle(theList);

        filterListOfStrings(theList, commons);

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + ((endTime - startTime)) + " milliseconds");
    }

    // Read the words file, and return list of them
    public static List<String> readFile(String fileName) {
        List<String> thing = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(line -> {
                // Ignore header line if exists
                if(!line.startsWith("WORD,ALPHA")) {
                    thing.add(line);
                }
            });
        } catch (Exception ex) {
            System.out.println("Error reading file: " + ex.getMessage());
        }
        return thing;
    }

    public static void filterListOfStrings(List<String> toFilter, List<String> filter) {
        try {
            File output = new File("random-list.csv");
            FileWriter writer = new FileWriter(output);
            writer.write("WORD,ALPHA,LETTER,INDEX" + "\n");
            for(String line : toFilter) {
                String word = line.split(",")[0];
                if(filter.contains(word)) {
                    writer.write(line + "\n");
                }
            }
            writer.close();
        } catch(Exception e) {
            System.out.println("error writing: " + e.getMessage());
        }
    }
}