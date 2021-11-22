package com.example.pepperecycle;

import static com.example.pepperecycle.PlayGameActivity.N_ROUNDS;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_CARDBOARD;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_GLASS;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_METAL;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_ORGANIC;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PAPER;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PAPER_CARDBOARD;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PLASTIC;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PLASTIC_METAL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
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
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NextTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {

    private static final String TAG = "NextTurnActivity";

    boolean isPepperTurn, isAnswerCorrect, pressed;
    byte wasteType, round;
    TextView selectedBinIs;
    ImageView selectedBin;
    Map<String, Byte> scores = new HashMap<String, Byte>();
    //    Map<Byte, String> tutorialStates = new HashMap<Byte, String>();
    static byte pepperScore, userScore;
    QiContext qiContext;
    String binType;
    String wasteTypeString;
    String factAboutRecycle;
    boolean tutorialEnabled;
    boolean roundTutorial;
    boolean pepperTeaches;
    TextView tvMessage;
    Dialog dialog;
    String desc;
    TextView textViewAskForConfirm, tvTutorialJudge;
    byte currentRound;
    byte tutorialState = -1;
    byte trialState;
    boolean endOfTutorial;
    boolean restartGame;
    Button buttonPlay;
    TextView tvUserScore, tvPepperScore;
    ImageView imageViewUserScore,imageViewPepperScore;
    String exclamation;
    boolean isFromPepperTeaches;
    int resAnim; // conterrà l'animazione

    String[] pepperCorrectPhrase = {
            "Evvài, ho indovinato. ",
            "Mi sto impegnando. ",
            "Questa cosa la sapevo proprio bene. ",
            "Si vede che ho studiato. ",
            "Certo che sono proprio bravo. "
    };
    String[] pepperWrongPhrase = {
            "Oh no, ho sbagliato. ",
            "Dovrei impegnarmi di più. ",
            "Devo fare più attenzione. "
    };
    String[] userWrongPhrase = {
            "Argh, dovresti impegnarti di più. ",
            "Oh no, hai sbagliato. ",
            "Cerca di tenere alta la concentrazione. "
    };
    String[] userCorrectPhrase = {
            "Complimenti. ",
            "Che bello, hai indovinato. ",
            "Risposta corretta? Allora hai guadagnato un punto, complimenti. "
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        Log.d(TAG, "NextTurn iniziato");
        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_next_turn);
       /* selectedBinIs = findViewById(R.id.textViewSelectedBinIs);
        selectedBin = findViewById(R.id.selectedBin);
        textViewAskForConfirm = findViewById(R.id.textViewAskForConfirm);
        tvTutorialJudge = findViewById(R.id.tvTutorialJudge);*/

        desc = "In questa fase del gioco,\n" +
                "il giudice deve stabilire se la risposta è corretta.\n" +
                "Chi ha indovinato guadagnerà un punto!";

        dialog = new Dialog(this);
        buttonPlay = findViewById(R.id.ivPlay);
        tvMessage = findViewById(R.id.tvMessage);
        tvUserScore = findViewById(R.id.textViewUserScore);
        tvPepperScore = findViewById(R.id.textViewPepperScore);
        tvTutorialJudge = findViewById(R.id.tvTutorialJudge);
        imageViewUserScore = findViewById(R.id.imageViewUserScore);
        imageViewPepperScore = findViewById(R.id.imageViewPepperScore);

        trialState = -1;
        isFromPepperTeaches = false;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            isAnswerCorrect = extras.getBoolean("isAnswerCorrect");
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            wasteTypeString = extras.getString("wasteTypeString");
            roundTutorial = extras.getBoolean("roundTutorial");
            endOfTutorial = extras.getBoolean("endOfTutorial");
            restartGame = extras.getBoolean("restartGame");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            currentRound = extras.getByte("currentRound");
            tutorialState = extras.getByte("tutorialState");
            trialState = extras.getByte("trialState");
            isFromPepperTeaches = extras.getBoolean("isFromPepperTeaches");
        }

        Log.d(TAG, "TrialState: " + trialState);

        selectExclamation(isAnswerCorrect, isPepperTurn, trialState);

       /* if (trialState == 0 || trialState == 1) {
            tvTutorialJudge.setVisibility(View.VISIBLE);
            Log.d(TAG, "Siamo nel trial. trialState: " + trialState);
        } else {
            tvTutorialJudge.setVisibility(View.INVISIBLE);
            Log.d(TAG, "NON siamo nel trial. trialState: " + trialState);
        }*/

        // Per far terminare l'activity dopo un certo tempo prestabilito
        // Non utilizzato perché è apparentemente incompatibile con il pulsante buttonPlay
        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                buttonPlay.setClickable(false);
                if(!pressed)
                    nextTurn();
                *//*Intent mInHome = new Intent(PlayJudgeTurnActivity.this, NextTurnActivity.class);
                PlayJudgeTurnActivity.this.startActivity(mInHome);
                PlayJudgeTurnActivity.this.finish();*//*
            }
        }, 3000); // 3000 millisecondi == 3 secondi*/

        setScore();

        if(trialState == -1) {
            //Incrementa il punteggio e lo mostra
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (isAnswerCorrect) // Incremento punteggio
                        updateScore(isPepperTurn);
                }
            }, 1000);
        }


        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonPlay cliccato");
                pressed = true;
                nextTurn();
            }
        });
    }
    void setScore () {
        tvPepperScore.setText("" + pepperScore);
        tvUserScore.setText("" + userScore);

        if(trialState == 0 || trialState == 1) {
            tvTutorialJudge.setVisibility(View.VISIBLE);
            tvUserScore.setVisibility(View.INVISIBLE);
            tvPepperScore.setVisibility(View.INVISIBLE);
            imageViewUserScore.setVisibility(View.INVISIBLE);
            imageViewPepperScore.setVisibility(View.INVISIBLE);
        } else {
            tvTutorialJudge.setVisibility(View.INVISIBLE);
            tvUserScore.setVisibility(View.VISIBLE);
            tvPepperScore.setVisibility(View.VISIBLE);
            imageViewUserScore.setVisibility(View.VISIBLE);
            imageViewPepperScore.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    Animate animate;

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        if(!isFromPepperTeaches) {
            Animation animation = AnimationBuilder.with(qiContext)
                    .withResources(resAnim)
                    .build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .build();

            Say sayResult = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText(exclamation) // Set the text to say.
                    .withBodyLanguageOption(BodyLanguageOption.DISABLED)
                    .build(); // Build the say action.

            animate.async().run();
            sayResult.run();

            PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                    //.withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "pepper sì", "pepper si")
                    .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "pepper sì", "pepper si", "ok",
                            "okay", "andiamo avanti", "possiamo andare avanti", "vai avanti", "prosegui",
                            "vai avanti", "prossimo turno", "vai al prossimo turno",
                            "passa al prossimo turno", "giochiamo", "prosegui", "proseguiamo",
                            "prosegui con il gioco", "voglio proseguire con il gioco",
                            "voglio proseguire", "voglio andare avanti", "possiamo proseguire",
                            "possiamo", "continua", "continuiamo", "avanti", "giochiamo")
                    .build();
            PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                    .withTexts("No", "non mi va", "non voglio continuare", "non voglio proseguire",
                            "basta", "non è corretta", "stop", "basta", "chiudi il gioco", "chiudi",
                            "no Pepper", "Pepper no", "Esci", "voglio andare via")
                    .build();

            PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                    .withTexts("Ripeti", "Da capo", "Non ho capito", "Puoi ripetere",
                            "pepper ripeti")
                    .build();

            PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                    .withTexts("Torna alla home", "Home")
                    .build();

            Listen listenPlay = ListenBuilder
                    .with(qiContext)
                    .withPhraseSets(phraseSetYes, phraseSetRepeat, phraseSetClose, phraseSetHome)
                    .build();
            ListenResult listenResult = listenPlay.run();
            PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

            if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
                nextTurn();
            } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere
                Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.coughing_left_b001).build();
                Animate animateCorrect = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();
                animateCorrect.run();

                Intent activity2Intent = new Intent(getApplicationContext(), NextTurnActivity.class);
                activity2Intent.putExtra("wasteType", wasteType); // The key argument here must match that used in the other activity
                activity2Intent.putExtra("isAnswerCorrect", isAnswerCorrect);
                activity2Intent.putExtra("round", round);
                activity2Intent.putExtra("isPepperTurn", isPepperTurn);
                activity2Intent.putExtra("pepperScore", pepperScore);
                activity2Intent.putExtra("userScore", userScore);
                activity2Intent.putExtra("wasteTypeString", wasteTypeString);
                activity2Intent.putExtra("roundTutorial", roundTutorial);
                activity2Intent.putExtra("endOfTutorial", endOfTutorial);
                activity2Intent.putExtra("restartGame", restartGame);
                activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
                activity2Intent.putExtra("currentRound", currentRound);
                activity2Intent.putExtra("tutorialState", tutorialState);
                activity2Intent.putExtra("trialState", trialState);
                startActivity(activity2Intent); //Per ripetere
                finish();

            } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetHome)) {     // Torna alla home
                /*Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.affirmation_a002).build();
                Animate animateCorrect = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();
                animateCorrect.run();*/
                Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity2Intent);
                finish();

            } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) {    // Chiude il gioco
                Animation correctAnswer = AnimationBuilder.with(qiContext)
                        .withResources(R.raw.hello_a004).build();
                animate = AnimateBuilder.with(qiContext)
                        .withAnimation(correctAnswer).build();

                Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                        .withText("Va bene, allora sto chiudendo il gioco. Spero di rivederti presto!") // Set the text to say.
                        .withBodyLanguageOption(BodyLanguageOption.DISABLED)
                        .build(); // Build the say action.

                sayGoodbye.run();
                animate.async().run();

                this.finishAffinity(); // Close all activites
                System.exit(0);

            }
        }
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    public void updateScore(boolean isPepperTurn) {
        if (isPepperTurn) {
            ++pepperScore;
            tvPepperScore.setText(" " + pepperScore);
//            startPepperTeacher();
            Log.d(TAG, "Incrementato pepperScore");
        } else {
            ++userScore;
            tvUserScore.setText("" + userScore);
            Log.d(TAG, "Incrementato userScore");
        }
    }
    byte nextTrialState(byte state) {
        switch (state) {
            case 0:     // è appena stato effettuato il turno utente.
                state = 1;
                break;
            case 1:     // è appena stato effettuato il turno di Pepper, perciò si va a 2, in cui si chiede di nuovo di iniziare il tutorial.
                state = 2;
                break;
            default: //case -1
                break;
        }
        return state;
    }
    void startPepperTeacher() {

        Log.e(TAG, "Entrato nella funzione startPepperTeacher.");
        Intent activity2Intent = new Intent(NextTurnActivity.this, PepperTeachesActivity.class);//TODO GameOverActivity
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        //activity2Intent.putExtra("scores", (Serializable) scores);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
//        activity2Intent.putExtra("tutorialEnabled", false);
        //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");

        startActivity(activity2Intent);
        finish();
    }

    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent;
        trialState = nextTrialState(trialState); //TODO se si bugga, metti prima di questa riga if(!isFromPepperTeaches)
        if (trialState == 2) { // Se si è nel trial ed è finito
            Log.e(TAG, "trialState = " + trialState);
            endTutorial();
        } else { // se si è nel gioco e si deve incrementare il turno
            // Tutorial finito
            if (trialState == 1) {// Turno di Pepper durante il trial
                activity2Intent = new Intent(NextTurnActivity.this, PlayPepperTurnActivity.class);
            } else {
                if (!isFromPepperTeaches) {
                    isPepperTurn = !isPepperTurn; // Turno successivo
                    ++currentRound;
                    Log.d(TAG, "currentRound: " + currentRound);
                }
//                Log.d("ROUND", "currentRound++");
                if (currentRound < N_ROUNDS) {
                    // TODO sostituisci il 6 con una costante, tipo WINNER_SCORE o simili
                    if (isPepperTurn) {     //Tocca a Pepper
                        activity2Intent = new Intent(NextTurnActivity.this, PlayPepperTurnActivity.class);
                        Log.d(TAG, "trialState passato a PepperTurn: " + trialState);
                    } else {                // Tocca all'utente
                        activity2Intent = new Intent(NextTurnActivity.this, PlayUserTurnActivity.class);
                        Log.d(TAG, "trialState passato a UserTurn: " + trialState);
                    }
                } else {                    // Game over
                    activity2Intent = new Intent(NextTurnActivity.this, GameOverActivity.class);
                }

            }
            activity2Intent.putExtra("round", round);
            activity2Intent.putExtra("pepperScore", pepperScore);
            activity2Intent.putExtra("userScore", userScore);
            activity2Intent.putExtra("tutorialEnabled", false); // Tutorial finito
            activity2Intent.putExtra("currentRound", currentRound);
            activity2Intent.putExtra("trialState", trialState);
            activity2Intent.putExtra("isFromPepperTeaches", isFromPepperTeaches);

            startActivity(activity2Intent);
            finish();
        }
    }

    public void endTutorial() {
        Intent activity2Intent = new Intent(NextTurnActivity.this, TutorialEndActivity.class);
        activity2Intent.putExtra("endOfTutorial", true); // Tutorial finito
        activity2Intent.putExtra("pgIndex", 0); //TODO SOSTITUISCI NUMERO MAGICO
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent);
        finish();
    }

    void selectExclamation(boolean isTrue, boolean isPepperTurn, int trialState) {
        if (isPepperTurn) {
            if (isTrue) {
                exclamation = pepperCorrectPhrase[new Random().nextInt(pepperCorrectPhrase.length)];
                resAnim = R.raw.nicereaction_a002;
                if(trialState == -1)
                    tvMessage.setText("Ho indovinato,\nperciò guadagno un punto!");
                else
                    tvMessage.setText("Ho indovinato!");
            } else {
                exclamation = pepperWrongPhrase[new Random().nextInt(pepperWrongPhrase.length)];
                resAnim = R.raw.sad_a001;
                if(trialState == -1)
                    tvMessage.setText("Ho sbagliato.\nNon mi è stato assegnato nessun punto.");
                else
                    tvMessage.setText("Ho sbagliato...");
            }
            if (currentRound < N_ROUNDS)
                exclamation += "Ora tocca a te. Ok?";

        } else {

            if (isTrue) {
                exclamation = userCorrectPhrase[new Random().nextInt(userCorrectPhrase.length)];
                resAnim = R.raw.nicereaction_a001;

                if(trialState == -1)
                    tvMessage.setText("Complimenti,\nHai guadagnato un punto!");
                else
                    tvMessage.setText("Complimenti,\nHai indovinato!");

            } else {
                exclamation = userWrongPhrase[new Random().nextInt(userWrongPhrase.length)];
                resAnim = R.raw.sad_a001;

                if(trialState == -1)
                    tvMessage.setText("Hai sbagliato.\nNon ti è stato assegnato nessun punto.");
                else
                    tvMessage.setText("La risposta era sbagliata.");
            }

            if (currentRound < N_ROUNDS)
                exclamation += "Adesso è il mio turno. ";
        }
    }

    /*public void buttonBack(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent;//Per andare alla pagina principale
        if(isPepperTurn) { //TODO Rimuovi il bottone back se il turno era di Pepper o lascialo se si vuole ri-scattare la foto?
            activity2Intent = new Intent(getApplicationContext(), PlayPepperTurnActivity.class);
        } else {
            activity2Intent = new Intent(getApplicationContext(), PlayUserTurnActivity.class);
        }
        activity2Intent.putExtra("round", round);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }*/
    public void buttonHelp(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
//        showDialog(desc);

        CommonUtils.showDialog(NextTurnActivity.this, desc);
        //showDialog(desc);
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        CommonUtils.showDialogExit(this);
        //finish();
    }

}