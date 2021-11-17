package com.example.pepperecycle;

import static com.example.pepperecycle.PlayGameActivity.N_ROUNDS;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PepperTeachesActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {

    String TAG = "PepperTeachesActivity";
    boolean isPepperTurn, isAnswerCorrect, pressed;
    byte wasteType, currentRound, round;
    TextView textViewRandomFact, textViewFactAbout;
    ImageView selectedBin;
    static byte pepperScore, userScore;
    QiContext qiContext;
    String binType;
    String wasteTypeString;
    String factAboutRecycle, textFactAboutRecycle;
    boolean tutorialEnabled;
    boolean pepperTeaches;
    Button buttonPlay;
    Animate animate;
    String exclamation;
    byte trialState;
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
    String[] factsPlasticMetal = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori o imballaggi dei prodotti che compriamo, come la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e tanto àltro.",
            "Una mascherina impiegherebbe tra i 400 e i 450 anni per degradarsi completamente nell'ambiente. Durante questo processo, rilascerebbe micro-particelle di plastica, che, in mare, potrebbero essere ingerite dalle specie marine.",
            "Una lattina di alluminio resta dai 20 ai 100 anni nell'ambiente prima di degradarsi completamente, ecco perché sarebbe meglio riciclarla nell'apposita categoria!",
            "Moltissime creature marine muoiono ogni anno a causa delle buste di platica e della spazzatura che vengono gettate in mare.",
            "Una bottiglia di plastica resta per circa 450 anni nell'ambiente prima di degradarsi completamente.",
            "L'alluminio può essere riciclato infinite volte, senza perdita di qualità.",
            "In soli due mesi, le lattine di alluminio possono essere riciclate e rimesse in commercio.",
            "Riciclare una lattina di alluminio farebbe risparmiare l'energia necessaria per poter guardare la tv per circa 3 ore.",
            "Una lattina di alluminio può essere riciclata usando solo il 5% dell'energia che bisognerebbe impiegare per fabbricarla da 0.",
            "Riciclare la plastica fa risparmiare il doppio dell'energia che verrebbe consumata per bruciarla in un inceneritore.",
            "Riciclare la plastica fa risparmiare l'88% dell'energia che verrebbe consumata per crearla dalle materie prime."
    };
    String[] factsCardCardboard = {
            "Moltissimi oggetti che usiamo oggi rièntrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste.",
            "Molti degli scontrini attualmente in circolazione vanno gettati nell'indifferenziato, perché sono composti da carte termiche che, se catalogate come carta e cartone, potrebbero causare problemi durante il riciclo.",
            "Una scatola di cartone resta per circa 9 mesi nell'ambiente prima di decomporsi completamente.",
            "Se riciclassimo tutti i giornali, potremmo salvare oltre 250 milioni di alberi ogni anno.",
            "Riciclare un cartone richiede solo il 75% dell'energia necessaria per produrne uno nuovo.",
            "La carta riciclata produce circa il 70% in meno di inquinamento atmosferico rispetto alla sua creazione a partire dalle materie prime."
    };
    String[] factsGlass = {
            "Il vetro può essere riciclato e rifabbricato un numero infinito di volte e non si usura mai.",
            "Il vetro mantiene il suo colore anche dopo il riciclo, perciò viene suddiviso in colori.",
            "Una moderna bottiglia di vetro potrebbe impiegare più di 4000 anni per decomporsi del tutto, forse anche di più se si trova in discarica.",
            "Anche gli antichi Romani riciclavano il vetro.",
            "Circa 7 bottiglie su 10 sono prodotte con vetro riciclato.",
            "Riciclando 1 chilo di rifiuti in vetro, si otterrà 1 chilo di nuovi prodotti in vetro riciclato, senza perdere nulla!",

    };

    String[] textFactsOrganic = {
            "L'organico è formato da scarti alimentari\n" +
                    "e altri rifiuti facilmente biodegradabili.",
            "Una torsolo di mela resta per circa 2 mesi\n" +
                    "nell'ambiente prima di degradarsi completamente.",
            "Circa un terzo dei rifiuti prodotti da una persona\n" +
                    "è composto da rifiuti organici.",
            "Dalla decomposizione dei rifiuti organici, si ottiene\n" +
                    "il compost, un ottimo concime per il terreno.",
            "Il compost può essere prodotto sia su scala industriale che domestica."
    };
    String[] textFactsPlasticMetal = {
            "Fanno parte della categoria di plastica e metalli\n" +
                    "quasi tutti i contenitori o imballaggi dei prodotti che compriamo\n" +
                    "come la lattina della coca cola, la scatoletta di tonno,\n" +
                    "la bottiglia dello shampoo e tanto altro.",
            "Una mascherina impiegherebbe tra i 400 e i 450 anni\n" +
                    "per degradarsi completamente nell'ambiente.\n" +
                    "Durante questo processo, rilascerebbe micro-particelle di plastica,\n" +
                    "che, in mare, potrebbero essere ingerite dalle specie marine.",
            "Una lattina di alluminio resta dai 20 ai 100 anni nell'ambiente\n" +
                    "prima di degradarsi completamente,\n" +
                    "ecco perché sarebbe meglio riciclarla nell'apposita categoria!",
            "Moltissime creature marine muoiono ogni anno\n" +
                    "a causa delle buste di platica e della spazzatura\n" +
                    "che vengono gettate in mare.",
            "Una bottiglia di plastica resta per circa 450 anni\n" +
                    "nell'ambiente prima di degradarsi completamente.",
            "L'alluminio può essere riciclato infinite volte,\n" +
                    "senza perdita di qualità.",
            "In soli due mesi, le lattine di alluminio\n" +
                    "possono essere riciclate e rimesse in commercio.",
            "Riciclare una lattina di alluminio\n" +
                    "farebbe risparmiare l'energia necessaria\n" +
                    "per poter guardare la tv per circa 3 ore.",
            "Una lattina di alluminio può essere riciclata\n" +
                    "usando solo il 5% dell'energia\n" +
                    "che bisognerebbe impiegare per fabbricarla da 0.",
            "Riciclare la plastica fa risparmiare il doppio dell'energia\n" +
                    " che verrebbe consumata per bruciarla in un inceneritore.",
            "Riciclare la plastica fa risparmiare l'88% dell'energia\n" +
                    "che verrebbe consumata per crearla dalle materie prime."
    };
    String[] textFactsCardCardboard = {
            "Moltissimi oggetti che usiamo oggi rientrano\n" +
                    "nella categoria di carta e cartone:\n" +
                    "i quaderni di scuola, i giornali e le riviste.",
            "Molti degli scontrini attualmente in circolazione\n" +
                    "vanno gettati nell'indifferenziato, perché sono composti\n" +
                    "da carte termiche che, se catalogate come carta e cartone,\n" +
                    "potrebbero causare problemi durante il riciclo.",
            "Una scatola di cartone resta per circa 9 mesi nell'ambiente\n" +
                    "prima di decomporsi completamente.",
            "Se riciclassimo tutti i giornali,\n" +
                    "potremmo salvare oltre 250 milioni di alberi ogni anno.",
            "Riciclare un cartone richiede solo\n" +
                    "il 75% dell'energia necessaria per produrne uno nuovo.",
            "La carta riciclata produce circa\n" +
                    "il 70% in meno di inquinamento atmosferico \n" +
                    "rispetto alla sua creazione a partire dalle materie prime."
    };
    String[] textFactsGlass = {
            "Il vetro può essere riciclato e rifabbricato\n" +
                    "un numero infinito di volte e non si usura mai.",
            "Il vetro mantiene il suo colore anche dopo il riciclo,\n" +
                    "perciò viene suddiviso in colori.",
            "Una moderna bottiglia di vetro potrebbe impiegare\n" +
                    "più di 4000 anni per decomporsi del tutto,\n" +
                    "forse anche di più se si trova in discarica.",
            "Anche gli antichi Romani riciclavano il vetro.",
            "Circa 7 bottiglie su 10 sono prodotte con vetro riciclato.",
            "Riciclando 1 chilo di rifiuti in vetro,\n" +
                    "si otterrà 1 chilo di nuovi prodotti\n" +
                    "in vetro riciclato, senza perdere nulla!",

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        Log.d(TAG, "Start.");

        setContentView(R.layout.activity_pepper_teaches);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        textViewFactAbout = findViewById(R.id.textViewFactAbout);
        textViewRandomFact = findViewById(R.id.textViewRandomFact);
        selectedBin = findViewById(R.id.selectedBin);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        Bundle extras = getIntent().getExtras();

        Log.e(TAG, "Prima di avere gli extras.");
        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            currentRound = extras.getByte("currentRound");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            trialState = extras.getByte("trialState");
            wasteTypeString = extras.getString("wasteTypeString");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        Log.e(TAG, "Dopo extras.");

        Log.e(TAG, "Prima del selectFact.");
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
        Say sayRandomFact= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                //.withText(exclamation + "." + factAboutRecycle + "\\rspd=90\\ Adesso possiamo proseguire con il gioco o vuoi sentire un'altra curiosità riguardante il riciclo?") // Set the text to say.
                .withText(factAboutRecycle + "\\rspd=95\\ Adesso possiamo proseguire con il gioco, o vuoi sentire un'altra curiosità riguardante il riciclo?") // Set the text to say.
                .build(); // Build the say action.
        Animation sayRandomFactAnim = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione
                .build();
        Animate animateRandomFact = AnimateBuilder.with(qiContext)
                .withAnimation(sayRandomFactAnim)
                .build();

        sayRandomFact.run();
//        animateRandomFact.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "ok", "okay", "andiamo avanti",
                        "possiamo andare avanti", "vai avanti", "prosegui",
                        "vai avanti", "passiamo", "prossimo turno", "vai al prossimo turno",
                        "passa al prossimo turno", "giochiamo", "prosegui", "proseguiamo",
                        "prosegui con il gioco", "voglio proseguire con il gioco",
                        "voglio proseguire", "voglio andare avanti", "possiamo proseguire",
                        "possiamo", "continua", "continuiamo", "avanti")
                .build();

        /*PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper").build(); //TODO idk */

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "no Pepper", "Pepper no", "raccontami altro", "curiosità", "voglio sapere un'altra curiosità",
                        "voglio sentire un'altra curiosità", "racconta", "non possiamo", "non voglio giocare")
                .build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", /*"Ricominciamo", "Ricomincia",*/ "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta", "voglio andare via")
                .build();

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetNo, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            Say sayNextTurn= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Perfetto, allora proseguiamo con il gioco!") // Set the text to say.
                    .build(); // Build the say action.

            if (currentRound<N_ROUNDS-1) {
                //Pepper dice chi è il prossimo giocatore
                Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                        .withText("Adesso è il tuo turno.") // Set the text to say.
                        .build(); // Build the say action.
                sayTurn.run();
            }
            nextTurn();


        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {   // Ripete l'activity con un altro fattp random riguardante il materiale scelto
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PepperTeachesActivity.class);
            activity2Intent.putExtra("wasteType", wasteType);
            startActivity(activity2Intent);
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

    void selection(String typeForTV, int res, String[] textFactsAboutSmth, String[] factsAboutSmth) {
        textViewFactAbout.setText(typeForTV);
        selectedBin.setBackground(getDrawable(res));
        textFactAboutRecycle = textFactsAboutSmth[ new Random().nextInt(textFactsAboutSmth.length)];
        factAboutRecycle = factsAboutSmth[ new Random().nextInt(factsAboutSmth.length)];
    }

    void selectFact() {
        Log.e(TAG, "Entrato nel selectFact.");
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case 0: // case "organic":
                selection("Curiosità - ORGANICO", R.drawable.bin_brown_shadow, factsOrganic, textFactsOrganic);
                break;
            case 1: // case "paper": case "cardboard":
                selection("Curiosità - CARTA E CARTONE", R.drawable.bin_blue_shadow, factsCardCardboard, textFactsCardCardboard);
                break;
            case 2: // case "plastic": case "metal":
                selection("Curiosità - PLASTICA E METALLI", R.drawable.bin_yellow_shadow, factsPlasticMetal, textFactsPlasticMetal);
                break;
            case 3: // case "glass":
                selection("Curiosità - VETRO", R.drawable.bin_green_shadow, factsGlass, textFactsGlass);
                break;
            default:
                textViewFactAbout.setText("Errore"); //TODO MODIFICA
                selectedBin.setBackground(getDrawable(R.drawable.bin_unknown_shadow));
                factAboutRecycle = "Si è verificato un errore."; //textViewFactAbout.setText("ERRORE.");
                break;
        }
        Log.e(TAG, "factAboutRecycle" + factAboutRecycle);
        textViewRandomFact.setText(factAboutRecycle);

    }
    void selectExclamation() {
        Log.e(TAG, "Entrato nel selectExclamation.");
        exclamation = pepperExclamations[ new Random().nextInt(pepperExclamations.length)];

        Log.e(TAG, "exclamation: " + exclamation);

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
                // TODO sostituisci il 6 con una costante, tipo WINNER_SCORE o simili4
                if(isPepperTurn) {/*
                    phrase = "Ora tocca a me.";*/
                    activity2Intent = new Intent(PepperTeachesActivity.this, PlayPepperTurnActivity.class);
                    Log.d(TAG, "trialState passato a PepperTurn: " + trialState);
                } else {/*
                    phrase = "Adesso è il tuo turno.";*/
                    activity2Intent = new Intent(PepperTeachesActivity.this, PlayUserTurnActivity.class);
                    Log.d(TAG, "trialState passato a UserTurn: " + trialState);
                }
//                pepperSayTurn(isPepperTurn);
               /* Say sayTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                        .withText(phrase) // Set the text to say.
                        .build(); // Build the say action.
                sayTurn.run();*/

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
                activity2Intent = new Intent(PepperTeachesActivity.this, GameOverActivity.class);//TODO GameOverActivity
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

    public void endTutorial() {
        Intent activity2Intent = new Intent(PepperTeachesActivity.this, TutorialEndActivity.class);
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

    /*public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent;
        isPepperTurn = !isPepperTurn;
        tutorialEnabled=false;
        if ( pepperScore < 3 && userScore < 3 )   { // Si ripete fin quando uno dei giocatori non ha raggiunto il punteggio massimo
            // TODO sostituisci il 3 con una costante, tipo WINNER_SCORE o simili
            if (isPepperTurn) {
                if(tutorialEnabled) {
                    activity2Intent = new Intent(PepperTeachesActivity.this, PlayGameActivity.class); // PlayPepperTurnActivity.class);
                } else {
                    activity2Intent = new Intent(PepperTeachesActivity.this, PlayPepperTurnActivity.class); // PlayPepperTurnActivity.class);
                }
            } else {
                activity2Intent = new Intent(PepperTeachesActivity.this, PlayUserTurnActivity.class);
            }
            *//*
            pepperScore = activity2Intent.getExtras().getByte("pepperScore");
            userScore = activity2Intent.getExtras().getByte("userScore");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)//activity2Intent.putExtra("scores", scores);
            *//*
        } else {            // Game over
            activity2Intent = new Intent(PepperTeachesActivity.this, GameOverActivity.class);//TODO GameOverActivity
        }
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        //activity2Intent.putExtra("scores", (Serializable) scores);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent);
        finish();
    }*/
    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        CommonUtils.showDialogExit(this);
        //finish();
    }
    public void buttonPlay(View v) {
        nextTurn();
    }
/* void selectFact() {
        Log.e(TAG, "Entrato nel selectFact.");
        switch (wasteType) { // Modifica la label in base al tipo di bidone selezionato
            case 0: // case "organic":
                textViewFactAbout.setText("Curiosità - ORGANICO"); //TODO MODIFICA
                selectedBin.setImageResource(R.drawable.bin_brown_shadow);
                factAboutRecycle = factsOrganic[ new Random().nextInt(factsOrganic.length)];
                break;
            case 1: // case "paper": case "cardboard":
                textViewFactAbout.setText("Curiosità - CARTA E CARTONE");
                selectedBin.setImageResource(R.drawable.bin_blue_shadow);
                factAboutRecycle = factsCardCardboard[ new Random().nextInt(factsCardCardboard.length)];
                break;
            case 2: // case "plastic": case "metal":
                textViewFactAbout.setText("Curiosità - PLASTICA E METALLI");
                selectedBin.setImageResource(R.drawable.bin_yellow_shadow);
                factAboutRecycle = factsPlasticMetal[ new Random().nextInt(factsPlasticMetal.length)];
                break;
            case 3: // case "glass":
                textViewFactAbout.setText("Curiosità - VETRO");
                selectedBin.setImageResource(R.drawable.bin_green_shadow);
                factAboutRecycle = factsGlass[ new Random().nextInt(factsGlass.length)];
                break;
            default:
                textViewFactAbout.setText("ERRORE.");
                break;
        }
        Log.e(TAG, "factAboutRecycle" + factAboutRecycle);
        textViewRandomFact.setText(factAboutRecycle);

    }*/
}