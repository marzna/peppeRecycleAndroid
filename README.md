# PeppeRecycle
>Progetto relativo alla tesi intitolata *"PeppeRecycle: un Serious Game per Imparare a Riciclare con il Social Robot Pepper"*
>di Anna Marzella
## Strumenti utilizzati
Gli ***IDE*** utilizzati per lo sviluppo dell'app sono stati *Android Studio* e *PyCharm*.
Un altro strumento utile è ***ngrok***, per poter usufruire dell'URL Web fornito, in modo tale da permettere l'accesso al server da parte del robot. ngrok è scaricabile al seguente [link](https://ngrok.com/download). 
Il progetto è stato sviluppato per Pepper, la cui **SDK** per Android è scaricabile al [link](https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/index.html).
- - - 
## *Struttura repository*
Nel repository, sono presenti due cartelle: 
- ***PeppeRecycle***, contenente il progetto
- ***PeppeRecycleServer***, contenente la parte relativa al server .

Nel dettaglio:
## PeppeRecycle
La cartella *java.com.example.pepperecycle*  contiene le diverse classi del progetto, organizzate in base alla funzione: ***game*** per le Activity relative al gioco vero e proprio, ***tutorial*** per la spiegazione delle regole e, infine, ***utils*** per delle classi contenenti dei metodi utili per diverse Activity. In particolare:
```
'- - - app
    '- - - src             
        '- - - main
            '- - - java
                '- - - com
                    '- - - example
                        '- - - pepperecycle
                            '- - - game
                            |       GameOverActivity.java
                            |       NextTurnActivity.java
                            |       PepperTeachesActivity.java
                            |       PlayGameActivity.java
                            |       PlayIntroActivity.java
                            |       PlayJudgeTurnActivity.java
                            |       PlayPepperTurnActivity.java
                            |       PlayUserTurnActivity.java
                            '- - - tutorial
                            |       TutorialActivity.java
                            |       TutorialEndActivity.java
                            '- - - utils
                            |        ClientManager.java
                            |       CommonUtils.java
                            '- - - MainActivity.java
```

## peppeRecycleServer
Il server è stato sviluppato in Python.
In particolare, nella cartella *peppeRecycleServer*, sono presenti:
- `garbage.h5`, il modello addestrato;
- `server.py`, il file da eseguire per attivare il server;
- `util.py`, un file contenente funzioni utili alla classificazione, come il caricamento del modello, la classificazione vera e propria, etc.;
- `waste-labels-pepper-android.txt`, il file di testo contenenti le labels della classificazione;
- `testRequest.py`, un file utile se si vuole verificare il funzionamento del server;
- `test_images`, una cartella contenente delle immagini che si potrebbero utilizzare nel file `testRequest.py` .
