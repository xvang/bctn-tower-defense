package com.padisDefense.game.MiscellaniousCharacters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Explosion extends Miscellaneous {


    public Explosion(){
        initMovement();
    }

    public Vector2 explosionPosition;

    public void initMovement(){
        int ROW = 8;
        int COL = 8;
        Texture walkSheet = new Texture(Gdx.files.internal("animation/particlefx_06.png")); // #9
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth()/COL, walkSheet.getHeight()/ROW);              // #10
        TextureRegion[] walkFrames = new TextureRegion[COL * ROW];

        int index = 0;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }
        animation = new Animation(1/((float)(ROW*COL)), walkFrames);      // #11

        stateTime = 0f;                         // #13

        explosionPosition = new Vector2();

    }


    public void animate(SpriteBatch batch){
        if(alive){
            stateTime += Gdx.graphics.getDeltaTime()/2;

            currentFrame = animation.getKeyFrame(stateTime, false);

            batch.draw(currentFrame, explosionPosition.x, explosionPosition.y);             // #17

            if(animation.isAnimationFinished(stateTime)){
                alive = false;
            }
        }

    }

    @Override
    public void reset(){
        alive = true;
        stateTime = 0f;

    }
}
