import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Anagrammer {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        if (args.length != 1) {
            System.out.println("Please supply an argument.");
            return;
        }

        Map<String, List<String>> words = readFile(args[0]);

        createOutput(args[0], words);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + ((endTime - startTime)) + " milliseconds");
    }

    // Read text file of words and create a map of:
    // Key - alphagram
    // Value - words that share the alphagram key
    public static Map<String, List<String>> readFile(String fileName) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(word -> {
                String[] letters = word.split("");
                Arrays.sort(letters);
                String alphagram = String.join("", letters);

                List<String> anagrams = result.getOrDefault(alphagram, new ArrayList<>());
                anagrams.add(word);
                result.put(alphagram, anagrams);

            });
        } catch (Exception ex) {
            System.out.println("Error reading file: " + ex.getMessage());
        }

        return result;
    }

    // Create the output file by iterating over each group of alphagrams
    public static void createOutput(String fileName, Map<String, List<String>> alphas) {
        try {
            File output = new File(fileName.substring(0, fileName.indexOf('.')) + ".csv");
            FileWriter writer = new FileWriter(output);
            // Write the header line
            writer.write("WORD,ALPHA,LETTER,INDEX" + "\n");

            for (Map.Entry<String, List<String>> entry : alphas.entrySet()) {
                for (List<Triple> list : handleAlphagram(entry.getKey(), entry.getValue()).values()) {
                   for(Triple t : list) {
                    writer.write(t.toJSONString() + "\n");
                   }
                }
            }
            writer.close();
        } catch (Exception ex) {
            System.out.println("Error making output: " + ex.getMessage());
        }
    }

    // Take a group of anagrams sharing an alphagram, and see which ones have a
    // unique Triple
    //
    public static Map<String, List<Triple>> handleAlphagram(String alpha, List<String> anagrams) {
        Map<String, List<Triple>> res = new HashMap<String, List<Triple>>();
        if (anagrams.size() == 1) {
            String only = anagrams.get(0);
            List<Triple> occurrences = new ArrayList<Triple>();
            occurrences.add(new Triple(only, alpha));
            res.put(only, occurrences);
            return res;
        }

        List<String> allLetters = Arrays.asList(alpha.split(""));
        List<String> uniqueLetters = allLetters.stream().distinct().collect(Collectors.toList());
        // Iterate over all unique letters in alphagram
        for (String character : uniqueLetters) {
            // Test each unique letter in each index, from index 1-6 (spot 2-7)
            for (int i = 1; i < alpha.length()-1; i++) {
                String reg = "^.{" + (i) + "}" + character + ".{" + (alpha.length() - (i + 1)) + "}$";
                String match = getMatches(reg, anagrams);
                if (match != null) {
                    Triple solution = new Triple(match, alpha, character, i);
                    List<Triple> occurrences = res.getOrDefault(solution.getWord(), new ArrayList<Triple>());
                    occurrences.add(solution);
                    // For words that are anagrams of each other i.e. REPRINTS and PRINTERS
                    if(occurrences.size() == 6) {
                        occurrences.clear();
                        occurrences.add(new Triple(solution.getWord(), alpha));
                    }
                    res.put(solution.getWord(), occurrences);
                }
            }
        }
        return res;
    }

    // Check to see how many words match the given regex.
    // If there is only one word, then return it
    private static String getMatches(String regex, List<String> anagrams) {
        Pattern p = Pattern.compile(regex);
        List<String> matches = new ArrayList<String>();
        for (String s : anagrams) {
            if (p.matcher(s).matches()) {
                matches.add(s);
            }
        }

        if (matches.size() == 1) {
            return matches.get(0);
        } 
        return null;
    }

    static class Triple {
        String word;
        String alphagram;
        String letter;
        int index;

        public Triple(String word, String alphagram) {
            this.word = word;
            this.alphagram = alphagram;
            this.index = -1;
            this.letter = "*";
        }

        public Triple(String word, String alphagram, String letter, int index) {
            this.word = word;
            this.letter = letter;
            this.index = index;
            this.alphagram = alphagram.replaceFirst(letter, "");

        }

        public String getWord() {
            return this.word;
        }

        public String toJSONString() {
            return this.word + "," + this.alphagram + "," + this.letter + "," + this.index;
        }
    }
}
