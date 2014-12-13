package com.padisDefense.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.padisDefense.game.GameScreen;
import com.padisDefense.game.Padi;



/**
 * @author Xeng
 *
 * @param 'padi'
 *
 * */
public class WorldMap implements Screen {

    private Padi padi;



    Texture background_texture;

    Array<TextButton> buttons;

    Sprite background;
    Stage stage;
    WorldMap(Padi p){
        padi = p;
    }//End of Worldmap() Constructor.


    @Override
    public void show() {



        background_texture = new Texture("worldmap.png");
        background = new Sprite(background_texture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        buttons = new Array<TextButton>();
        stage = new Stage();


        /**
         * For-loop makes the buttons.
         * Buttons 0-8 open up a gamescreen. button 9 and 10 opens up store/menu.
         *
         * new TextButtons take parameters of (string value, skin, stylename).
         * Each button gets its own ClickListener.
         * When the buttons are clicked, the game level will open up, or the menu/store screen
         * The new GameScreen() takes in a game object(padi) and a int level.
         * 'x' is not final, so it couldn't be passed into GameScreen.
         * That's why [final int g] was created.
         * **/
        for(int x = 0; x < 11; x++){
            final int g = x + 1;
            //Adds a button. The last two buttons are 'menu' and 'store'.
            if(x < 9) {
                buttons.add(new TextButton(String.valueOf(x + 1), padi.skin, "default"));

                buttons.get(x).addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent e, float a, float b) {
                        padi.setScreen(new GameScreen(padi, g));
                    }
                });

            }
            else if(x == 9) {
                buttons.add(new TextButton(" menu ", padi.skin, "default"));
                buttons.get(x).addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent e, float a, float b){
                        padi.setScreen(padi.main_menu);
                    }
                });
            }
            else if(x == 10) {
                buttons.add(new TextButton(" store ", padi.skin, "default"));
                buttons.get(x).addListener(new ClickListener(){
                    @Override
                    public void clicked(InputEvent e, float a, float b){
                        padi.setScreen(padi.store);
                    }
                });
            }


            //This sets the dimensions and color of the buttons.
            buttons.get(x).setSize(50f, 30f);
            buttons.get(x).setColor(0.1f,0.3f, 0.4f, 0.6f);

            //This is to position the buttons. Each button is 50f lower than the previous one.
            if(x == 0)
                buttons.get(x).setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 50f);
            else
                buttons.get(x).setPosition(Gdx.graphics.getWidth() / 2,buttons.get(x-1).getY() - 50f);
        }

        for(int x = 0; x < buttons.size; x++)
            stage.addActor(buttons.get(x));

        Gdx.input.setInputProcessor(stage);
    }



    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0,0,0.8f,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        padi.batch.begin();

        background.draw(padi.batch);//Draw background.

        for(int x = 0; x < buttons.size; x++)
            buttons.get(x).draw(padi.batch, 3);//Draw buttons.

        padi.batch.end();

        /*if(Gdx.input.isKeyPressed(2)){
            padi.setScreen(padi.main_menu);
        }*/
    }


    @Override
    public void resize(int width, int height) {

    }



    @Override
    public void hide() {

    }


    @Override
    public void pause() {
    }


    @Override
    public void resume() {
    }




    @Override
    public void dispose(){

        stage.dispose();
        background_texture.dispose();
    }
}