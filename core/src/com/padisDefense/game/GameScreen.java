package com.padisDefense.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.padisDefense.game.Managers.BulletManager;
import com.padisDefense.game.Managers.DamageManager;
import com.padisDefense.game.Managers.EnemyManager;
import com.padisDefense.game.Managers.LevelManager;
import com.padisDefense.game.Managers.SpawnManager;
import com.padisDefense.game.Managers.TowerManager;
import com.padisDefense.game.Managers.UIManager;


public class GameScreen extends ScreenAdapter implements InputProcessor {

    public Padi padi;

    private boolean GAME_OVER = false;
    private EndGameAnimation endGameAnimation;
    public String gameStatus = "";


    public EnemyManager enemy;
    public TowerManager tower;
    public BulletManager bullet;
    public LevelManager level;
    public SpawnManager spawn;
    public DamageManager damage;



    //stuff for the UI
    public UIManager UI;
    private float oldEnemyCount = 0;
    private float newEnemyCount = 0;


    //maybe right now a multi is not needed,
    //but it never hurts to have,  right?
    public InputMultiplexer multi;

    public Music gameMusic;

    public GameScreen(Padi p){
        padi = p;

        endGameAnimation = new EndGameAnimation();
        level = new LevelManager(this, padi);
        tower = new TowerManager(this, padi);
        enemy = new EnemyManager(this, padi);
        spawn = new SpawnManager(this, padi);
        UI = new UIManager(this, padi);
        damage = new DamageManager(this, padi);
        bullet = new BulletManager(this, padi);
        multi = new InputMultiplexer();

        multi.addProcessor(UI.getStage());
        multi.addProcessor(UI);
        multi.addProcessor(this);

    }

    @Override
    public void show(){

        Gdx.input.setInputProcessor(multi);
        gameStatus = "";

        int x = (int)(Math.random()*10);

        if(x % 2 == 0) gameMusic =  padi.assets.star;
        else gameMusic = padi.assets.east;

        gameMusic.play();
    }

    public int playLevel, limit;
    //example: if level 1, then 'limit' limits the towers and enemies to 3.
    //if level 2, then 'limit' limits the towers and enemies to 6.
    public void initLevel(int L){
        this.reset();
        playLevel = L;
        limit = L*3;

        if(limit > 9)
            limit = 9;

        UI.clearTables();
        UI.initUI();

        level.determineLevelSettings(L);//stores level and determines how many enemies to spawn, pathing, etc.

        enemy.setEnemyAmount(level.getEnemyAmount());//telling enemyManager how many enemy to spawn.

        enemy.setPath(level.getPath());//setting the path where all enemy will travel.

        level.spawnBuildableSpots(tower, playLevel - 1);//getting locations for buildableSpots.

        spawn.initSpawn();//types of enemies that can spawn on a level is stored in an array.

        tower.populateTowerPool();//creating some towers in advance.

        tower.inGameMoney = level.getInitialMoney();
    }



    boolean do_once = true;
    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(!UI.PAUSED){

            oldEnemyCount = enemy.getEnemyCounter();

            tower.drawCircles();
            enemy.startEnemy();
            tower.startTowers();

            if(!GAME_OVER) {

                gatherCharge();
                if(enemy.getCountDownTimer() <= 0f){//game clock starts when countdown ends.
                    UI.updateTimer(Gdx.graphics.getDeltaTime());
                    UI.updateTimerMessage();
                }
            }
            newEnemyCount = enemy.getEnemyCounter();

            calcMoney();

            UI.getStage().draw();

            //checks if end-game conditions are met.
            if(((enemy.noMoreEnemy() || UI.fullChargeMeter()) && do_once)
                    || gameStatus.equals("lose")){

                //updating stats.
                if(gameStatus.equals("win")){

                    padi.player.wins++;

                    UI.winMessage.setVisible(true);
                    UI.loseMessage.setVisible(false);
                    padi.player.money +=  100 + padi.assets.getDifficulty() * 2;
                }
                else{
                    padi.player.loss++;
                    UI.winMessage.setVisible(false);
                    UI.loseMessage.setVisible(true);
                    padi.player.money += 100 + padi.assets.getDifficulty();
                }

                padi.player.gamesPlayed++;

                do_once = false;
                GAME_OVER = true;
                enemy.destroyAllEnemy();
                UI.updateUIStuff(enemy.getEnemyCounter(), tower.getInGameMoney());

                UI.gameOver();

                //multi.addProcessor(UI.endStage);
                Gdx.input.setInputProcessor(UI.endStage);
                UI.endGameTable.setVisible(true);
                UI.endStage.draw();
                UI.masterTable.setVisible(false);
                UI.hideButton.setPosition(Gdx.graphics.getWidth()-UI.hideButton.getWidth() - 10f, 10f);

                //enemy.reset();//clears all the remaining enemy.
                if(playLevel < padi.player.getNumberOfLevels())
                    padi.player.setLevelsUnlocked(playLevel);

                padi.loadsave.savePlayer(padi.player);

            }
            else if (GAME_OVER){

                if(UI.fullChargeMeter()){
                    endGameAnimation.run();

                }
                UI.endStage.draw();
            }
            UI.updateUIStuff(enemy.getEnemyCounter(), tower.getInGameMoney());
        }

        //PAUSED
        else{
            UI.pauseStage.draw();
        }

    }





    public void calcMoney(){
        tower.updateInGameMoney((int) (Math.abs(oldEnemyCount - newEnemyCount) * 5));
        oldEnemyCount = newEnemyCount;
    }

    //if getState() returns 'false', then it must be in charging mode.
    //its chargRate is retrieved and added to temp.
    //temp is passed to UIManager to update charging meter.
    public void gatherCharge(){

        float temp = 0;
        for(int x = 0; x < tower.getTowerArray().size; x++){
            if(!tower.getTowerArray().get(x).state)
                temp += tower.getTowerArray().get(x).getChargeRate();
        }

        UI.updateChargeMeter(temp);
    }

    @Override
    public void dispose(){
        endGameAnimation.dispose();
        level.dispose();
        enemy.dispose();
        tower.dispose();
        bullet.dispose();
        UI.dispose();
        spawn.dispose();
        damage.dispose();

        //Not sure what this really does.
        multi.getProcessors().clear();
        multi.clear();

    }

    public void reset(){
        endGameAnimation.reset();
        enemy.reset();
        tower.reset();
        bullet.reset(); //currently nothing.
        tower.populateTowerPool();

        UI.reset();
        level.reset(); //currently nothing.
        spawn.reset(); //currently nothing.
        damage.reset(); //currently nothing.

        GAME_OVER = false;
        enemy.setEnemyAmount(level.getEnemyAmount());
        do_once = true;
        oldEnemyCount = 0;
        newEnemyCount = 0;

        //Gdx.input.setInputProcessor(multi);
        gameStatus = "";
    }
    @Override
    public void resize(int x, int y){
        UI.getStage().getViewport().update(x, y, true);
    }

    @Override
    public void hide(){
        //this.reset();
        //pause the ingame music
        gameMusic.stop();

    }
    @Override
    public void pause(){}

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        UI.clickedTower(x, y);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {return false;}

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        return false;
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {return false;}

    @Override
    public boolean scrolled(int amount) {return false;}

    @Override
    public boolean keyDown(int keycode) {return false;}

    @Override
    public boolean keyUp(int keycode) {return false;}

    @Override
    public boolean keyTyped(char character) {return false;}

}


