package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

// Activity contenente il tutorial del gioco
public class TutorialActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "TutorialActivity";

    private int pgIndex; //Indice della pagina
    boolean tutorialEnabled;
    boolean endOfTutorial;
    TextView tvExplaination;
    byte trialState;
    boolean lastPage = false; //ultima pag del tutorial
    Button buttonSkipTutorial;
    private Button buttonNext, buttonPrev, buttonPlay;
    ImageView ivTutorial;
    private Animate animate;
    String currPhrase;
    int nPages = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_tutorial);

        buttonNext = findViewById(R.id.buttonNext);
        buttonPrev = findViewById(R.id.buttonPrev);
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonSkipTutorial = findViewById(R.id.buttonSkipTutorial);
        tvExplaination = findViewById(R.id.tvExplaination);
        ivTutorial = findViewById(R.id.ivTutorial);

        pgIndex = 0;
        tutorialEnabled = true;
        endOfTutorial = false;
        trialState = 0;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
        }

        checkPage(pgIndex);
        Log.d(TAG, "Indice pagina:" + pgIndex);

        //OnClickListeners
        buttonPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                endOfTutorial = true;
                startPage(-1);
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
                startPage(-1);
                endOfTutorial = true;
            }
        });

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say tutorialIntro = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(currPhrase) // Set the text to say.
                .build(); // Build the say action.

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTutorial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();

        PhraseSet phraseSetNextPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "è chiaro", "sì", "sei stato chiaro", "Ho capito", "Vai avanti", "avanti", "ok", "okay", "va bene")
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

        PhraseSet phraseSetSkipTutorial = PhraseSetBuilder.with(qiContext)
                .withTexts("Salta", "Salta il tutorial", "ignora il tutorial", "ignora", "non mi interessa")
                .build();

        PhraseSet phraseSetBackPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro")
                .build();

        PhraseSet phraseSetBackHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna alla Home", "Menu principale", "vai al menu principale", "torna al menu principale")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta", "Stop")
                .build();

        tutorialIntro.run();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetNextPage, phraseSetBackPage, phraseSetPlay,
                        phraseSetRepeatPage, phraseSetRepeatFromPg0, phraseSetBackHome,
                        phraseSetClose, phraseSetSkipTutorial)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNextPage)) {             // Risposta utente affermativa => va alla pagina successiva
            if(lastPage) {
                endOfTutorial=true;
                startPage(pgIndex);
            } else { //Incrementa la pagina fin quando non si arriva all'ultima
                nextPage();
            }
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatPage)) {   // Richiesta utente di ripetere la pagina corrente
            Say pepperRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, allora ripeterò quest'ultima parte. ") // Set the text to say.
                    .build(); // Build the say action.

            pepperRepeat.run();
            startPage(pgIndex);

        }  else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetSkipTutorial)) {   // Richiesta utente di skippare il tutorial
            Say pepperSkipTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene.") // Set the text to say.
                    .build(); // Build the say action.

            pepperSkipTutorial.run();
            endOfTutorial = true;
            startPage(-1);

        }  else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatFromPg0)) {   // Richiesta utente di ripetere
            Say pepperRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Ok, allora ricomincio tutto da capo!") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            pepperRepeat.run();
            animateCorrect.run();

            pgIndex=0; //Ritorna alla prima pagina
            startPage(pgIndex);

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
                    .withText("Va bene, sto chiudendo il gioco. Ciao!") // Set the text to say.
                    .build(); // Build the say action.

            sayGoodbye.run();
            animate.run();

            this.finishAffinity(); // Close all activites
            System.exit(0);

        }

    }
    @Override
    public void onRobotFocusLost() {
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
        startActivity(activity2Intent);
        finish();
    }


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
            case 1: // seconda pagina
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                currPhrase = "A turno, il giudice ci mostrerà un oggetto e dovremo indovinare in quale bidone riciclarlo. Ochei?";

                tvExplaination.setText("A turno, il giudice ci mostrerà un oggetto" +
                        "\ne dovremo indovinare in quale bidone riciclarlo.");
                ivTutorial.setImageResource(R.drawable.screen_turns);
                break;

            case 2:
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                currPhrase = "Una volta scelto il tipo di bidone, il giudice dirà se è quello giusto. \\rspd=85\\ Posso andare avanti ora?";

                tvExplaination.setText("Una volta scelto il tipo di bidone, il giudice dirà se è quello giusto.");
                ivTutorial.setImageResource(R.drawable.screen_turn_judge);

                break;

            case 3: // Ultima pagina
                buttonPrev.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.INVISIBLE);

                buttonPlay.setVisibility(View.VISIBLE);
                lastPage = true;

                currPhrase = "Se la risposta è corretta, chi ha indovinato guadagnerà un punto! Va bene?";

                tvExplaination.setText("Se la risposta è corretta, chi ha indovinato guadagnerà un punto!");
                ivTutorial.setImageResource(R.drawable.screen_scores);

                break;
            default: //Prima pagina
                buttonPrev.setVisibility(View.INVISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                buttonPlay.setVisibility(View.INVISIBLE);

                currPhrase = "Quando le luci sulle mie spalle si illuminano, vuol dire che ti sto ascoltando. Facciamo una prova: dimmi Sì quando è il momento corretto.";

                tvExplaination.setText("Quando le luci sulle mie spalle si illuminano, vuol dire che ti sto ascoltando. Facciamo una prova:\ndimmi \"Sì\" quando è il momento corretto.");
                ivTutorial.setImageResource(R.drawable.status_blue_leds_transparent);

                break;
        }
    }

    int enterAnim, exitAnim;
    public void prevPage() {
     /* Va alla pagina precedente, a meno che non ci si trova nella prima.
    In quel caso, non fa nulla, anche se non dovrebbe verificarsi
    perché il bottone correlato non dovrebbe essere visibile
    */
        pgIndex = decreasesPage(pgIndex);
        enterAnim = R.anim.slide_in_left;
        exitAnim = R.anim.slide_out_right;
        startPage(pgIndex);
    }

    public void nextPage() {
    /* Va alla pagina successiva, a meno che non ci troviamo nell'ultima.
    In quel caso, non fa nulla, anche se non dovrebbe verificarsi
    perché il bottone correlato non dovrebbe essere visibile
    */
        pgIndex = increasesPage(pgIndex);
        enterAnim = R.anim.slide_in_right;
        exitAnim = R.anim.slide_out_left;
        startPage(pgIndex);
    }

    public void startPage(int pgIndex) {

        Intent activity2Intent = null;
        if (endOfTutorial) {
            activity2Intent = new Intent(TutorialActivity.this, TutorialEndActivity.class);
        } else if (pgIndex != -1) {
            activity2Intent = new Intent(TutorialActivity.this, TutorialActivity.class);
        }
        if (activity2Intent != null) {
            activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
            activity2Intent.putExtra("pgIndex", pgIndex); //passa il numero di pagina
            activity2Intent.putExtra("endOfTutorial", endOfTutorial);
            startActivity(activity2Intent);
            overridePendingTransition(enterAnim, exitAnim);
            finish();
        }

    }

}