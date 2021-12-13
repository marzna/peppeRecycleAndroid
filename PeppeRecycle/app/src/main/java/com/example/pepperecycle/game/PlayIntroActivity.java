package com.example.pepperecycle.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

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
import com.example.pepperecycle.MainActivity;
import com.example.pepperecycle.R;
import com.example.pepperecycle.tutorial.TutorialActivity;
import com.example.pepperecycle.utils.CommonUtils;

/* Classe relativa alla parte introduttiva del gioco, in cui il robot chiede all'utente
 * se vuole ascoltare il tutorial o se vuole iniziare direttamente una nuova partita
 */
public class PlayIntroActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{
    private Animate animate;
    boolean tutorialEnabled = false;
    byte trialState = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        // Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            trialState = extras.getByte("trialState");
        }
        setContentView(R.layout.activity_play_intro);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayAskTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Bene, vuoi sapere come si gioca?") // Set the text to say.
                .build(); // Build the say action.

        Say sayAskAgainTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Il tutorial è finito. Vuoi che te lo spieghi di nuovo o vuoi iniziare a giocare?") // Set the text to say.
                .build(); // Build the say action.

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build();
        Animate animateAskTutorial = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Si", "Voglio sapere", "ripeti il tutorial", "Voglio saperlo",
                        "spiegamelo", "spiegalo", "spiega di nuovo", "voglio che me lo spieghi",
                        "voglio che lo spieghi", "tutorial", "ok", "okay",
                        "Pepper sì", "sì Pepper", "Pepper si", "si Pepper", "ok Pepper")
                .build();

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "Non voglio", "No Pepper", "Giochiamo", "Voglio giocare", "voglio iniziare a giocare").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", "Ricominciamo", "Ricomincia", "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home", "Pepper vai indietro", "Pepper torna alla home",
                        "pepper torna al menu principale", "pepper torna al menu", "pepper torna indietro")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        if (tutorialEnabled) {
            sayAskAgainTutorial.run();
        } else {
            sayAskTutorial.run();
        }
        animateAskTutorial.run();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetNo, phraseSetRepeat, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {             // Risposta utente affermativa
            Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
            startActivity(activity2Intent); // Spiega il gioco

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {       // Va direttamente al gioco, saltando il tutorial
            Say playGame= SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Ok, allora iniziamo subito una nuova partita.") // Set the text to say.
                    .build(); // Build the say action.
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.exclamation_both_hands_a001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            tutorialEnabled=false;
            trialState = -1;
            playGame.run();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
            activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
            activity2Intent.putExtra("trialState", trialState);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetHome)) {     // Torna alla home
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) {    // Chiude il gioco
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a004).build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, sto chiudendo il gioco. ù!") // Set the text to say.
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    public void buttonPlay(View v) { //Pressione tasto "no"
        Intent activity2Intent = new Intent(getApplicationContext(), PlayGameActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonTutorial(View v) { //Pressione tasto "yes"
        Intent activity2Intent = new Intent(getApplicationContext(), TutorialActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home"
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        CommonUtils.showDialogExit(this);
    }
}