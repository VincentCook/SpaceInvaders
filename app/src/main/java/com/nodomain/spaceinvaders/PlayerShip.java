package com.nodomain.spaceinvaders;import android.graphics.RectF;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.nodomain.spaceinvaders.R;

public class PlayerShip {

    RectF rect;
    //playership bitmap
    private Bitmap bitMap;
    //ships length and height
    private float length;
    private float height;

    //far left of the rectangle for ship
    private float x;
    //top coord
    private float y;
    //speed the ship moves
    private float shipSpeed;
    //ways the ship can move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;
    //is the ship moving and where
    private int moving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY)
    //player ship constructor
    {

        rect = new RectF();

        length = screenX / 10;
        height = screenY / 10;

        //start the ship around the centre
        x = screenX / 2;
        y = screenY - 20;

        //init bitmap
        bitMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitMap = Bitmap.createScaledBitmap(bitMap,
                (int) (length),
                (int) (height),
                false);
        //set the ships speed
        shipSpeed = 350;

    }

    public RectF getRect()
    {

        return rect;

    }

    //get methods
    public Bitmap getBitmap()
    {
        return bitMap ;
    }
    public float getX()
    {
        return x ;
    }
    public float getLength()
    {
        return length ;
    }

    //method to set the state for movement
    public void setMovementState(int state)
    {
        moving = state ;
    }

    public void update(long fps)
    {
        if (moving == LEFT)
        {
            x = x - shipSpeed / fps ;
        }
        if(moving == RIGHT)
        {
            x = x + shipSpeed / fps ;
        }
        //update the collision rectangle
        rect.top = y ;
        rect.bottom = y + height ;
        rect.left = x ;
        rect.right = x + length ;
    }
}