package com.example.pepperecycle;

import static com.example.pepperecycle.PlayGameActivity.N_TURNS;
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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PlayJudgeTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "PlayJudgeTurnActivity" ;//, CameraBridgeViewBase.CvCameraViewListener2{

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
    Dialog dialog;
    String desc;
    Button buttonYes, buttonNo;
    ImageButton buttonBack;
    TextView textViewAskForConfirm, tvTutorialJudge;
    byte currentRound;
    byte tutorialState = -1;
    byte trialState;
    boolean endOfTutorial;
    boolean restartGame;
    boolean pepperShouldTeach;

    String exclamation;

    String[] pepperExclamations = {
            "Evvài, ho indovinato!",
            "Mi sto impegnando!",
            "Questa cosa la sapevo proprio bene! ",
            "Si vede che ho studiato!",
            "Certo che sono proprio bravo!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        Log.d(TAG, "JudgeConfirm iniziato");
        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_judge_confirm);

        selectedBinIs = findViewById(R.id.textViewSelectedBinIs);
        selectedBin = findViewById(R.id.selectedBin);
        textViewAskForConfirm = findViewById(R.id.textViewAskForConfirm);
        tvTutorialJudge = findViewById(R.id.tvTutorialJudge);

        buttonYes = findViewById(R.id.buttonAnswerYes);
