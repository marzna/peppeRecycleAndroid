package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JudgeConfirmActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{

    boolean isPepperTurn, isAnswerCorrect, pressed;
    byte wasteType, round;
    TextView selectedBinIs;
    ImageView selectedBin;
    Map<String, Byte> scores = new HashMap<String, Byte>();
    static byte pepperScore, userScore;
    QiContext qiContext;
    String binType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_judge_confirm);

        selectedBinIs = findViewById(R.id.textViewSelectedBinIs);
        selectedBin = findViewById(R.id.selectedBin);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores"); //TODO Serializable(?)
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        startJudgeConfirm();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
    Animate animate;
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        //        ackSelectedBin(qiContext); // Pepper dice all'utente qual è il bidone selezionato
        checkBin();
        Say sayAskForConfirm= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Giudice, la risposta è corretta?") // Set the text to say.
                .build(); // Build the say action.
        Animation askForConfirm = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione
                .build();
        Animate animateAskForConfirm = AnimateBuilder.with(qiContext)
                .withAnimation(askForConfirm)
                .build();

        sayAskForConfirm.run();
        animateAskForConfirm.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "è corretta", "è giusta", "corretta", "giusta")
                .build();

        /*PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper").build(); //TODO idk */

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "è sbagliata", "sbagliata","No Pepper", "è errata", "non è corretta").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", /*"Ricominciamo", "Ricomincia",*/ "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetNo, phraseSetRepeat, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            //todo se l'utente ha sbagliato, insegna qualcosa
            updateScore(isPepperTurn);
            nextTurn();
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {
            //todo se Pepper ha risp correttamente, insegna qualcosa
            nextTurn();
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), JudgeConfirmActivity.class);
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
                    .withText("Va bene, sto chiudendo il gioco. Spero di rivederti presto!") // Set the text to say.
                    .build(); // Build the say action.

            sayGoodbye.run();
            animate.run();

            finish();

        }
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    void startJudgeConfirm() {
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case 0: // case "organic":
                selectedBinIs.setText("Il bidone selezionato è quello\ndell'organico");
                selectedBin.setImageResource(R.drawable.bin_brown_shadow);
                break;
            case 1: // case "paper": case "cardboard":
                selectedBinIs.setText("Il bidone selezionato è quello\ndi carta e cartone");
                selectedBin.setImageResource(R.drawable.bin_blue_shadow);
                break;
            case 2: // case "plastic": case "metal":
                selectedBinIs.setText("Il bidone selezionato è quello\ndi plastica e metalli");
                selectedBin.setImageResource(R.drawable.bin_yellow_shadow);
                break;
            case 3: // case "glass":
                selectedBinIs.setText("Il bidone selezionato è quello\ndel vetro");
                selectedBin.setImageResource(R.drawable.bin_green_shadow);
                break;
            default:
                selectedBinIs.setText("ERRORE.");
                break;
        }

        if(pressed) {
            // TODO Incrementa score
            nextTurn();
        }


    }
    void ackSelectedBin(QiContext qiContext) { //Todo forse va messo direttamente in JudgeConfirm...
        checkBin();
        Say sayAskForConfirm= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("" + binType + ". Giudice, la risposta è corretta?") // Set the text to say.
                .build(); // Build the say action.
        Animation askForConfirm = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione
                .build();
        Animate animateAskForConfirm = AnimateBuilder.with(qiContext)
                .withAnimation(askForConfirm)
                .build();

        animateAskForConfirm.run();
        sayAskForConfirm.run();
    }

    void checkBin() {
        switch (wasteType) {
            case 0: // case "organic":
                binType = "Organico";
                break;
            case 1: // case "paper": case "cardboard":
                binType = "Carta e cartone";
                break;
            case 2: // case "plastic": case "metal":
                binType = "Plastica e metalli";
                break;
            case 3: // case "glass":
                binType = "Vetro";
                break;
            default:
                binType = "Errore";
                break;
        }
    }
    public void buttonYes(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        //Il punteggio viene incrementato solo se la risposta è corretta
        isAnswerCorrect = true;
        pressed = true;
        updateScore(isPepperTurn);
        nextTurn();
    }
    public void buttonNo(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        isAnswerCorrect = false;
        pressed = true;
        //TODO Se il turno era del bambino, fai dire a Pepper qualcosa random sul riciclo
        nextTurn();
    }
    public void updateScore(boolean isPepperTurn) {
        if(isPepperTurn) {
            ++pepperScore;//scores.put("score_pepper", (byte) (scores.get("score_pepper") + 1)); //Incrementa il punteggio di Pepper
            pepperTeaches();
            //TODO inserisci nozioni relative all'oggetto classificato
        } else {
            ++userScore;//scores.put("score_user1", (byte) (scores.get("score_user1") + 1)); //Incrementa il punteggio dell'utente
            //TODO inserisci congratulazioni
        }
    }
    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent;
        isPepperTurn = !isPepperTurn; // Turno successivo
        // if (round < 6) {    // TODO sostituisci 6 con una costante
        // if((scores.get("score_pepper") < 3 )    ||  (scores.get("score_user1") < 3) )   {
        if ( pepperScore < 3 && userScore < 3 )   { // Si ripete fin quando uno dei giocatori non ha raggiunto il punteggio massimo
            // TODO sostituisci il 3 con una costante, tipo WINNER_SCORE o simili
            if (isPepperTurn) {
                activity2Intent = new Intent(JudgeConfirmActivity.this, PlayPepperTurnActivity.class); // PlayPepperTurnActivity.class);
            } else {
                activity2Intent = new Intent(JudgeConfirmActivity.this, PlayUserTurnActivity.class);
            }
            /*
            pepperScore = activity2Intent.getExtras().getByte("pepperScore");
            userScore = activity2Intent.getExtras().getByte("userScore");
            scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores"); //TODO Serializable(?)//activity2Intent.putExtra("scores", scores);
            */
        } else {            // Game over
            activity2Intent = new Intent(JudgeConfirmActivity.this, GameOverActivity.class);//TODO GameOverActivity
        }
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("scores", (Serializable) scores);
        startActivity(activity2Intent);
        finish();
    }

    public void buttonBack(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent;//Per andare alla pagina principale
        if(isPepperTurn) { //TODO Rimuovi il bottone back se il turno era di Pepper o lascialo se si vuole ri-scattare la foto?
            activity2Intent = new Intent(getApplicationContext(), PlayPepperTurnActivity.class);
        } else {
            activity2Intent = new Intent(getApplicationContext(), PlayUserTurnActivity.class);
        }
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonHelp(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        //TODO spiegazione di cosa dovrebbe fare il giudice
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }
    public void buttonClose(View v) { //Pressione tasto "Chiudi" TODO Togli perché è un duplicato? [???]
        finish();
        /* Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        startActivity(activity2Intent);
        //TODO Chiudi gioco */
    }

    void pepperTeaches() {
        //TODO Frasi che insegnano all'utente nozioni sulla raccolta differenziata
    }

}