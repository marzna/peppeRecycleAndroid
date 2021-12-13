package com.example.pepperecycle.game;

import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_CARDBOARD;
import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_GLASS;
import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_METAL;
import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_ORGANIC;
import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_PAPER;
import static com.example.pepperecycle.game.PlayPepperTurnActivity.TYPE_PLASTIC;

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
import com.example.pepperecycle.MainActivity;
import com.example.pepperecycle.R;
import com.example.pepperecycle.utils.CommonUtils;

import java.util.Random;

// Activity in cui Pepper racconta una curiosità relativa al rifiuto appena indovinato
public class PepperTeachesActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {

    String TAG = "PepperTeachesActivity";
    boolean isPepperTurn;
    byte wasteType, currentTurn, round;
    TextView textViewRandomFact, textViewFactAbout;
    ImageView selectedBin;
    static byte pepperScore, userScore;
    String wasteTypeString;
    String factAboutRecycle, textFactAboutRecycle;
    boolean tutorialEnabled;
    Button buttonPlay;
    Animate animate;
    String exclamation;
    byte trialState;
    boolean pepperShouldTeach;

    String[] pepperExclamations = {
            "Evvài, ho indovinato!",
            "Mi sto impegnando!",
            "Questa cosa la sapevo proprio bene! ",
            "Si vede che ho studiato!",
            "Certo che sono proprio bravo!"
    };
    String[] factsOrganic = {
            "L'organico è formato da scarti alimentari e altri rifiuti facilmente biodegradabili.",
            "Una torsolo di mela resta per circa 2 mesi nell'ambiente prima di degradarsi completamente.",
            "Circa un terzo dei rifiuti prodotti da una persona è composto da rifiuti organici.",
            "Dalla decomposizione dei rifiuti organici, si ottiene il compost, un ottimo concime per il terreno.",
            "Il compost può essere prodotto sia su scala industriale che domestica."
    };
    String[] factsPlastic = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori, o imballaggi, dei prodotti che compriamo, come, la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e tanto altro.",
            "Una mascherina impiegherebbe tra i 400 e i 450 anni per degradarsi completamente nell'ambiente. Durante questo processo, rilascerebbe micro-particelle di plastica, che, in mare, potrebbero essere ingerite dalle specie marine.",
            "Moltissime creature marine muoiono ogni anno a causa delle buste di plastica e della spazzatura che vengono gettate in mare.",
            "Una bottiglia di plastica resta per circa 450 anni nell'ambiente prima di degradarsi completamente.",
            "Riciclare la plastica fa risparmiare il doppio dell'energia che verrebbe consumata per bruciarla in un inceneritore.",
            "Riciclare la plastica fa risparmiare l'88% dell'energia che verrebbe consumata per crearla dalle materie prime."
    };
    String[] factsMetal = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori, o imballaggi, dei prodotti che compriamo, come, la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e tanto altro.",
            "Una lattina di alluminio resta dai 20 ai 100 anni nell'ambiente prima di degradarsi completamente, ecco perché sarebbe meglio riciclarla nell'apposita categoria!",
            "L'alluminio può essere riciclato infinite volte, senza perdita di qualità.",
            "In soli due mesi, le lattine di alluminio possono essere riciclate e rimesse in commercio.",
            "Riciclare una lattina di alluminio farebbe risparmiare l'energia necessaria per poter guardare la tv per circa 3 ore.",
            "Una lattina di alluminio può essere riciclata usando solo il 5% dell'energia che bisognerebbe impiegare per fabbricarla da zero.",
    };
    String[] factsPaper = {
            "Moltissimi oggetti che usiamo oggi rientrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste.",
            "Molti degli scontrini attualmente in circolazione non vanno gettati nel bidone dell'indifferenziato, perché sono composti da carte termiche che, se catalogate come carta e cartone, potrebbero causare problemi durante il riciclo.",
            "Se riciclassimo tutti i giornali, potremmo salvare oltre 250 milioni di alberi ogni anno.",
            "La carta riciclata produce circa il 70% in meno di inquinamento atmosferico rispetto alla sua creazione a partire dalle materie prime."
    };
    String[] factsCardboard = {
            "Moltissimi oggetti che usiamo oggi rientrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste.",
            "Una scatola di cartone resta per circa 9 mesi nell'ambiente prima di decomporsi completamente.",
            "Riciclare un cartone richiede solo il 75% dell'energia necessaria per produrne uno nuovo.",
    };
    String[] factsGlass = {
            "Il vetro può essere riciclato e rifabbricato un numero infinito di volte e non si usura mai.",
            "Il vetro mantiene il suo colore anche dopo il riciclo, perciò viene suddiviso in colori.",
            "Una moderna bottiglia di vetro potrebbe impiegare più di 4000 anni per decomporsi del tutto, forse anche di più se si trova in discarica.",
            "Anche gli antichi Romani riciclavano il vetro.",
            "Circa 7 bottiglie su 10 sono prodotte con vetro riciclato.",
            "Riciclando 1 chilo di rifiuti in vetro, si otterrà 1 chilo di nuovi prodotti in vetro riciclato, senza perdere nulla!",
    };
    String[] factsOrganicSpeech = {
            "L'organico è formato da scarti alimentari e altri rifiuti facilmente biodegradabili.",
            "Una torsolo di mela resta per circa 2 mesi nell'ambiente prima di degradarsi completamente.",
            "Circa un terzo dei rifiuti prodotti da una persona è composto da rifiuti organici.",
            "Dalla decomposizione dei rifiuti organici, si ottiene il compost, un ottimo concime per il terreno.",
            "Il compost può essere prodotto sia su scala industriale che domestica."
    };
    String[] factsPlasticSpeech = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori, o imballaggi, dei prodotti che compriamo, come, la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e \\rspd=80\\tànto àltro.",
            "Una mascherina impiegherebbe tra i 400 e i 450 anni per degradarsi completamente nell'ambiente. Durante questo processo, rilascerebbe micro-particelle di plastica, che, in mare, potrebbero essere ingerite dalle specie marine.",
            "Moltissime creature marine muoiono ogni anno a causa delle buste di plastica e della spazzatura che vengono gettate in mare.",
            "Una bottiglia di plastica resta per circa 450 anni nell'ambiente prima di degradarsi completamente.",
            "Riciclare la plastica fa risparmiare il doppio dell'energia che verrebbe consumata per bruciarla in un inceneritore.",
            "Riciclare la plastica fa risparmiare l'88% dell'energia che verrebbe consumata per crearla dalle materie prime."
    };
    String[] factsMetalSpeech = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori, o imballaggi, dei prodotti che compriamo, come, la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e \\rspd=80\\tànto àltro.",
            "Una lattina di alluminio resta dai 20 ai 100 anni nell'ambiente prima di degradarsi completamente, ecco perché sarebbe meglio riciclarla nell'apposita categoria!",
            "L'alluminio può essere riciclato infinite volte, senza perdita di qualità.",
            "In soli due mesi, le lattine di alluminio possono essere riciclate e rimesse in commercio.",
            "Riciclare una lattina di alluminio farebbe risparmiare l'energia necessaria per poter guardare la tv per circa 3 ore.",
            "Una lattina di alluminio può essere riciclata usando solo il 5% dell'energia che bisognerebbe impiegare per fabbricarla da zero.",
    };
    String[] factsPaperSpeech = {
            "Moltissimi oggetti che usiamo oggi, rièntrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste.",
            "Molti degli scontrini attualmente in circolazione non vanno gettati nel bidone dell'indifferenziato, perché sono composti da carte termiche che, se catalogate come carta e cartone, potrebbero causare problemi durante il riciclo.",
            "Se riciclassimo tutti i giornali, potremmo salvare oltre 250 milioni di alberi ogni anno.",
            "La carta riciclata produce circa il 70% in meno di inquinamento atmosferico rispetto alla sua creazione a partire dalle materie prime."
    };
    String[] factsCardboardSpeech = {
            "Moltissimi oggetti che usiamo oggi, rièntrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste.",
            "Una scatola di cartone resta per circa 9 mesi nell'ambiente prima di decomporsi completamente.",
            "Riciclare un cartone richiede solo il 75% dell'energia necessaria per produrne uno nuovo.",
    };
    String[] factsGlassSpeech = {
            "Il vetro può essere riciclato e rifabbricato un numero infinito di volte e non si usura mai.",
            "Il vetro mantiene il suo colore anche dopo il riciclo, perciò viene suddiviso in colori.",
            "Una moderna bottiglia di vetro potrebbe impiegare più di 4000 anni per decomporsi del tutto, forse anche di più se si trova in discarica.",
            "Anche gli antichi Romani riciclavano il vetro.",
            "Circa 7 bottiglie su 10 sono prodotte con vetro riciclato.",
            "Riciclando 1 chilo di rifiuti in vetro, si otterrà 1 chilo di nuovi prodotti in vetro riciclato, senza perdere nulla!",

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        //Log.d(TAG, "Start.");

        setContentView(R.layout.activity_pepper_teaches);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        textViewFactAbout = findViewById(R.id.textViewFactAbout);
        textViewRandomFact = findViewById(R.id.textViewRandomFact);
        selectedBin = findViewById(R.id.selectedBin);
        buttonPlay = findViewById(R.id.buttonPlay);

        trialState = -1;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            currentTurn = extras.getByte("currentTurn");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            wasteTypeString = extras.getString("wasteTypeString");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            pepperShouldTeach = extras.getBoolean("pepperShouldTeach");
        }
        selectFact();
        selectExclamation();
        buttonPlay.setVisibility(View.VISIBLE);
        buttonPlay.setEnabled(true);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayRandomFact= SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Ecco una curiosità:" + factAboutRecycle + "\\rspd=95\\ Adesso proseguiamo con il gioco. Tocca a te. Va bene?") // Set the text to say.
                .build(); // Build the say action.
        /*Animation sayRandomFactAnim = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001)
                .build();
        Animate animateRandomFact = AnimateBuilder.with(qiContext)
                .withAnimation(sayRandomFactAnim)
                .build();*/

        sayRandomFact.run();
        //animateRandomFact.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "ok", "okay", "andiamo avanti",
                        "va bene", "possiamo andare avanti", "vai avanti", "prosegui",
                        "vai avanti", "passiamo", "prossimo turno", "vai al prossimo turno",
                        "passa al prossimo turno", "giochiamo", "prosegui", "proseguiamo",
                        "prosegui con il gioco", "voglio proseguire con il gioco",
                        "voglio proseguire", "voglio andare avanti", "possiamo proseguire",
                        "possiamo", "continua", "continuiamo", "avanti")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();
        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "non mi va", "non voglio continuare", "non voglio proseguire",
                        "basta", "non è corretta", "stop", "basta", "chiudi il gioco", "chiudi",
                        "no Pepper", "Pepper no", "Esci", "voglio andare via")
                .build();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            Say sayNextTurn= SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Okay.") // Set the text to say.
                    .build(); // Build the say action.

            nextTurn();
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

    void selection(String typeForTV, int res, String[] factsText, String[] factsSpeech) {
        textViewFactAbout.setText(typeForTV);
        selectedBin.setBackground(getDrawable(res));
        int index = new Random().nextInt(factsText.length);
        //Log.d(TAG,"nextInt: "+ index);
        textFactAboutRecycle = factsText[index];
        factAboutRecycle = factsSpeech[index];
    }

    void selectFact() {
        //Log.d(TAG, "Entrato nel selectFact.");
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case TYPE_ORGANIC: // case "organic":
                selection("Curiosità - ORGANICO", R.drawable.bin_brown_shadow, factsOrganic, factsOrganicSpeech);
                break;
            case TYPE_PAPER:
                selection("Curiosità - CARTA", R.drawable.bin_blue_shadow, factsPaper, factsPaperSpeech);
                break;
            case TYPE_CARDBOARD:
                selection("Curiosità - CARTONE", R.drawable.bin_blue_shadow, factsCardboard, factsCardboardSpeech);
                break;
            case TYPE_PLASTIC:
                selection("Curiosità - PLASTICA", R.drawable.bin_yellow_shadow, factsPlastic, factsPlasticSpeech);
                break;
            case TYPE_METAL:
                selection("Curiosità - METALLI", R.drawable.bin_yellow_shadow, factsMetal, factsMetalSpeech);
                break;
            case TYPE_GLASS: // case "glass":
                selection("Curiosità - VETRO", R.drawable.bin_green_shadow, factsGlass, factsGlassSpeech);
                break;
            default:
                selection("Curiosità - ORGANICO", R.drawable.bin_brown_shadow, factsOrganic, factsOrganicSpeech);
                /*textViewFactAbout.setText("ERRORE");
                selectedBin.setBackground(getDrawable(R.drawable.bin_unknown_shadow));
                factAboutRecycle = "Si è verificato un errore.";*/
                break;
        }
        //Log.d(TAG, "factAboutRecycle" + factAboutRecycle);
        textViewRandomFact.setText(textFactAboutRecycle);

    }
    void selectExclamation() {
        exclamation = pepperExclamations[ new Random().nextInt(pepperExclamations.length)];
        Log.d(TAG, "exclamation: " + exclamation);

    }
    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent = new Intent(PepperTeachesActivity.this, NextTurnActivity.class);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("tutorialEnabled", false); // Tutorial finito
        activity2Intent.putExtra("currentTurn", currentTurn);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("isAnswerCorrect", true);
        activity2Intent.putExtra("isPepperTurn", true);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("isFromPepperTeaches", true);
        activity2Intent.putExtra("pepperShouldTeach", false);
        startActivity(activity2Intent);
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
    public void buttonPlay(View v) { //Pressione tasto "Gioca"
        nextTurn();
    }
}