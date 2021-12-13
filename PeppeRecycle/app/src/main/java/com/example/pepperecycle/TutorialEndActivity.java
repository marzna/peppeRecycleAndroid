package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class TutorialEndActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "TutorialEndActivity";
    Button buttonYes, buttonNo, buttonPlayEndTutorial;
    ImageButton buttonBack, buttonHome, buttonClose;
    boolean endOfTutorial;
    boolean roundTutorial;
    int pgIndex; //Non serve??
    byte trialState = -1;
    TextView tvTrialTitle, tvTrialQuestion;
    String currPhrase;
    boolean playGameAfterTrial = false; // true se ci si trova dopo il round di prova //TODO ????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_tutorial_end);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        buttonYes = findViewById(R.id.buttonTrialYes);
        buttonNo = findViewById(R.id.buttonTrialNo);
        buttonPlayEndTutorial = findViewById(R.id.buttonPlayEndTutorial);
        buttonBack = findViewById(R.id.buttonBack);
        buttonHome = findViewById(R.id.buttonHome);
        buttonClose = findViewById(R.id.buttonClose);
        tvTrialTitle = findViewById(R.id.tvTrialTitle);
        tvTrialQuestion = findViewById(R.id.tvTrialQuestion);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
            trialState = extras.getByte("trialState");
        }

        if(trialState == 2) {
            buttonPlayEndTutorial.setEnabled(true);
            buttonPlayEndTutorial.setVisibility(View.VISIBLE);
            buttonYes.setVisibility(View.INVISIBLE);
            buttonNo.setVisibility(View.INVISIBLE);

            tvTrialTitle.setText("Round di prova - FINE");
            tvTrialQuestion.setText("Il round di prova è finito.\nAdesso giochiamo sul serio!");
            currPhrase = "Il raund di prova è finito. Adesso giochiamo sul serio! .";
            playGameAfterTrial = true;
//            trialState = -1;
        } else {
            buttonPlayEndTutorial.setEnabled(false);
            buttonPlayEndTutorial.setVisibility(View.INVISIBLE);

            tvTrialTitle.setText("Round di prova - INIZIO");
            tvTrialQuestion.setText("Vuoi giocare un round di prova?");
            currPhrase = "Il tutòrial è finito. Vogliamo giocare un raund di prova?";
            playGameAfterTrial = false;
        }
        trialState = -1;

        //OnClickListeners
        buttonYes.setOnClickListener(new View.OnClickListener() {
            // Si gioca un round di prova
            @Override
            public void onClick(View view) {
                playGame(true);
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            // Si gioca direttamente, saltando il round di prova

            @Override
            public void onClick(View view) {
                playGame(false);
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
                activity2Intent.putExtra("endOfTutorial", false);
                activity2Intent.putExtra("pgIndex", 2); //TODO ultima pagina?
                activity2Intent.putExtra("trialState", trialState);
                startActivity(activity2Intent); //Per andare alla pagina precedente
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                finish();
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity2Intent); //Per andare alla pagina principale
                finish();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CommonUtils.showDialogExit(TutorialEndActivity.this);
            }
        });

        buttonPlayEndTutorial.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playGame(false);
            }
        });

    }


    /*Porta al gioco
     * @param tutorialEnabled true  ->  indica che si farà un round di prova
     * @param tutorialEnabled false ->  no round di prova
     * */
    public void playGame(boolean tutorialEnabled) {
        Log.d(TAG, "entrato in playGame");
        Intent activity2Intent;
        //if(tutorialEnabled) { //Se il tutorial è attivo, toccherà all'utente
        if (tutorialEnabled) {
            activity2Intent= new Intent(TutorialEndActivity.this, PlayUserTurnActivity.class);
            activity2Intent.putExtra("isPepperTurn", false); // toccherà all'utente in quanto è tutorial
            trialState = 0;
        } else {
            activity2Intent= new Intent(TutorialEndActivity.this, PlayGameActivity.class);
            trialState = -1; //TODO Controlla, non so se vada bene così
        }

        //TODO Servono?
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("pepperScore", false);
        activity2Intent.putExtra("userScore", 0);
        activity2Intent.putExtra("roundTutorial", roundTutorial);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("endOfTutorial", endOfTutorial);


        startActivity(activity2Intent);
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    String endedTutorial = "";
    Animate animate;
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say askForTrialRound = SayBuilder.with(qiContext) // Create the builder with the context.
                //.withText("Il tutorial è finito. Vogliamo giocare un raund di prova?") // Set the text to say.
                .withText(currPhrase) // Set the text to say.
                .build(); // Build the say action.

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTrial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();
        PhraseSet phraseSetPlay, phraseSetNoPlay;
        if (playGameAfterTrial) {
            //In risposta a: "Il round di prova è finito. Adesso giochiamo sul serio! .";
            phraseSetPlay = PhraseSetBuilder.with(qiContext)
                    .withTexts("Si", "sì", "va bene", "certo",
                            "Giochiamo", "Voglio giocare", "gioca",
                            "ok", "okay")
                    .build();
            phraseSetNoPlay = PhraseSetBuilder.with(qiContext)
                    .withTexts("No", "Non mi va", "Non giochiamo", "Non voglio giocare", "Salta",
                            "Torna alla Home", "Menu principale", "vai al menu principale", "torna al menu principale")
                    .build();
        } else {
            phraseSetPlay = PhraseSetBuilder.with(qiContext)
                    .withTexts("Si", "sì", "va bene", "certo", "raund di prova",
                            "round di prova", "giochiamo il raund di prova",
                            "giochiamo il round di prova", "giochiamo il round di prova",
                            "ok", "okay", "gioca")
                    .build();
            phraseSetNoPlay = PhraseSetBuilder.with(qiContext)
                    .withTexts("Giochiamo", "Voglio giocare", "Non mi va", "No", "Salta")
                    .build();
        }
        /*PhraseSet phraseSetPlayTrial = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "sì", "va bene", "ochei", "certo", "raund di prova",
                        "round di prova", "giochiamo il raund di prova",
                        "giochiamo il round di prova", "giochiamo il round di prova",
                        "ok", "okay")
                .build();

        //In risposta a: "Il round di prova è finito. Adesso giochiamo sul serio! .";
        PhraseSet phraseSetPlayGameAfterTrial = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "sì", "va bene", "ochei", "certo",
                        "Giochiamo", "Voglio giocare" ,
                        "ok", "okay")
                .build();

        PhraseSet phraseSetSkipTrial = PhraseSetBuilder.with(qiContext)
                .withTexts("Giochiamo", "Voglio giocare", "Non mi va", "No").build();

        PhraseSet phraseSetNoPlay = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "Non mi va", "Non giochiamo", "Non voglio giocare",
                        "Torna alla Home", "Menu principale", "vai al menu principale", "torna al menu principale")
                .build();*/

        PhraseSet phraseSetRepeatPage = PhraseSetBuilder.with(qiContext)
                .withTexts("Non è chiaro", "Ripeti",
                        "Non ho capito", "Puoi ripetere")
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

        askForTrialRound.run();
        animateAskTrial.run();
        /*  while(pgIndex <3 ) { //Incrementa la pagina fin quando non si arriva all'ultima
            nextPage();
        }
        askForContinue.run();*/

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetPlay, phraseSetBackPage, phraseSetRepeatPage,
                        phraseSetBackHome, phraseSetClose, phraseSetNoPlay)
                /*, phraseSetPlayTrial, phraseSetSkipTrial, phraseSetPlayGameAfterTrial*/
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        //Se il trial non è stato ancora svolto
        //if (!playGameAfterTrial && PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlayTrial)) {             // Risposta utente affermativa
        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlay)) {             // Risposta utente affermativa
            if (!playGameAfterTrial) {// Inizia il round di prova
                Say startTrialPhrase = SayBuilder.with(qiContext) // Create the builder with the context.
                        .withText("Perfetto! Allora iniziamo il round di prova! In questo round, non terremo conto del punteggio") // Set the text to say.
                        .build(); // Build the say action.
                startTrialPhrase.run();

                Log.d(TAG, "phraseSetPlayTrial + playGameAfterTrial false: " + playGameAfterTrial);

                playGame(true); //Inizia il round di prova (bc tutorialEnabled == true)
            } else { // Inizia direttamente a giocare, saltando il round di prova
                Log.d(TAG, "phraseSetPlayGameAfterTrial + playGameAfterTrial false: " + playGameAfterTrial);
                //Prosegue col gioco vero e proprio dopo aver fatto il round di prova
                Say sayPlayGame = SayBuilder.with(qiContext) // Create the builder with the context.
                        .withText("Perfetto, buona fortuna! Ricòrdati che, d'ora in poi, calcoleremo il punteggio!") // Set the text to say.
                        .build(); // Build the say action.

                Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.affirmation_a002).build();
                Animate animateCorrect = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();

                sayPlayGame.run();
                animateCorrect.run();

                playGame(false); //Inizia il gioco saltando il round di prova (bc tutorialEnabled == false)
            /*Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", false); // Non è attivo, avendolo già fatto in passato
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();*/
            }
