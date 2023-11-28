import java.util.*;
import java.io.*;

public class Absurdle  {
    public static final String GREEN = "🟩";
    public static final String YELLOW = "🟨";
    public static final String GRAY = "⬜";

    // [[ ALL OF MAIN PROVIDED ]]
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        System.out.println("Welcome to the game of Absurdle.");

        System.out.print("What dictionary would you like to use? ");
        String dictName = console.next();

        System.out.print("What length word would you like to guess? ");
        int wordLength = console.nextInt();

        List<String> contents = loadFile(new Scanner(new File(dictName)));
        Set<String> words = pruneDictionary(contents, wordLength);

        List<String> guessedPatterns = new ArrayList<>();
        while (!isFinished(guessedPatterns)) {
            System.out.print("> ");
            String guess = console.next();
            String pattern = record(guess, words, wordLength);
            guessedPatterns.add(pattern);
            System.out.println(": " + pattern);
            System.out.println();
        }
        System.out.println("Absurdle " + guessedPatterns.size() + "/∞");
        System.out.println();
        printPatterns(guessedPatterns);
    }

    // [[ PROVIDED ]]
    // Prints out the given list of patterns.
    // - List<String> patterns: list of patterns from the game
    public static void printPatterns(List<String> patterns) {
        for (String pattern : patterns) {
            System.out.println(pattern);
        }
    }

    // [[ PROVIDED ]]
    // Returns true if the game is finished, meaning the user guessed the word. Returns
    // false otherwise.
    // - List<String> patterns: list of patterns from the game
    public static boolean isFinished(List<String> patterns) {
        if (patterns.isEmpty()) {
            return false;
        }
        String lastPattern = patterns.get(patterns.size() - 1);
        return !lastPattern.contains("⬜") && !lastPattern.contains("🟨");
    }

    // [[ PROVIDED ]]
    // Loads the contents of a given file Scanner into a List<String> and returns it.
    // - Scanner dictScan: contains file contents
    public static List<String> loadFile(Scanner dictScan) {
        List<String> contents = new ArrayList<>();
        while (dictScan.hasNext()) {
            contents.add(dictScan.next());
        }
        return contents;
    }

    // TODO: Write your code here! 

    // Loads a list of words and removes the words not adhering to the user's specification 
    // (word length)
    // Inputs a List of words, and an int representing their prefered word length
    // Returns a set of words that meet the prefered word length
    // Throws an IllegalArgumentException if the word length selected is less than one
    public static Set<String> pruneDictionary(List<String> contents, int wordLength) {
        if(wordLength<1){
            throw new IllegalArgumentException("Too short of a length!");
        }
        Set<String> prunedSet = new HashSet<String>();
        for(String word : contents){
            if(!prunedSet.contains(word) && word.length()==wordLength){
                prunedSet.add(word);
            }
        }

        return prunedSet;
    }

    // Given the user's guess and the list of possible answers, selects the pattern that has
    // the most possible answers associated with it, and returns it as a string 
    // Inputs a String representing the user's guess, a Set of answers, and an int representing
    // prefered word length
    // Returns a String of the selected "best" pattern
    // Removes the words that can no longer be considered given the selected best pattern 
    // from the overall set of words
    // Throws an IllegalArgumentException if the Set of words is empty, or the inputted guess 
    // is not the correct length
    public static String record(String guess, Set<String> words, int wordLength) {
        if(words.size()==0 || guess.length()!=wordLength){
            throw new IllegalArgumentException("Either the dictionary is empty or your guess "
                                                     + "is the wrong length!");
        }
        Map<String, Set<String>> patternsToWords = populateMap(guess, words);
        int bestNum=0;
        String bestPattern="";

        for(String key : patternsToWords.keySet()){
            if(patternsToWords.get(key).size()>bestNum){
                bestPattern="";
                bestNum = patternsToWords.get(key).size();
                bestPattern+=key;
            }
        }
        clearBadWords(words, bestPattern, guess);
        patternsToWords = populateMap(guess, words);


        
        return bestPattern;
    }

    // Given a guess and set of possible answers, populates a map with the possible patterns
    // and the associated words with those patterns
    // Returns a map of those correlations

    public static Map<String, Set<String>> populateMap(String guess, Set<String> words){
        Map<String, Set<String>> guessToWords = new TreeMap<>();
        for(String currWord : words){
            String currPattern = patternFor(currWord, guess);

            if(!(guessToWords.containsKey(currPattern))){
                Set<String> wordSet = new HashSet<String>();
                wordSet.add(currWord);
                guessToWords.put(currPattern, wordSet);
            } else {
                guessToWords.get(currPattern).add(currWord);
            }
        }

        return guessToWords;
    }

    // Given a set of possible answers, and the best pattern given the user's guess deletes
    // the unusable remaining words left in the set of possible answers
    // Does not return anything

    public static void clearBadWords(Set<String> words, String bestPattern, String guess){
        Iterator<String> it = words.iterator();
        while(it.hasNext()){
            String currWord = it.next();
            String currPattern = patternFor(currWord, guess);
            if(!(currPattern.equals(bestPattern))){
                it.remove();
            }
        }
    }


    // Creates a string out of emojis to indicate how many letters the user has guessed correctly
    // and if the positions are right
    // Inputs a string for the guessed word, and the correct word
    // Returns a string of the pattern for the user
    public static String patternFor(String word, String guess) {
       List<String> guessList = new LinkedList<String>();
       for(int i=0;i<word.length();i++){
            guessList.add(Character.toString(guess.charAt(i)));
       } 

       Map<String, Integer> letterCount = new HashMap<>();

        //puts into lists
       for(int j=0;j<word.length();j++){
            if(!letterCount.containsKey(letterInWord(word, j))){
                letterCount.put(letterInWord(word, j), 1);
            } else {
                int adjNum = letterCount.get(letterInWord(word, j))+1;
                letterCount.put(letterInWord(word, j), adjNum);
            }
       }

        //replaces guessList correct guesses with greens
        for(int guessPos=0;guessPos<guessList.size();guessPos++){
            if(guessList.get(guessPos).equals(letterInWord(word, guessPos))){
                guessList.set(guessPos, GREEN);
                int newNum = letterCount.get(letterInWord(word, guessPos))-1;
                letterCount.put(letterInWord(word, guessPos), newNum);
            }
        }

        //replaces guesslist correct letter/wrong place with yellows
        for(int guessPos=0;guessPos<guessList.size();guessPos++){
            if(letterCount.containsKey(guessList.get(guessPos))){
                if(letterCount.get(guessList.get(guessPos))>0){
                    int newNum = letterCount.get(guessList.get(guessPos))-1;
                    letterCount.put(guessList.get(guessPos), newNum);
                    guessList.set(guessPos, YELLOW);
                }
            }
        }

        //replaces remaining letters with grey squares
        for(int guessPos=0;guessPos<guessList.size();guessPos++){
            if(!(guessList.get(guessPos).equals(GREEN)) && 
                           !(guessList.get(guessPos).equals(YELLOW))){
                guessList.set(guessPos, GRAY);
            }
        }

        //turns guesslist into a String
        String pattern="";

        for(int k=0;k<guessList.size();k++) {
            pattern=pattern+guessList.get(k);
        }       

       return pattern;
    }

    // Given a String word and int position, returns the letter at that position as a String
    public static String letterInWord(String word, int pos){
        return Character.toString(word.charAt(pos));
    }
}
