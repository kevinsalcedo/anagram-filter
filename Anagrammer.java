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
        // Create 7s

        // Create 8s
        // if (args.length != 1) {
        // System.out.println("Please supply an argument.");
        // return;
        // }
        // Map<String, List<String>> words = readFile(args[0]);
        // createOutput(args[0], words);

        // Create 9s
        List<String> words = readWordsToList("one_nines.txt");
        Map<String, List<String>> allAlphas = generateAlphagramMap("nines.txt");
        Map<String, List<String>> result = filterNines(words, allAlphas);
        createNinesOutput(result);

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + ((endTime - startTime)) + " milliseconds");
    }

    // Read text file of words and create a map of:
    // Key - alphagram
    // Value - words that share the alphagram key
    public static Map<String, List<String>> generateAlphagramMap(String fileName) {
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

    // Read a text file of one word per line, and return a list of words
    public static List<String> readWordsToList(String fileName) {
        List<String> result = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(word -> {
                result.add(word);
            });
            Collections.sort(result);
        } catch (Exception ex) {
            System.out.println("error reading nines: " + ex.getMessage());
        }
        return result;
    }

    // Weekly Nines List Generation - return a map of words with letters that can be
    // replaced with a wildcard
    // And still produce only one solution
    public static Map<String, List<String>> filterNines(List<String> words, Map<String, List<String>> allWords) {
        String[] abc = "abcdefghijklmnopqrstuvwxyz".toUpperCase().split("");
        Set<String> keys = allWords.keySet();
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (String word : words) {
            String[] letters = word.split("");
            Arrays.sort(letters);
            Set<String> uniqueChars = new HashSet<>(Arrays.asList(letters));
            List<String> indices = new ArrayList<String>();
            // Go thru each unique character
            for (String c : uniqueChars) {
                boolean found = false;
                for (int i = 0; i < abc.length && !found; i++) {
                    String letter = abc[i].toUpperCase();
                    if (!c.equals(letter)) {
                        String[] replaced = word.replaceFirst(c, letter).split("");
                        Arrays.sort(replaced);
                        String tester = String.join("", replaced);

                        if (keys.contains(tester) && allWords.get(tester).size() > 0) {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    indices.add(c);
                }
            }
            if (indices.size() > 0) {
                result.put(word, indices);
            }
        }

        return result;
    }

    public static void createNinesOutput(Map<String, List<String>> map) {
        try {
            File output = new File("nines_with_hints.csv");
            FileWriter writer = new FileWriter(output);

            // Write the header line
            writer.write("WORD,ALPHAGRAM,HINTS\n");

            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String word = entry.getKey();
                String[] alphaArray = word.split("");
                Arrays.sort(alphaArray);
                String alpha = String.join("", alphaArray);
                List<String> letters = entry.getValue();
                int index = getRandomIndex(word, letters.size());
                String result = word + "," + alpha.replaceFirst(letters.get(index), "") + "_,";

                List<Integer> hints = new ArrayList<>();
                while (hints.size() < 3) {
                    int randomHint = (int) Math.floor(Math.random() * word.length());
                    if (word.charAt(randomHint) != letters.get(index).toCharArray()[0] && !hints.contains(randomHint)) {
                        hints.add(randomHint);
                    }
                }
                result += hints.toString() + "\n";
                writer.write(result);
            }
            writer.close();

        } catch (Exception ex) {
            System.out.println("Error " + ex.getMessage());
        }
    }

    public static int getRandomIndex(String word, int size) {
        int count = 0;
        String[] letters = word.split("");
        for (String c : letters) {
            count += (int) c.charAt(0);
        }
        return count % size;
    }

    // Create the output file by iterating over each group of alphagrams
    public static void createEightsOutput(String fileName, Map<String, List<String>> alphas) {
        try {
            File output = new File(fileName.substring(0, fileName.indexOf('.')) + ".csv");
            FileWriter writer = new FileWriter(output);
            // Write the header line
            writer.write("WORD,ALPHA,LETTER,INDEX" + "\n");

            for (Map.Entry<String, List<String>> entry : alphas.entrySet()) {
                for (List<Triple> list : filterEights(entry.getKey(), entry.getValue()).values()) {
                    for (Triple t : list) {
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
    public static Map<String, List<Triple>> filterEights(String alpha, List<String> anagrams) {
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
            for (int i = 1; i < alpha.length() - 1; i++) {
                String reg = "^.{" + (i) + "}" + character + ".{" + (alpha.length() - (i + 1)) + "}$";
                String match = getMatches(reg, anagrams);
                if (match != null) {
                    Triple solution = new Triple(match, alpha, character, i);
                    List<Triple> occurrences = res.getOrDefault(solution.getWord(), new ArrayList<Triple>());
                    occurrences.add(solution);
                    // For words that are anagrams of each other i.e. REPRINTS and PRINTERS
                    if (occurrences.size() == 6) {
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

    // Custom formatting class for Eight Letter words
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
