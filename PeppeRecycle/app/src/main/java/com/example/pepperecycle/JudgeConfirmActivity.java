package com.example.pepperecycle;

import static com.example.pepperecycle.PlayGameActivity.N_ROUNDS;

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

public class JudgeConfirmActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "JudgeConfirmActivity" ;//, CameraBridgeViewBase.CvCameraViewListener2{

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
        selectExclamation();
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
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        Log.d(TAG, "TrialState: "+ trialState);
        if(trialState == 0 || trialState == 1) {
            tvTutorialJudge.setVisibility(View.VISIBLE);
            Log.d(TAG, "Siamo nel trial. trialState: "+ trialState);
        } else {
            tvTutorialJudge.setVisibility(View.INVISIBLE);
            Log.d(TAG, "NON siamo nel trial. trialState: "+ trialState);
            /*textViewUserScore.setEnabled(true);
            textViewPepperScore.setEnabled(true);
            imageViewUserScore.setEnabled(true);
            imageViewPepperScore.setEnabled(true);*/
        }

        //OnClickListeners
        buttonYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonYes cliccato");
                //Il punteggio viene incrementato solo se la risposta è corretta
                isAnswerCorrect = true;
                pressed = true;
                buttonYes.setClickable(false);
                // Incrementa il punteggio solo se non si tratta di un turno di prova
                if(trialState == -1) {//                if(!tutorialEnabled) {
                    updateScore(isPepperTurn);
                    /*trialState = 1;*/ //Pronto per passare al turno di Pepper
                    Log.e(TAG, "Punteggio incrementato.");
                } else {
                    Log.d(TAG, "Turno di prova. Punteggio non incrementato.");
                    nextTurn();
                }
                //Rendo i bottoni non cliccabili per evitare di incrementare ulteriormente i punteggi
//                buttonYes.setClickable(false);
//                buttonNo.setClickable(false);
//                nextTurn(); //startPepperTeacher();//TODO ELIMINA tutta questa riga
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e(TAG, "buttonNo cliccato");
                isAnswerCorrect = false;
                pressed = true;

                //TODO Se il turno era dell'utente, fai dire a Pepper qualcosa random sul riciclo o no?
                //pepperTeaches = pepperTeacher();
                //Rendo i bottoni non cliccabili
//                buttonYes.setClickable(false);
//                buttonNo.setClickable(false);
                nextTurn();
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
        //        ackSelectedBin(qiContext); // Pepper dice all'utente qual è il bidone selezionato
        checkBin();
        Say selectedBin = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText(typeBinSelectedIs) // Set the text to say.
                .build(); // Build the say action.
        Say sayAskForConfirm = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Giudice, la risposta è corretta?") // Set the text to say.
                .build(); // Build the say action.
        Animation askForConfirm = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione
                .build();
        Animate animateAskForConfirm = AnimateBuilder.with(qiContext)
                .withAnimation(askForConfirm)
                .build();


        selectedBin.run();
        sayAskForConfirm.run();
        animateAskForConfirm.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "è corretta", "è giusta", "corretta", "giusta",
                        "pepper sì", "pepper si")
                .build();

        /*PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper").build(); //TODO idk */

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "è sbagliata", "sbagliata","No Pepper", "è errata", "non è corretta",
                        "pepper no", "pepper no").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", /*"Ricominciamo", "Ricomincia",*/ "Da capo", "Non ho capito", "Puoi ripetere",
                        "pepper ripeti")
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

        String phrase = "";
        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            //todo se l'utente ha sbagliato, insegna qualcosa
            if(trialState == -1) {// if(!tutorialEnabled)
                if(isPepperTurn) {
                    Say sayRandomFact = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                            .withText(exclamation) // Set the text to say.
                            .build(); // Build the say action.
                    sayRandomFact.run();
                } else {
                    if (currentRound<N_ROUNDS-1) {
                        phrase = "Hai indovinato. \\rspd=80\\ Complimenti. Ora tocca a me.";
                        Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                                .withText(phrase) // Set the text to say.
                                .build(); // Build the say action.
                        sayTurn.run();
                    }
                }
                updateScore(isPepperTurn);

            } else {
                //Pepper dice chi è il prossimo giocatore
                if (currentRound<N_ROUNDS-1 && !isPepperTurn) {
                    phrase = "Hai indovinato. complimenti. Ora tocca a me.";

                    Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                            .withText(phrase) // Set the text to say.
                            .build(); // Build the say action.
                    sayTurn.run();
                }
                nextTurn();
            }

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {
            if (currentRound<N_ROUNDS-1) {
                if (!isPepperTurn) {
                    phrase = "Oh no. Stavolta hai sbagliato. Tieni alta la concentrazione. Adesso tocca a me!";
                } else {
                    phrase = "Oh no. Devo impegnarmi di più. Adesso è il tuo turno.";
                }
                Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                        .withText(phrase) // Set the text to say.
                        .build(); // Build the say action.
                sayTurn.run();
            }
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

    public String setFactRecycle(String[] factType) {
        int randomFact = new Random().nextInt(factType.length);
        return factType[randomFact];
    }
    String typeBinSelectedIs ;
    void startJudgeConfirm() {
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case 0: // case "organic":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone dell'organico.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello dell'organico.";
//                selectedBinIs.setText("Il bidone selezionato è quello\ndell'organico");
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_brown_shadow));
//                selectedBin.setImageResource(R.drawable.closed_bin_brown_shadow);
                fading(selectedBin, getDrawable(R.drawable.bin_brown_shadow));
