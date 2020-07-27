package com.nodomain.spaceinvaders;

import android.graphics.RectF;
import android.content.res.AssetFileDescriptor ;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.media.SoundPool ;
import android.media.AudioManager ;
import java.io.IOException ;
import android.content.res.AssetManager ;

public class Bullet
{

    float x ;
    float y ;

    RectF rect ;
    //bullet shooting up or down
    public final int UP = 0 ;
    public final int DOWN = 1 ;

    int heading  = -1 ;
    float speed = 350 ;

    private int width = 1 ;
    private int height ;

    private boolean active;

    public Bullet(int screenY)
            //construct
    {

        height = screenY /20 ;
        active = false ;

        rect = new RectF() ;

    }

    //getters
    public RectF getRect()
    {
        return rect ;
    }
    public Boolean getStatus()
    {
        return active ;
    }
    public void setInactive()
    {
        active = false;
    }
    public float getImpactPointY()
    {
        if(heading == DOWN)
        {
            return y + height ;
        }
        else
        {
            return y ;
        }
    }

    public Boolean shoot(float startX, float startY, int direction)
    {

        if(!active)
        {
            x = startX ;
            y = startY ;
            heading = direction ;
            active = true ;
            return true ;
        }

        //is already active
        return false ;
    }

    public void update(long fps)
    {
        //move up or down
        if(heading == UP)
        {
            y = y - speed/fps ;
        }
        if(heading == DOWN)
        {
            y = y + speed/fps ;
        }

        //update the collision rect
        rect.left = x ;
        rect.right = x + width ;
        rect.top = y ;
        rect.bottom = y + height ;
    }
public class SpaceInvadersView extends android.view.SurfaceView implements Runnable
{
    Context context ;

    //game thread
    private Thread gameThread = null ;
    //surfaceholder to lock the surface before drawing
    private SurfaceHolder holder ;
    //boolean to check if the game is running
    private volatile boolean running ;
    //check if the game is paused
    private boolean paused = true ;
    //canvas and paint objects
    private Canvas canvas ;
    private Paint paint ;
    //storage for the fps
    private long fps ;
    //store the frametime
    private long frameTime ;
    //size fo the screen in pixels
    private int screenX ;
    private int screenY ;
    // the players ship
    private PlayerShip playerShip ;
    //player bullet
    private com.nodomain.spaceinvaders.Bullet bullet ;
    //invader bullets
    private Bullet[] invadersBullets = new Bullet[200] ;
    private int nextBullet ;
    private int maxInvaderBullets = 10 ;
    //upto 60 invaders
    Invader[] invaders = new Invader[60] ;
    int numInvaders = 0 ;
    //bricks the shelters built from
    private DefenceBrick[] bricks = new DefenceBrick[400] ;
    private int numBricks ;
    //soundFX
    private SoundPool soundPool ;
    private int playerExplodeId = -1 ;
    private int invaderExplodeId = -1 ;
    private int shootId = -1 ;
    private int damageShelterId = -1 ;
    private int uhId = -1 ;
    private int ohId = -1 ;
    //store the players score
    int score = 0 ;
    //players lives
    int lives = 3 ;
    //how menacing the sound will be (get worse as game speeds up)
    private int menaceInterval = 1000 ;
    //boolean to see which sound to play
    private boolean uhoh ;
    //time the last sound played
    private long lastSoundTime = System.currentTimeMillis() ;


    public SpaceInvadersView(Context context, int x, int y)
    {

        super(context) ;

        //make a copy of context avaliable for other methods
        this.context = context ;

        //initialize holder and paint
        holder = getHolder() ;
        paint = new Paint() ;

        screenX = x ;
        screenY = y ;

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0) ;

