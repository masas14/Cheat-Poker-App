package com.example.CheatPoker;

import java.util.ArrayList;

public class User {

    String Uid;
    String username;
    String myLastUser;
    String email;
    ArrayList<Card> hand;
    int minusCards = 0;


    public User(String Uid) {
        this.Uid = Uid;
    }

    public User(String Uid, String email) {
        this.Uid = Uid;
        this.email = email;
    }

    public User(String Uid, String email, String username) {
        this.Uid = Uid;
        this.email = email;
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }
    public void setHand(Card card) {
        if(this.hand==null){
            this.hand = new ArrayList<>();
        }
        this.hand.add(card);
    }

    public String getMyLastUser() {
        return myLastUser;
    }

    public void setMyLastUser(String myLastUser) {
        this.myLastUser = myLastUser;
    }

    public int getMinusCards() {
        return minusCards;
    }

    public void setMinusCards(int minusCards) {
        this.minusCards = minusCards;
    }

    public User() {
    }
}