//                factAboutRecycle = setFactRecycle(factsOrganic);
                break;
            case 1: // case "paper": case "cardboard":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone di carta e cartone.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello di carta e cartone.";
//                selectedBinIs.setText("Il bidone selezionato è quello\ndi carta e cartone");
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_blue_shadow));
//              selectedBin.setImageResource(R.drawable.closed_bin_blue_shadow);
                fading(selectedBin, getDrawable(R.drawable.bin_blue_shadow));
//                factAboutRecycle = setFactRecycle(factsCardCardboard);
                break;
            case 2: // case "plastic": case "metal":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone di plastica e metalli.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello di plastica e metalli.";
//                selectedBinIs.setText("Il bidone selezionato è quello\ndi plastica e metalli");
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_yellow_shadow));
//                selectedBin.setImageResource(R.drawable.closed_bin_yellow_shadow);
                fading(selectedBin, getDrawable(R.drawable.bin_yellow_shadow));
//                factAboutRecycle = setFactRecycle(factsPlasticMetal);
                break;
            case 3: // case "glass":
                if (isPepperTurn)
                    typeBinSelectedIs = "Penso che questo rifiuto vada gettato nel bidone del vetro.";
                else
                    typeBinSelectedIs = "Il bidone selezionato è quello del vetro.";
//                selectedBinIs.setText("Il bidone selezionato è quello\ndel vetro");
                selectedBin.setBackground(getDrawable(R.drawable.closed_bin_green_shadow));
//                selectedBin.setImageResource(R.drawable.closed_bin_green_shadow);
                fading(selectedBin, getDrawable(R.drawable.bin_green_shadow));
