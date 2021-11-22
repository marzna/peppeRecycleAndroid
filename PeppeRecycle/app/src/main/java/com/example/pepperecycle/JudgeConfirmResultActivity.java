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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JudgeConfirmResultActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "JudgeConfirmActivityResult" ;

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
    ImageButton buttonBack;
    TextView textViewAskForConfirm, tvTutorialJudge;
    byte currentRound;
    byte tutorialState = -1;
    byte trialState;
    boolean endOfTutorial;
    boolean restartGame;
    boolean isTrue;

    String exclamation;

    String[] pepperWrongPhrase = {
            "Argh, dovresti impegnarti di più.",
            "Oh no, hai sbagliato.",
            "Cerca di tenere alta la concentrazione."
    };
    String[] pepperCorrectPhrase = {
            "Complimenti.",
            "Che bello, hai indovinato.",
            "Risposta corretta? Allora hai guadagnato un punto, complimenti."
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        Log.d(TAG, "JudgeConfirmResult iniziato");

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_judge_confirm_result);

        selectedBinIs = findViewById(R.id.textViewSelectedBinIs);
        selectedBin = findViewById(R.id.selectedBin);
        textViewAskForConfirm = findViewById(R.id.textViewAskForConfirm);
        tvTutorialJudge = findViewById(R.id.tvTutorialJudge);

        buttonBack = findViewById(R.id.buttonBack);

        selectedBinIs.setVisibility(View.INVISIBLE);

        desc = "In questa fase del gioco,\n" +
                "il giudice deve stabilire se la risposta è corretta.\n" +
                "Chi ha indovinato guadagnerà un punto!";

        dialog = new Dialog(this);

        trialState = -1;
        Bundle extras = getIntent().getExtras();
        isTrue = false;
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
            isTrue = extras.getBoolean("isTrue");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }


        selectExclamation(isTrue);
        setSelectedBin();

        Log.d(TAG, "TrialState: "+ trialState);
        if(trialState == 0 || trialState == 1) {
            tvTutorialJudge.setVisibility(View.VISIBLE);
            Log.d(TAG, "Siamo nel trial. trialState: "+ trialState);
        } else {
            tvTutorialJudge.setVisibility(View.INVISIBLE);
            Log.d(TAG, "NON siamo nel trial. trialState: "+ trialState);
        }

        //OnClickListeners
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



    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
    Animate animate;
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        checkBin();

        Say sayResult = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText(exclamation) // Set the text to say.
                .build(); // Build the say action.


        sayResult.run();
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
    void setSelectedBin() {
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case TYPE_ORGANIC: // case "organic":
                selectedBin.setBackground(getDrawable(R.drawable.bin_brown_shadow));
                break;
            case TYPE_PAPER_CARDBOARD: case TYPE_PAPER: case TYPE_CARDBOARD: // case "paper": case "cardboard":
                selectedBin.setBackground(getDrawable(R.drawable.bin_blue_shadow));
                break;
            case TYPE_PLASTIC_METAL: case TYPE_PLASTIC: case TYPE_METAL: // case "plastic": case "metal":
                selectedBin.setBackground(getDrawable(R.drawable.bin_yellow_shadow));
                break;
            case TYPE_GLASS: // case "glass":
                selectedBin.setBackground(getDrawable(R.drawable.bin_green_shadow));
                break;
            default:
                buttonBack.performClick();               ;
                break;
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

    public void updateScore(boolean isPepperTurn) {
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
    public void goToNextTurn() {
        Intent activity2Intent;
        activity2Intent = new Intent(JudgeConfirmResultActivity.this, NextTurnActivity.class);//TODO GameOverActivity

        activity2Intent.putExtra("isTrue", isTrue);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("tutorialEnabled", false);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent);
        finish();
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
                    activity2Intent = new Intent(JudgeConfirmResultActivity.this, PlayPepperTurnActivity.class);
                    Log.d(TAG, "trialState passato a PepperTurn: " + trialState);
                } else {
                    phrase = "Adesso è il tuo turno.";
                    activity2Intent = new Intent(JudgeConfirmResultActivity.this, PlayUserTurnActivity.class);
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
                activity2Intent = new Intent(JudgeConfirmResultActivity.this, GameOverActivity.class);//TODO GameOverActivity
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
        Intent activity2Intent = new Intent(JudgeConfirmResultActivity.this, TutorialEndActivity.class);
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
    void selectExclamation(boolean isTrue) {
        if (isTrue)
            exclamation = pepperCorrectPhrase[ new Random().nextInt(pepperCorrectPhrase.length)];
        else
            exclamation = pepperWrongPhrase[ new Random().nextInt(pepperWrongPhrase.length)];
    }

    void startPepperTeacher() {

        Log.e(TAG, "Entrato nella funzione startPepperTeacher.");
        Intent activity2Intent = new Intent(JudgeConfirmResultActivity.this, PepperTeachesActivity.class);//TODO GameOverActivity
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

        CommonUtils.showDialog(JudgeConfirmResultActivity.this, desc);
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
        android.view.animation.Animation fadeOut = AnimationUtils.loadAnimation(JudgeConfirmResultActivity.this, R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {

            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                android.view.animation.Animation fadeIn = AnimationUtils.loadAnimation(JudgeConfirmResultActivity.this, R.anim.fade_in);
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

    public void nextTurn(boolean isTrue) { // Avvia la activity relativa al prossimo turno (o di Game Over)
        // NB: dubito entri se Pepper indovina, perché fa startare pepperteaches..
        Intent activity2Intent;
        trialState = nextTrialState(trialState);
        if (trialState == 2) {
            endTutorial();
        } else {
            isPepperTurn = !isPepperTurn; // Turno successivo
            ++currentRound;
            Log.d("ROUND", "currentRound++");
            if (currentRound < N_ROUNDS) {
                String phrase;
                // TODO sostituisci il 6 con una costante, tipo WINNER_SCORE o simili4
                if(isPepperTurn) {
                    phrase = "Ora tocca a me.";
                    activity2Intent = new Intent(JudgeConfirmResultActivity.this, PlayPepperTurnActivity.class);
                    Log.d(TAG, "trialState passato a PepperTurn: " + trialState);
                } else {
                    phrase = "Adesso è il tuo turno.";
                    activity2Intent = new Intent(JudgeConfirmResultActivity.this, PlayUserTurnActivity.class);
                    Log.d(TAG, "trialState passato a UserTurn: " + trialState);
                }
            } else {
                // Game over
                activity2Intent = new Intent(JudgeConfirmResultActivity.this, GameOverActivity.class);//TODO GameOverActivity
            }
            activity2Intent.putExtra("isTrue", isTrue); //Se la risposta è corretta
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

}