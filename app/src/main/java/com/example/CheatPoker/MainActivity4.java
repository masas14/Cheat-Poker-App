package com.example.CheatPoker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class MainActivity4 extends AppCompatActivity implements View.OnClickListener {


    FirebaseAuth firebaseUser = FirebaseAuth.getInstance();
    String userUid = firebaseUser.getCurrentUser().getUid();
    User user = new User(userUid);
    String hostUid, code;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference gamesReference = db.collection("games");
    DocumentReference gameReference;
    CollectionReference gamersRef;



    TextView textGenerateCode, textOutcome;

    ArrayList<ImageView> cardsImagesArr = new ArrayList<>();
    ArrayList<Card> cardDeckTemp;

    ArrayList<ImageView> lastHandBetImageArr = new ArrayList<>(5);

    int[] deckArr = {
            R.drawable.card_00, R.drawable.card_01, R.drawable.card_02, R.drawable.card_03, R.drawable.card_04,
            R.drawable.card_05, R.drawable.card_06, R.drawable.card_07, R.drawable.card_08, R.drawable.card_09,
            R.drawable.card_10, R.drawable.card_11, R.drawable.card_12, R.drawable.card_13, R.drawable.card_14,
            R.drawable.card_15, R.drawable.card_16, R.drawable.card_17, R.drawable.card_18, R.drawable.card_19,
            R.drawable.card_20, R.drawable.card_21, R.drawable.card_22, R.drawable.card_23, R.drawable.card_24,
            R.drawable.card_25, R.drawable.card_26, R.drawable.card_27, R.drawable.card_28, R.drawable.card_29,
            R.drawable.card_30, R.drawable.card_31, R.drawable.card_32, R.drawable.card_33, R.drawable.card_34,
            R.drawable.card_35, R.drawable.card_36, R.drawable.card_37, R.drawable.card_38, R.drawable.card_39,
            R.drawable.card_40, R.drawable.card_41, R.drawable.card_42, R.drawable.card_43, R.drawable.card_44,
            R.drawable.card_45, R.drawable.card_46, R.drawable.card_47, R.drawable.card_48, R.drawable.card_49,
            R.drawable.card_50, R.drawable.card_51};

    ArrayList<Card> cardDeck = new ArrayList<>(52);

    Vibrator vibrator;

    Button btnEnterHand,btnCheat, btnHand, btnShuffle,btnStartRound,btnReady, btnHome, btnLeave;

    ArrayAdapter<String> subArrAdapter;
    ArrayList<String> subArrList = new ArrayList<>();
    ArrayList<ListView> subList = new ArrayList<>(5);

    ArrayList<Card> hand = new ArrayList<>(5);
    ArrayList<Card> lastHand = new ArrayList<>(5);
    int strengthLastHand=0;

    Random rnd = new Random();


    SensorManager sensorManager;
    Sensor sensorLight;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        final Boolean[] isDark = {false};
// create light sensor
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensorLight = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        // listen for sensor events
        SensorEventListener sensorEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                /*
                this function will run when the lightSensor level changed
                param: sensorEvent : SensorEvent
                return:void
                */
                float lux = sensorEvent.values[0];
                if(lux < 1 && !isDark[0]) // if lux is less than 1 its very dark outside
                {
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "It's Dark, Lower your phone brightness", Toast.LENGTH_SHORT);
                    toast.show();
                    isDark[0] = true;
                }
                else if (lux > 200)
                {
                    isDark[0] = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {} // i don't use this one.
        };
        // set the delay for the sensor
        this.sensorManager.registerListener(sensorEventListenerLight,this.sensorLight,sensorManager.SENSOR_DELAY_NORMAL);


        btnShuffle = findViewById(R.id.btnShuffle);
        btnShuffle.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        hostUid = extras.getString("hostUid");
        if (hostUid != null) {
            code = extras.getString("generateCode");
            textGenerateCode = findViewById(R.id.textGenerateCode);
            textGenerateCode.setText(code);
            textGenerateCode.setVisibility(View.VISIBLE);
        } else {
            code = extras.getString("insertCode");
        }

        user.setUid(firebaseUser.getCurrentUser().getUid());
        user.setMyLastUser(extras.getString("myLastGamer"));
        user.setEmail(firebaseUser.getCurrentUser().getEmail());
        user.setUsername(user.getEmail().replace("@gmail.com", ""));
        user.setMyLastUser(extras.getString("myLastGamer"));


        for (int i = 0; i < 52; i++) {
            cardDeck.add(new Card());
            int num = (i+2)%13;
            if(num<2) {
                num += 13;
            }
            cardDeck.get(i).setId(deckArr[i]);
            cardDeck.get(i).setNum(num);
        }


        gameReference = gamesReference.document(code);
        gamersRef = gameReference.collection("gamers");

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        btnStartRound = findViewById(R.id.btnStartRound);
        btnStartRound.setOnClickListener(this);

        btnReady = findViewById(R.id.btnReady);
        btnReady.setOnClickListener(this);

        btnCheat = findViewById(R.id.btnCheat);
        btnCheat.setOnClickListener(this);

        btnHand = findViewById(R.id.btnHand);
        btnHand.setOnClickListener(this);

        btnEnterHand = findViewById(R.id.btnEnterHand);
        btnEnterHand.setOnClickListener(this);

        btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        btnLeave = findViewById(R.id.btnLeave);
        btnLeave.setOnClickListener(this);

        textOutcome = findViewById(R.id.textOutcome);
        textOutcome.setOnClickListener(this);

        lastHandBetImageArr.add(findViewById(R.id.lastHandCard1));
        lastHandBetImageArr.add(findViewById(R.id.lastHandCard2));
        lastHandBetImageArr.add(findViewById(R.id.lastHandCard3));
        lastHandBetImageArr.add(findViewById(R.id.lastHandCard4));
        lastHandBetImageArr.add(findViewById(R.id.lastHandCard5));

        for (int i = 0; i < 5; i++) {
            hand.add(new Card());
        }

        subArrAdapter = new ArrayAdapter<>
                (getApplicationContext(), android.R.layout.simple_list_item_1, subArrList);
        subList.add(0, findViewById(R.id.subList1));
        subList.add(1, findViewById(R.id.subList2));
        subList.add(2, findViewById(R.id.subList3));
        subList.add(3, findViewById(R.id.subList4));
        subList.add(4, findViewById(R.id.subList5));
        for (int i = 0; i < 5; i++) {
            subList.get(i).setAdapter(subArrAdapter);
            subArrAdapter.notifyDataSetChanged();
        }

        cardsImagesArr.add(findViewById(R.id.crdSlot1));
        cardsImagesArr.add(findViewById(R.id.crdSlot2));
        cardsImagesArr.add(findViewById(R.id.crdSlot3));
        cardsImagesArr.add(findViewById(R.id.crdSlot4));
        cardsImagesArr.add(findViewById(R.id.crdSlot5));
        cardsImagesArr.add(findViewById(R.id.crdSlot6));
        cardsImagesArr.add(findViewById(R.id.crdSlot7));
        cardsImagesArr.add(findViewById(R.id.crdSlot8));
        cardsImagesArr.add(findViewById(R.id.crdSlot9));
        cardsImagesArr.add(findViewById(R.id.crdSlot10));

        subArrAdapter.add("two");
        subArrAdapter.add("three");
        subArrAdapter.add("four");
        subArrAdapter.add("five");
        subArrAdapter.add("six");
        subArrAdapter.add("seven");
        subArrAdapter.add("eight");
        subArrAdapter.add("nine");
        subArrAdapter.add("ten");
        subArrAdapter.add("jack");
        subArrAdapter.add("queen");
        subArrAdapter.add("king");
        subArrAdapter.add("ace");
        subArrAdapter.notifyDataSetChanged();

        // בשינוי בגיים
        gameReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot valueGame, @Nullable FirebaseFirestoreException error) {

                //בשינוי של לסט האנד
                CollectionReference lastHandRef = valueGame.getReference().collection("lastHand");
                lastHandRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot valueLastHandRef, @Nullable FirebaseFirestoreException error) {
                        strengthLastHand = valueGame.getDouble("lastHandStrength").intValue();
                        lastHand = getLastHandFromFirebase();
                        showCards(lastHand, lastHandBetImageArr);
                        if (valueGame.getString("lastGamer").equals(user.getMyLastUser())) {
                            if (valueGame.getBoolean("status")) {
                                btnHand.setVisibility(View.VISIBLE);
                                btnHand.setClickable(true);
                                btnCheat.setVisibility(View.VISIBLE);
                                btnCheat.setClickable(true);
                            }
                        } else if (lastHand != null) {
                            btnHand.setVisibility(View.GONE);
                            btnCheat.setVisibility(View.GONE);
                        }
                    }
                });

                DocumentReference gamerRef = gamersRef.document(user.getUid());
                gamerRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user.setMyLastUser(documentSnapshot.getString("myLastGamer"));
                    }
                });

                //בשינוי של גיימרס
                gamersRef = gameReference.collection("gamers");
                gamersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot valueGamers, @Nullable FirebaseFirestoreException error) {

                        //בשינוי של גיימר
                        gamerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot valueGamer, @Nullable FirebaseFirestoreException error) {

                                //בשינוי של האנד(רק אחרי שאפל יש שינוי)
                                CollectionReference handRef = gamerRef.collection("hand");
                                handRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot valueHand, @Nullable FirebaseFirestoreException error) {

                                        if (!valueGame.getBoolean("status")) {
                                            List<DocumentSnapshot> snapshotListCards = valueHand.getDocuments();
                                            ArrayList<Card> myCards = new ArrayList<>();
                                            for (DocumentSnapshot snapshotCard : snapshotListCards) {
                                                Card tempCard = new Card(snapshotCard.getDouble("num").intValue(),
                                                        snapshotCard.getDouble("id").intValue());
                                                myCards.add(tempCard);
                                            }
                                            if (!myCards.isEmpty()) {
                                                showCards(myCards, cardsImagesArr);
                                            }
                                            user.setMyLastUser(valueGamer.getString("myLastGamer"));
//                                                if(cardsImagesArr.get(0)!=null) {
//                                                    cardsImagesArr.get(0).setImageResource(R.drawable.card_back);
//                                                }
                                        }

                                    }
                                });
                            }
                        });
                    }
                });

                //משחק לא רץ
                if (!valueGame.getBoolean("status")) {
                    for (int i = 0; i < lastHandBetImageArr.size(); i++) {
                        lastHandBetImageArr.get(i).setImageResource(R.drawable.card_placeholder);
                    }
                    if(valueGame.getString("loser")!=null) {
                        if (valueGame.getDouble("numGamers") == 1) {
                            btnShuffle.setVisibility(View.GONE);

                            if (valueGame.getString("loser").equals(user.getUid())) {
                                textOutcome.setText("you Won");
                            } else {
                                textOutcome.setText(valueGame.getString("lastGamer") + " Won");
                            }
                            textOutcome.setVisibility(View.VISIBLE);
                            btnHome.setVisibility(View.VISIBLE);
                        } else {
                            if (valueGame.getString("loser").equals(user.getUsername())) {
                                textOutcome.setText("you lost");
                                textOutcome.setVisibility(View.VISIBLE);
                                if (valueGame.getDouble("minusCards") != null) {
                                    if (valueGame.getDouble("minusCards").equals(valueGame.getDouble("numCards"))) {
                                        btnHome.setVisibility(View.VISIBLE);
                                        btnShuffle.setVisibility(View.GONE);
                                    } else {
                                        btnShuffle.setVisibility(View.VISIBLE);
                                    }
                                }
                                btnShuffle.setVisibility(View.VISIBLE);
                            } else {
                                if (valueGame.getString("loser") != user.getUsername()
                                        && valueGame.getString("lastGamer") == user.getMyLastUser()) {
                                    btnShuffle.setVisibility(View.VISIBLE);
                                } else {
                                    gameReference.update("lastGamer", gamersRef.document().getId());
                                }
                                textOutcome.setText(valueGame.getString("loser") + " lost");
                                textOutcome.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if(valueGame.getString("lastGamer").equals(user.getUid())){
                        if(valueGame.getDouble("numGamers")>1) {
                            btnShuffle.setVisibility(View.VISIBLE);
                        }
                    } else {
                        btnShuffle.setVisibility(View.GONE);
                    }

//
//                    if (valueGame.getString("lastGamer").equals(user.getUid())) {
//                        if(valueGame.getDouble("numGamers")>1) {
//                            btnShuffle.setVisibility(View.VISIBLE);
//                        }
//                    } else {
//                            btnShuffle.setVisibility(View.GONE);
//                        if (valueGame.getDouble("numGamers") == 1) {
//                            textOutcome.setText("you Won");
//                            textOutcome.setVisibility(View.VISIBLE);
//                            btnHome.setVisibility(View.VISIBLE);
//                        }
//                        if (valueGame.getString("loser") != null) {
//                            if (valueGame.getString("loser").equals(user.getUsername())) {
//                                if (valueGame.getDouble("numGamers") == 1) {
//                                    textOutcome.setText("you Won");
//                                    textOutcome.setVisibility(View.VISIBLE);
//                                    btnHome.setVisibility(View.VISIBLE);
//                                } else {
//                                    textOutcome.setVisibility(View.VISIBLE);
//                                    textOutcome.setText("you lost");
//                                    if (valueGame.getDouble("minusCards") != null) {
//                                        if (valueGame.getDouble("minusCards").equals(valueGame.getDouble("numCards"))) {
//                                            btnHome.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//                                }
//                            } else {
//                                textOutcome.setText(valueGame.getString("loser") + " lost");
//                                textOutcome.setVisibility(View.VISIBLE);
//                            }
                    btnHand.setVisibility(View.GONE);
                    btnCheat.setVisibility(View.GONE);
                    lastHand = new ArrayList<>();
                    showCards(lastHand, lastHandBetImageArr);
                    if (cardsImagesArr.get(0) != null) {
                        cardsImagesArr.get(0).setImageResource(R.drawable.card_back);
                    }

                } else if (valueGame.getBoolean("status")){
                    textOutcome.setVisibility(View.GONE);
                    btnShuffle.setVisibility(View.GONE);

                    if (lastHand != null) {
                        if(valueGame.getDouble("lastHandStrength")!=null) {
                            if (valueGame.getString("lastGamer").equals(user.getMyLastUser())) {
                                btnHand.setVisibility(View.VISIBLE);
                                btnCheat.setVisibility(View.VISIBLE);
                            }

                            if (!valueGame.getString("lastGamer").equals(user.getMyLastUser())) {
                                btnHand.setVisibility(View.GONE);
                                btnCheat.setVisibility(View.GONE);
                            }
                        }
                    }
                }

            }
        });




    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnShuffle:
                btnShuffle.setVisibility(View.GONE);
                gameReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("loser")==null) {
                            btnCheat.setVisibility(View.VISIBLE);
                            btnHand.setVisibility(View.VISIBLE);

                        }
                    }
                });
                shuffle();
                break;

            case R.id.btnHand:
                btnHand.setClickable(false);
                btnCheat.setClickable(false);
                for (int i=0;i<5;i++) {
                    int finalI = i;
                    hand.add(i, null);
                    lastHandBetImageArr.get(i).setOnClickListener(this);
                    lastHandBetImageArr.get(i).setClickable(true);
                    lastHandBetImageArr.get(i).setImageResource(R.drawable.card_placeholder);
                    lastHandBetImageArr.get(i).setVisibility(View.VISIBLE);
                    lastHandBetImageArr.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            lastHandBetImageArr.get(finalI).setVisibility(View.GONE);
                            vibrator.vibrate(50);
                            hand.set(finalI, null);
                            subList.get(finalI).setVisibility(View.VISIBLE);
                            subList.get(finalI).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                    vibrator.vibrate(50);
                                    subList.get(finalI).setVisibility(View.GONE);
                                    hand.set(finalI, cardDeck.get(position));
                                    lastHandBetImageArr.get(finalI).setImageResource(hand.get(finalI).getId());
                                    lastHandBetImageArr.get(finalI).setVisibility(View.VISIBLE);
                                    btnEnterHand.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
                break;

            case R.id.btnEnterHand:
                hand = CheckTheHand(hand);
                if (Strength(hand)>strengthLastHand){

                    for(int i=0; i<5; i++){
                        gameReference.collection("lastHand").document(String.valueOf(i)).delete();
                        subList.get(i).setVisibility(View.GONE);
                    }
                    for(int i=0;i<lastHandBetImageArr.size();i++) {
                        if(i<hand.size()) {
                            if (hand.get(i) != null) {
                                if (hand.get(i).getNum() != 0) {
                                    gameReference.collection("lastHand").document(String.valueOf(i)).set(hand.get(i));
                                }
                            }
                        } else {
                            lastHandBetImageArr.get(i).setImageResource(R.drawable.card_placeholder);
                        }
                        lastHandBetImageArr.get(i).setClickable(false);
                    }
                    gameReference.update("lastGamer",user.getUid());
                    btnCheat.setVisibility(View.GONE);
                    btnHand.setVisibility(View.GONE);
                    btnEnterHand.setVisibility(View.GONE);
                    gameReference.update("lastHandStrength", Strength(hand));
                } else {
                    Toast.makeText(MainActivity4.this,
                            "this hand isn't stronger then the last hand", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnCheat:
                gameReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        lastHand = getLastHandFromFirebase();
                        if(lastHand==null){
                            Toast.makeText(MainActivity4.this, "you cant check for cheat if there is no lastHand", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!isCheat(lastHand)) {
                                Loser(gameReference.collection("gamers").document(user.getUid()));

                            } else {
                                Loser(gameReference.collection("gamers").document(user.getMyLastUser()));
                            }
                            btnEnterHand.setVisibility(View.GONE);
                            btnHand.setVisibility(View.GONE);
                            btnCheat.setVisibility(View.GONE);
                        }
                    }
                });
                gameReference.update("status", false);
                newRound();
