package com.padisDefense.game.Enemies;






public class Snuggo extends Enemy {

    public Snuggo(){
        //health, armor, texture
        super(100,10, "snuggo.png");
        setName("snuggo");
        //setSize(25f, 25f);
        setRate(0.06f + (float)Math.random()*0.09f);
        //userSetSize(new Vector2(12f, 18f));

    }


}