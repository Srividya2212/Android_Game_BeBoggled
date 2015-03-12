package com.segames.boggle;

//import android.os.CountDownTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by li on 15/02/15.
 */
public class BBServerDouble implements GlobalConstants{

    private int gameMode;
    private int gameLevel;
    private int gameRound;

    private int gameScoreP1;
    private int gameScoreP2;
    private int totalGameScoreP1;
    private int totalGameScoreP2;
    private int nbRoundWonP1;
    private int nbRoundWonP2;

    private BBWords BBWords;// = new BBWords();

    //guessed words String = word / Integer = player
    private HashMap<String,Integer> allGuessedWords = new HashMap();

    //guessed words per player String = word / Integer = word value
    private HashMap<String,Integer> guessedWordsP1 = new HashMap();
    private HashMap<String,Integer> guessedWordsP2 = new HashMap();
    private ArrayList<String> list_guessedWordsP1 = new ArrayList<>();
    private ArrayList<String> list_guessedWordsP2 = new ArrayList<>();
    //timer
    private final long startTime = 60 * 1000;
    private final long interval = 1 * 1000;
    //private CountDownTimer countDownTimer;


    //Constructor
    public BBServerDouble(int gameMode, int gameLevel, BBWords bbWords){
        this.gameMode = gameMode;
        this.gameLevel = gameLevel;
        this.gameRound = 1;
        this.gameScoreP1 = 0;
        this.gameScoreP2 = 0;
        //this.totalGameScoreP1 = 0;
        //this.totalGameScoreP2 = 0;
        this.nbRoundWonP1 = 0;
        this.nbRoundWonP2 = 0;
        this.BBWords = bbWords;
    }


