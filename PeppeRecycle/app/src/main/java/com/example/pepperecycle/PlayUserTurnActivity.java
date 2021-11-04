package com.example.pepperecycle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlayUserTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "PlayUserTurnActivity" ;//, CameraBridgeViewBase.CvCameraViewListener2{
    byte round;
    boolean isPepperTurn;
    byte wasteType=-1; //0=Organico, 1=Carta/Cartone, 2=Plastica/Metalli, 3=Vetro
    String binType;
    boolean binPressed=false;
    //TODO sposta le costanti in una posizione adeguata
    static final byte TYPE_ORGANIC = 0;
    static final byte TYPE_PAPER_CARDBOARD = 1;
    static final byte TYPE_PLASTIC_METAL = 2;
    static final byte TYPE_GLASS = 3;
    Map<String, Byte> scores = new HashMap<String, Byte>();
    static byte pepperScore;
    static byte userScore;
    TextView textViewUserScore, textViewPepperScore;
    boolean tutorialEnabled;
    // Store the Animate action.
    private Animate animate;

    Dialog dialog;

    CommonUtils commonUtils = new CommonUtils();
    String desc;
    ImageButton buttonHelp;
    boolean canCloseApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        setContentView(R.layout.activity_play_user_turn);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        isPepperTurn=false;
        textViewUserScore = findViewById(R.id.textViewUserScore);
        textViewPepperScore = findViewById(R.id.textViewPepperScore);
        dialog = new Dialog(this);
        canCloseApp = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            round = extras.getByte("round");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
        }
        showScore();

        /*if (scores != null) { //TODO Per il momento uso i byte per segnare gli scores... Nel caso, poi, userò dizionari
            textViewPepperScore.setText(scores.get("score_pepper"));
            textViewUserScore.setText(scores.get("score_user1"));
        } else {
            textViewPepperScore.setText("Non valido");
            textViewUserScore.setText("Non valido");
        }*/

        desc = "Qui, sul mio tablet, ci sono quattro bidoni:\n" +
                "organico, carta e cartone, plastica e metalli, vetro.\n" +
                "Il giudice ti mostrerà un rifiuto e tu dovrai dirmi in quale bidone buttarlo per un corretto smaltimento.\n" +
                "Se indovinerai, guadagnerai un punto.\n" +
                "Buona fortuna!";


        buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.showDialog(PlayUserTurnActivity.this, desc);
                //showDialog(desc);
            }

        });
/*

        if(canCloseApp) {
            finish();
        }
*/

    }
    void showScore () {
        textViewPepperScore.setText(""+pepperScore);
        textViewUserScore.setText(""+userScore);
        /*if ((pepperScore >= 0 && pepperScore <3) &&
                (userScore >= 0 && userScore <3)        ) {
            textViewPepperScore.setText(pepperScore);
            textViewUserScore.setText(userScore);
        }*/
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayUserTurn= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Tocca a te!") // Set the text to say.
                .build(); // Build the say action.
        //TODO Help ... in una dialog
        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_right_hand_a002).build();
        Animate animateUserTurn = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();
        Say sayUserTurnTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Qui, sul mio tablet, ci sono quattro bidoni: organico, carta e cartone, plastica e metalli, vetro. Il giudice ti mostrerà un rifiuto e tu dovrai dirmi in quale bidone buttarlo per un corretto smaltimento. Se indovinerai, guadagnerai un punto.! Giudice, per favore, mostraci l'oggetto, così da iniziare subito con un turno di prova.! ") // Set the text to say.
                .build(); // Build the say action.;

        Say sayUserTurnExplain= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Qual è il bidone adatto a questo rifiuto?") // Set the text to say.
                .build(); // Build the say action.

        PhraseSet phraseSelectBrownBin = PhraseSetBuilder.with(qiContext)
                .withTexts("primo", "primo bidone", "bidone uno", "bidone marrone", "organico", "bidone dell'organico", "umido", "bidone dell'umido")
                .build();
        PhraseSet phraseSelectBlueBin = PhraseSetBuilder.with(qiContext)
                .withTexts("secondo", "secondo bidone", "bidone due", "bidone blu", "carta", "cartone", "carta e cartone", "bidone della carta", "bidone del cartone", "bidone di carta e cartone")
                .build();
        PhraseSet phraseSelectYellowBin = PhraseSetBuilder.with(qiContext)
                .withTexts("terzo", "terzo bidone", "bidone tre", "bidone giallo", "metallo", "plastica", "bidone della plastica", "bidone dei metalli", "bidone del metallo")
                .build();
        PhraseSet phraseSelectGreenBin = PhraseSetBuilder.with(qiContext)
                .withTexts("quarto", "quarto bidone", "bidone quattro", "bidone verde", "vetro", "bidone del vetro", "ultimo", "l'ultimo","l'ultimo bidone")
                .build();