//        buttonYes.setClickable(true);
        buttonNo = findViewById(R.id.buttonAnswerNo);
        buttonBack = findViewById(R.id.buttonBack);

        desc = "In questa fase del gioco,\n" +
                "il giudice deve stabilire se la risposta è corretta.\n" +
                "Chi ha indovinato guadagnerà un punto!";

        dialog = new Dialog(this);

        trialState = -1;
        pepperShouldTeach = false;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
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
            pepperShouldTeach = extras.getBoolean("pepperShouldTeach");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        Log.d(TAG, "TrialState: "+ trialState);
        if(trialState == 0 || trialState == 1) {
            tvTutorialJudge.setVisibility(View.VISIBLE);
            Log.d(TAG, "Siamo nel trial. trialState: "+ trialState);
        } else {
            tvTutorialJudge.setVisibility(View.INVISIBLE);
            Log.d(TAG, "NON siamo nel trial. trialState: "+ trialState);
        }

        //OnClickListeners
        buttonYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonYes cliccato");
                isAnswerCorrect = true;
                if(isPepperTurn)
                    pepperShouldTeach = true;
                goToNextTurn();
                /*
                if(!isPepperTurn) //TODO???
                    goToNextTurn();
                else
                    startPepperTeacher();
                */
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonNo cliccato");
                isAnswerCorrect = false;
                goToNextTurn();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonBack cliccato");

                Intent activity2Intent;//Per andare alla pagina principale
                if (isPepperTurn) { //TODO Rimuovi il bottone back se il turno era di Pepper o lascialo se si vuole ri-scattare la foto?
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


                activity2Intent.putExtra("roundTutorial", roundTutorial);
                activity2Intent.putExtra("endOfTutorial", endOfTutorial);
                activity2Intent.putExtra("restartGame", restartGame);
                activity2Intent.putExtra("roundTutorial", roundTutorial);
                activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
                activity2Intent.putExtra("tutorialState", tutorialState);
                startActivity(activity2Intent); //Per andare alla pagina principale

                finish();
            }
        });


        startJudgeConfirm();

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
    Animate animate;
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        checkBin();
        Say selectedBin = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(typeBinSelectedIs) // Pepper dice all'utente qual è il bidone selezionato
                .build(); // Build the say action.
        Say sayAskForConfirm = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Giudice, la risposta è corretta?") // Set the text to say.
                .build(); // Build the say action.

        selectedBin.run();
        sayAskForConfirm.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "è corretta", "è giusta", "corretta", "giusta",
                        "pepper sì", "pepper si", "confermi", "confermo")
                .build();

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "è sbagliata", "sbagliata","No Pepper", "è errata", "non è corretta",
                        "pepper no", "pepper no").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", /*"Ricominciamo", "Ricomincia",*/ "Da capo", "Non ho capito", "Puoi ripetere",
                        "pepper ripeti")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna alla home", "Home")
                .build();

        PhraseSet phraseSetBack = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna indietro", "Indietro")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetNo, phraseSetRepeat, phraseSetClose, phraseSetHome, phraseSetBack)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            isAnswerCorrect = true;
            if(isPepperTurn)
                pepperShouldTeach = true;
            goToNextTurn();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {
            isAnswerCorrect = false;
            goToNextTurn();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayJudgeTurnActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetBack)) {   // Richiesta utente di tornare alla pagina precedente (selezione rifiuto o simile)
            Intent activity2Intent;//Per andare alla pagina precedente
            if (isPepperTurn) { //TODO Rimuovi il bottone back se il turno era di Pepper o lascialo se si vuole ri-scattare la foto?
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


            activity2Intent.putExtra("roundTutorial", roundTutorial);
            activity2Intent.putExtra("endOfTutorial", endOfTutorial);
            activity2Intent.putExtra("restartGame", restartGame);
            activity2Intent.putExtra("roundTutorial", roundTutorial);
            activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
            activity2Intent.putExtra("tutorialState", tutorialState);
            startActivity(activity2Intent); //Per andare alla pagina principale

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

    String typeBinSelectedIs ;
    void startJudgeConfirm() {
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case TYPE_ORGANIC: // case "organic":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone dell'ORGANICO.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello dell'ORGANICO.";
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_brown_shadow));
                fading(selectedBin, getDrawable(R.drawable.bin_brown_shadow));
                break;
            case TYPE_PAPER_CARDBOARD: case TYPE_PAPER: case TYPE_CARDBOARD: // case "paper": case "cardboard":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone di CARTA E CARTONE.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello di CARTA E CARTONE.";
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_blue_shadow));
                fading(selectedBin, getDrawable(R.drawable.bin_blue_shadow));
                break;
            case TYPE_PLASTIC_METAL: case TYPE_PLASTIC: case TYPE_METAL: // case "plastic": case "metal":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone di PLASTICA E METALLI.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello di PLASTICA E METALLI.";
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_yellow_shadow));
                fading(selectedBin, getDrawable(R.drawable.bin_yellow_shadow));
                break;
            case TYPE_GLASS: // case "glass":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone del VETRO.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello del VETRO.";
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_green_shadow));
                fading(selectedBin, getDrawable(R.drawable.bin_green_shadow));
                break;
            default:
                typeBinSelectedIs = "Si è verificato un problema. Torniamo indietro e ripetiamo il turno.";

                //Centrare il button
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams
                        .WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                this.selectedBinIs.setLayoutParams(layoutParams);

                //"Questa era difficile. Non sono riuscito a capire il tipo di bidone. Torniamo indietro e ripetiamo il turno.";
                selectedBinIs.setGravity(Gravity.CENTER);
                textViewAskForConfirm.setVisibility(View.GONE); //View.INVISIBLE
                selectedBin.setVisibility(View.GONE); //View.INVISIBLE
                buttonYes.setVisibility(View.GONE); //View.INVISIBLE
                buttonNo.setVisibility(View.GONE); //View.INVISIBLE
                //TODO GOBACK
                //selectedBinIs.setText("ERRORE.");
                buttonBack.performClick();               ;
                break;
        }
        selectedBinIs.setText(typeBinSelectedIs);

        if(pressed) {
            // TODO Incrementa score
            goToNextTurn();
        }


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

    void goToNextTurn() {
        // NB: dubito entri se Pepper indovina, perché fa startare pepperteaches..
        Intent activity2Intent = new Intent(PlayJudgeTurnActivity.this, NextTurnActivity.class);//TODO GameOverActivity

        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("isAnswerCorrect", isAnswerCorrect); //Se la risposta è corretta
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        //activity2Intent.putExtra("scores", (Serializable) scores);
        activity2Intent.putExtra("tutorialEnabled", false); // Tutorial finito
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("pepperShouldTeach", pepperShouldTeach);
        startActivity(activity2Intent);
        finish();
    }

    public void buttonHelp(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        CommonUtils.showDialog(PlayJudgeTurnActivity.this, desc);
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

    boolean pepperTeacher() {
        //TODO Frasi che insegnano all'utente nozioni sulla raccolta differenziata
        return true;

    }

    //Dissolvenza bidone (animazione quando il bidone viene selezionato)
    public void fading(ImageView imageView, Drawable res) {
        //Credits: https://stackoverflow.com/questions/24939387/android-change-background-image-with-fade-in-out-animation
        android.view.animation.Animation fadeOut = AnimationUtils.loadAnimation(PlayJudgeTurnActivity.this, R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {

            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                android.view.animation.Animation fadeIn = AnimationUtils.loadAnimation(PlayJudgeTurnActivity.this, R.anim.fade_in);
                imageView.startAnimation(fadeIn);
                imageView.setBackground(res);
                imageView.setMaxWidth(230);
                imageView.setMaxHeight(338);

                /*
            android:layout_width="230dp"
            android:layout_height="338dp"*/
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {

            }

        });
    }

    void startPepperTeacher() {

        Log.e(TAG, "Entrato nella funzione startPepperTeacher.");
        Intent activity2Intent = new Intent(PlayJudgeTurnActivity.this, PepperTeachesActivity.class);//TODO GameOverActivity
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);

        startActivity(activity2Intent);
        finish();
    }
}