    //getters
    public int getGameMode() {
        return gameMode;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public int getGameRound() {
        return gameRound;
    }

    public int getGameScoreP1() {
        return gameScoreP1;
    }

    public int getGameScoreP2() {
        return gameScoreP2;
    }

    public int getTotalScoreP1() {
        return totalGameScoreP1;
    }

    public int getTotalScoreP2() {
        return totalGameScoreP2;
    }

    public int getNbRoundWonP1() {
        return nbRoundWonP1;
    }

    public int getNbRoundWonP2() {
        return nbRoundWonP2;
    }

    public BBWords getBBWords() {
        return BBWords;
    }

    public HashMap<String, Integer> getAllGuessedWords() {
        return allGuessedWords;
    }

    public HashMap<String, Integer> getGuessedWordsP1() {
        return guessedWordsP1;
    }

    public HashMap<String, Integer> getGuessedWordsP2() {
        return guessedWordsP2;
    }

    public ArrayList<String> getGuessedWordsP1List() {
        return list_guessedWordsP1;
    }

    public ArrayList<String> getGuessedWordsP2List() {
        return list_guessedWordsP2;
    }

    //setters
    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

    public void setGameRound(int gameRound) {
        this.gameRound = gameRound;
    }

    public void setGameScoreP1(int gameScoreP1) {
        this.gameScoreP1 = gameScoreP1;
    }

    public void setGameScoreP2(int gameScoreP2) {
        this.gameScoreP2 = gameScoreP2;
    }

    //functions
    public void updateGameScore(int wordValue, int playerId) {
        if(playerId == 1){
            this.gameScoreP1 = this.gameScoreP1 + wordValue;
            this.totalGameScoreP1 = this.totalGameScoreP1 + wordValue;
        }else if(playerId == 2){
            this.gameScoreP2 = this.gameScoreP2 + wordValue;
            this.totalGameScoreP2 = this.totalGameScoreP2 + wordValue;
        }
    }

    public void updateGameRound(){
        this.gameRound++;
    }

    public void updateNbRoundWon(int playerId){
        if(playerId == 1){
            this.nbRoundWonP1++;
        }else if(playerId == 2){
            this.nbRoundWonP2++;
        }
    }

    public void clearGuessedWords(){
        allGuessedWords.clear();
        guessedWordsP1.clear();
        guessedWordsP2.clear();
    }

    public void newRound(){
        this.allGuessedWords.clear();
        this.guessedWordsP1.clear();
        this.guessedWordsP2.clear();
        this.getGuessedWordsP1List().clear();
        this.getGuessedWordsP2List().clear();
        this.setGameScoreP1(0);
        this.setGameScoreP2(0);
        this.updateGameRound();
        this.BBWords.Grid(this.getGameLevel());
    }

    public void dumpAllWordsP1(){
        for (Entry<String, Integer> entry : this.getGuessedWordsP1().entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public void dumpAllWordsP2(){
        for (Entry<String, Integer> entry : this.getGuessedWordsP2().entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public void dumpAllWordsPlayed(){
        for (Entry<String, Integer> entry : this.getAllGuessedWords().entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    public String incomeWordBasic(String candidateWord, int playerId){
        String message = ok;

        if(playerId == 1){
            if(guessedWordsP1.containsKey(candidateWord) != true){
                //if(BBWords.isWordValid(candidateWord) && this.getBBWords().searchForWord(this.getBBWords().get_grid_str(),candidateWord)){
                if(BBWords.isWordValid(candidateWord)){
                    if(BBWords.wordsValue(candidateWord) >= 0){
                        //score update
                        this.updateGameScore(BBWords.wordsValue(candidateWord),playerId);

                        //update of guessedWords of player 1
                        guessedWordsP1.put(candidateWord,BBWords.wordsValue(candidateWord));
                        list_guessedWordsP1.add(candidateWord);
                    }
                }
                else{
                    //the word submitted id not a correct word
                    message = wordIncorrect;
                }
            }
            else{
                //word submitted was already guessed by the player
                message = wordAlreadyGuessed;
            }
        }else if(playerId == 2){
            if(guessedWordsP2.containsKey(candidateWord) != true){
                //if(BBWords.isWordValid(candidateWord) && this.getBBWords().searchForWord(this.getBBWords().get_grid_str(),candidateWord)){
                if(BBWords.isWordValid(candidateWord)){
                    if(BBWords.wordsValue(candidateWord) >= 0){
                        //score update
                        this.updateGameScore(BBWords.wordsValue(candidateWord),playerId);

                        //update of guessedWords of player 1
                        guessedWordsP2.put(candidateWord,BBWords.wordsValue(candidateWord));
                        list_guessedWordsP2.add(candidateWord);
                    }
                }
                else{
                    //the word submitted is not a correct word
                    message = wordIncorrect;
                }
            }
            else{
                //word submitted was already guessed by the player
                message = wordAlreadyGuessed;
            }
        }

        return message;
    }

    public String incomeWordCutthroat(String candidateWord, int playerId){
        String message = ok;

        if(allGuessedWords.containsKey(candidateWord) != true){
            //if(BBWords.isWordValid(candidateWord) && this.getBBWords().searchForWord(this.getBBWords().get_grid_str(),candidateWord)){
            if(BBWords.isWordValid(candidateWord)){
                if(BBWords.wordsValue(candidateWord) >= 0){
                    //score update
                    this.updateGameScore(BBWords.wordsValue(candidateWord),playerId);

                    //update of allGuessedWords
                    allGuessedWords.put(candidateWord,playerId);

                    if(playerId == 1){
                        //update of guessedWords of player 1
                        guessedWordsP1.put(candidateWord,BBWords.wordsValue(candidateWord));
                        list_guessedWordsP1.add(candidateWord);
                    }else if (playerId == 2){
                        //update of guessedWords of player 2
                        guessedWordsP2.put(candidateWord,BBWords.wordsValue(candidateWord));
                        list_guessedWordsP2.add(candidateWord);
                    }

                }
            }
            else{
                //the word submitted is not a correct word
                return wordIncorrect;
            }
        }
        else{
            //word submitted was already guessed by the player
            if(guessedWordsP2.containsKey(candidateWord)){
                return wordOppGuessed;
            }
            else{
                return wordAlreadyGuessed;
            }

        }

        return message;
    }

    public String incomeWord(String candidateWord, int playerId){
        String message = ok;

        //if Basic Double Mode
        if(this.getGameMode() == BBDoubleBasicMode){
            return this.incomeWordBasic(candidateWord,playerId);
        }else if (this.getGameMode() == BBDoubleCutMode){
            return this.incomeWordCutthroat(candidateWord,playerId);
        }

        return message;
    }

    public int winnerRound(){
        if (this.getGameScoreP1() < this.getGameScoreP2()){
            this.updateNbRoundWon(2);
            return 2;
        }
        else if(this.getGameScoreP2() < this.getGameScoreP1()){
            this.updateNbRoundWon(1);
            return 1;
        }
        else{
            return 0;
        }
    }

    public int winner(){
        if (this.getNbRoundWonP1() < this.getNbRoundWonP2()){
            return 2;
        }
        else if(this.getNbRoundWonP2() < this.getNbRoundWonP1()){
            return 1;
        }
        else{
            return 0;
        }
    }

    public static void main(String[] args) throws IOException {

        /*BBServerDouble server = new BBServerDouble(BBDoubleBasicMode,BBNormalLevel,bbWords);
        server.getBBWords().Grid(BBNormalLevel);
        server.getBBWords().dumpGrid();
        System.out.println( "\n" + server.getBBWords().getGridWords() + "\n");

        BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
        String s;
        System.out.println("Game round :" + server.gameRound);
        do{
            System.out.println(" \n ******* ");
            //player 1
            System.out.print("P1 Enter word : ");
            s = br.readLine();
            System.out.println(server.incomeWord(s, 1));


            //player 2
            System.out.print("P2 Enter word : ");
            s = br.readLine();
            System.out.println(server.incomeWord(s, 2));

            System.out.println( "\n" + server.getBBWords().getGridWords() + "\n");

            System.out.println(" ******* \n ");


        }while(!s.equals("stopgame"));

        System.out.println("Words guessed by P1");
        server.dumpAllWordsP1();
        System.out.println("\n Words guessed by P2");
        server.dumpAllWordsP2();

        System.out.println("\n All guessed words");
        server.dumpAllWordsPlayed();

        System.out.println("\n The winner of the round is : " + server.winnerRound());

        server.newRound();

        System.out.println("Game round :" + server.gameRound);
        server.getBBWords().dumpGrid();
        do{
            System.out.println(" \n ******* ");
            //player 1
            System.out.print("P1 Enter word : ");
            s = br.readLine();
            System.out.println(server.incomeWord(s, 1));


            //player 2
            System.out.print("P2 Enter word : ");
            s = br.readLine();
            System.out.println(server.incomeWord(s, 2));

            System.out.println( "\n" + server.getBBWords().getGridWords() + "\n");

            System.out.println(" ******* \n ");


        }while(!s.equals("stopgame"));
        System.out.println("Words guessed by P1");
        server.dumpAllWordsP1();
        System.out.println("\n Words guessed by P2");
        server.dumpAllWordsP2();

        System.out.println("\n All guessed words");
        server.dumpAllWordsPlayed();

        System.out.println("\n The winner of the round is : " + server.winnerRound());
        System.out.println("\n The winner of the game is : " + server.winner());

        System.out.println("\nEnd of test");
        */

    }


}