//                if(cardsImagesArr.get(0)!=null) {
//                    cardsImagesArr.get(0).setImageResource(R.drawable.card_back);
//                }

                break;

            case R.id.btnHome:
                startActivity(new Intent(MainActivity4.this, MainActivity.class));
                break;

            case R.id.btnLeave:
                gameReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshotGame) {
                        gameReference.collection("gamers").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshotGamer) {

                                if (!documentSnapshotGame.getBoolean("status")) {
                                    int numCards = documentSnapshotGame.getDouble("numCards").intValue();

                                    int minusCards = 0;
                                    if(documentSnapshotGamer.getDouble("minusCards")!=null) {
                                        minusCards = documentSnapshotGamer.getDouble("minusCards").intValue();
                                    }
                                    DocumentReference gamerRef = gamersRef.document(user.getUid());;
                                    //Toast.makeText(MainActivity4.this, "numCards: "+ numCards, Toast.LENGTH_SHORT).show();
                                    for (minusCards = minusCards; minusCards < numCards+1; minusCards++) {
                                        Loser(gamerRef);
                                        gamerRef = gamersRef.document(user.getUid());
                                    }
                                    if(documentSnapshotGame.getDouble("numCards").intValue()==documentSnapshotGamer.getDouble("minusCards").intValue()+1) {
                                        btnLeave.setVisibility(View.GONE);
                                        btnHome.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Toast.makeText(MainActivity4.this, "you cant leave the match in a middle of a round", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

    public void shuffle(){
        cardDeckTemp = (ArrayList<Card>) cardDeck.clone();
        gameReference.update("lastGamer", user.getUid());
        gameReference.update("loser", null);
        gameReference.update("status",true);

        gameReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshotGame) {

                int numCards = documentSnapshotGame.getDouble("numCards").intValue();

                gamersRef = documentSnapshotGame.getReference().collection("gamers");
                gamersRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : snapshotList) {
                            DocumentReference gamerRef = snapshot.getReference();
                            User tempUser = new User(gamerRef.getId());

                            gamerRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshotGamer) {

                                    if(documentSnapshotGamer.getString("myLastGamer")==null){
                                        user.setMyLastUser(documentSnapshotGame.getString("lastGamer"));
                                    }
                                    if(documentSnapshotGamer.getString("myLastGamer")==null){
                                        gamerRef.update("myLastGamer", documentSnapshotGame.getString("lastGamer"));
                                    }
                                    int minusCards = 0;
                                    if(documentSnapshotGamer.getDouble("minusCards")!=null){
                                        minusCards = documentSnapshotGamer.getDouble("minusCards").intValue();
                                    }

                                    ArrayList<Card> hand=new ArrayList<>();
                                    for (int i = 0; i < numCards - minusCards; i++) {
                                        int r = rnd.nextInt(cardDeckTemp.size()-1);
                                        gamerRef.collection("hand").document(String.valueOf(i)).set(cardDeckTemp.get(r));
                                        cardDeckTemp.remove(r);
                                    }
                                    tempUser.setHand(hand);
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity4.this, "game hasn't been read properly", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    public boolean isCheat(ArrayList<Card> hand){
        System.out.println("===========================");
        System.out.println("handCopy v");
        PrintHand(hand);
        System.out.println("===========================");
        gamersRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                ArrayList<Card> handCopy=hand;
                List<DocumentSnapshot> snapshotListGamers = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshotGamer : snapshotListGamers){
                    snapshotGamer.getReference().collection("hand").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            List<DocumentSnapshot> snapshotListHand = queryDocumentSnapshots.getDocuments();
                            ArrayList<Card> tempHand = new ArrayList<>();
                            for (DocumentSnapshot snapshotCard : snapshotListHand){
                                Card tempCard = new Card
                                        (snapshotCard.getDouble("num").intValue(),
                                                snapshotCard.getDouble("id").intValue());
                                tempHand.add(tempCard);
                            }
                            System.out.println("===========================");
                            System.out.println("tempHand v");
                            PrintHand(tempHand);
                            System.out.println("===========================");

                            for(int i=0; i<handCopy.size();i++){


                                for(int j=0; j<tempHand.size();j++){
                                    if(handCopy.get(i)!=null && tempHand.get(j)!=null) {
                                        if (handCopy.get(i).getNum() == tempHand.get(j).getNum()) {
                                            handCopy.remove(i);
                                            i--;
                                            tempHand.remove(j);
                                            j = tempHand.size();
                                        }
                                    }
                                }
                            }
                            System.out.println("===========================");
                            System.out.println("END handCopy v");
                            PrintHand(handCopy);
                            System.out.println("===========================");
                        }
                    });
                }
            }
        });

        if(hand.size()==0){
            return false;
        } else return true;
    }

    public ArrayList<Card> CheckTheHand(ArrayList<Card> lastHand) {

        int pair = 0;
        int highCard = 0;
        int indexLastHighCard = -1;
        ArrayList<Boolean> flag = new ArrayList<>(lastHand.size());
        for (int i = 0; i < lastHand.size(); i++) {
            flag.add(i, false);
        }

        for(int i=0;i<hand.size();i++) {
            if (hand.get(i) != null) {
                if (hand.get(i).getNum() == 0) {
                    hand.set(i, null);
                    i--;
                }
            }
        }
//בודק אם יש חמישה קלפים מאותו סוג
        for (int i = 0; i < lastHand.size() - 1; i++) {
            if (lastHand.get(i) != null && lastHand.get(i + 1) != null) {
                if (lastHand.get(i).getNum() == (lastHand.get(i + 1).getNum())) {
                    flag.set(i, true);
                    flag.set(i + 1, true);
                    pair++;
                    if (pair == 4) {
                        lastHandBetImageArr.get(4).setImageResource(R.drawable.card_placeholder);
                        lastHand.set(4,null);
                        Toast.makeText(MainActivity4.this,
                                "there is only four card of each number", Toast.LENGTH_SHORT).show();

                        Toast.makeText(MainActivity4.this,
                                "this is your sent hand", Toast.LENGTH_SHORT).show();
                        return lastHand;
                    }
                }
            }
        }


        for (int i = 0; i < lastHand.size(); i++) {
            flag.add(i, false);
        }

        for (int n = 0; n < lastHand.size()-1; n++) {
            //בודק אם יש לקלף זוג או שהוא חשוד לקלף גבוה
            if (n < lastHand.size() - 1) {
                if (lastHand.get(n) != null) {
                    for (int j = n + 1; j < lastHand.size(); j++) {
                        if (lastHand.get(j) != null) {
                            if (lastHand.get(n).equals(lastHand.get(j))) {
                                flag.set(n, true);
                                flag.set(j, true);
                            }
                        }
                    }
                }
            }

            //בודק קלף גבוה
            if (!flag.get(n)) {
                if (lastHand.get(n)!=null) {
                    if (lastHand.get(n).getNum() > highCard) {
                        if (indexLastHighCard != -1) {
                            lastHandBetImageArr.get(indexLastHighCard).setImageResource(R.drawable.card_placeholder);
                            hand.set(indexLastHighCard,null);
                            Toast.makeText(MainActivity4.this,
                                    "there was unnecessary card in this hand", Toast.LENGTH_SHORT).show();
                        }
                        highCard = lastHand.get(n).getNum();
                        indexLastHighCard = n;
                    } else {
                        lastHandBetImageArr.get(n).setImageResource(R.drawable.card_placeholder);
                        hand.set(n,null);
                        n--;
                    }
                }
            }
        }
        return lastHand;
    }

    public int Strength(ArrayList<Card> hand) {

        int strength=0;
        int pairCount;
        ArrayList<Boolean> flag = new ArrayList<>(hand.size());
        SortCards(hand);

        PrintHand(hand);
            for (int i = 0; i < hand.size(); i++) {
                flag.add(i, false);
            }
            for (int i = 0; i < hand.size() - 1; i++) {
                if (hand.get(i) != null) {
                    for (int j = i + 1; j < hand.size(); j++) {
                        if (hand.get(j) != null) {
                            //בודק זוגות ובהתאם סוג יד
                            if (hand.get(i).equals(hand.get(j))) {
                                strength += 1000000;
                                flag.set(i, true);
                                flag.set(j, true);
                            }
                        } else {
                            hand.set(j, null);
                            flag.set(j, null);
                        }
                    }

                } else {
                    hand.set(i, null);
                    flag.set(i, null);
                }

            }

        for(int i=0; i<flag.size();i++){
            //בודק קלף גבוה
            if(flag.get(i)!=null){
                if (!flag.get(i)) {
                    strength += hand.get(i).getNum();
                }
            }
        }

            if (hand.size() >= 4) {
                pairCount = strength / 1000000;
                if (pairCount == 5) {
                    strength = strength - 1000000;
                    pairCount = pairCount - 1;
                }
                if (pairCount == 2 || pairCount == 4) {
                    //בודק אם הפול האוס שלשה ואז זוג או הפוך
                    if (pairCount == 4 && hand.get(1) == hand.get(2)) {
                        strength += 10000 * hand.get(1).getNum();
                        strength += 100 * hand.get(3).getNum();
                    } else {
                        strength += 10000 * hand.get(3).getNum();
                        strength += 100 * hand.get(1).getNum();
                    }
                } else {
                    for(int i=0;i<hand.size();i++) {
                        if(hand.get(i)!=null) {
                            strength += 10000 * hand.get(i).getNum();
                            i=hand.size();
                        }
                    }
                }
            }
        return strength;
    }

    public void Loser(DocumentReference gamer) {

        gamer.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshotGamer) {

                gameReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshotGame) {

                        int numCards = Objects.requireNonNull(documentSnapshotGame.getDouble("numCards")).intValue();
                        int minusCards=0;
                        gameReference.update("loser", documentSnapshotGamer.getString("username"));
                        if(documentSnapshotGamer.getDouble("minusCards")!=null){
                            minusCards = documentSnapshotGamer.getDouble("minusCards").intValue();
                        }

                        if(minusCards<numCards-1){
                            gamer.update("minusCards",minusCards+1);
                        } else {
                            gamersRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                    for(int i=0; i<snapshotList.size();i++){
                                        if(snapshotList.get(i).getString("myLastGamer").equals(gamer.getId())){
                                            snapshotList.get(i).getReference().update("myLastGamer",
                                                    documentSnapshotGamer.getString("myLastGamer"));
                                            gamersRef.document(gamer.getId()).delete();
                                        }
                                    }
                                    int numGamers = documentSnapshotGame.getDouble("numGamers").intValue();
                                    gameReference.update("numGamers",numGamers -1);
                                    if(numGamers-1==1){
                                        gameReference.update("lastGamer", gamersRef.document().getId());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public ArrayList<Card> SortCards(ArrayList<Card> cards){
        for (int i = 1; i < cards.size(); i++) {
            if(cards.get(i)!=null) {
                int key = cards.get(i).getNum();
                int j = i - 1;
                while (j >= 0 && key < cards.get(i).getNum()) {
                    if(cards.get(j)!=null) {
                        cards.set(j + 1, cards.get(j));
                    }
                    j--;
                }
                cards.set(j + 1, cards.get(i));
            }
        }
        return cards;
    }

    public void showCards(ArrayList<Card> cards, ArrayList<ImageView> images){


            for (int i = 0; i < images.size(); i++) {
                if(cards!=null) {
                    if (i < cards.size()) {
                        if (images.get(i) != null && cards.get(i) != null) {
                            images.get(i).setVisibility(View.VISIBLE);
                            images.get(i).setImageResource(cards.get(i).getId());
                        }
                    } else {
                        images.get(i).setVisibility(View.VISIBLE);
                        images.get(i).setImageResource(R.drawable.card_placeholder);
                    }
                } else {
                    images.get(i).setVisibility(View.VISIBLE);
                    images.get(i).setImageResource(R.drawable.card_placeholder);
                }
            }

}

    public void PrintHand(ArrayList<Card> Hand){
        if(Hand==null){
            System.out.println("null");
        } else{
            for (int i = 0; i < Hand.size(); i++) {
                System.out.println("hand" + i + ": " + Hand.get(i));
                if (Hand.get(i) != null) {
                    System.out.println("hand Num " + Hand.get(i).getNum());
                    System.out.println("hand id " + Hand.get(i).getId());
                } else {
                    System.out.println("hand Num " + null);
                    System.out.println("hand id " + null);
                }
            }
        }
    }

    public ArrayList<Card> getLastHandFromFirebase() {

        gameReference.collection("lastHand").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                lastHand = new ArrayList<>();
                if(snapshotList.size()>0) {
                    for (int i = 0; i < snapshotList.size(); i++) {
                        if (snapshotList.get(i) != null) {
                            Card tempCard = new Card(snapshotList.get(i).getDouble("num").intValue(),
                                    snapshotList.get(i).getDouble("id").intValue());
                            lastHand.add(i, tempCard);
                        }
                    }
                } else {
                    lastHand = null;
                }
            }
        });

        return lastHand;
    }

    public void newRound(){

        lastHand = new ArrayList<>(5);
        gameReference.collection("lastHand").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i<snapshotList.size(); i++){
                    gameReference.collection("lastHand")
                            .document(snapshotList.get(i).getId()).delete();
                }
            }
        });


        gameReference.collection("gamers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotListGamers = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i<snapshotListGamers.size(); i++) {
                    CollectionReference handRef = snapshotListGamers.get(i).getReference().collection("hand");
                    handRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> snapshotListCards = queryDocumentSnapshots.getDocuments();
                            for (int j = 0; j < snapshotListCards.size(); j++) {
                                handRef.document(snapshotListCards.get(j).getId()).delete();
                            }
                        }
                    });
                }
            }
        });

        gameReference.update("lastHandStrength", 0);
        gameReference.update("status", false);
        if(cardsImagesArr.get(0)!=null) {
            cardsImagesArr.get(0).setImageResource(R.drawable.card_back);
        }

    }


}


