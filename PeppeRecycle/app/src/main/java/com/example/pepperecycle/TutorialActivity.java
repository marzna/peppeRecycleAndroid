package com.example.pepperecycle;

import android.os.Bundle;
import android.text.Html;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

// Activity contenente il tutorial del gioco
public class TutorialActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "TutorialActivity" ;
    // Store the Chat action.
    private Chat chat;
    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;

    private TextView[] mDots;

    private SliderAdapter sliderAdapter;

    private Button buttonNext, buttonPrev;
    private int mCurrentPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_tutorial);

        mSlideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.dotsLayout);

        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrev);

        sliderAdapter = new SliderAdapter(this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        //OnClickListeners

        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage - 1);

            }
        });
    }

    public void addDotsIndicator(int position) {
        mDots = new TextView[3]; //TODO il 3?
        mDotLayout.removeAllViews();

        for(int i = 0; i< mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            mDotLayout.addView(mDots[i]);

        }

        if(mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.white));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            mCurrentPage = position;

            if (position == 0) { //prima pagina

                Log.d("Tap", "Prima pagina");
                buttonNext.setEnabled(true);
                buttonPrev.setEnabled(false);
                buttonPrev.setVisibility(View.INVISIBLE);

                buttonNext.setText("Avanti     >");
                buttonPrev.setText("");

            } else if (position == mDots.length - 1) { //ultima pagina
                Log.d("Tap", "Ultima pagina");
                buttonNext.setEnabled(true);
                buttonPrev.setEnabled(true);
                buttonPrev.setVisibility(View.VISIBLE);
                //https://youtu.be/byLKoPgB7yA?t=1168

                /*
                todo SECONDO ME
                buttonNext.setEnabled(false);
                buttonNext.setVisibility(View.INVISIBLE);
                */

                buttonNext.setText("Finito");
                buttonPrev.setText("<     Indietro");

            } else { //pagine intermedie

                Log.d("Tap", "Pagina intermedia");
                buttonNext.setEnabled(true);
                buttonPrev.setEnabled(true);
                buttonPrev.setVisibility(View.VISIBLE);

                buttonNext.setText("Avanti     >");
                buttonPrev.setText("<     Indietro");

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    //TODO https://www.youtube.com/watch?v=byLKoPgB7yA&ab_channel=TVACStudio
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // TODO Tutorial
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.greetings) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        // Add an on started listener to the Chat action. -> Avvisa quando la chat è iniziata
        chat.addOnStartedListener(() -> Log.i(TAG, "Discussion started."));

        // Run the Chat action asynchronously. -> Chat iniziata
        Future<Void> chatFuture = chat.async().run();

        // Add a lambda to the action execution. -> Ci avvisa che la conversazione è terminata con un errore
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Discussion finished with error.", future.getError());
            }
        });

    }

    @Override
    public void onRobotFocusLost() {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
/*
    public void goPrev(View view) {

        mSlideViewPager.setCurrentItem(mCurrentPage - 1);
    }

    public void goNext(View view) {

        mSlideViewPager.setCurrentItem(mCurrentPage + 1);
    }*/
}