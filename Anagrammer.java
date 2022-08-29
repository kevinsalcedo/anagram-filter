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

        // Create 8s
        // Map<String, List<String>> alphaMap = generateAlphagramMap("alleights.txt");
        // createEightsOutput("alleights.txt", alphaMap);

        // // Create 9s
        List<String> words = readWordsToList("one_nines.txt");
        List<String> twos = readWordsToList("twos.txt");
        Map<String, List<String>> allAlphas = generateAlphagramMap("nines.txt");
        Map<String, List<String>> nineToWildcardMap = filterNines(words, allAlphas);
        createNinesOutput(nineToWildcardMap, twos);

        // Create 7s
        // words = readWordsToList("common_sevens.txt");
        // createSevensOutput(words);

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

    public static void createNinesOutput(Map<String, List<String>> map, List<String> twos) {
        try {
            File output = new File("nines_with_hints.csv");
            FileWriter writer = new FileWriter(output);

            // Write the header line
            writer.write("WORD;ALPHA;LETTERS;INDICES;HINTS\n");
            // Go through each word and create an entry

            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                // Get the word
                String word = entry.getKey();
                // Create the alphagram
                String[] alphaArray = word.split("");
                Arrays.sort(alphaArray);
                String alpha = String.join("", alphaArray);
                // Get a random letter that can be used as a wild card
                List<String> letters = entry.getValue();
                int wildcardIndex = getRandomIndex(word, letters.size());
                // Get two random indices within the word (not including first or last letters)
                int[] givenIndices = getGivenIndices(word, wildcardIndex, twos);
                String[] givenLetters = new String[2];
                givenLetters[0] = Character.toString(word.charAt(givenIndices[0]));
                givenLetters[1] = Character.toString(word.charAt(givenIndices[1]));
                // Result to write [word];[alpha];[]
                String result = "\"" + word + "\";\"" + alpha.replaceFirst(letters.get(wildcardIndex), "") + "_\";" +
                    givenLetters.toString() + ";" + givenIndices.toString();
                List<Integer> hints = new ArrayList<>();
                while (hints.size() < 3) {
                    int randomHint = (int) Math.floor(Math.random() * word.length());
                    if (word.charAt(randomHint) != letters.get(wildcardIndex).toCharArray()[0] && !hints.contains(randomHint)) {
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

    public static int[] getGivenIndices(String word, int wildcardIndex, List<String> twos) {
        // Get the indices for given letters
        int[] givenIndices = new int[]{-1, -1};
        for(int i = 0; i < givenIndices.length; i++) {
            int index = getRandomIndex(word, word.length()-2) + 1;
            // Make sure that index 1 != index 2 != wildcardIndex
            while(index == wildcardIndex || index == ((i + 1) % givenIndices.length)) {
                index = getRandomIndex(word, word.length()-2) + 1;
            }
        }

        // Check to make sure that the given indices form a word if sequential
        if(Math.abs(givenIndices[0] - givenIndices[1]) == 1) {
            String givenLetters = Character.toString(word.charAt(givenIndices[0])) + Character.toString(word.charAt(givenIndices[1]));
            // If they don't form a word, recurse
            if(!twos.contains(givenLetters)) {
                givenIndices = getGivenIndices(word, wildcardIndex,twos);
            }
        }

        return givenIndices;
    }


    public static void createSevensOutput(List<String> words) {
        try {
            File output = new File("sevens_with_hints.csv");
            FileWriter writer = new FileWriter(output);

            writer.write("WORD;ALPHA;HINTS\n");
            for (String w : words) {
                String result = "\"" + w + "\";\"";
                String[] alpha = w.split("");
                Arrays.sort(alpha);
                String alphaStirng = String.join("", alpha);
                result += alphaStirng + "\";";

                List<Integer> hints = new ArrayList<>();
                while (hints.size() < 3) {
                    int randomHint = (int) Math.floor(Math.random() * w.length());
                    if (!hints.contains(randomHint)) {
                        hints.add(randomHint);
                    }
                }
                result += hints.toString() + "\n";
                writer.write(result);
            }

            writer.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
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
            writer.write("WORD;ALPHA;LETTER;INDEX;HINTS" + "\n");

            for (Map.Entry<String, List<String>> entry : alphas.entrySet()) {
                Map<String, List<Triple>> lists = filterEights(entry.getKey(), entry.getValue());

                for (List<Triple> list : lists.values()) {
                    // No duplicate words
                    int idx = getRandomIndex(entry.getKey(), list.size());
                    writer.write(list.get(idx).toJSONString() + "\n");
                    // for (Triple t : list) {
                    // System.out.println(t.toJSONString());
                    // writer.write(t.toJSONString() + "\n");
                    // }
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
        List<Integer> hints;

        public Triple(String word, String alphagram) {
            this.word = word;
            this.alphagram = alphagram;
            this.index = -1;
            this.letter = "*";

            // For words with only one anagram for their alphagram
            if (this.letter.equals("*") && this.index == -1) {
                int idx = getRandomIndex(this.word, 6) + 1;
                this.letter = String.valueOf(word.charAt(idx));
                this.index = idx;
                this.alphagram = alphagram.replaceFirst(this.letter, "");

                List<Integer> hints = new ArrayList<>();
                List<String> letters = Arrays.asList(this.word.split(""));
                while (hints.size() < 3) {
                    int randomHint = (int) Math.floor(Math.random() * word.length());
                    if (word.charAt(randomHint) != letters.get(index).toCharArray()[0] && !hints.contains(randomHint)
                            && randomHint != this.index) {
                        hints.add(randomHint);
                    }
                }
                this.hints = hints;

            }
        }

        public Triple(String word, String alphagram, String letter, int index) {
            this.word = word;
            this.letter = letter;
            this.index = index;
            this.alphagram = alphagram.replaceFirst(letter, "");

            List<Integer> hints = new ArrayList<>();
            List<String> letters = Arrays.asList(this.word.split(""));
            while (hints.size() < 3) {
                int randomHint = (int) Math.floor(Math.random() * word.length());
                if (word.charAt(randomHint) != letters.get(index).toCharArray()[0] && !hints.contains(randomHint)
                        && randomHint != this.index) {
                    hints.add(randomHint);
                }
            }
            this.hints = hints;

            // For words with only one anagram for their alphagram
            if (this.letter.equals("*") && this.index == -1) {
                int idx = getRandomIndex(this.word, 6) + 1;
                this.letter = String.valueOf(word.charAt(idx));
                this.index = idx;
                this.alphagram = alphagram.replaceFirst(this.letter, "");

                hints = new ArrayList<>();
                letters = Arrays.asList(this.word.split(""));
                while (hints.size() < 3) {
                    int randomHint = (int) Math.floor(Math.random() * word.length());
                    if (word.charAt(randomHint) != letters.get(index).toCharArray()[0] && !hints.contains(randomHint)
                            && randomHint != this.index) {
                        hints.add(randomHint);
                    }
                }
                this.hints = hints;
            }

        }

        public String getWord() {
            return this.word;
        }

        public String toJSONString() {
            return this.word + ";" + this.alphagram + ";" + this.letter + ";" + this.index + ";"
                    + this.hints.toString();
        }
    }

    static class TripleMulti {
        String word;
        String alphagram;
        String[] letters;
        int[] indices;

        public TripleMulti(String word, String alphagram, String[] letters, int[] indices) {
            this.word = word;
            this.alphagram = alphagram;
            this.letters = letters;
            this.indices = indices;
        }

        public String getWord() {
            return this.word;
        }

        public String toJSONString() {
            return this.word + "," + this.alphagram + "," + this.letters.toString() + "," + this.indices.toString();
        }
    }
}
