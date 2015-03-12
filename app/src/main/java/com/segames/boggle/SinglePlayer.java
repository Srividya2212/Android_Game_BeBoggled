package com.segames.boggle;



import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

//import android.widget.TableRow;


public class SinglePlayer extends ActionBarActivity implements View.OnClickListener,GlobalConstants{



    /***************All Variables*******************/
    Gameboard gameboard;
    Drawable[] arrows = new Drawable[9];
    static int game_boardSize=4;
    //Adding test comment
    //Counter variables
    private final long startTime = BBSingleGameTime * 1000;
    private final long interval = 1 * 1000;
    private CountDownTimer countDownTimer;

    //Game state variables
    static int level = BBEasyLevel;
    static boolean gameInProgress = false;
    static int score = 0;
    static int numRounds = 1;
    static String selection="";

    //XML components
    Button button_submit;
    TextView my_wordlist;
    TextView timer;
    String my_list = "";

    //Shake-detection variables
    GestureDetector gestureDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeEventManager mShakeDetector;
    private String gridstr;
    Animation rotation;
    private Vibrator vibrator;
    //Animation red
    public boolean mContentLoaded;
    private View mContentView;
    private View mLoadingView;
    private int mShortAnimationDuration;


    /**********************Body of code ************************/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_player);
       //Code for Animation red
        mContentView = findViewById(R.id.single_player);
        mLoadingView = findViewById(R.id.red_layout);
        mLoadingView.setVisibility(View.GONE);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //Initialize game
        numRounds = getIntent().getExtras().getInt("Round");
        score = getIntent().getExtras().getInt("Score");
        //Log.v("Round",Integer.toString(numRounds));
        int blevelsize;
        if(numRounds<=BBMaxEasyRounds) {
            blevelsize = BBEasyLevelSize;
            //setContentView(R.layout.single_player3);
        }
        else{
            blevelsize = BBNormalLevelSize;
            //setContentView(R.layout.single_player4);
        }
        gameboard = new Gameboard(blevelsize);
        //set up shake button
        button_submit = (Button) findViewById(R.id.button_submit);
       button_submit.setOnClickListener(this);


        //Set up
        initializeBoardButtons();
        setTimer(this);
        setShakeDetection();
        setDoubleTap();

        //Display appropriate score, round, arrow button images
        TextView roundtext = (TextView) findViewById(R.id.round);
        roundtext.setText(Integer.toString(numRounds));
        TextView scoretext = (TextView) findViewById(R.id.score);
        scoretext.setText(Integer.toString(score));
        arrows= new Drawable[]{getResources().getDrawable(R.drawable.yellowtopleft2), getResources().getDrawable(R.drawable.yellowup_alt), getResources().getDrawable(R.drawable.yellowtopright2),
                getResources().getDrawable(R.drawable.yellowleft_alt), getResources().getDrawable(R.drawable.yellowdie),getResources().getDrawable(R.drawable.yellowright_alt), getResources().getDrawable(R.drawable.yellowbottomleft2), getResources().getDrawable(R.drawable.yellowdown_alt), getResources().getDrawable(R.drawable.yellowbottomright2)};

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

    }


    /* startNewGame: clears the state and sets up for a new game */
    void startNewGame()
    {
        gameInProgress=true;
        if(numRounds <= BBMaxEasyRounds) level = BBEasyLevel;
        else level = BBNormalLevel;

        if(level == BBEasyLevel){ game_boardSize = BBEasyLevelSize; gameboard.hideButtons();}
        else { game_boardSize = BBNormalLevelSize; gameboard.showButtons();}

    }

    void shakeGrid(int length){

        if(!gameInProgress){
            setAuxiliary();
            setGameBoard();
            startNewGame();

        }
    }

    void setAuxiliary()
    {
        my_wordlist = (TextView)findViewById(R.id.my_wordlist);
        if(my_wordlist != null){my_list = my_wordlist.getText().toString(); my_wordlist.setMovementMethod(new ScrollingMovementMethod());}

        TableLayout listtable = (TableLayout)findViewById(R.id.table2);
        listtable.setVisibility(View.VISIBLE);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void wordfinalize(){
        //button_submit = (Button) findViewById(R.id.button_submit);

        if(selection.length()>=3) {
            gameboard.opaqueButtons(getResources().getDrawable(R.drawable.whitedie));
            int tempscore = wordscore(selection);
            if (tempscore > 0) {
                MediaPlayer mp = MediaPlayer.create(this,R.raw.glass_ping);
                mp.start();
                my_list = my_list.concat("\n" + selection);
                //Log.v("Tag", selection);
                my_wordlist.setText(my_list);
                score += tempscore;
                setScore(score);
            } else {
                //System.out.println("score: "+tempscore);
                String str = (tempscore == -999) ? "Selected!" : "Bad Word!";
                vibrator.vibrate(50);
                //Animation red
                mContentLoaded = !mContentLoaded;
                showContentOrLoadingIndicator(mContentLoaded);
                //Sound effect
                if(str.equals("Bad Word!")){
                    MediaPlayer mp = MediaPlayer.create(this,R.raw.badsound);
                    mp.start();
                }
                if(str.equals("Selected!")){
                    MediaPlayer mp = MediaPlayer.create(this,R.raw.selected);
                    mp.start();
                }
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

        if(selection.length() >= BBMinWordLength)
        {
            String serverreply = CommManager.SendServer("word",selection);
            score = Integer.parseInt(serverreply);
        }

        return score;

    }


    //Shake related functions
    void setShakeDetection()
    {
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
            }
        });
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

    //Gameboard related functions
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
                //gameboard.buttons[i][j].setLayoutParams(new TableRow.LayoutParams(210, 210));
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

        if(numRounds>BBMaxEasyRounds){gameboard.showButtons();}else{gameboard.hideButtons();}

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
                gameboard.setGameboard(gridstr);
                countDownTimer.start();
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
        startWobble();
        int gamelevel = (numRounds>BBMaxEasyRounds)?BBNormalLevel:BBEasyLevel;
        String str = CommManager.RequestNewGrid(gamelevel, this);
        gridstr=str;
        //Log.v("strlen",Integer.toString(str.length()));
    }
    boolean ignore = false;
    //All button stuff: click, double tap, pressing back
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
                break;
            default:
