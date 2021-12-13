package com.example.pepperecycle;

import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_GLASS;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_ORGANIC;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PAPER_CARDBOARD;
import static com.example.pepperecycle.PlayPepperTurnActivity.TYPE_PLASTIC_METAL;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
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
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

// Activity relativa al turno dell'utente
public class PlayUserTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "PlayUserTurnActivity" ;//, CameraBridgeViewBase.CvCameraViewListener2{
    byte round;
    boolean isPepperTurn;
    byte wasteType=-1; //0=Organico, 1=Carta/Cartone, 2=Plastica/Metalli, 3=Vetro
    String binType;

    static byte pepperScore, userScore;
    TextView textViewUserScore, textViewPepperScore, tvTutorial;
    ImageView imageViewUserScore, imageViewPepperScore;
    boolean tutorialEnabled;

    byte trialState;
    private Animate animate;    // Store the Animate action.

    ImageView binBrown, binBlue, binYellow, binGreen;

    Dialog dialog;

    String desc;
    ImageButton buttonHelp;
    boolean canCloseApp;
    byte currentTurn;
    boolean restartGame;
    boolean roundTutorial;
    boolean endOfTutorial, tutorialState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        setContentView(R.layout.activity_play_user_turn);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        isPepperTurn=false;
        roundTutorial=false;
        textViewUserScore = findViewById(R.id.textViewUserScore);
        textViewPepperScore = findViewById(R.id.textViewPepperScore);
        imageViewUserScore = findViewById(R.id.imageViewUserScore);
        imageViewPepperScore = findViewById(R.id.imageViewPepperScore);
        tvTutorial = findViewById(R.id.tvTutorial);

        buttonHelp = findViewById(R.id.buttonHelp);

        binBrown = findViewById(R.id.binBrown);
        binBlue = findViewById(R.id.binBlue);
        binYellow = findViewById(R.id.binYellow);
        binGreen = findViewById(R.id.binGreen);

        dialog = new Dialog(this);
        canCloseApp = false;
        trialState = -1;
        restartGame=false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            round = extras.getByte("round");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            currentTurn = extras.getByte("currentTurn");
            roundTutorial = extras.getBoolean("roundTutorial");
            trialState = extras.getByte("trialState");
            restartGame = extras.getBoolean("restartGame");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            endOfTutorial = extras.getBoolean("endOfTutorial");
            tutorialState = extras.getBoolean("tutorialState");
        }
        showScore();

        if(trialState == 0) { //turno di prova
            textViewUserScore.setVisibility(View.INVISIBLE);
            textViewPepperScore.setVisibility(View.INVISIBLE);
            imageViewUserScore.setVisibility(View.INVISIBLE);
            imageViewPepperScore.setVisibility(View.INVISIBLE);
        } else {
            tvTutorial.setVisibility(View.INVISIBLE);
        }

        desc = "Qui, sul mio tablet, ci sono quattro bidoni:\n" +
                "organico, carta e cartone, plastica e metalli, vetro.\n" +
                "Il giudice ti mostrerà un rifiuto e tu dovrai dirmi in quale bidone buttarlo per un corretto smaltimento.\n" +
                "Se indovinerai, guadagnerai un punto.\n" +
                "Buona fortuna!";

        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtils.showDialog(PlayUserTurnActivity.this, desc);
            }

        });

        binBrown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "selezionato bidone ORGANICO");
                wasteType = TYPE_ORGANIC;
                askForConfirm();
            }

        });
        binBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "selezionato bidone CARTA E CARTONE");
                wasteType = TYPE_PAPER_CARDBOARD;
                askForConfirm();
            }

        });
        binYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "selezionato bidone PLASTICA E METALLI");
                wasteType = TYPE_PLASTIC_METAL;
                askForConfirm();
            }

        });
        binGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "selezionato bidone VETRO");
                wasteType = TYPE_GLASS;
                askForConfirm();
            }

        });

    }
    void showScore () {
        textViewPepperScore.setText(""+pepperScore);
        textViewUserScore.setText(""+userScore);
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayUserTurn= SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Iniziamo dal tuo turno.") // Set the text to say.
                .build(); // Build the say action.

        Animation explain = AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_right_hand_a002).build();
        Animate animateUserTurn = AnimateBuilder.with(qiContext)
                .withAnimation(explain).build();
        Say sayUserTurnTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Qui, sul mio tablet, ci sono quattro bidoni: organico, carta e cartone, plastica e metalli, vetro. Il giudice ti mostrerà un rifiuto e tu dovrai indicare in quale bidone buttarlo per un corretto smaltimento. Se indovinerai, guadagnerai un punto.! Giudice, per favore, mostraci l'oggetto.! ") // Set the text to say.
                .build(); // Build the say action.;

        Say sayUserTurnExplain= SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("in quale bidone decidi di buttare il rifiuto che ti ha mostrato il giudice?") // Set the text to say.
                .withBodyLanguageOption(BodyLanguageOption.DISABLED)
                .build(); // Build the say action.

        PhraseSet phraseSelectBrownBin = PhraseSetBuilder.with(qiContext)
                .withTexts("primo", "primo bidone", "bidone uno", "bidone marrone", "marrone",
                        "organico", "bidone dell'organico", "umido", "bidone dell'umido")
                .build();
        PhraseSet phraseSelectBlueBin = PhraseSetBuilder.with(qiContext)
                .withTexts("secondo", "secondo bidone", "bidone due", "bidone blu", "blu",
                        "carta", "cartone", "carta e cartone", "bidone della carta",
                        "bidone del cartone", "bidone di carta e cartone")
                .build();
        PhraseSet phraseSelectYellowBin = PhraseSetBuilder.with(qiContext)
                .withTexts("terzo", "terzo bidone", "bidone tre", "bidone giallo",
                        "giallo", "plastica e metalli", "metallo", "plastica", "metalli",
                        "bidone della plastica", "bidone dei metalli", "bidone del metallo")
                .build();
        PhraseSet phraseSelectGreenBin = PhraseSetBuilder.with(qiContext)
                .withTexts("quarto", "quarto bidone", "bidone quattro", "bidone verde", "verde",
                        "vetro", "bidone del vetro", "ultimo", "l'ultimo","l'ultimo bidone")
                .build();

        PhraseSet phraseSelectOtherBins = PhraseSetBuilder.with(qiContext)
                .withTexts("indifferenziato", "indifferenziata", "rifiuto speciale", "rifiuti speciali")
                .build();

        PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper", "aiutami tu pepper", "pepper aiutami tu", "aiutami").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", "Ricominciamo", "Ricomincia", "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        if(currentTurn==0)
            sayUserTurn.run();
        if(trialState == 0) {
            sayUserTurnTutorial.run();
        }
        animateUserTurn.async().run();
        sayUserTurnExplain.run();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSelectBrownBin, phraseSelectBlueBin, phraseSelectYellowBin, phraseSelectGreenBin,
                        phraseSetRepeat, phraseSetClose, phraseSetHome, phraseSelectOtherBins, phraseSetIdk)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectBrownBin)) {             // Utente seleziona il bidone dell'organico
            wasteType = TYPE_ORGANIC;
            binType = "Organico";
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectBlueBin)) {      // Utente seleziona il bidone carta e cartone
            wasteType = TYPE_PAPER_CARDBOARD;
            binType = "Carta e cartone";
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectYellowBin)) {      // Utente seleziona il bidone plastica e metalli
            wasteType = TYPE_PLASTIC_METAL;
            binType = "Plastica e metalli";
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectGreenBin)) {      // Utente seleziona il bidone vetro
            wasteType = TYPE_GLASS;
            binType = "Vetro";
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSelectOtherBins)) {      // Altri bidoni non contemplati
            Say sayRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("La scelta non è valida. Puoi selezionare solamente i quattro bidoni qui presenti.") // Set the text to say.
                    .build(); // Build the say action.
            sayRepeat.run();

            repeatActivity();
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetIdk)) {      // Utente seleziona il bidone carta e cartone
            Say sayRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Dai. So che puoi farcela.") // Set the text to say.
                    .build(); // Build the say action.
            sayRepeat.run();

            repeatActivity();
        }else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere
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
    void askForConfirm() {
        Intent activity2Intent = new Intent(PlayUserTurnActivity.this, PlayJudgeTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("roundTutorial", roundTutorial);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentTurn", currentTurn);
        startActivity(activity2Intent);
        finish();
    }

    void repeatActivity() {
        Intent activity2Intent = new Intent(PlayUserTurnActivity.this, PlayUserTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("roundTutorial", roundTutorial);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentTurn", currentTurn);
        startActivity(activity2Intent);
        finish();
    }

    public void buttonHelp(View v) { //Pressione tasto "?"
        showDialog(desc);
    }
    public void buttonHome(View v) { //Pressione tasto "Home"
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "X"
        CommonUtils.showDialogExit(this);
    }

    public void showDialog(String mex) { //desc sarà il contenuto della finestra di dialogo
        dialog.setContentView(R.layout.dialog_tutorial_layout);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        TextView textViewDialogTutorial = dialog.findViewById(R.id.textViewDialogTutorial);
        ImageButton dialogButtonClose = dialog.findViewById(R.id.dialogButtonClose);

        textViewDialogTutorial.setText(mex);

        dialogButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }
}