        try
        {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootId = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeId = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterId = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeId = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhId = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohId = soundPool.load(descriptor, 0);

        }
        catch(IOException e){
            // print error
            Log.e("error", "failed to load sound files");
        }
        prepareLevel() ;
    }
    private void prepareLevel()
    {

        //initialize game objects

        //make new ship
        playerShip = new PlayerShip(context, screenX, screenY) ;

        //prepare bullet
        bullet = new Bullet(screenY) ;
        //initialize invader bullet array
        for(int i = 0 ; i < invadersBullets.length; i++)
        {
            invadersBullets[i] = new Bullet(screenY) ;
        }
        //build invaders
        numInvaders = 0 ;
        for(int column = 0 ; column < 6 ; column++)
        {
            for(int row = 0 ; row < 5 ; row++)
            {
                invaders[numInvaders]= new Invader(context, row, column, screenX, screenY) ;
                numInvaders++ ;
            }
        }
        //build shelters
        numBricks = 0 ;
        for(int shelterNumber = 0 ; shelterNumber < 4 ; shelterNumber++)
        {
            for(int column = 0 ; column < 10 ; column++)
            {
                for(int row = 0 ; row < 5 ; row++)
                {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY) ;
                    numBricks++ ;
                }
            }
        }

        //reset menace level
        menaceInterval = 1000 ;

    }

    @Override
    public void run()
    {

        while(running) {
            //get the start time
            long startFrameTime = System.currentTimeMillis();

            //update the frame
            if (!paused)
            //check if the game is paused before running update
            {

                update();

            }

            //draw frame
            draw();

            //calculate fps
            frameTime = System.currentTimeMillis() - startFrameTime;
            if (frameTime >= 1) {

                fps = 1000 / frameTime;

            }
            //activate sound and animation of invaders
            if(!paused)
            {
                if((startFrameTime - lastSoundTime) > menaceInterval)
                {
                    if(uhoh)
                    {
                        soundPool.play(uhId,1,1,0,0,1) ;
                    }
                    else
                    {
                        soundPool.play(ohId,1,1,0,0,1) ;
                    }
                    //reset sounds time
                    lastSoundTime = System.currentTimeMillis() ;
                    //swap values
                    uhoh = !uhoh ;
                }
            }
        }
    }

    public void update()
    {

        //boolean to check if invader hit screen
        boolean bumped = false ;
        //boolean for checking if the player loses
        boolean lost = false ;

        // Move the player's ship
        playerShip.update(fps);
        // Update the invaders if visible
        for(int i = 0  ; i < numInvaders ; i++)
        {

            if(invaders[i].getVisibility())
            {
                //move the next invader
                invaders[i].update(fps);

                //check if shoots
                if(invaders[i].takeAim(playerShip.getX(),playerShip.getLength()))
                {
                    //if so try spawn bullet
                    if(invadersBullets[nextBullet].shoot(invaders[i].getLength() /2,
                    invaders[i].getY(),bullet.DOWN))
                    {
                        nextBullet++ ;

                        if(nextBullet == maxInvaderBullets)
                        {
                            nextBullet = 0 ;
                        }
                    }
                }
                if(invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0 )
                {
                    bumped = true ;
                }
            }

        }

        // Update all the invaders bullets if active
        for(int i = 0 ; i < invadersBullets.length; i++)
        {
            if(invadersBullets[i].getStatus())
            {
                invadersBullets[i].update(fps);
            }
        }
        // Did an invader bump into the edge of the screen
        if(bumped)
        {
            //move down and change direction
            for(int i = 0 ; i < numInvaders ; i++)
            {
                invaders[i].dropReverse();
                //have invaders landed
                if (invaders[i].getY() > screenY - screenY / 10)
                {
                    lost = true ;
                }
            }
            menaceInterval = menaceInterval - 80 ;
        }

        if(lost)
        {

            prepareLevel();

        }

        // Update the players bullet
        if(bullet.getStatus())
        {
            bullet.update(fps);
        }
        // Has the player's bullet hit the top of the screen
        if(bullet.getImpactPointY() < 0 )
        {

            bullet.setInactive() ;

        }

        // Has an invaders bullet hit the bottom of the screen

        for ( int i = 0 ; i < invadersBullets.length ; i++)
        {

            if(invadersBullets[i].getImpactPointY() > screenY)
            {

                invadersBullets[i].setInactive() ;

            }

        }

        // Has the player's bullet hit an invader
        if(bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeId, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }

            // Has an alien bullet hit a shelter brick
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                for(int j = 0; j < numBricks; j++){
                    if(bricks[j].getVisibility()){
                        if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())){
                            // A collision has occurred
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterId, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }
            // Has a player bullet hit a shelter brick
        if(bullet.getStatus()){
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bullet.getRect(), bricks[i].getRect())){
                        // A collision has occurred
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterId, 1, 1, 0, 0, 1);
                    }
                }
            }
        }
        // Has an invader bullet hit the player ship
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())){
                    invadersBullets[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeId, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if(lives == 0){
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }

    }

    public void draw()
    {

        if(holder.getSurface().isValid())
        {
            //lock canvas
            canvas = holder.lockCanvas() ;
            //draw background
            canvas.drawColor(Color.argb(255,0,0,0));
            //set the brush color
            paint.setColor(Color.argb(255,255,255,255));

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(),playerShip.getX(),screenY - 50, paint);
            // Draw the invaders
            for(int i = 0 ; i < numInvaders ; i ++)
            {
                if(invaders[i].getVisibility())
                {
                    if(uhoh)
                    {
                        canvas.drawBitmap(invaders[i].getBitmap1(),
                                invaders[i].getX(),
                                invaders[i].getY(),
                                paint);
                    }
                    else
                    {
                        canvas.drawBitmap(invaders[i].getBitmap2(),
                                invaders[i].getX(),
                                invaders[i].getY(),
                                paint) ;
                    }
                }
            }
            // Draw the bricks if visible
            for(int i = 0 ; i < numBricks ; i++)
            {

            if(bricks[i].getVisibility())
            {

                canvas.drawRect(bricks[i].getRect(), paint) ;

            }

            }
            // Draw the players bullet if active
            if(bullet.getStatus())
            {
                canvas.drawRect(bullet.getRect(), paint);
            }

            // Draw the invaders bullets if active
            for (int i = 0 ; i < invadersBullets.length; i++)
            {
                if(invadersBullets[i].getStatus())
                {
                    canvas.drawRect(invadersBullets[i].getRect(), paint) ;
                }
            }
            //draw score and lives
            paint.setColor(Color.argb(255,249,129,0));
            canvas.drawText("score: " + score + "Lives: " + lives, 10, 50, paint);

            //draw everything to canvas
            holder.unlockCanvasAndPost(canvas);

        }

    }

    public void pause()
    {

        running = false ;

        try
        {

            gameThread.join() ;

        }
        catch(InterruptedException e)
        {

            Log.e("Error","Joining thread") ;

        }
    }

    public void resume()
    {

        running = true ;
        gameThread = new Thread(this) ;
        gameThread.start() ;

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {

        switch(motionEvent.getAction() & motionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_DOWN :
                paused = false ;
                if(motionEvent.getY() > screenY - screenY / 8 )
                {
                    if(motionEvent.getX() > screenX / 2 )
                    {
                        playerShip.setMovementState(playerShip.RIGHT);
                    }
                    else
                    {
                        playerShip.setMovementState(playerShip.LEFT);
                    }
                }
                if(motionEvent.getY() < screenY - screenY / 8)
                {
                    //fire shot
                    if(bullet.shoot(playerShip.getX() +
                            playerShip.getLength() / 2, screenY, bullet.UP))
                    {
                        soundPool.play(shootId,1,1,0,0,1) ;
                    }
                }
                break ;
                //finger lifted
            case MotionEvent.ACTION_UP :

                if(motionEvent.getY() > screenY - screenY / 10 )
                {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break ;

        }
        return true ;
    }
}}
