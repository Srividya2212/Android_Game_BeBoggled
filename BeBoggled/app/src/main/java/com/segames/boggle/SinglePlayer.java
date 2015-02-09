package com.segames.boggle;



import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SinglePlayer extends ActionBarActivity implements View.OnClickListener,GlobalConstants{

    static final int maxEasyRounds = 3;
    static final int easy = 0;
    static int level = easy;
    static final int normal = 1;
    static final int easy_size = 3;
    static final int normal_size = 4;

    private final long startTime = 180 * 1000;
    private final long interval = 1 * 1000;
    private CountDownTimer countDownTimer;

    //Game set-up utility variables
    static char[][] letters = null;
    static int game_boardSize=4;
    //Game state variables
    static boolean gameInProgress = false;
    static int score = 0;
    static int numRounds = 1;
    //static int mode = basic;
    static String selection="";
    //XML components
    Button button_submit;
    Button button_exit;
    TextView my_wordlist;
    TextView timer;
    String my_list = "";
    TextView opp_wordlist;
    String opp_list = "";
    //static Button[][] game_buttons = new Button[normal_size][normal_size];
    Gameboard gameboard;
    CommManager mgr;
    int numPlayers;
    GestureDetector gestureDetector;
    //Shake-detection variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeEventManager mShakeDetector;


    /* OnCreate - All the start-up stuff here */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_player);

        numPlayers = getIntent().getExtras().getInt("NumPlayers");
        gameboard = new Gameboard(BBEasyLevelSize);

        button_submit = (Button) findViewById(R.id.button_submit);
        button_submit.setOnClickListener(this);

        initializeBoardButtons();
        setTimer(this);
        setShakeDetection();
        setDoubleTap();

        //startNewGame();


    }

    void setTimer(Context c){
        countDownTimer = new CountDownTimerActivity(startTime, interval,c);
        timer = (TextView) findViewById(R.id.timer);
        timer.setText("" + String.format("%02d",((startTime/1000)/60))+":"+String.format("%02d",((startTime/1000)%60)));
        //timer.setText(String.valueOf((startTime/1000)/60)+":"+String.valueOf((startTime/1000)%60));
    }
    void setDoubleTap()
    {
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                //button_submit = (Button) findViewById(R.id.button_submit);
                gameboard.opaqueButtons();
                int tempscore = wordscore(selection);
                if( tempscore > 0){
                    my_list = my_list.concat("\n"+selection);
                    Log.v("Tag",selection);
                    my_wordlist.setText(my_list);
                    score+=tempscore;
                    setScore(score);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Bad Word!",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.LEFT, 400, 400);
                    toast.show();
                }
                gameboard.clearpreviousclick();
                selection="";
                return true;
            }
        });
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
            String serverreply = CommManager.SendServer("word",selection);
            score = Integer.parseInt(serverreply);
        }

        return score;

    }
    void shakeGrid(int length){

        if(!gameInProgress){
            setAuxiliary();
            setGameBoard();
            startNewGame();
            countDownTimer.start();
        }
    }

    /* startNewGame: clears the state and sets up for a new game */
    void startNewGame()
    {
        score = 0;
        gameInProgress=true;
        if(numRounds <= maxEasyRounds) level = easy;
        else level = normal;

        if(level == easy){ game_boardSize = easy_size; gameboard.hideButtons();}
        else { game_boardSize = normal_size; gameboard.showButtons();}

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

        //setGameBoard();

    }

    void setGameBoard()
    {
        String str = CommManager.RequestNewGrid(BBEasyLevel, this);
        Log.v("strlen",Integer.toString(str.length()));
        gameboard.setGameboard(str);
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
                //shakeGrid(game_boardSize*game_boardSize);
                shakeGrid(gameboard.size*gameboard.size);
                //startNewGame();
                button_submit.setVisibility(View.GONE);
            }
        });
    }

    void setAuxiliary()
    {

        my_wordlist = (TextView)findViewById(R.id.my_wordlist);
        if(my_wordlist != null)my_list = my_wordlist.getText().toString();

        my_wordlist.setMovementMethod(new ScrollingMovementMethod());
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


    @Override
    public void onClick(View v) {
        Button current_button= (Button) v;
        switch(v.getId()){
            case R.id.button_submit:
                shakeGrid(gameboard.size*gameboard.size);
                //startNewGame();
                current_button.setVisibility(View.GONE);
                break;
            default:
                if(gameInProgress && gameboard.isvalidclick(current_button.getId())) {
                    current_button.setAlpha(0.25f);
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
