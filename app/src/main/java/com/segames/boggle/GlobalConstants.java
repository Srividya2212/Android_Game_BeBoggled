package com.segames.boggle;

/**
 * Global constants; implement this interface to access them conveniently.
 */
public interface GlobalConstants {

    /*
     *  bbwords constants
    */

    public static final int BBEasyLevel = 1;
    public static final int BBNormalLevel = 2;


    public static final int BBMaxWordLenEasyLevel = 9;
    public static final int BBMaxWordLenNormalLevel = 16;

    public static final int BBEasyLevelSize = 3;    // i.e. 3 by 3
    public static final int BBNormalLevelSize = 4;  // i.e. 4 by 4

    public static final int BBEasyLevelNoWords = 10;
    public static final int BBNormalLevelNoWords = 20;

    public static final int BBWordsSuccess = 0;
    public static final int BBWordsFailure = -1;

    public static final int BBGameTime = 45;
    public static final int BBSingleGameTime = 15;

    public static final String BBValidWordsFile = "bbwords.txt";

    /*
     *  BBServer constants
    */
    public static final int ServerRole = 1;
    public static final int ClientRole = 2;

    public static final int BBPlayer_Me = 1;
    public static final int BBPlayer_Other = 2;

    public static final int BBMinGridLen = 9;

    //messages
    public static final String ok = "ok";
    public static final String wordAlreadyGuessed = "Word already guessed";
    public static final String wordIncorrect = "Incorrect";
    public static final String wordOppGuessed = "Opponent Guessed";
    public static final int BBResultTie = 0;
    public static final int BBResultSelfWin = 1;
    public static final int BBResultOppWin = 2;


    /*
     * BBClient display constants
     */

    public static final int BBMaxEasyRounds = 3;
    public static final int BBMaxTotalRounds = 5;
    public static final int BBMinWordLength = 3;

    public static final int BBSingleMode = 1;
    public static final int BBDoubleBasicMode = 2;
    public static final int BBDoubleCutMode = 3;

    public static final int topleft = 0;
    public static final int topup = 1;
    public static final int topright=2;
    public static final int midleft = 3;
    public static final int midmid =4;
    public static final int midright =5;
    public static final int botleft = 6;
    public static final int botdown = 7;
    public static final int botright =8;



} // public interface GlobalConst
