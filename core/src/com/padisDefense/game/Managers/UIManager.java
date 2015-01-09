package com.padisDefense.game.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.padisDefense.game.GameScreen;
import com.padisDefense.game.Padi;
import com.padisDefense.game.Towers.BuildableSpot;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class UIManager implements InputProcessor{


    Padi padi;
    GameScreen game;
    //Used to create/upgrade towers
    public boolean  GAME_OVER = false;
    Stage stage;
    public Table masterTable;
    public TextButton hideButton;


    //prints money and enemy left.
    Table messageTable;
    Label moneyMessage;
    Label enemyMessage;

    //prints time in float.
    private float TIMER = 0;
    private Label timeMessage;

    private Table dragTowers;//table containing towers that can be dragged to build.
    private Array<Image> image;



    //charging meter.
    public Image loadingHidden;
    private Image loadingFrame;
    public Actor loadingBar;


    //optionMenu, when clicked.
    private Table clickedOptionTable;
    private TextButton charge, upgrade;//, sell;
    private BuildableSpot currentBS = null;//points to the clicked buildableSpot.
    private boolean b = false;// to hide the option popup after a change has been made.


    //displays the towers when buildable spot is clicked.
    private Table clickedTowerTable;
    private Array<TextButton> towerOptions;
    private ShapeRenderer shapeRenderer;

    //TODO: make the circles around the range visible.
    //table for countdown.
    private Table countDownTable;
    private Label countDownMessage2;

    //end-GameScreen
    public Stage endStage;//This is to make the endgame popup the only thing that takes input.
    public Table endGameTable;
    private Label endGameTimeMessage;


    //pause Screen
    public boolean PAUSED = false;
    public Table pauseTable;
    public Stage pauseStage;
    private ImageButton pauseButton;


    public UIManager(GameScreen g, Padi p){
        game = g;
        padi = p;
        this.init();
    }

    public UIManager(){
        this.init();
    }

    public void setGame(GameScreen g){
        game = g;
    }

    public void init(){

        stage = new Stage();

        createDragTowers();
        createCountDownTable();
        createTowerTable();
        createEndGameTable();
        createChargeMeter();
        createOptionTable();
        createMessageTable();
        createPauseTable();

        createMasterTable();

        //button to hide the UI
        hideButton = new TextButton("Hide", padi.assets.skin2, "default");
        hideButton.setSize(80f, 50f);
        hideButton.setPosition(Gdx.graphics.getWidth()-hideButton.getWidth() - 10f, 10f);
        hideButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(masterTable.isVisible()){
                    hideButton.setText("Show");
                    masterTable.setVisible(false);
                    //hideButton.setPosition(Gdx.graphics.getWidth()-hideButton.getWidth() - 10f, 10f);
                }
                else{
                    hideButton.setText("Hide");
                    //hideButton.setPosition(Gdx.graphics.getWidth()-masterTable.getWidth(), 10f);
                    masterTable.setVisible(true);
                }

            }
        });



        stage.addActor(loadingFrame);
        stage.addActor(loadingHidden);
        stage.addActor(loadingBar);
        stage.addActor(clickedOptionTable);
        stage.addActor(masterTable);
        stage.addActor(hideButton);
        stage.addActor(clickedTowerTable);
        stage.addActor(pauseButton);

    }

    public Stage getStage(){return stage;}

    public void updateTimer(float d){TIMER += d;}


    public void updateEnemyMessage(int e){
        enemyMessage.setText("Enemies Left: " + String.valueOf(e));
    }

    public void updateMoneyMessage(int m){
        moneyMessage.setText("$ " + String.valueOf(m));
    }

    public void updateTimerMessage(){
        timeMessage.setText("Timer: " + String.valueOf(round(TIMER, 1)));
        endGameTimeMessage.setText("Time: " + String.valueOf(round(TIMER, 3)) + " secs.");
    }


    //this function also hides the countdown table.
    public void updateCountDownMessage(){
        countDownMessage2.setText("Time left: " + String.valueOf((int) game.enemy.getCountDownTimer()));

        if(game.enemy.getCountDownTimer() <= 0f){
            countDownTable.setVisible(false);
        }
    }

    float currentCharge = 0;
    boolean stopUpdatingChargeMeter = false;
    public void updateChargeMeter(float d){
        if(!stopUpdatingChargeMeter) {
            if (loadingBar.getWidth() < loadingHidden.getWidth()) {
                currentCharge += d;
                loadingBar.setSize(currentCharge, loadingHidden.getHeight());
                stage.act();
            }


            if (loadingBar.getWidth() >= loadingHidden.getWidth()) {
                stopUpdatingChargeMeter = true;
                loadingBar.setWidth(loadingHidden.getWidth());
            }
        }
    }

    public void updateUIStuff(int enemyCounter, int inGameMoney){
        if(!GAME_OVER){
            updateEnemyMessage(enemyCounter);
            updateMoneyMessage(inGameMoney);
            updateCountDownMessage();
        }


    }

    public boolean fullChargeMeter(){
        return (loadingBar.getWidth() == loadingHidden.getWidth());
    }


    //Not mine. some answer on Stackoverflow.
    public static double round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    /**CALLED BY touchDown() in GAMESCREEN.
     * Takes the coord of a click, and the coord of a tower
     * if their respective rectangles overlap, then user has
     * "clicked" a tower.  Towers actually take no direct user input.
     * The optionsTable will be located at where the clicked tower is.
     * 'b' boolean below toggles the option table.
     * 'currentBS' points to the clicked BuildableSpot
     * it will be passed into buildATower() in towerManager.
     * */
    public void clickedTower(int x, int y){


        Rectangle rec1 = new Rectangle();
        rec1.setSize(2f, 2f);
        rec1.setPosition(x, Gdx.graphics.getHeight() - y);
        BuildableSpot currentBuildable;
        for(int s = 0; s < game.tower.getBuildableArray().size; s++){
            currentBuildable = game.tower.getBuildableArray().get(s);

            if(rec1.overlaps(currentBuildable.getBoundingRectangle())){
                b = !b;

                //updating the 'charge' button message.
                try{
                    charge.setText(currentBuildable.getCurrentTower().getMessage());
                }catch(Exception e){

                    //charge.setText(tower.getBuildableArray().get(s).getMessage());
                }

                //setting the optiontable's location to where clicked tower is.
                clickedOptionTable.setPosition(currentBuildable.getX() - (clickedOptionTable.getWidth()/2),
                        game.tower.getBuildableArray().get(s).getY() - (clickedOptionTable.getHeight() - 5f));

                //setting the clickedTowerTable's location.
                clickedTowerTable.setPosition(currentBuildable.getX() - (clickedTowerTable.getWidth()/2),
                        currentBuildable.getY() + currentBuildable.getHeight() + 40f);

                //'currentBuildable' is local to this function.
                //'currentBS' is global in this class.
                currentBS = game.tower.getBuildableArray().get(s);//pointer to clicked buildablespot.

                //if buildable is empty, choices of towers to build should pop up.
                if(currentBuildable.emptyCurrentTower()){
                    clickedTowerTable.setVisible(true);
                }
                //else, the option table containing 'shoot', 'upgrade', 'sell' should pop up.
                else
                    clickedOptionTable.setVisible(true);

                break;//breaks the forloop.
                // if clicked buildablespot is found, no need to keep checking
            }
        }
    }


    public void gameOver(){
        GAME_OVER = true;
        endGameTable.setVisible(true);
    }


    //check if user dropped the image on a buildableSpot.
    public void checkTheDrop(Rectangle r, String type){
        //access the buildable array via the spawnManager.
        Array<BuildableSpot> BS = game.tower.getBuildableArray();

        //create Rectangle around BuildableSpot.
        for(int x = 0; x < BS.size; x++){
            Rectangle rec = new Rectangle(BS.get(x).getX(), BS.get(x).getY(),
                    BS.get(x).getWidth(), BS.get(x).getHeight());

            if(rec.overlaps(r) && BS.get(x).emptyCurrentTower()){
                game.spawn.buildATower("build", BS.get(x), type.toUpperCase(), 1);//passes in the buildablespot, name of tower, and level.
                clickedOptionTable.setVisible(false);
                clickedTowerTable.setVisible(false);
            }




        }

    }


    public void createPauseTable(){

        TextureRegion r = padi.assets.skin3.getRegion("SYMB_PAUSE");

        Image pauseImage = new Image(r);
        //pauseImage.setScale(0.5f, 0.5f);
        pauseButton = new ImageButton(padi.assets.skin2, "pause");
        pauseButton.add(pauseImage);
        pauseButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                System.out.println("CLICKED THE PAUSE BUTTON");
                PAUSED = true;
                game.multi.clear();
                game.multi.addProcessor(pauseStage);
            }
        });
        //pauseButton.setScale(0.2f, 0.2f);

        pauseButton.setPosition(20f, Gdx.graphics.getHeight() - 20f - pauseButton.getHeight());

        pauseTable = new Table();
        pauseStage = new Stage();

        final TextButton resume = new TextButton("Resume", padi.assets.skin2, "default");
        final TextButton quit = new TextButton("Quit", padi.assets.skin2, "default");
        final TextButton mute = new TextButton("Mute", padi.assets.skin2, "default");


        resume.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                PAUSED = false;
                game.multi.clear();
                game.multi.addProcessor(game.UI.getStage());
                game.multi.addProcessor(game.UI);
                game.multi.addProcessor(game);
            }
        });

        quit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){

                padi.setScreen(padi.worldmap);
            }
        });

        mute.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(padi.assets.getSoundLevel() == 0){
                    padi.assets.setSoundLevel(padi.assets.getOriginalSoundLevel());
                    mute.setText("Mute");
                }

                else{
                    padi.assets.setSoundLevel(0);
                    mute.setText("Un-Mute");
                }


            }
        });


        pauseTable.add(quit);
        pauseTable.add(resume);
        pauseTable.add(mute);
        pauseTable.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);

        pauseStage.addActor(pauseTable);
    }
    public void createChargeMeter(){
        loadingHidden = new Image(new Texture("progressbarempty.png"));
        loadingBar = new Image(new Texture("progressbar.png"));
        loadingFrame = new Image(new Texture("progressbarbackground.png"));

        loadingFrame.setSize(padi.assets.getScreenWidth()/3, padi.assets.getScreenHeight()/40);
        loadingHidden.setSize(loadingFrame.getWidth()-10f, loadingFrame.getHeight()-10f);

        //charging meter sizes and positions
        loadingFrame.setCenterPosition(Gdx.graphics.getWidth() / 2, loadingFrame.getHeight()+10f);
        loadingHidden.setCenterPosition(loadingFrame.getCenterX(), loadingFrame.getCenterY());
        loadingBar.setPosition(loadingHidden.getX(), loadingHidden.getY());
        loadingBar.setSize(0, 0);
    }
    public void createCountDownTable(){
        countDownTable = new Table();
        final TextButton startButton = new TextButton("START", padi.assets.skin2, "default");
        startButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                game.enemy.setCountDownTimer(0f);
                countDownTable.setVisible(false);
            }
        });
        final Label countDownMessage1 = new Label("Click the Start Button to start!", padi.assets.someUIskin);

        float temp = game.enemy.getCountDownTimer();
        countDownMessage2 = new Label("Time Left: " + String.valueOf((int)temp),
                padi.assets.someUIskin, "default");

        startButton.setSize(40f, 30f);
        countDownTable.add(startButton).row().pad(15f);
        countDownTable.add(countDownMessage1).row().pad(15f);
        countDownTable.add(countDownMessage2).row().pad(15f);


    }

    public void createOptionTable(){
        clickedOptionTable = new Table();
        charge = new TextButton("Charge", padi.assets.skin2, "default");
        upgrade = new TextButton("Upgrade", padi.assets.skin2, "default");
        final TextButton sell = new TextButton("Sell", padi.assets.skin2, "default");
        clickedOptionTable.add(charge).width(70f).height(40f).pad(5f);
        clickedOptionTable.add(upgrade).width(70f).height(40f).pad(5f);
        clickedOptionTable.add(sell).width(70f).height(40f).pad(5f);
        //clickedOptionTable.setSize(50f, 50f);
        clickedOptionTable.setVisible(false);
        //adding clicklisteners for option table.

        //'currentBS' points to the BuildableSpot that was clicked.
        charge.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y){

                b = !b;
                clickedOptionTable.setVisible(b);
                clickedTowerTable.setVisible(b);
                //emtyCurrentTower() returns true if
                //nothing is built on the buildablespot.
                if(!currentBS.emptyCurrentTower()) {

                    //getState() returns true if in shooting mode.
                    //changes the state, and the button message.
                    if (currentBS.getCurrentTower().getState()) {
                        currentBS.getCurrentTower().setState(false);
                        charge.setText(currentBS.getCurrentTower().getMessage());
                    } else {
                        currentBS.getCurrentTower().setState(true);
                        charge.setText(currentBS.getCurrentTower().getMessage());
                    }

                    clickedOptionTable.setVisible(false);
                    clickedTowerTable.setVisible(false);
                }
            }
        });

        //upgrades the tower, and keeps the old state of the tower.
        //Example: tower was charging, got upgraded, should still be charging.
        upgrade.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                b = !b;
                clickedOptionTable.setVisible(b);

                //If the button says "build" then we want it to
                //open up the option of choosing towers.
                if(String.valueOf(upgrade.getText()).equals("Build")){

                    clickedTowerTable.setVisible(true);
                    clickedOptionTable.setVisible(true);
                }

                else{
                    //stores old state of the tower: charge or shoot.
                    //default is shoot.
                    boolean oldState = true;

                    //if buildablespot has a tower built on it
                    //that tower's state is saved.
                    if(!currentBS.emptyCurrentTower()){
                        oldState = currentBS.getCurrentTower().getState();
                    }
                    game.spawn.upgradeTower(currentBS);
                    currentBS.getCurrentTower().setState(oldState);

                    clickedOptionTable.setVisible(false);
                }



            }//end click()
        });//end clicklistener

        sell.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                b = !b;
                clickedOptionTable.setVisible(false);
                clickedTowerTable.setVisible(false);
                game.tower.clearBuildable(currentBS);


            }
        });
    }

    public void createTowerTable(){
        //tower option table. it shows up with all the towers the user can make.
        clickedTowerTable = new Table();
        towerOptions = new Array<TextButton>();

        String[] names = {"strength", "laser", "aoe", "speed", "sniper", "rogue"};


        for(String s: names){
            TextButton t = new TextButton(s, padi.assets.skin2, "default");
            t.setSize(60f, 20f);
            t.setName(s);
            towerOptions.add(t);
        }

        //adding listeners to the towers.
        for(int x = 0; x < towerOptions.size; x++){
            final int xx = x;
            towerOptions.get(x).addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent e, float x, float y){
                    game.spawn.buildATower("build",currentBS, towerOptions.get(xx).getName().toUpperCase(), 1);
                }
            });
        }

        //creating the images and adding them to the table.
        //table should be 3 to a row.
        for(int x = 0; x < 6; x++) {

            if(x % 3 == 0 && x != 0) clickedTowerTable.row();

            clickedTowerTable.add(towerOptions.get(x)).width(70f).height(35f).pad(8f);
            clickedTowerTable.pad(5f);
        }



        //clickedTowerTable.setSize(100f, 100f);
        clickedTowerTable.setVisible(false);


        //adding listeners. hiding the tables.
        for(int x = 0; x < towerOptions.size; x++){
            towerOptions.get(x).addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent e, float x, float y){


                    clickedTowerTable.setVisible(false);
                    clickedOptionTable.setVisible(false);
                }
            });
        }
    }

    public void createEndGameTable(){

        endStage = new Stage();
        endGameTable = new Table();
        Label winMessage = new Label("You Won!", padi.assets.someUIskin, "default");
        Label loseMessage = new Label("You Lost!", padi.assets.someUIskin, "default");
        endGameTimeMessage = new Label("Time: ", padi.assets.someUIskin, "default");
        final TextButton returnButton = new TextButton(" World Map ", padi.assets.skin2, "default");
        final TextButton retryButton = new TextButton("Try Level Again", padi.assets.skin2, "default");
        returnButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                padi.setScreen(padi.worldmap);
                //game.dispose();
            }
        });

        retryButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                //game.padi.setScreen(new GameScreen(game.padi, game.whatLevel));
                game.reset();
            }
        });

        returnButton.setSize(40, 20f);
        retryButton.setSize(40, 20f);


        endGameTable.add(winMessage).row().pad(15f);
        //endGameTable.add(loseMessage).row().pad(15f);
        endGameTable.add(endGameTimeMessage).row().pad(15f);
        endGameTable.add(returnButton).padRight(30f);
        endGameTable.add(retryButton).row().pad(15f);

        endGameTable.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        //endGameTable.setVisible(true);
        endStage.addActor(endGameTable);
    }

    public void createDragTowers(){

        dragTowers = new Table();
        image = new Array<Image>();
        shapeRenderer = new ShapeRenderer();

        //Creating the images for the towers.
        final Image rogue = new Image(new Texture("icetower_small.png"));
        final Image speed = new Image(new Texture("strengthtower_small.png"));
        final Image laser = new Image(new Texture("roguetower_small.png"));
        final Image sniper = new Image(new Texture("ghosttower_small.png"));
        final Image strength = new Image(new Texture("speedtower_small.png"));
        final Image aoe = new Image(new Texture("aoetower_small.png"));

        //giving each image the appropriate names.
        rogue.setName("rogue");
        speed.setName("speed");
        laser.setName("laser");
        sniper.setName("sniper");
        strength.setName("strength");
        aoe.setName("aoe");

        //adding the images to the images array.
        image.add(aoe);
        image.add(speed);
        image.add(laser);
        image.add(sniper);
        image.add(rogue);
        image.add(aoe);

        dragTowers.clear();
        for(int w = 0; w < image.size; w++){
            if(w % 2 == 0 && w != 0)
                dragTowers.row();
            dragTowers.add(image.get(w)).width(50f).height(50f).pad(25f);


        }
        dragTowers.setOrigin(0,0);

        for(int s = 0; s < image.size; s++){
            final int ss = s;
            image.get(s).addListener(new DragListener(){
                @Override
                public void drag(InputEvent e, float x, float y, int pointer){
                    image.get(ss).setCenterPosition(image.get(ss).getX() + x, image.get(ss).getY() + y);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    shapeRenderer.setColor(1, 1, 1, 1);
                    shapeRenderer.circle(image.get(ss).getCenterX(),image.get(ss).getCenterY(), 200f);
                    shapeRenderer.end();
                }

                @Override
                public void dragStop(InputEvent e, float x, float z, int pointer){
                    Vector2 a = new Vector2(e.getStageX(), e.getStageY());//gets the coord with center at (0,0)

                    //if tower was dragged onto an empty BuildableSpot.
                    //passes in a rectangle of the image, and the image's name.
                    checkTheDrop(new Rectangle(a.x, a.y, image.get(ss).getWidth(), image.get(ss).getHeight()), image.get(ss).getName());

                    dragTowers.clear();
                    for(int w = 0; w < image.size; w++) {

                        if (w % 2 == 0 && w != 0) dragTowers.row();
                        dragTowers.add(image.get(w)).width(50f).height(50f).pad(30f);
                    }
                }
            });


        }
    }

    public void createMessageTable(){
        moneyMessage = new Label("$ ", padi.assets.someUIskin);
        enemyMessage = new Label("Enemies left: ", padi.assets.someUIskin);

        //making table for messages.
        messageTable = new Table();
        timeMessage = new Label("Total time: " + String.valueOf(TIMER), padi.assets.someUIskin);

        messageTable.setSize(200f, Gdx.graphics.getHeight());
        messageTable.setPosition(Gdx.graphics.getWidth() - 250f, 0);
        messageTable.add(enemyMessage).pad(20f).row();
        messageTable.add(moneyMessage).pad(20f).row();
        messageTable.add(timeMessage).row();
    }

    public void createMasterTable(){
        masterTable = new Table();
        masterTable.setSize(Gdx.graphics.getWidth()*5/16, Gdx.graphics.getHeight());
        masterTable.setPosition(Gdx.graphics.getWidth() - Gdx.graphics.getWidth()*5/16,0);



        //creating the background for  the table.
        TextureRegionDrawable background = new TextureRegionDrawable(
                new TextureRegion(new Texture("uitablebackground.png")));

        masterTable.setBackground(background);
        masterTable.add(countDownTable).row();
        masterTable.add(messageTable).padBottom(20f).row();
        masterTable.add(dragTowers);

    }

    @Override
    public boolean keyDown(int keycode) {return false;}

    @Override
    public boolean keyUp(int keycode) {return false;}

    @Override
    public boolean keyTyped(char character) {return false;}

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        clickedOptionTable.setVisible(false);
        clickedTowerTable.setVisible(false);
        return false;}

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        return false;}



    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {return false;}

    @Override
    public boolean scrolled(int amount) {return false;}


    public void dispose(){
        stage.dispose();
        endStage.dispose();
        masterTable.clearChildren();
        masterTable.clear();
        hideButton.clear();
        messageTable.clearChildren();
        messageTable.clear();
        moneyMessage.clear();
        enemyMessage.clear();
        timeMessage.clear();
        dragTowers.clearChildren();
        dragTowers.clear();



        loadingHidden.clear();
        loadingBar.clear();
        clickedOptionTable.clear();
        charge.clear();
        upgrade.clear();
        clickedTowerTable.clear();

        shapeRenderer.dispose();
        countDownTable.clear();
        countDownMessage2.clear();
        endGameTable.clear();
        endGameTimeMessage.clear();
        pauseButton.getImage().clear();
        pauseButton.clear();
        pauseTable.clear();
        pauseStage.dispose();

        for(int x = 0; x < towerOptions.size; x++)
            towerOptions.get(x).clear();

        for(int x = 0; x < image.size; x++)
            image.get(x).clear();
    }

    public void reset(){
        //nothing for now.
        countDownTable.setVisible(true);
        endGameTable.setVisible(false);
        clickedTowerTable.setVisible(false);
        clickedOptionTable.setVisible(false);
        masterTable.setVisible(true);
        TIMER = 0f;
        GAME_OVER = false;
        stopUpdatingChargeMeter = false;
        PAUSED = false;
        updateTimerMessage();

        loadingBar.setSize(0, loadingBar.getHeight());
        currentCharge = 0;
    }
}

/**http://stackoverflow.com/questions/18075414/getting-stage-coordinates-of-actor-in-table-in-libgdx
 *
 *
 *
 * */
