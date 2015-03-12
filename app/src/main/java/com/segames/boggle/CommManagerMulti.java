package com.segames.boggle;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by SAMINA on 1/27/15.
 */

/*
    EA - Turned into a singleton class and made to use BBServeDouble instead
        of BBWords directly.
 */
public class CommManagerMulti implements GlobalConstants {

    private static Context context;
    private static CommManagerMulti instance = new CommManagerMulti();
    private static BluetoothChatService mChatService;

    private CommManagerMulti(){};

    public static CommManagerMulti getInstance() {
        return instance;
    }

    private static BBServerDouble BBServerDouble1;
    private static BBWords BBWords1;
    private static List clientWords = new ArrayList();
    private static List clientWords2 = new ArrayList();

    private static String multi_grid_str = "";
    private static boolean client_ready_status = false;
    private static boolean server_waiting_status = false;

    public static void setMultiGrid(String str) { multi_grid_str = new String(str); }
    public static void setClientReadyStatus(boolean ready_status) { client_ready_status = ready_status; }
    public static boolean isClientReady() { return client_ready_status; }
    public static void setServerWaitingStatus(boolean waiting_status) { server_waiting_status = waiting_status; }
    public static boolean isServerWaiting() { return server_waiting_status; }

    //tag argument unused: use as you see fit
    public static String SendServer(String tag, String arg){ // Role is not needed here

        String returnstr = "";
        if(tag == "word") {
            String rslt_str = BBServerDouble1.incomeWord(arg, BBPlayer_Me);
            if (rslt_str == wordAlreadyGuessed) return "-999";
            if (rslt_str == wordOppGuessed) return "-888";
            if (rslt_str == wordIncorrect) return "0";

            sendMessage(arg);
            returnstr = Integer.toString(BBWords1.wordsValue(arg));
        }
        else if(tag == "message"){
            sendMessage(arg);
            returnstr = "ok";
        }
        return returnstr;
    }

    public static void setChatService(BluetoothChatService bt1, Context c1) {
        mChatService = bt1;
        context = c1;
    }

    // Empty until the shake occurs from the Server. Only used by the client
    public static String getGridFromServer(int round, int role, int level, int mode, Context context) {




        if(round == 1){
            BBWords1 = new BBWords(context);
            BBServerDouble1 = new BBServerDouble(mode,level,BBWords1);}
        else{BBServerDouble1.newRound();}

        // TODO - this should probably use a synchronous function to update multi_grid_str
        String str = "";
        if(role == ServerRole) {
            str = BBWords1.Grid(level);
            System.out.println("Sent to client:"+str);
            sendMessage(str);
            return str;
        } else {
            if(multi_grid_str != "") {
                BBWords1.putGrid(level,multi_grid_str);
                str = BBWords1.getGrid();
            } else {
                BBWords1.putGrid(level,"nogridfromserver");
            }
        }
        clearlists();
        return BBWords1.getGrid();
    }

    public static void writeOppWord(String opp_word) {
        BBServerDouble1.incomeWord(opp_word, BBPlayer_Other);
    }

    public static String getGridWords(){
         return BBWords1.getGridWords();
    }

    /* Change required: Server provides opponent's words */
    public static String getOppWords(){
        HashMap gridWords = BBServerDouble1.getGuessedWordsP2();
        Set set = gridWords.entrySet();
        Iterator i = set.iterator();
        String str = "";
        while(i.hasNext()) {
            Map.Entry<String,Integer> me = (Map.Entry)i.next();
            str += me.getKey() + ": " + me.getValue() + "|";
        }
        return str;
    }
    public static String getOppWordsList(){
        ArrayList<String> gridWords = BBServerDouble1.getGuessedWordsP2List();

        String str = "";
        for(String word: gridWords){
            str += word + "|";
        }
        return str;
    }
    public static String getYourWordsList(){
        ArrayList<String> gridWords = BBServerDouble1.getGuessedWordsP1List();

        String str = "";
        for(String word: gridWords){
            str += word + "|";
        }
        return str;
    }

    private static void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    public static void clearlists(){
        clientWords.clear(); clientWords2.clear();
    }
    public static String getOnGrid(String grid, String word) {
        //System.out.println("grid: "+grid+" word: "+word+" result: "+BBWords1.annotatedGrid(grid,word));
        word = word.toLowerCase();
        return BBWords1.annotatedGrid(grid,word);
    }

    public static int getOppScore(){
        return BBServerDouble1.getGameScoreP2();
    }

    public static int winnerRound(){ return BBServerDouble1.winnerRound();}

    public static int getTotalScore(){return BBServerDouble1.getTotalScoreP1();}
    public static int getTotalOppScore(){return BBServerDouble1.getTotalScoreP2();}
    public static int winner(){return BBServerDouble1.winner();}
    public static int getNbRoundWon(){return BBServerDouble1.getNbRoundWonP1();}
    public static int getNbRoundWonOpp(){return BBServerDouble1.getNbRoundWonP2();}

}
