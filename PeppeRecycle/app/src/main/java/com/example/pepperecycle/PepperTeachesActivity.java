package com.example.pepperecycle;

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
    byte wasteType, round;
    TextView textViewRandomFact, textViewFactAbout;
    ImageView selectedBin;
    static byte pepperScore, userScore;
    QiContext qiContext;
    String binType;
    String wasteTypeString;
    String factAboutRecycle;
    boolean tutorialEnabled;
    boolean pepperTeaches;
    Button buttonPlay;
    Animate animate;
    String[] factsOrganic = {
            "L'organico è formato da scarti alimentari e altri rifiuti facilmente biodegradabili",
            "Una torsolo di mela resta per circa 2 mesi nell'ambiente prima di degradarsi completamente",
            "Circa un terzo dei rifiuti prodotti da una persona è composto da rifiuti organici?",
            "Dalla decomposizione dei rifiuti organici, si ottiene il compost, un ottimo concime per il terreno",
            "Il compost può essere prodotto sia su scala industriale che domestica"
    };
    String[] factsPlasticMetal = {
            "Fanno parte della categoria di plastica e metalli quasi tutti i contenitori o imballaggi dei prodotti che compriamo, come la lattina della coca cola, la scatoletta di tonno, la bottiglia dello shampoo e tanto altro",
            "Una mascherina impiegherebbe tra i 400 e i 450 anni per degradarsi completamente nell'ambiente. Durante questo processo, rilascerebbe micro-particelle di plastica, che, in mare, potrebbero essere ingerite dalle specie marine.",
            "Una lattina di alluminio resta dai 20 ai 100 anni nell'ambiente prima di degradarsi completamente, ecco perché sarebbe meglio riciclarla nell'apposita categoria!",
            "Moltissime creature marine muoiono ogni anno a causa delle buste di platica e della spazzatura che vengono gettate in mare.",
            "Una bottiglia di plastica resta per circa 450 anni nell'ambiente prima di degradarsi completamente",
            "L'alluminio può essere riciclato infinite volte, senza perdita di qualità",
            "In soli due mesi, le lattine di alluminio possono essere riciclate e rimesse in commercio.",
            "Riciclare una lattina di alluminio farebbe risparmiare l'energia necessaria per poter guardare la tv per circa 3h",
            "Una lattina di alluminio può essere riciclata usando solo il 5% dell'energia che bisognerebbe impiegare per fabbricarla da 0.",
            "Riciclare la plastica fa risparmiare il doppio dell'energia che verrebbe consumata per bruciarla in un inceneritore.",
            "Riciclare la plastica fa risparmiare l'88% dell'energia che verrebbe consumata per crearla dalle materie prime."
    };
    String[] factsCardCardboard = {
            "Moltissimi oggetti che usiamo oggi rientrano nella categoria di carta e cartone: i quaderni di scuola, i giornali e le riviste",
            "Molti degli scontrini attualmente in circolazione vanno gettati nell'indifferenziato, perché sono composti da carte termiche, che, se catalogate come carta e cartone, potrebbero causare problemi durante il riciclo.",
            "Una scatola di cartone resta per circa 9 mesi nell'ambiente prima di decomporsi completamente",
            "Se riciclassimo tutti i giornali, potremmo salvare oltre 250 milioni di alberi ogni anno.",
            "Riciclare un cartone richiede solo il 75% dell'energia necessaria per produrne uno nuovo.",
            "La carta riciclata produce circa il 70% in meno di inquinamento atmosferico rispetto alla sua creazione a partire dalle materie prime."
    };
    String[] factsGlass = {
            "Il vetro può essere riciclato e rifabbricato un numero infinito di volte e non si usura mai.",
            "Il vetro mantiene il suo colore anche dopo il riciclo, perciò viene suddiviso in colori.",
            "Una moderna bottiglia di vetro potrebbe impiegare più di 4000 anni per decomporsi del tutto, forse anche di più se si trova in discarica.",
            "Anche gli antichi Romani riciclavano il vetro.",
            "Circa 7 bottiglie su 10 sono prodotte con vetro riciclato",
            "Riciclando 1 chilo di rifiuti in vetro, si otterrà 1 chilo di nuovi prodotti in vetro riciclato, senza perdere nulla!",

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_pepper_teaches);

        textViewFactAbout = findViewById(R.id.textViewFactAbout);
        textViewRandomFact = findViewById(R.id.textViewRandomFact);
        selectedBin = findViewById(R.id.selectedBin);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        Bundle extras = getIntent().getExtras();

        Log.e(TAG, "Prima di avere gli extras.");
        if (extras != null) {
            wasteType = extras.getByte("wasteType"); // The key argument here must match that used in the other activity
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            wasteTypeString = extras.getString(wasteTypeString);
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            //scores = (HashMap<String, String>) getIntent().getSerializableExtra("scores");
        }
        Log.e(TAG, "Dopo extras.");

        Log.e(TAG, "Prima del selectFact.");
        selectFact();
        buttonPlay.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayRandomFact= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText(factAboutRecycle + ". Adesso possiamo proseguire con il gioco o vuoi sentire un'altra curiosità riguardante il riciclo?") // Set the text to say.
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
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "ok", "ochei", "andiamo avanti",
                        "vai avanti", "passiamo", "prossimo turno", "vai al prossimo turno",
                        "passa al prossimo turno", "giochiamo", "prosegui", "proseguiamo",
                        "prosegui con il gioco", "voglio proseguire con il gioco",
                        "voglio proseguire", "voglio andare avanti", "possiamo proseguire",
                        "possiamo")
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
            nextTurn();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {   // Ripete l'activity con un altro fattp random riguardante il materiale scelto
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PepperTeachesActivity.class);
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

    void selectFact() {
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

    }

    public void nextTurn() { // Avvia la activity relativa al prossimo turno (o di Game Over)
        Intent activity2Intent;
        isPepperTurn = !isPepperTurn;
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
            /*
            pepperScore = activity2Intent.getExtras().getByte("pepperScore");
            userScore = activity2Intent.getExtras().getByte("userScore");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)//activity2Intent.putExtra("scores", scores);
            */
        } else {            // Game over
            activity2Intent = new Intent(PepperTeachesActivity.this, GameOverActivity.class);//TODO GameOverActivity
        }
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        //activity2Intent.putExtra("scores", (Serializable) scores);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        startActivity(activity2Intent);
        finish();
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
    public void buttonPlay(View v) {
        nextTurn();

    }

}