/*
                if(current_button==gameboard.getpreviousclick()){
                    if(ignore == false){
                        current_button.setBackground(getResources().getDrawable(R.drawable.whitedie));
                        ignore = true;
                    }
                    else if(ignore == true){
                        ignore = false;
                    }
                }*/
                if(gameInProgress && gameboard.isvalidclick(current_button.getId())) {
                    //current_button.setAlpha(0.55f);
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
    public void onBackPressed() {
        //gameInProgress=false;
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

    void setTimer(Context c){
        countDownTimer = new CountDownTimerActivity(startTime, interval,c);
        timer = (TextView) findViewById(R.id.timer);
        timer.setText("" + String.format("%02d",((startTime/1000)/60))+":"+String.format("%02d",((startTime/1000)%60)));
    }

    //Class that handles Timer activity
    public class CountDownTimerActivity extends CountDownTimer {
        Context context;
        public CountDownTimerActivity(long startTime, long interval, Context context) {
            super(startTime, interval);
            this.context=context;
        }

        @Override
        public void onFinish() {
            Intent scoreIntent = new Intent(context, Score.class);

            scoreIntent.putExtra("Score",score);
            scoreIntent.putExtra("Round",numRounds);
            scoreIntent.putExtra("Mode", BBSingleMode);
            scoreIntent.putExtra("Grid",gridstr);
            gameInProgress=false;

            startActivity(scoreIntent);
        }


        @Override
        public void onTick(long millisUntilFinished) {
            timer = (TextView) findViewById(R.id.timer);
            if(millisUntilFinished/1000 == 30){
                timer.setTextColor(Color.RED);
            }
            timer.setText("" + String.format("%02d",((millisUntilFinished/1000)/60))+":"+String.format("%02d",((millisUntilFinished/1000)%60)));
        }
    }
}
