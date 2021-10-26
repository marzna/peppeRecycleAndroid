package com.example.pepperecycle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

import java.util.HashMap;
import java.util.Map;

public class GameOverActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{{

    Map<String, Byte> scores = new HashMap<>();
    byte round;
    boolean isPepperTurn;
    byte pepperScore;
    byte userScore;
    TextView textViewResult;
    ImageView imageViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_game_over);

        textViewResult = findViewById(R.id.textViewResult);
        imageViewResult= findViewById(R.id.imageViewResult);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            round = extras.getByte("round");
            isPepperTurn = extras.getBoolean("isPepperTurn");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
        }
        if (pepperScore > userScore ) {     // if (pepperScore == 3 ) { //if(scores.get("score_pepper") == 3 ) {
            userLoser();
        } else {//} else if (scores.get("score_user1") == 3) {
            textViewResult.setText("pepper: " + pepperScore + "\nuser: " + userScore);
            //userWinner();
        }
//        showScore();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

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
        textViewResult.setText("Congratulazioni,\nhai vinto!");
        imageViewResult.setImageResource(R.drawable.trophy);
    }
    void userLoser() {
        textViewResult.setText("Uhm,\ncredo che sia meglio rivedere qualcosa!");
        imageViewResult.setImageResource(R.drawable.sad_face);

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
}