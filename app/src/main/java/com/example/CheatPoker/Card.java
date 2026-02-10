package com.example.CheatPoker;

public class Card {

    private int id;
    private int num;

    public Card(int num, int id) {
        this.id = id;
        this.num =num;
    }

    public Card(Integer integer, Integer integer1) {
    }
    public Card() {
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
