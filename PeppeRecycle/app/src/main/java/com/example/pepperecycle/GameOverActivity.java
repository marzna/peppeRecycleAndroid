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

/* Classe relativa al game over, in cui si mostrano i punteggi raggiunti
 * e il robot chiede all'utente se vuole giocare un'altra partita
 */
public class GameOverActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    byte round;
    boolean isPepperTurn;
    byte pepperScore;
    byte userScore;
    TextView tvGameOver;
    TextView tvResult;
    TextView tvPepperScore, tvUserScore; //score che verrà mostrato
    String resultPhrase; // frase che dirà Pepper
    byte currentTurn;
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
        tvPepperScore = findViewById(R.id.textViewPepperScore);
        tvUserScore = findViewById(R.id.textViewUserScore);
        tvResult = findViewById(R.id.tvResult);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            currentTurn = extras.getByte("currentTurn");
        }

        if (pepperScore > userScore ) { // Utente perde
            userLoser();
        } else if(userScore > pepperScore){ // Utente vince
            userWinner();
        } else {    // Pareggio
            userDraw();
        }

        tvPepperScore.setText(" " + pepperScore + " ");
        tvUserScore.setText(" " + userScore + " ");
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Say sayResult = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("La partita è finita. " + resultPhrase) // Set the text to say.
                .build(); // Build the say action.
        Say sayAskNewGame =  SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("\\rspd=90\\Ti andrebbe di fare un'altra partita?") // Set the text to say.
                .build();

        /*Animation resultAnim = AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001)
                .build();
        Animate animateResult = AnimateBuilder.with(qiContext)
                .withAnimation(resultAnim)
                .build();*/

        sayResult.run();
        sayAskNewGame.run();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Sì", "Si", "ok", "Giochiamo", "va bene", "certo",
                        "Voglio giocare", "Voglio fare un'altra partita", "facciamo un'altra partita",
                        "Rigiochiamo", "Rigioca", "Gioca", "Gioca di nuovo", "giochiamo di nuovo",
                        "Voglio giocare di nuovo", "mi andrebbe", "Ricominciamo", "Ricomincia", "Da capo", "dall'inizio")
                .build();

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "no Pepper", "Pepper no", "non mi va", "non posso", "non voglio", "non mi andrebbe", "non voglio giocare")
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

    void userWinner() {
        resultPhrase = "Congratulazioni, hai vinto. Conosci molte informazioni sul riciclo."; //TODO metti una frase migliore per quando l'utente vince
        tvResult.setText("Hai vinto!");
        resAnim = R.raw.right_hand_high_b001;
    }
    void userLoser() {
        resultPhrase = "Hai perso. Stavolta sono stato più bravo di te.";
        tvResult.setText("Hai perso!");
        resAnim = R.raw.funny_a001;
    }
    void userDraw() { //In caso di pareggio
        if (userScore == 0 && pepperScore == 0) { // score == 0
            resultPhrase = "Oh no, non abbiamo indovinato neanche una volta. Dovremmo impegnarci di più.";
            resAnim = R.raw.sad_a001;
        } else if (userScore == 1 && pepperScore == 1) { // score == 3
            resultPhrase = "Oh no, abbiamo indovinato solo una volta. Dovremmo impegnarci di più.";
            resAnim = R.raw.sad_a001;
        } else if (userScore == 3 && pepperScore == 3) { // score == 3
            resultPhrase = "Uau, le abbiamo indovinate tutte. Siamo stati bravissimi entrambi.";
            resAnim = R.raw.right_hand_high_b001;
        } else { // score == 2
            resultPhrase = "Siamo stati bravissimi entrambi, e abbiamo avuto lo stesso punteggio.";
            resAnim = R.raw.funny_a001;
        }
        tvResult.setText("Pareggio!");
    }

    public void buttonHome(View v) { //Pressione tasto "torna alla Home"
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        CommonUtils.showDialogExit(this);
    }

    void newGame() {
        Byte trialState = -1;
        Byte round = 0;
        Intent activity2Intent = new Intent(GameOverActivity.this, PlayGameActivity.class); // PlayPepperTurnActivity.class);
        activity2Intent.putExtra("tutorialEnabled", false);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("roundTutorial", false);
        activity2Intent.putExtra("endOfTutorial", true);
        activity2Intent.putExtra("restartGame", true);
        startActivity(activity2Intent);
        finish();
    }

}