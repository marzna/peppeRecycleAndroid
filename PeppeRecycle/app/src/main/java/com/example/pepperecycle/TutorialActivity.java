package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    boolean endOfTutorial;
    TextView tvExplaination;
    byte trialState; //Qui dovrebbe essere 0
    boolean lastPage = false; //ultima pagg del tutorial
    Button buttonSkipTutorial;
    private Button buttonNext, buttonPrev, buttonPlay;
    private int mCurrentPage;
    ImageView ivTutorial;
    private Animate animate;
    String currPhrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_tutorial);

        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrev);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonSkipTutorial = (Button) findViewById(R.id.buttonSkipTutorial);

        tvExplaination = (TextView) findViewById(R.id.tvExplaination);
        ivTutorial = (ImageView) findViewById(R.id.ivTutorial);

        pgIndex = 0;
        tutorialEnabled = true;
        endOfTutorial = false;
        trialState = 0;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
//            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }

        checkPage(pgIndex);
        Log.d(TAG, "Indice pagina:" + pgIndex);

        //OnClickListeners
        buttonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                nextTurn();
                endOfTutorial = true; //TODO Inutile? Forse è già stato inizializzato a true?
                nextPage();
            }
        });

        //OnClickListeners
        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                nextPage();
            }
        });
        buttonPrev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                prevPage();
            }
        });
        buttonSkipTutorial.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                pgIndex=3;
                startPage(pgIndex);
                endOfTutorial = true; //TODO Inutile? Forse è già stato inizializzato a true?
            }
        });

    }

    //TODO https://www.youtube.com/watch?v=byLKoPgB7yA&ab_channel=TVACStudio
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        //mCurrentPage
        Say tutorialIntro = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(currPhrase) // Set the text to say.
                //.withText(currPhrase + ". Posso andare avanti ora?") // Set the text to say.
                //.withText("Ecco come si gioca. a turno, il giudice ci mostra un oggetto e noi dobbiamo indovinare in quale bidone riciclarlo. Poi, il giudice deve dire se la risposta è corretta o no.. Chi indovina, guadagnerà un punto!") // Set the text to say.
                /*.withText("Ecco come si gioca. a turno, il giudice ci mostra un oggetto e noi dobbiamo indovinare in quale bidone riciclarlo. "/* +
                                "Poi, il giudice deve dire se la risposta è corretta o no.. " +
                                "Chi indovina, guadagnerà un punto!") // Set the text to say.*/
                .build(); // Build the say action.

        Say askForContinue = SayBuilder.with(qiContext) // Create the builder with the context.
                //.withText("Vuoi fare un turno di prova per imparare a giocare?") // Set the text to say.
                .withText("Tutto chiaro?") // Set the text to say.
                .build(); // Build the say action.;

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTutorial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();

        PhraseSet phraseSetNextPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "è chiaro", "sì", "sei stato chiaro", "Ho capito", "Vai avanti", "avanti", "ok", "okay", "ochei", "va bene")
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

        //askForContinue.run(); L'ho messo direttamente in currPhrase

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetNextPage, phraseSetBackPage, phraseSetPlay,
                        phraseSetRepeatPage, phraseSetRepeatFromPg0, phraseSetBackHome,
                        phraseSetClose)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNextPage)) {             // Risposta utente affermativa => va alla pagina successiva
            Say nextPage = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Perfetto!") // Set the text to say.
                    .build(); // Build the say action.
            if(lastPage) {
                endTutorial();
            } else { //Incrementa la pagina fin quando non si arriva all'ultima
                nextPage();
            }
            // buttonNext.performClick();

//            nextTurn(); //
           /* Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
            activity2Intent.putExtra("tutorialEnabled", true);
            activity2Intent.putExtra("round", 0);
            activity2Intent.putExtra("pepperScore", false);
            activity2Intent.putExtra("userScore", 0);
            //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
            startActivity(activity2Intent);
            finish();*/


        } /*else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlay)) {       // Va direttamente al gioco, saltando il tutorial
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
        }*/ else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatPage)) {   // Richiesta utente di ripetere la pagina corrente
            Say pepperRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, allora ripeterò quest'ultima parte. ") // Set the text to say.
                    .build(); // Build the say action.

            pepperRepeat.run();
            startPage(pgIndex);

        }  else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatFromPg0)) {   // Richiesta utente di ripetere
            Say pepperRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Ochei, allora ricomincio tutto da capo!") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            pepperRepeat.run();
            animateCorrect.run();

            pgIndex=0; //Ritorna alla prima pagina
            startPage(pgIndex);

/*          Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();*/

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
    /*void buttonPlay(View view) {
        nextTurn();
        *//*Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();*//*
     *//*Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
        activity2Intent.putExtra("tutorialEnabled", true);
        startActivity(activity2Intent); //Per iniziare il gioco.
        finish();*//*
    }*/
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

    public void nextTurn() { // Avvia la activity relativa al prossimo turno
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("endOfTutorial", endOfTutorial);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }

    int nPages = 4;

    // Incrementa la pagina. Se è l'ultima, ritorna -1
    public int increasesPage(int currPage) {
        if (currPage < (nPages-1)) {
            return ++currPage;
        } else {
            endOfTutorial = true;
            return -1;
        }
    }
    // Decrementa la pagina. Se è la prima, ritorna -1
    public int decreasesPage(int currPage) {
        if (currPage > 0) {
            endOfTutorial=false;
            return --currPage;
        } else {
            return -1;
        }
    }

    public void checkPage(int pgIndex) {
        switch (pgIndex) {
            case 0: // Prima pagina
                buttonPrev.setVisibility(View.INVISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                currPhrase = "A turno, il giudice ci mostrerà un oggetto e dovremo indovinare in quale bidone riciclarlo.";

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

                currPhrase = "Una volta scelto il tipo di bidone, il giudice dirà se è quello giusto. Posso andare avanti ora? .";

                tvExplaination.setText("Una volta scelto il tipo di bidone, il giudice dirà se è quello giusto.");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN ADEGUATO

                break;

            case 2:
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                currPhrase = "Se la risposta è corretta, chi ha indovinato guadagnerà un punto! Va bene? .";

                tvExplaination.setText("Se la risposta è corretta, chi ha indovinato guadagnerà un punto!");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN ADEGUATO

                break;

            case 3: // Ultima pagina
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.INVISIBLE);

                buttonPlay.setVisibility(View.VISIBLE);

                currPhrase = "Il tutòrial è finito! Vogliamo fare un turno di prova?"; //TODO non serve?
                endOfTutorial = true;

                tvExplaination.setText("Il tutorial è finito!\nVogliamo fare un turno di prova?");
                ivTutorial.setImageResource(R.drawable.bin_brown_shadow); //TODO METTI SCREEN ADEGUATO
                /*TODO Metti bottoni sì no.
                   Se l'utente risponde di sì, vai avanti con il turno di prova,
                    altrimenti inizia a giocare direttamente
                 */
                lastPage = true;

                break;

            default: //case 4: tutorial finito, dovrebbe essere pgIndex==-1
                //nextTurn(); //TODO Controlla se abbia senso
                break;
        }
    }

    int enterAnim, exitAnim;
    public void prevPage() {
     /* Va all'altra pagina, a meno che non ci troviamo nella prima.
    In quel caso, non fa nulla, anche se non dovrebbe verificarsi
    perché il bottone correlato non dovrebbe essere visibile
    */
        pgIndex = decreasesPage(pgIndex);
        enterAnim = R.anim.slide_in_left;
        exitAnim = R.anim.slide_out_right;
        startPage(pgIndex);
    }

    public void nextPage() {
    /* Va all'altra pagina, a meno che non ci troviamo nell'ultima.
    In quel caso, non fa nulla, anche se non dovrebbe verificarsi
    perché il bottone correlato non dovrebbe essere visibile
    */
        pgIndex = increasesPage(pgIndex);
        enterAnim = R.anim.slide_in_right;
        exitAnim = R.anim.slide_out_left;
        startPage(pgIndex);
        /* if (pgIndex != -1) { ... TODO Ricorda di testarlo,
                                      l'ho spostato nella funzione startPage bc era uguale per nextPage e prevPage
                                      se ci son problemi, rimettilo qui.*/
    }

    public void startPage(int pgIndex) {

        Intent activity2Intent = null;
        /*if (pgIndex == 0) {
            activity2Intent = new Intent(TutorialActivity.this, TutorialActivity.class);
        } else */if (endOfTutorial) {
            activity2Intent = new Intent(TutorialActivity.this, TutorialEndActivity.class);
        } else if (pgIndex != -1) {
//            Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
            activity2Intent = new Intent(TutorialActivity.this, TutorialActivity.class);
        }
        if (activity2Intent != null) {
            activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
            activity2Intent.putExtra("pgIndex", pgIndex); //passa il numero di pagina
            activity2Intent.putExtra("endOfTutorial", endOfTutorial);
//            activity2Intent.putExtra("lastPage", lastPage);
            startActivity(activity2Intent);
            overridePendingTransition(enterAnim, exitAnim);
            finish();
        }

    }

    public void buttonPlay() {
        //Todo metti inizio gioco in caso di turno prova e no
        Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        activity2Intent.putExtra("endOfTutorial", endOfTutorial);
        startActivity(activity2Intent); //Per iniziare il gioco.
        finish();
    }

    public void lastPage(View v) { //TODO Serve?
        endOfTutorial = true;
        Intent activity2Intent = new Intent(TutorialActivity.this, TutorialActivity.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        activity2Intent.putExtra("endOfTutorial", endOfTutorial);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }

    public void endTutorial() { //TODO Serve?
        endOfTutorial = true;
        //TODO fai distinzione turno di prova e non
        Intent activity2Intent = new Intent(TutorialActivity.this, PlayUserTurnActivity.class);
//        Intent activity2Intent = new Intent(TutorialActivity.this, TutorialActivity2.class);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("isPepperTurn", false); //toccherà all'utente in quanto è tutorial
        activity2Intent.putExtra("endOfTutorial", endOfTutorial);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        startActivity(activity2Intent);
        finish();
    }


}