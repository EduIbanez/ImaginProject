package eina.imagine;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    /** Custom variables */
    private SwipeDeck cardStack;
    private ArrayList<State> testData;
    private SwipeAdapter adapter;
    private Properties properties;
    private State actual;
    private State actual1;
    private State actual2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setVisibility(View.INVISIBLE);

        //example of buttons triggering events on the deck
        Button btn = (Button) findViewById(R.id.button);
        Button btn2 = (Button) findViewById(R.id.button2);
        Button btn3 = (Button) findViewById(R.id.button3);

        btn3.setVisibility(View.INVISIBLE);

        properties = new Properties();
        try {
            properties.load(getApplicationContext().getAssets().open("automata.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        Automata auto = new Automata("label.inicio", properties.get("label.inicio").toString());
        actual = auto.readDecision();
        actual.setPathImage("label_inicio");

        testData = new ArrayList<>();
        testData.add(actual);

        Automata auto1 = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
        actual1 = auto1.readDecision();

        Automata auto2 = new Automata(actual.getSonB(), properties.get(actual.getSonB()).toString());
        actual2 = auto2.readDecision();

        btn.setText(actual1.getLine().toString());
        btn2.setText(actual2.getLine().toString());



        adapter = new SwipeAdapter(testData, this);
        if(cardStack != null){
            cardStack.setAdapter(adapter);
        }
        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long positionInAdapter) {
                Log.i("FullscreenActivity", "card was swiped left, position in adapter: " + positionInAdapter);

                Automata auto = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
                State decisionTomada = auto.readDecision();
                /** Calcular siguiente estado */
                String nuevo = auto.nextState(decisionTomada);
                auto = new Automata(nuevo, properties.get(nuevo).toString());
                actual = auto.readDecision();

                actual.setPathImage(auto.getPath());
                adapter.addCard(actual);

                Button btn = (Button) findViewById(R.id.button);
                Button btn2 = (Button) findViewById(R.id.button2);
                Button btn3 = (Button) findViewById(R.id.button3);

                if (actual.getSonA() != null && actual.getSonB() != null) {
                    Automata auto1 = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
                    actual1 = auto1.readDecision();

                    Automata auto2 = new Automata(actual.getSonB(), properties.get(actual.getSonB()).toString());
                    actual2 = auto2.readDecision();

                    btn.setText(actual1.getLine().toString());
                    btn2.setText(actual2.getLine().toString());
                    btn.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.VISIBLE);
                    btn3.setVisibility(View.INVISIBLE);
                } else {
                    auto = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
                    actual = auto.readDecision();
                    actual.setPathImage(auto.getPath());
                    adapter.addCard(actual);

                    btn3.setText(actual.getLine().toString());
                    btn.setVisibility(View.INVISIBLE);
                    btn2.setVisibility(View.INVISIBLE);
                    btn3.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void cardSwipedRight(long positionInAdapter) {
                Log.i("FullscreenActivity", "card was swiped right, position in adapter: " + positionInAdapter);
                Automata auto = new Automata(actual.getSonB(), properties.get(actual.getSonB()).toString());
                State decisionTomada = auto.readDecision();
                /** Calcular siguiente estado */
                String nuevo = auto.nextState(decisionTomada);
                auto = new Automata(nuevo, properties.get(nuevo).toString());
                actual = auto.readDecision();

                actual.setPathImage(auto.getPath());
                adapter.addCard(actual);

                Button btn = (Button) findViewById(R.id.button);
                Button btn2 = (Button) findViewById(R.id.button2);
                Button btn3 = (Button) findViewById(R.id.button3);

                if (actual.getSonA() != null && actual.getSonB() != null) {
                    ImageView img = (ImageView) findViewById(R.id.offer_image);
                    int id = getResources().getIdentifier("eina.imagine:drawable/" + actual.getPathImage(), null, null);
                    img.setImageResource(id);
                    Automata auto1 = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
                    actual1 = auto1.readDecision();

                    Automata auto2 = new Automata(actual.getSonB(), properties.get(actual.getSonB()).toString());
                    actual2 = auto2.readDecision();

                    btn.setText(actual1.getLine().toString());
                    btn2.setText(actual2.getLine().toString());
                    btn.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.VISIBLE);
                    btn3.setVisibility(View.INVISIBLE);
                } else {
                    auto = new Automata(actual.getSonA(), properties.get(actual.getSonA()).toString());
                    actual = auto.readDecision();
                    actual.setPathImage(auto.getPath());
                    adapter.addCard(actual);
                    btn3.setText(actual.getLine().toString());
                    btn.setVisibility(View.INVISIBLE);
                    btn2.setVisibility(View.INVISIBLE);
                    btn3.setVisibility(View.VISIBLE);

                }
            }
        });

        //cardStack.setLeftImage(R.id.left_image);
        //cardStack.setRightImage(R.id.right_image);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardLeft(180);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardStack.swipeTopCardRight(180);
            }
        });


        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
