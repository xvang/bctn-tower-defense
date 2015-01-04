package com.padisDefense.game;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.padisDefense.game.MiscellaniousCharacters.Explosion;
import com.padisDefense.game.MiscellaniousCharacters.FireBall;


public class EndGameAnimation {


    public EndGameAnimation(){
        show();
    }
    Array<FireBall> activeFireBall;
    Pool<FireBall> fireBallPool;


    Pool<Explosion> explosionPool;
    Array<Explosion> activeExplosion;
    Vector2 explosionPosition;

    SpriteBatch batch;

    public void show() {
        batch = new SpriteBatch();
        //FIREBALL
        activeFireBall = new Array<FireBall>();
        fireBallPool = new Pool<FireBall>() {
            @Override
            protected FireBall newObject() {
                FireBall f = new FireBall();
                f.setPosition(-100f, -100f);
                return f;
            }
        };

        for(int x = 0; x < 10; x++){
            activeFireBall.add(fireBallPool.obtain());
        }
        fireBallPool.freeAll(activeFireBall);
        activeFireBall.clear();

        //EXPLOSION
        explosionPool = new Pool<Explosion>() {
            @Override
            protected Explosion newObject() {
                return new Explosion();
            }
        };
        activeExplosion = new Array<Explosion>();
        explosionPosition = new Vector2();

        for(int x = 0; x < 30; x++){
            activeExplosion.add(explosionPool.obtain());
        }
        explosionPool.freeAll(activeExplosion);
        activeExplosion.clear();
    }


    public void run() {

        batch.begin();
        //drawing fireballs
        for(int x = 0; x < activeFireBall.size; x++){
            activeFireBall.get(x).animate(batch);
        }

        //checking for fireballs that reached destination.
        for(int x = 0; x < activeFireBall.size; x++){
            FireBall f = activeFireBall.get(x);

            if(!f.alive){

                Explosion e = explosionPool.obtain();
                e.explosionPosition.set(f.out);
                activeExplosion.add(e);

                activeFireBall.removeIndex(x);

                fireBallPool.free(f);
            }
        }

        int r = (int)(Math.random()*3);
        for(int x=  0; x < r; x++){
            if(activeFireBall.size < 8){
                FireBall f = fireBallPool.obtain();
                activeFireBall.add(f);

            }
        }

        for(int x = 0; x < activeExplosion.size; x++){
            activeExplosion.get(x).animate(batch);
        }

        batch.end();
        for(int x = 0; x < activeExplosion.size; x++){
            if(!activeExplosion.get(x).alive){
                Explosion pointer = activeExplosion.get(x);
                activeExplosion.removeIndex(x);
                explosionPool.free(pointer);
            }
        }


        System.out.println(activeExplosion.size + "  " + activeFireBall.size);
    }
}

