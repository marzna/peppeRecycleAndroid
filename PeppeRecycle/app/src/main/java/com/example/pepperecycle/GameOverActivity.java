package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

public class GameOverActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{{

    Map<String, Byte> scores = new HashMap<>();
    byte round;
    boolean isPepperTurn;
    byte pepperScore;
    byte userScore;
    TextView tvGameOver;
    TextView tvResult;
    TextView tvPepperScore, tvUserScore; //score che verrà mostrato
    String resultPhrase; //frase che dirà Pepper
    //    ImageView imageViewResult;
    String result;
    byte currentRound;
    int resAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_game_over);

        tvGameOver = findViewById(R.id.tvGameOver);
//        imageViewResult= findViewById(R.id.imageViewResult); TODO RIMUOVI ALTRE REFERENCES A QUESTA VARIABILE
        tvPepperScore = findViewById(R.id.textViewPepperScore);
        tvUserScore = findViewById(R.id.textViewUserScore);
        tvResult = findViewById(R.id.tvResult);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            currentRound = extras.getByte("currentRound");
        }

        if (pepperScore > userScore ) { // Utente perde
            // if (pepperScore == 3 ) { //if(scores.get("score_pepper") == 3 ) {
            userLoser();
            // result = "Oh no, hai perso!";
        } else if(userScore > pepperScore){ // Utente vince
            //} else if (scores.get("score_user1") == 3) {
            userWinner();
            // result = "Congratulazioni, hai vinto!";
        } else {    // Pareggio
            userDraw();
            // result = "Siamo stati bravissimi entrambi!";;
        }

        tvPepperScore.setText(" " + pepperScore + " ");
        tvUserScore.setText(" " + userScore + " ");
//        showScore();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) { //TODO TESTARE TUTTA QUESTA FUNZIONE
//        resultPhrase += "\\rspd=90\\Ti andrebbe di fare un'altra partita?";
        Say sayResult = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("La partita è finita. " + resultPhrase) // Set the text to say.
                //.withText("\\rspd=95\\" + resultPhrase + "\\rspd=90\\Ti andrebbe di fare un'altra partita?") // Set the text to say.
                .build(); // Build the say action.
        Say sayAskNewGame =  SayBuilder.with(qiContext) // Create the builder with the context.
//                .withText(resultPhrase) // Set the text to say.
                .withText("\\rspd=90\\Ti andrebbe di fare un'altra partita?") // Set the text to say.
                .build();

        Animation resultAnim = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001) //TODO Animazione triste
                .build();
        Animate animateResult = AnimateBuilder.with(qiContext)
                .withAnimation(resultAnim)
                .build();

        sayResult.run();
        sayAskNewGame.run();
//      animateResult.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "ok", "Giochiamo", "va bene", "certo",
                        "Voglio giocare", "Voglio fare un'altra partita", "facciamo un'altra partita",
                        "Rigiochiamo", "Rigioca", "Gioca", "Gioca di nuovo", "giochiamo di nuovo",
                        "Voglio giocare di nuovo")
                .build();

        /*PhraseSet phraseSetIdk = PhraseSetBuilder.with(qiContext)
                .withTexts("Non lo so", "bo", "Aiutami Pepper").build(); //TODO idk */

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "no Pepper", "Pepper no", "non mi va", "non posso", "non voglio", "non mi andrebbe", "non voglio giocare")
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
            Say sayNewGame= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Perfetto, allora iniziamo subito un'altra partita!!") // Set the text to say.
                    .build(); // Build the say action.
            sayNewGame.run();
            newGame();

        } else if ( PhraseSetUtil.equals(matchedPhraseSet, phraseSetHome)) {     // Torna alla home
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();

        } else if ( (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) ||
                (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) ) {    // Chiude il gioco
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a004).build();
            Animate animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, allora sto chiudendo il gioco. Spero di rivederti presto!") // Set the text to say.
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

    /*void showScore() {

    }*/
    void userWinner() {
        resultPhrase = "Congratulazioni, hai vinto. Conosci molte informazioni sul riciclo."; //TODO metti una frase migliore per quando l'utente vince
        tvResult.setText("Hai vinto!");
        /*tvGameOver.setText("Congratulazioni,\nhai vinto!\nPepper: " + pepperScore + "\nUser: " + userScore);
        imageViewResult.setImageResource(R.drawable.trophy);*/
        resAnim = R.raw.right_hand_high_b001;
    }
    void userLoser() {
        resultPhrase = "Hai perso. Stavolta sono stato più bravo di te.";
        tvResult.setText("Hai perso!");
        /*tvGameOver.setText("Uhm,\ncredo che sia meglio rivedere qualcosa!");
        imageViewResult.setImageResource(R.drawable.sad_face);*/
        resAnim = R.raw.funny_a001;
    }
    void userDraw() { //In caso di pareggio
        if (userScore == 0 && pepperScore == 0) { // score == 0
            resultPhrase = "Oh no, non abbiamo indovinato neanche una volta. Dovremmo impegnarci di più.";
            tvResult.setText("Pareggio!");
            resAnim = R.raw.sad_a001;
        } else if (userScore == 3 && pepperScore == 3) { // score == 3
            resultPhrase = "Uau, le abbiamo indovinate tutte. Siamo stati bravissimi entrambi.";
            tvResult.setText("Pareggio!");
            resAnim = R.raw.right_hand_high_b001;
        } else { // score == 2
            resultPhrase = "Siamo stati bravissimi entrambi, e abbiamo avuto lo stesso punteggio.";
            tvResult.setText("Pareggio!");
            resAnim = R.raw.funny_a001;
        }
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

    void newGame() { //TODO Da testare
        Byte trialState = -1;
        Intent activity2Intent = new Intent(GameOverActivity.this, PlayGameActivity.class); // PlayPepperTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", false);
        activity2Intent.putExtra("trialState", trialState); // TODO Ho messo trialstate = -1
        Log.d("GameOverActivity", "trialState passato a PLAYGAME: " + trialState);
        activity2Intent.putExtra("round", 0);
        activity2Intent.putExtra("roundTutorial", false);
        activity2Intent.putExtra("endOfTutorial", true);
        activity2Intent.putExtra("restartGame", true);
        startActivity(activity2Intent);
        finish();
    }

}