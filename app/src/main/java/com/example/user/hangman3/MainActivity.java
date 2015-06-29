package com.example.user.hangman3;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    int currentGameMode=-1;
    int displayWidth;
    int currentQuestionNumber;
    int currentCount;
    int lifeCounter=14;
    int score=0;
    int hintCount=4;

    question currentWord;

    question[] questions;

    TableLayout keyboardContainer;
    Typeface typeface;
    RelativeLayout mainMenuContainer;
    TextView scoreContainer;
    TextView questionContainer;
    ImageView personContainer;

    LinearLayout letterContainer;
    LinearLayout hintContainer;

    ImageView easyButton, mediumButton, hardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/arnamu.ttf");

        Locale locale = new Locale("hy");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        initKeyboard();


        mainMenuContainer=(RelativeLayout)findViewById(R.id.mainMenu);
        mainMenuContainer.setVisibility(View.VISIBLE);
        mainMenuContainer.bringToFront();

        easyButton = (ImageView) findViewById(R.id.easyButton);
        mediumButton = (ImageView) findViewById(R.id.mediumButton);
        hardButton = (ImageView) findViewById(R.id.hardButton);

        easyButton.setOnClickListener(startGameButtonClick);
        mediumButton.setOnClickListener(startGameButtonClick);
        hardButton.setOnClickListener(startGameButtonClick);

        scoreContainer=(TextView) findViewById(R.id.score);
        scoreContainer.setTypeface(typeface);

        Display display = getWindowManager().getDefaultDisplay();
        displayWidth=display.getWidth();

        scoreContainer.setText("\u0540\u0561\u0577\u056B\u057E: 0");

        questionContainer = (TextView) findViewById(R.id.questionContainer);
        letterContainer = (LinearLayout) findViewById(R.id.letterContainer);
        personContainer=(ImageView)findViewById(R.id.personContainer);
        hintContainer=(LinearLayout)findViewById(R.id.hintCountContainer);
        hintContainer.setOnClickListener(skipButtonClick);


        initQuestions();
        nextWord();
    }

    private View.OnClickListener skipButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            currentWord.a.length();
        }
    };

    private View.OnClickListener startGameButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            String mode;
            if (v.getId()==R.id.easyButton)
                currentGameMode=0;
            else if (v.getId()==R.id.mediumButton)
                currentGameMode=1;
            else
                currentGameMode=2;
            initQuestions();
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(300);
            mainMenuContainer.startAnimation(anim);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    mainMenuContainer.setVisibility(View.GONE);

                }
            });
        }
    };

    private void initQuestions()
    {
        questions=questionDatabase.getArray(currentGameMode);
        currentQuestionNumber=-1;
    }

    private void initKeyboard()
    {
        int i,j;
        String[][] keyboardLayout=new String[][]{
                {"\u0567", "\u0569", "\u0583", "\u0571", "\u057B", "\u0587", "\u0580", "\u0579", "\u0573", "\u056A"},
                {"\u0584", "\u0578", "\u0535", "\u057C", "\u057F", "\u0568", "\u0578\u0582", "\u056B", "\u0585", "\u057A"},
                {"\u0561", "\u057D", "\u0564", "\u0586", "\u0563", "\u0570", "\u0575", "\u056F", "\u056C", "\u0577"},
                {"\u0566", "\u0572", "\u0581", "\u057E", "\u0562", "\u0576", "\u0574", "\u056D", "\u056E"}
        };

        //scoreContainer.setTypeface(typeface);
        //questionContainer.setTypeface(typeface);

        keyboardContainer = (TableLayout) findViewById(R.id.myKeyboard);
        TableRow currentRow;
        Button currentButton;
        for (i=0;i<keyboardContainer.getChildCount();i++)
        {
            currentRow=(TableRow) keyboardContainer.getChildAt(i);
            for (j=0;j<currentRow.getChildCount();j++)
            {

                currentButton=(Button)(currentRow.getChildAt(j));
                currentButton.setText(keyboardLayout[i][j].toLowerCase());
                currentButton.setTypeface(typeface);
                currentButton.setEnabled(true);
                currentButton.setTransformationMethod(null);
                currentButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_button));

                currentButton.setOnClickListener(keyboardButtonPress);
            }
        }
    }

    private void clearKeyboard()
    {
        TableRow currentRow;
        Button currentButton;
        int i,j;
        for (i=0;i<keyboardContainer.getChildCount();i++)
        {
            currentRow=(TableRow) keyboardContainer.getChildAt(i);
            for (j=0;j<currentRow.getChildCount();j++)
            {
                currentButton=(Button)(currentRow.getChildAt(j));
                currentButton.setEnabled(true);
                currentButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_button));
            }
        }
    }

    private View.OnClickListener keyboardButtonPress = new View.OnClickListener() {
        @SuppressWarnings("deprecation")
        public void onClick(View v) {

            long startTime= System.currentTimeMillis();

            Button currentButton=(Button)(v);
            // 	tmpTextBox.setText(tmpTextBox.getText()+currentButton.getText().toString());
            if(lifeCounter<0 || currentCount<0)
                return;

            //currentButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_invalid));
            //currentButton.setEnabled(false);

            //currentButton.setText("v");


            //currentButton.setGravity(Gravity.TOP);
            //currentButton.setGravity(Gravity.CENTER_HORIZONTAL);

            int i,cnt=0;
            Boolean exists=false;
            String ans=currentWord.a;
            String pressedText=currentButton.getText().toString();

            for(i=0;i<ans.length();++i,++cnt)
            {
                if(i+1<ans.length() && ans.charAt(i+1)=='\u0582')
                {
                    if(pressedText.equals("\u0578\u0582"))
                    {
                        ((TextView)letterContainer.getChildAt(cnt)).setText(pressedText);
                        exists=true;
                        --currentCount;
                    }
                    ++i;
                }
                else
                {
                    if(pressedText.equals(""+ans.charAt(i)))
                    {
                        ((TextView)letterContainer.getChildAt(cnt)).setText(pressedText);
                        exists=true;
                        --currentCount;
                    }
                }
            }
            if(!exists)
            {
                currentButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_invalid));
                --lifeCounter;
                final int[] imageNames=new int[]{R.drawable.sm_0,R.drawable.sm_1,R.drawable.sm_2,R.drawable.sm_3,R.drawable.sm_4,R.drawable.sm_5,R.drawable.sm_6,R.drawable.sm_7,R.drawable.sm_8,R.drawable.sm_9,R.drawable.sm_10,R.drawable.sm_11,R.drawable.sm_12,R.drawable.sm_13,R.drawable.sm_14};
                //personContainer.setImageResource(imageNames[lifeCounter]);

                final Handler imageChangeHandler = new Handler();
                imageChangeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (lifeCounter<0)
                            lifeCounter=0;
                            personContainer.setImageResource(imageNames[lifeCounter]);
                    }
                }, 10);

            }

            if(lifeCounter==0)
            {
                //lifeCounter--;
                for(cnt=0,i=0;i<ans.length();++i,++cnt)
                {
                    if(i+1<ans.length() && ans.charAt(i+1)=='?')
                    {
                        ((TextView)letterContainer.getChildAt(cnt)).setText("\u0578\u0582");
                        ++i;
                    }
                    else
                        ((TextView)letterContainer.getChildAt(cnt)).setText(ans.charAt(i)+"");
                    ((TextView)letterContainer.getChildAt(cnt)).setBackgroundColor(Color.RED);
                }

                final Handler wrongHandler = new Handler();
                wrongHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Intent myIntent = new Intent(MainActivity.this, YouLostDialog.class);
                        //myIntent.putExtra("score", "" + score); //Optional parameters
                        //myIntent.putExtra("mode", mode);
                        //MainActivity.this.startActivity(myIntent);
                        //MainActivity.this.finish();
                    }
                }, 1750);


                //krvar
            }

            currentButton.setEnabled(false);
            if(currentCount==0)
            {
                currentCount--;
                ++score;
                scoreContainer.setText("\u0540\u0561\u0577\u056B\u057E: "+score);

                for(cnt=0;cnt<currentWord.a.length()-currentWord.a.length()+currentWord.a.replace("\u0582", "").length();++cnt)
                    ((TextView)letterContainer.getChildAt(cnt)).setBackgroundColor(Color.parseColor("#2a7907"));


                final Handler rightHandler = new Handler();
                rightHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clearKeyboard();
                        nextWord();
                    }
                }, 800);
            }
            Log.v("time",""+(System.currentTimeMillis()-startTime));
        }
    };

    private void nextWord()
    {
        initKeyboard();
        while(!wordFits(questions[++currentQuestionNumber].a)){}

        currentWord=questions[currentQuestionNumber];
        questionContainer.setText(currentWord.q);
        currentCount=currentWord.a.length()-currentWord.a.length()+currentWord.a.replace("\u0582", "").length();
        renderLetters(currentCount);
    }
    private boolean wordFits(String word){
        float textWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        if (displayWidth<(word.length())*(textWidth+10)-10)
            return false;
        return true;
    }
    //	@SuppressWarnings("deprecation")
    private void renderLetters(int count)
    {
        int i;

        letterContainer.removeAllViews();

        float textHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams textViewLayout=new LinearLayout.LayoutParams((int) textHeight, ActionBar.LayoutParams.MATCH_PARENT);
        textViewLayout.setMargins(5, 0, 5, 0);


        for (i=0;i<count;i++)
        {
            TextView currentText= new TextView(this);
            currentText.setText("");
            currentText.setBackgroundResource(R.drawable.letter_shape);
            currentText.setGravity(Gravity.CENTER);
            currentText.setLayoutParams(textViewLayout);
            currentText.setTextColor(Color.WHITE);
            currentText.setTypeface(typeface);
            letterContainer.addView(currentText);
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
}