//        } else if (!playGameAfterTrial && PhraseSetUtil.equals(matchedPhraseSet, phraseSetNoPlay)) {       // Va direttamente al gioco, saltando il tutorial
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNoPlay)) {
            //Riporta l'utente alla home, dato che non vuole giocare dopo aver fatto il round di prova
            if (playGameAfterTrial) {
                Log.d(TAG, "phraseSetPlayGameAfterTrial + playGameAfterTrial false: " + playGameAfterTrial);

                Say playGame = SayBuilder.with(qiContext) // Create the builder with the context.
                        .withText("Okay, sarà per un'altra volta! Ti porto alla houm") // Set the text to say.
                        .build(); // Build the say action.

                Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.affirmation_a002).build();
                Animate animateCorrect = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();

                playGame.run();
                animateCorrect.run();

                Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity2Intent);
                finish();
            } else {
                // Va direttamente al gioco, saltando il round di prova
                Say sayPlayGame = SayBuilder.with(qiContext) // Create the builder with the context.
                        .withText("Uau, vuoi già giocare? Ochei, allora iniziamo subito!") // Set the text to say.
                        .build(); // Build the say action.
                Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.exclamation_both_hands_a001).build();
                Animate animateCorrect = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();

                Log.d(TAG, "phraseSetSkipTrial + playGameAfterTrial true: " + playGameAfterTrial);

                sayPlayGame.run();

                animateCorrect.run();
                playGame(false); //Inizia il round di prova (bc tutorialEnabled == true)
            }
            /*Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", false);
            activity2Intent.putExtra("trialState", -1);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();*/

//        } else if (playGameAfterTrial && PhraseSetUtil.equals(matchedPhraseSet, phraseSetBackHome)) {     // Torna alla home
        /*} else if (playGameAfterTrial && PhraseSetUtil.equals(matchedPhraseSet, phraseSetSkipTrial)) {
            // Va direttamente al gioco, saltando il round di prova
            Say sayPlayGame = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Uau, vuoi già giocare? Ochei, allora iniziamo subito!") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.exclamation_both_hands_a001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Log.d(TAG, "phraseSetSkipTrial + playGameAfterTrial true: " + playGameAfterTrial);

            if(trialState == 0 || trialState == 1 ) { // se ci troviamo nel trial

            } else {
                sayPlayGame.run();
            }
            animateCorrect.run();
            playGame(false); //Inizia il round di prova (bc tutorialEnabled == true)
            *//*Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", false);
            activity2Intent.putExtra("trialState", -1);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();*/
//        } else if (playGameAfterTrial && PhraseSetUtil.equals(matchedPhraseSet, phraseSetPlayGameAfterTrial)) {
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetBackHome)) {     // Torna alla home

            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent);
            finish();


        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeatPage)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), TutorialEndActivity.class);
            startActivity(activity2Intent); //Per ripetere
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
}