//                factAboutRecycle = setFactRecycle(factsGlass);
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
            nextTurn();
            /*if(trialState != 2)
                nextTurn();
            else
                endTutorial();*/
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
        if (isPepperTurn) {
            Say sayPepperSelectBin = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Ci sono!. \\rspd=95\\" + wasteTypeString) // Set the text to say.
                    .build(); // Build the say action.
            sayPepperSelectBin.run();
        }
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
    /* public void buttonYes(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
         //Il punteggio viene incrementato solo se la risposta è corretta
         isAnswerCorrect = true;
         pressed = true;
         if(!tutorialEnabled) {
             updateScore(isPepperTurn);
         }

         Log.e(TAG, "Entrato nel buttonYes.");

         nextTurn();//startPepperTeacher();//TODO ELIMINA tutta questa riga
     }
     public void buttonNo(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
         isAnswerCorrect = false;
         pressed = true;
         //TODO Se il turno era del bambino, fai dire a Pepper qualcosa random sul riciclo
 //        pepperTeaches = pepperTeacher();
         nextTurn();
     }*/
    public void updateScore(boolean isPepperTurn) {
//        if(trialState == -1) { controllato già fuori
        if(isPepperTurn) {

            ++pepperScore;//scores.put("score_pepper", (byte) (scores.get("score_pepper") + 1)); //Incrementa il punteggio di Pepper
            // pepperTeaches = pepperTeacher();
            isPepperTurn = false; // solitamente il turno viene cambiato in nextTurn ma in questo caso non viene richiamata
            startPepperTeacher();
            Log.d(TAG, "Incrementato pepperScore");
            //nextTurn();
        } else {
            ++userScore;//scores.put("score_user1", (byte) (scores.get("score_user1") + 1)); //Incrementa il punteggio dell'utente
//            buttonYes.setEnabled(false);
            nextTurn();
            Log.d(TAG, "Incrementato userScore");
            //TODO inserisci congratulazioni
        }
    }
    //}
    byte nextTrialState(byte state) {
        switch(state) {
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
    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        // NB: dubito entri se Pepper indovina, perché fa startare pepperteaches..
        Intent activity2Intent;
        trialState = nextTrialState(trialState);
        if (trialState == 2) {
            endTutorial();
        } else {
            isPepperTurn = !isPepperTurn; // Turno successivo
            ++currentRound;
            Log.d("ROUND", "currentRound++");
            // if (round < 6) {    // TODO sostituisci 6 con una costante
            // if((scores.get("score_pepper") < 3 )    ||  (scores.get("score_user1") < 3) )   {
            // if ( pepperScore < 3 && userScore < 3 )   { // Si ripete fin quando uno dei giocatori non ha raggiunto il punteggio massimo
            if (currentRound < N_ROUNDS) {
                String phrase;
                // TODO sostituisci il 6 con una costante, tipo WINNER_SCORE o simili4
                if(isPepperTurn) {
                    phrase = "Ora tocca a me.";
                    activity2Intent = new Intent(JudgeConfirmActivity.this, PlayPepperTurnActivity.class);
                    Log.d(TAG, "trialState passato a PepperTurn: " + trialState);
                } else {
                    phrase = "Adesso è il tuo turno.";
                    activity2Intent = new Intent(JudgeConfirmActivity.this, PlayUserTurnActivity.class);
                    Log.d(TAG, "trialState passato a UserTurn: " + trialState);
                }
//                pepperSayTurn(isPepperTurn);

            /*if (isPepperTurn) { TODO se non va bene rimetti come stava
                if (tutorialEnabled) {
                    activity2Intent = new Intent(JudgeConfirmActivity.this, PlayGameActivity.class); // PlayPepperTurnActivity.class);
                } else {
                    activity2Intent = new Intent(JudgeConfirmActivity.this, PlayPepperTurnActivity.class); // PlayPepperTurnActivity.class);
                }

            } else {
                activity2Intent = new Intent(JudgeConfirmActivity.this, PlayUserTurnActivity.class);
            }*/

            /*
            pepperScore = activity2Intent.getExtras().getByte("pepperScore");
            userScore = activity2Intent.getExtras().getByte("userScore");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)//activity2Intent.putExtra("scores", scores);
            */
            } else {
                // Game over
                activity2Intent = new Intent(JudgeConfirmActivity.this, GameOverActivity.class);//TODO GameOverActivity
            }

            activity2Intent.putExtra("round", round);
            activity2Intent.putExtra("pepperScore", pepperScore);
            activity2Intent.putExtra("userScore", userScore);
            //activity2Intent.putExtra("scores", (Serializable) scores);
            activity2Intent.putExtra("tutorialEnabled", false); // Tutorial finito
            activity2Intent.putExtra("currentRound", currentRound);
            activity2Intent.putExtra("trialState", trialState);
            startActivity(activity2Intent);
            finish();
        }
    }

    /*void pepperSayTurn(boolean isPepperTurn) {
        String phrase;
        if(isPepperTurn)
            phrase = "Ora tocca a me.";
        else
            phrase = "Adesso è il tuo turno.";
        Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText(phrase) // Set the text to say.
                .build(); // Build the say action.
        sayTurn.run();
    }*/
    public void endTutorial() {
        Intent activity2Intent = new Intent(JudgeConfirmActivity.this, TutorialEndActivity.class);
        activity2Intent.putExtra("endOfTutorial", true); // Tutorial finito
        activity2Intent.putExtra("pgIndex", 0); //TODO SOSTITUISCI NUMERO MAGICO
        activity2Intent.putExtra("trialState", trialState);
        /*

         endOfTutorial = extras.getBoolean("endOfTutorial");
            pgIndex = extras.getInt("pgIndex");
            trialState = extras.getByte("trialState");
         */

        startActivity(activity2Intent);
        finish();
    }
    void selectExclamation() {
        Log.e(TAG, "Entrato nel selectExclamation.");
        exclamation = pepperExclamations[ new Random().nextInt(pepperExclamations.length)];

        Log.e(TAG, "exclamation: " + exclamation);

    }

    void startPepperTeacher() {

        Log.e(TAG, "Entrato nella funzione startPepperTeacher.");
        Intent activity2Intent = new Intent(JudgeConfirmActivity.this, PepperTeachesActivity.class);//TODO GameOverActivity
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

        CommonUtils.showDialog(JudgeConfirmActivity.this, desc);
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

    boolean pepperTeacher() {
        //TODO Frasi che insegnano all'utente nozioni sulla raccolta differenziata
        return true;

    }

    public void showDialog(String mex) { //desc sarà il contenuto della finestra di dialogo
        dialog.setContentView(R.layout.dialog_tutorial_layout);
        Log.e(TAG, "Entrata nella showDialog");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        TextView textViewDialogTutorial = (TextView) dialog.findViewById(R.id.textViewDialogTutorial);
        ImageButton dialogButtonClose = (ImageButton) dialog.findViewById(R.id.dialogButtonClose);
//TODOhttps://youtu.be/vDAO7H5w4_I
        Log.e(TAG, "Prima di settext");
        textViewDialogTutorial.setText(mex);
        /*textViewDialogTutorial.setText("Qui, sul mio tablet, ci sono quattro bidoni:\n" +
                "organico, plastica e metalli, carta e cartone, vetro.\n" +
                "Il giudice ti mostrerà un rifiuto e tu dovrai dirmi in quale bidone buttarlo per un corretto smaltimento.\n" +
                "Se indovinerai, guadagnerai un punto!");*/
        Log.e(TAG, "Dopo settext");

        dialogButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });

        Log.e(TAG, "Prima di dialog.show");
        dialog.show();
        Log.e(TAG, "Dopo dialog.show");

    }


    //Dissolvenza bidone (animazione quando il bidone viene selezionato)
    public void fading(ImageView imageView, Drawable res) {
        //Credits: https://stackoverflow.com/questions/24939387/android-change-background-image-with-fade-in-out-animation
        android.view.animation.Animation fadeOut = AnimationUtils.loadAnimation(JudgeConfirmActivity.this, R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {

            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                android.view.animation.Animation fadeIn = AnimationUtils.loadAnimation(JudgeConfirmActivity.this, R.anim.fade_in);
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


}