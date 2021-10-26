package com.example.pepperecycle;

import android.content.Intent;
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
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Activity contenente il tutorial del gioco
public class TutorialActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "TutorialActivity" ;
    // Store the Chat action.
    private Chat chat;
    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;
    private int pgIndex;//Indice della pagina
    boolean tutorialEnabled;
    private TextView[] mDots;

    private SliderAdapter sliderAdapter;

    private Button buttonNext, buttonPrev, buttonPlay;
    private int mCurrentPage;

    private Animate animate;

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
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        pgIndex = 0;
        tutorialEnabled = true;

       /* sliderAdapter = new SliderAdapter(this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mCurrentPage = extras.getByte("mCurrentPage"); // The key argument here must match that used in the other activity
        }*/

        //OnClickListeners
        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

       /* buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage - 1);

            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
                startActivity(activity2Intent); //Per andare alla pagina principale
                finish();

            }
        });*/
    }

    //TODO https://www.youtube.com/watch?v=byLKoPgB7yA&ab_channel=TVACStudio
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

//        mCurrentPage
        Say tutorialIntro = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Ecco come si gioca: " +
                        "A turno, il giudice ci mostra un oggetto." +
                        "Noi dobbiamo indovinare in quale bidone riciclarlo." +
                        "Poi, il giudice deve dire se la risposta è corretta o no." +
                        "Chi indovina, guadagnerà un punto!") // Set the text to say.
                .build(); // Build the say action.
        Say sayUserTurn = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Durante il tuo turno." +
                        "Qui, sul mio tablet, ti mostrerò quattro bidoni: " +
                        "organico, plastica e metalli, carta e cartone, vetro." +
                        "Dovrai dirmi dove buttare il rifiuto mostrato dal giudice." +
                        "Come si fa? È semplice!" +
                        "Basta che tu mi indichi " +
                        "la tipologia di rifiuto, il colore del bidone, oppure il numero." +
                        "Una volta che avrai scelto il bidone," +
                        "il giudice stabilirà se avrai indovinato o no!") // Set the text to say.
                .build(); // Build the say action.;

        Say sayPepperTurn = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Durante il mio turno," +
                        "il giudice mi mostrerà l’oggetto " +
                        "e dirò dove andrebbe gettato quel rifiuto, secondo me!" +
                        "Una volta che avrò scelto il bidone," +
                        "il giudice stabilirà se avrò indovinato o no!") // Set the text to say.
                .build(); // Build the say action.;

        Say askForContinue = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Vuoi fare un turno di prova per imparare a giocare?") // Set the text to say.
                .build(); // Build the say action.;

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTutorial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();

        PhraseSet phraseSetNextPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "è chiaro", "sei stato chiaro", "Ho capito", "Vai avanti", "avanti", "ok", "okay", "ochei")
                .build();

        PhraseSet phraseSetPlay = PhraseSetBuilder.with(qiContext)
                .withTexts("Giochiamo", "Voglio giocare").build();

        PhraseSet phraseSetRepeatPage = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "Non è chiaro", "Ripeti",
                        "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetRepeatFromPg0 = PhraseSetBuilder.with(qiContext)
                .withTexts("Ricominciamo", "Ricomincia", "Da capo", "Dall'inizio", "all'inizio")
                .build();

        PhraseSet phraseSetBackPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro")
                .build();

        PhraseSet phraseSetBackHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna alla Home", "Menu principale", "vai al menu principale", "torna al menu principale")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        animateAskTutorial.run();
        tutorialIntro.run();
        askForContinue.run();
        /*
        switch (mCurrentPage) { //in base alla scelta, spiega la pagina diversa
            case 0:
                tutorialIntro.run();
                askForContinue.run();
                break;
            case 1:
                sayUserTurn.run();
                askForContinue.run();
                break;
            case 2:
                sayPepperTurn.run();
                break;
            default:
                break;
        }
        */

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets( phraseSetNextPage, phraseSetBackPage, phraseSetPlay,
                        phraseSetRepeatPage, phraseSetRepeatFromPg0, phraseSetBackHome,
                        phraseSetClose )
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNextPage)) {             // Risposta utente affermativa
            //if(mCurrentPage >= 0 && mCurrentPage <= 1) { //Se ci si trova alla prima pagg o in una delle pagg intermedie
            //Va alla pagina successiva
            Say nextPage = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Perfetto, vado avanti!") // Set the text to say.
                    .build(); // Build the say action.
            nextPage.run();
            //buttonNext.performClick();

            Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
            activity2Intent.putExtra("tutorialEnabled", true);
            activity2Intent.putExtra("round", 0);
            activity2Intent.putExtra("pepperScore", false);
            activity2Intent.putExtra("userScore", 0);
            //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
            startActivity(activity2Intent);
            finish();

            //onRobotFocusGained(qiContext);

        }  else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlay)) {       // Va direttamente al gioco, saltando il tutorial
            Say playGame= SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Ochei, allora iniziamo subito a giocare!") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.exclamation_both_hands_a001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            playGame.run();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", true);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatPage)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();

        /* } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatFromPg0)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish(); */
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetBackHome)) {     // Torna alla home
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent);
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) {    // Chiude il gioco
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a004).build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, sto chiudendo il gioco. Ciaoo!") // Set the text to say.
                    .build(); // Build the say action.

            sayGoodbye.run();
            animate.run();

            finish();

        }
        // TODO Tutorial
        /*Uso di QiChatbot. Non dovrebbe essere necessario in questo caso
        //https://android.aldebaran.com/sdk/doc/pepper-sdk/ch4_api/conversation/tuto/execute_tutorial.html
        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.tutorial) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();
        Map<String, QiChatExecutor> executors = new HashMap<>();

        // Map the executor name from the topic to our qiChatExecutor
        executors.put("myExecutor", new MyQiChatExecutor(qiContext));

        // Set the executors to the qiChatbot
        qiChatbot.setExecutors(executors);

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

        // Execute the chat asynchronously
        final Future<Void> fchat = chat.async().run();

        // Stop the chat when the qichatbot is done
        //Codice completo dalla documentazione -> https://android.aldebaran.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/qichatbot.html#:~:text=withTopic(topic)%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20.build()%3B-,Ending%20a%20chat,-%C2%B6
        qiChatbot.addOnEndedListener(endReason -> {
            Log.i(TAG, "qiChatbot end reason = " + endReason);
            fchat.requestCancellation();
        });*/


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
    public void addDotsIndicator(int position) {
        mDots = new TextView[3]; //TODO il 3?
        mDotLayout.removeAllViews();

        for(int i = 0; i< mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));
            //mDots[i].setTextColor(getResources().getColor(R.color.colorGreenBtn));

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

            pgIndex = position; //TODO Rimuovi se inutile?
            if (position == 0) { //prima pagina
                Log.d("Tap", "Prima pagina");

                buttonPrev.setEnabled(false);
                buttonPrev.setVisibility(View.INVISIBLE);

                buttonNext.setEnabled(true);

                buttonPlay.setEnabled(false);

                *//*buttonNext.setText("Avanti     >");
                buttonPrev.setText("");*//*

            } else if (position == mDots.length - 1) { //ultima pagina
                Log.d("Tap", "Ultima pagina");
                buttonPrev.setEnabled(true);
                buttonPrev.setVisibility(View.VISIBLE);

                buttonNext.setEnabled(false);
                buttonNext.setVisibility(View.INVISIBLE);

                buttonPlay.setEnabled(true);
                buttonPlay.setVisibility(View.VISIBLE);
                //https://youtu.be/byLKoPgB7yA?t=1168

                *//*
                buttonNext.setBackgroundResource(R.drawable.button_play);
                buttonNext.setText("Finito");
                buttonPrev.setText("<     Indietro");
                *//*

            } else { //pagine intermedie

                Log.d("Tap", "Pagina intermedia");

                buttonPrev.setEnabled(true);
                buttonPrev.setVisibility(View.VISIBLE);

                buttonNext.setEnabled(true);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setEnabled(false);
                buttonPlay.setVisibility(View.INVISIBLE);

                *//*
                buttonNext.setBackgroundResource(R.drawable.button_next);
                buttonNext.setText("Avanti     >");
                buttonPrev.setText("<     Indietro");
                *//*

            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };*/

/*
    public void goPrev(View view) {

        mSlideViewPager.setCurrentItem(mCurrentPage - 1);
    }

    public void goNext(View view) {

        mSlideViewPager.setCurrentItem(mCurrentPage + 1);
    }*/
}