/*        PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper").build(); //TODO idk */

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", "Ricominciamo", "Ricomincia", "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        sayUserTurn.run();
        if(tutorialEnabled){
            sayUserTurnTutorial.run();
        }
        animateUserTurn.run();
        sayUserTurnExplain.run();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSelectBrownBin, phraseSelectBlueBin, phraseSelectYellowBin, phraseSelectGreenBin,
                        phraseSetRepeat, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectBrownBin)) {             // Utente seleziona il bidone dell'organico
            wasteType = TYPE_ORGANIC;
            ackSelectedBin(qiContext);
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectBlueBin)) {      // Utente seleziona il bidone carta e cartone
            wasteType = TYPE_PAPER_CARDBOARD;
            ackSelectedBin(qiContext);
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectYellowBin)) {      // Utente seleziona il bidone plastica e metalli
            wasteType = TYPE_PLASTIC_METAL;
            ackSelectedBin(qiContext);
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectGreenBin)) {      // Utente seleziona il bidone vetro
            wasteType = TYPE_GLASS;
            ackSelectedBin(qiContext);
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
                    .withText("Va bene, sto chiudendo il gioco. Ciaoo!") // Set the text to say.
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

    /* void startUserTurn() {
         if(binPressed) {
             Intent activity2Intent = new Intent(PlayUserTurnActivity.this, JudgeConfirmActivity.class);
             activity2Intent.putExtra("wasteType", wasteType);
             activity2Intent.putExtra("round", round);
             activity2Intent.putExtra("isPepperTurn", isPepperTurn);
             startActivity(activity2Intent);
             finish();
         }
 *//*
        Intent activity2Intent = new Intent(PlayGameActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);*//*
     *//*        Intent activity2Intent = new Intent(getApplicationContext(), JudgeConfirmActivity.class);
        startActivity(activity2Intent);
        finish();*//*
     *//* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        *//*
    }*/
    void ackSelectedBin(QiContext qiContext) { //Todo forse va messo direttamente in JudgeConfirm...
        checkBin();
        Say sayAskForConfirm= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Hai scelto" + binType + ". Giudice, la risposta è corretta?") // Set the text to say.
                .build(); // Build the say action.
        Animation askForConfirm = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione
                .build();
        Animate animateAskForConfirm = AnimateBuilder.with(qiContext)
                .withAnimation(askForConfirm)
                .build();

        animateAskForConfirm.run();
        askForConfirm();
    }

    void askForConfirm() {
        Intent activity2Intent = new Intent(PlayUserTurnActivity.this, JudgeConfirmActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        startActivity(activity2Intent);
        finish();
        /* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        */
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
    public void selectBinBrown(View v) {    // 0 = Organico
        wasteType = TYPE_ORGANIC;
        // binPressed=true;
        askForConfirm();
    }
    public void selectBinBlue(View v) {     // 1 = Carta / Cartone
        wasteType = TYPE_PAPER_CARDBOARD;
        // binPressed=true;
        askForConfirm();
    }
    public void selectBinYellow(View v) {   // 2 = Plastica / Metalli
        wasteType = TYPE_PLASTIC_METAL;
        // binPressed=true;
        askForConfirm();
    }
    public void selectBinGreen(View v) {    // 3 = Vetro
        wasteType = TYPE_GLASS;
        // binPressed=true;
        askForConfirm();
    }
    public void buttonHelp(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        showDialog(desc);
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

/*
    public void showDialogExit() { //desc sarà il contenuto della finestra di dialogo
        dialog.setContentView(R.layout.dialog_exit_confirm_layout);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        Log.e(TAG, "Entrata nella showDialog");
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        ImageButton dialogButtonCloseYes = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseYes);
        ImageButton dialogButtonCloseNo = (ImageButton) dialog.findViewById(R.id.dialogButtonCloseNo);

        dialogButtonCloseYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }

        });
        dialogButtonCloseNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });
        Log.e(TAG, "Prima di dialog.show");
        dialog.show();
        Log.e(TAG, "Dopo dialog.show");

    }*/
}