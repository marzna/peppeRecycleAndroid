package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
<<<<<<< HEAD
import android.widget.ImageView;
=======
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
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
    private static final String TAG = "TutorialActivity";
    // Store the Chat action.
    private Chat chat;
    private int pgIndex;//Indice della pagina
    boolean tutorialEnabled;
<<<<<<< HEAD
    TextView tvExplaination;

    private Button buttonNext, buttonPrev, buttonPlay;
    private int mCurrentPage;
ImageView ivTutorial;
=======


    private Button buttonNext, buttonPrev, buttonPlay;
    private int mCurrentPage;

>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
    private Animate animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_tutorial);

<<<<<<< HEAD
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrev);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        tvExplaination = (TextView) findViewById(R.id.tvExplaination);
        ivTutorial = (ImageView) findViewById(R.id.ivTutorial);

        pgIndex = 0;
        tutorialEnabled = true;
        checkPage(pgIndex);

        /*//OnClickListeners
=======
       /* buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrev);
       */
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        pgIndex = 0;
        tutorialEnabled = true;

        //OnClickListeners
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
        buttonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
<<<<<<< HEAD
                activity2Intent.putExtra("tutorialEnabled", tutorialEnabled); //TODO va cambiato?
                startActivity(activity2Intent); //Per iniziare il gioco.
                finish();
            }
        });*/
=======
                activity2Intent.putExtra("tutorialEnabled", false); //TODO va cambiato?
                startActivity(activity2Intent); //Per iniziare il gioco.
                finish();
            }
        });
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427

        //OnClickListeners
      /*  buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
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
<<<<<<< HEAD
                //.withText("Ecco come si gioca. a turno, il giudice ci mostra un oggetto e noi dobbiamo indovinare in quale bidone riciclarlo. Poi, il giudice deve dire se la risposta è corretta o no.. Chi indovina, guadagnerà un punto!") // Set the text to say.
                .withText("Ecco come si gioca. a turno, il giudice ci mostra un oggetto e noi dobbiamo indovinare in quale bidone riciclarlo. "/* +
                        "Poi, il giudice deve dire se la risposta è corretta o no.. " +
                        "Chi indovina, guadagnerà un punto!"*/) // Set the text to say.
=======
                .withText("Ecco come si gioca. a turno, il giudice ci mostra un oggetto e noi dobbiamo indovinare in quale bidone riciclarlo. Poi, il giudice deve dire se la risposta è corretta o no.. Chi indovina, guadagnerà un punto!") // Set the text to say.
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
                .build(); // Build the say action.

        Say askForContinue = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Vuoi fare un turno di prova per imparare a giocare?") // Set the text to say.
                .build(); // Build the say action.;

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTutorial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();

        PhraseSet phraseSetNextPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "è chiaro", "sì", "sei stato chiaro", "Ho capito", "Vai avanti", "avanti", "ok", "okay", "ochei")
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

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetNextPage, phraseSetBackPage, phraseSetPlay,
                        phraseSetRepeatPage, phraseSetRepeatFromPg0, phraseSetBackHome,
                        phraseSetClose)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNextPage)) {             // Risposta utente affermativa
<<<<<<< HEAD
=======
            //if(mCurrentPage >= 0 && mCurrentPage <= 1) { //Se ci si trova alla prima pagg o in una delle pagg intermedie
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
            //Va alla pagina successiva
            Say nextPage = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Perfetto, vado avanti!") // Set the text to say.
                    .build(); // Build the say action.
            nextPage.run();
            //buttonNext.performClick();

<<<<<<< HEAD
            nextTurn();
           /* Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
=======
            Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
            activity2Intent.putExtra("tutorialEnabled", true);
            activity2Intent.putExtra("round", 0);
            activity2Intent.putExtra("pepperScore", false);
            activity2Intent.putExtra("userScore", 0);
            //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
            startActivity(activity2Intent);
<<<<<<< HEAD
            finish();*/

=======
            finish();

            //onRobotFocusGained(qiContext);
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlay)) {       // Va direttamente al gioco, saltando il tutorial
            Say playGame = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Ochei, allora iniziamo subito a giocare!") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.exclamation_both_hands_a001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            playGame.run();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", false); //TODO va cambiato?
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
    void buttonPlay(View view) {
<<<<<<< HEAD
        nextTurn();
        /*Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
=======
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", false);
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
<<<<<<< HEAD
        finish();*/
=======
        finish();
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
        /*Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
        activity2Intent.putExtra("tutorialEnabled", true);
        startActivity(activity2Intent); //Per iniziare il gioco.
        finish();*/
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

<<<<<<< HEAD
    public void nextTurn() { // Avvia la activity relativa al prossimo turno
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }


    public void buttonPlay() {
        Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled); //TODO va cambiato?
        startActivity(activity2Intent); //Per iniziare il gioco.
        finish();
    }

    public void checkPage(int pgIndex) {
        switch (pgIndex) {
            case 0: // Prima pagina
                buttonPrev.setVisibility(View.INVISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                tvExplaination.setText("A turno, il giudice ci mostrerà un oggetto" +
                        "\ne dovremo indovinare in quale bidone riciclarlo.");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN DEI BIDONI
               /*Una volta scelto il tipo di bidone,\nil giudice dovrà dire se avremo indovinato o no.
               \nSe sì, si guadagnerà un punto!"
*/
                break;

            case 1:
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                tvExplaination.setText("Una volta scelto il tipo di bidone, il giudice dirà se è quello giusto.");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN ADEGUATO

                break;

            case 2: // Ultima pagina
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.INVISIBLE);

                buttonPlay.setVisibility(View.VISIBLE);

                tvExplaination.setText("Se la risposta è corretta, chi ha indovinato guadagnerà un punto!");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN ADEGUATO

                break;

            default: //case 3:
                break;
        }
    }

    public void prevPage(View v) {
        Intent activity2Intent = new Intent(TutorialActivity.this, TutorialActivity1.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }

    public void nextPage(View v) {
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        startActivity(activity2Intent);
        finish();
    }
    public void lastPage(View v) { //TODO Serve?
        Intent activity2Intent = new Intent(TutorialActivity.this, TutorialActivity2.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
=======
    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", true);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }

<<<<<<< HEAD

=======
>>>>>>> f96443c050f3ec4c641a302197cd2349a7f3d427
}