package com.segames.boggle;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DoublePlayerCut extends ActionBarActivity implements View.OnClickListener,GlobalConstants{

    private final long startTime = BBGameTime * 1000;
    private final long interval = 1 * 1000;
    private static CountDownTimer countDownTimer;

    public static boolean created = false;


    //Game state variables
    static boolean gameInProgress = false;
    static boolean gameboardset = false;
    static int score = 0;
    static int oppscore = 0;
    static int numRounds = 1;
    static int role;
    static String selection="";

    //XML components
    Button button_submit;
    TextView my_wordlist;
    TextView timer;
    String my_list = "";
    TextView opp_wordlist;
    String opp_list = "";
    private String gridstr;
    private Vibrator vibrator;

    Gameboard gameboard;
    GestureDetector gestureDetector;

    //Shake-detection variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeEventManager mShakeDetector;


    Drawable[] arrows = new Drawable[9];
    Animation rotation;

    //Animation red
    public boolean mContentLoaded;
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;
    static Timer synctimer = new Timer();

    public static void timerStart(int interval) {
        // int interval = 10000; // 10 sec
        long time = System.currentTimeMillis() + interval;
        Date timeToRun = new Date(time);

        synctimer.schedule(new TimerTask() {
            public void run() {
                synchroStart();
            }
        }, timeToRun);
    }
    static void synchroStart() {
        gameInProgress=true;
        countDownTimer.start();
    }

    /* OnCreate - All the start-up stuff here */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.double_playeralt);

        //Code for Animation red
        mContentView = findViewById(R.id.double_player);
        mLoadingView = findViewById(R.id.red_layout);
        mLoadingView.setVisibility(View.GONE);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        CommManagerMulti.setClientReadyStatus(false);
        CommManagerMulti.setServerWaitingStatus(false);

        /* Getting arguments from previous screen */
        numRounds = getIntent().getExtras().getInt("Round");
        score = getIntent().getExtras().getInt("Score");
        role = getIntent().getExtras().getInt("Role");
        created = true;

        //System.out.println("Role: "+role);

        //Log.v("Round",Integer.toString(numRounds));
        gameboard = new Gameboard(BBNormalLevelSize);


        button_submit = (Button) findViewById(R.id.button_submit);
        button_submit.setOnClickListener(this);



        initializeBoardButtons();
        setAuxiliary();


        setTimer(this);
        setShakeDetection();
        setDoubleTap();

        TextView roundtext = (TextView) findViewById(R.id.round);
        roundtext.setText(Integer.toString(numRounds));

        TextView scoretext = (TextView) findViewById(R.id.score);
        scoretext.setText(Integer.toString(score));
        /*
        arrows= new Drawable[]{getResources().getDrawable(R.drawable.yellowtopleft), getResources().getDrawable(R.drawable.yellowup_alt), getResources().getDrawable(R.drawable.yellowtopright),
                getResources().getDrawable(R.drawable.yellowleft_alt), getResources().getDrawable(R.drawable.yellowdie),getResources().getDrawable(R.drawable.yellowright_alt), getResources().getDrawable(R.drawable.yellowbottomleft), getResources().getDrawable(R.drawable.yellowdown_alt), getResources().getDrawable(R.drawable.yellowbottomright)};
        */
        arrows= new Drawable[]{getResources().getDrawable(R.drawable.yellowtopleft2), getResources().getDrawable(R.drawable.yellowup_alt), getResources().getDrawable(R.drawable.yellowtopright2),
                getResources().getDrawable(R.drawable.yellowleft_alt), getResources().getDrawable(R.drawable.yellowdie),getResources().getDrawable(R.drawable.yellowright_alt), getResources().getDrawable(R.drawable.yellowbottomleft2), getResources().getDrawable(R.drawable.yellowdown_alt), getResources().getDrawable(R.drawable.yellowbottomright2)};


        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

    }

    void setTimer(Context c){
        countDownTimer = new CountDownTimerActivity(startTime, interval,c);
        timer = (TextView) findViewById(R.id.timer);
        timer.setText("" + String.format("%02d",((startTime/1000)/60))+":"+String.format("%02d",((startTime/1000)%60)));
    }
    @Override
    public void onBackPressed() {
    }

    void setDoubleTap()
    {
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                wordfinalize();
                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void wordfinalize(){


        if(selection.length() >= BBMinWordLength) {
            gameboard.opaqueButtons(getResources().getDrawable(R.drawable.whitedie));
            int tempscore = wordscore(selection);
            if (tempscore > 0) {
                MediaPlayer mp = MediaPlayer.create(this,R.raw.glass_ping);
                mp.start();
                my_list = my_list.concat(selection+"\n");
                //Log.v("Tag", selection);
                if(my_wordlist != null)my_wordlist.setText(my_list);
                score += tempscore;
                setScore(score);
            } else {
                //System.out.println("tempscore "+tempscore);
                String str="";
                //Animation red
                mContentLoaded = !mContentLoaded;
                showContentOrLoadingIndicator(mContentLoaded);
                switch (tempscore){
                    case -999 :
                        str = "Selected!";
                        break;
                    case -888 :
                        str = "Opponent Selected!";
                        break;
                    case 0:
                        str = "Bad Word!";
                        break;
                }
                if(str.equals("Bad Word!")){
                    MediaPlayer mp = MediaPlayer.create(this,R.raw.badsound);
                    mp.start();
                }
                if(str.equals("Selected!")){
                    MediaPlayer mp = MediaPlayer.create(this,R.raw.selected);
                    mp.start();
                }
                if(str.equals("Opponent Selected!")){
                    MediaPlayer mp = MediaPlayer.create(this,R.raw.fuzzybeep);
                    mp.start();
                }
                vibrator.vibrate(50);
                /*String str = (tempscore == -999 || tempscore == -888) ? "Selected!" : "Bad Word!";*/
                /*MediaPlayer mp = MediaPlayer.create(this,R.raw.glass_ping);
                mp.start();*/
               /* LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.toast_layout,
                        (ViewGroup) findViewById(R.id.toast_layout_root));

                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText(str);

                Toast toast = new Toast(getApplicationContext());
                //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();*/
                //Toast
                Toast toast = Toast.makeText(getApplicationContext(), str,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM ,0,15);
                toast.show();
            }
            gameboard.clearpreviousclick();
            selection = "";
        }

    }


    private void showContentOrLoadingIndicator(boolean contentLoaded) {
        // Decide which view to hide and which to show.
        final View showView = contentLoaded ? mContentView : mLoadingView;
        final View hideView = contentLoaded ? mLoadingView : mContentView;

        // Set the "show" view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        showView.setAlpha(0f);

        showView.setVisibility(View.VISIBLE);

        // Animate the "show" view to 100% opacity, and clear any animation listener set on
        // the view. Remember that listeners are not limited to the specific animation
        // describes in the chained method calls. Listeners are set on the
        // ViewPropertyAnimator object for the view, which persists across several
        // animations.
        showView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the "hide" view to 0% opacity. After the animation ends, set its visibility
        // to GONE as an optimization step (it won't participate in layout passes, etc.)
        hideView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
        mContentLoaded=!contentLoaded;
    }


    void setScore(int score)
    {
        TextView scoretxt = (TextView) findViewById(R.id.score);
        scoretxt.setText(Integer.toString(score));

    }

    int wordscore(String word)
    {
        int score = -1;

        if(selection.length()>=3)
        {
            String serverreply = CommManagerMulti.SendServer("word",selection);
            score = Integer.parseInt(serverreply);
        }

        return score;

    }
    void shakeGrid(int length){

        if(!gameInProgress){
            if(role == ClientRole) {
                Toast toast = Toast.makeText(getApplicationContext(), "Waiting for Opponent",
                        Toast.LENGTH_SHORT);
                toast.show();
                CommManagerMulti.SendServer("message", "BBReady");
            } else {
                if(CommManagerMulti.isClientReady()) {
                   findViewById(R.id.overlaywait).setVisibility(View.GONE);
                   setAuxiliary();
                   setGameBoard();
                   startNewGame();
               } else {
                   Toast toast = Toast.makeText(getApplicationContext(), "Opponent is not Ready",
                           Toast.LENGTH_SHORT);
                   toast.show();
                   CommManagerMulti.setServerWaitingStatus(true);
               }
           }
        }
    }

    /* startNewGame: clears the state and sets up for a new game */
    void startNewGame()
    {
        gameInProgress=true;

    }


    void initializeBoardButtons()
    {

        //find buttons
        gameboard.buttons[0][0]= (Button) findViewById(R.id.button_0);
        gameboard.buttons[0][1]= (Button) findViewById(R.id.button_1);
        gameboard.buttons[0][2]= (Button) findViewById(R.id.button_2);
        gameboard.buttons[0][3]= (Button) findViewById(R.id.button_3);
        gameboard.buttons[1][0]= (Button) findViewById(R.id.button_4);
        gameboard.buttons[1][1]= (Button) findViewById(R.id.button_5);
        gameboard.buttons[1][2]= (Button) findViewById(R.id.button_6);
        gameboard.buttons[1][3]= (Button) findViewById(R.id.button_7);
        gameboard.buttons[2][0]= (Button) findViewById(R.id.button_8);
        gameboard.buttons[2][1]= (Button) findViewById(R.id.button_9);
        gameboard.buttons[2][2]= (Button) findViewById(R.id.button_10);
        gameboard.buttons[2][3]= (Button) findViewById(R.id.button_11);
        gameboard.buttons[3][0]= (Button) findViewById(R.id.button_12);
        gameboard.buttons[3][1]= (Button) findViewById(R.id.button_13);
        gameboard.buttons[3][2]= (Button) findViewById(R.id.button_14);
        gameboard.buttons[3][3]= (Button) findViewById(R.id.button_15);

        //set onClick Listener
        for(int i=0; i< 4; i++)
        {
            for(int j=0; j< 4;j++){
                gameboard.buttons[i][j].setOnClickListener(this);
               gameboard.buttons[i][j].setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {

                        return gestureDetector.onTouchEvent(event);
                    }
                });
            }
          /* insert code - dynamically change height of button to match width of button
            to go with all screen sizes */
        }

    }

    void startWobble(){
        rotation = AnimationUtils.loadAnimation(this, R.anim.wobble);
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                startWobbleSlow();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        for(Button[] a: gameboard.buttons){
            for(Button b: a){
                b.startAnimation(rotation);
            }

        }
    }
    void startWobbleSlow(){
        rotation = AnimationUtils.loadAnimation(this, R.anim.wobbleslow);
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.overlaywait).setVisibility(View.GONE);
                if (role == ServerRole) {
                    gameboard.setGameboard(gridstr);
                    gameboardset = true;
                    countDownTimer.start();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        for(Button[] a: gameboard.buttons){
            for(Button b: a){
                b.startAnimation(rotation);
            }

        }
    }

    void setGameBoard()
    {
        button_submit.setVisibility(View.GONE);
        findViewById(R.id.overlay).setVisibility(View.GONE);
        startWobble();
        String str = "";
        str = CommManagerMulti.getGridFromServer(numRounds, role, BBNormalLevel,BBDoubleCutMode, this);
        //Log.v("strlen",Integer.toString(str.length()));
        gridstr=str;
        //gameboard.setGameboard(str);
    }

    void setShakeDetection()
    {
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeEventManager();
        mShakeDetector.setOnShakeListener(new ShakeEventManager.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if(!gameInProgress) vibrator.vibrate(50);
                shakeGrid(gameboard.size*gameboard.size);
                button_submit.setVisibility(View.GONE);
                findViewById(R.id.overlay).setVisibility(View.GONE);
                findViewById(R.id.overlaywait).setVisibility(View.VISIBLE);
            }
        });
    }

    void setAuxiliary()
    {

        my_wordlist = (TextView)findViewById(R.id.my_wordlist);
        if(my_wordlist != null){my_list = my_wordlist.getText().toString();my_wordlist.setMovementMethod(new ScrollingMovementMethod());}
        opp_wordlist = (TextView)findViewById(R.id.opp_wordlist);
        if(opp_wordlist != null){opp_list = opp_wordlist.getText().toString();opp_wordlist.setMovementMethod(new ScrollingMovementMethod());}

        //my_wordlist.setMovementMethod(new ScrollingMovementMethod());
        //opp_wordlist.setMovementMethod(new ScrollingMovementMethod());
        TableLayout listtable = (TableLayout)findViewById(R.id.table2);
        listtable.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        // registering the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        //unregistering the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        Button current_button= (Button) v;
        switch(v.getId()){
            case R.id.button_submit:
                shakeGrid(gameboard.size*gameboard.size);
                //startNewGame();
                current_button.setVisibility(View.GONE);
                findViewById(R.id.overlay).setVisibility(View.GONE);
                findViewById(R.id.overlaywait).setVisibility(View.VISIBLE);
                break;
            default:
                if(gameInProgress && gameboard.isvalidclick(current_button.getId())) {
                    int arrow_index = gameboard.getArrow(current_button.getId());
                    current_button.setBackground(getResources().getDrawable(R.drawable.yellowdie));
                    if(arrow_index!=-1)gameboard.setArrow(arrows[arrow_index]);

                    selection = selection + current_button.getText();
                    gameboard.previousclick(current_button.getId());
                }
                else{
                    //do what?

                }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void populateOppInfo(){
        oppscore = CommManagerMulti.getOppScore();
        TextView score_opp = (TextView) findViewById(R.id.score_opp);
        if(score_opp!= null) score_opp.setText(Integer.toString(oppscore));

        String[] oppwords = CommManagerMulti.getOppWordsList().split("\\|");
        String str = "";
        for(String word: oppwords){
            String[] words = word.split(":");
            str = str + words[0] + "\n";
        }
        if(opp_wordlist!=null)opp_wordlist.setText(str);

    }
    public class CountDownTimerActivity extends CountDownTimer {
        Context context;
        public CountDownTimerActivity(long startTime, long interval, Context context) {
            super(startTime, interval);
            this.context=context;
        }

        @Override
        public void onFinish() {
            if(gameInProgress) {
                Intent scoreIntent = new Intent(context, Score.class);

                scoreIntent.putExtra("Score", score);
                scoreIntent.putExtra("OppScore", oppscore);
                scoreIntent.putExtra("Round", numRounds);
                scoreIntent.putExtra("Mode", BBDoubleCutMode);
                scoreIntent.putExtra("Grid", gridstr);
                scoreIntent.putExtra("Role", role);
                gameInProgress = false;
                gameboardset = false;
                created = false;
                startActivity(scoreIntent);
            }
        }


        @Override
        public void onTick(long millisUntilFinished) {

                if(gameInProgress) {
                    if(!gameboardset){
                        setGameBoard();
                        gameboard.setGameboard(gridstr);
                        gameboardset=true;
                    }
                    populateOppInfo();
                }
                timer = (TextView) findViewById(R.id.timer);
                if (millisUntilFinished / 1000 == 30) {
                    timer.setTextColor(Color.RED);
                }
                timer.setText("" + String.format("%02d", ((millisUntilFinished / 1000) / 60)) + ":" + String.format("%02d", ((millisUntilFinished / 1000) % 60)));

        }
    }
}
