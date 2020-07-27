package com.nodomain.spaceinvaders;import android.graphics.RectF;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

import com.nodomain.spaceinvaders.R;

public class Invader
{

    RectF rect ;

    //random number gen
    Random rn = new Random() ;

    //invader bitmaps
    private Bitmap bitmap1 ;
    private Bitmap bitmap2 ;

    //height and length
    private float height ;
    private float length ;

    //far left
    private float x ;
    //top
    private float y ;

    //speed of the invader
    private float speed ;

    //movement direction
    private final int LEFT = 1 ;
    private final int RIGHT = 2 ;

    //directtion the ship is moving
    private int direction = RIGHT ;

    boolean invisible ;

    public Invader(Context context, int row, int column, int screenX, int screenY)
    {
        rect = new RectF() ;
        length = screenX / 20 ;
        height = screenY / 20 ;

        invisible = true ;

        int padding = screenX /25 ;

        x = column * (length + padding) ;
        y = row * (height + padding / 4) ;

        //init bitmaps
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1) ;
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2) ;

        //stretch to sizes
        bitmap1 = Bitmap.createScaledBitmap(bitmap1,
                (int) (length),
                (int) (height),
                false ) ;
        bitmap2 = Bitmap.createScaledBitmap(bitmap2,
                (int) (length),
                (int) (height),
                false) ;

        speed = 40 ;
    }
    //setters and getters
    public void setInvisible()
    {
        invisible = false ;
    }
    public boolean getVisibility()
    {
        return invisible ;
    }
    public RectF getRect()
    {
        return rect ;
    }

    public Bitmap getBitmap1()
    {
        return bitmap1;
    }

    public Bitmap getBitmap2()
    {
        return bitmap2;
    }
    public float getX ()
    {
        return x ;
    }
    public float getY()
    {
        return y;
    }
    public float getLength()
    {
        return length ;
    }

    public void update(long fps)
    {
        //movement
        if (direction == LEFT)
        {
            x = x - speed /fps ;
        }
        if (direction == RIGHT)
        {
            x = x + speed / fps ;
        }
        //collision rect
        rect.top = y ;
        rect. bottom = y + height ;
        rect.left = x ;
        rect.right = x + length ;
    }

    public void dropReverse()
    {
        if(direction == LEFT)
        {
            direction = RIGHT ;
        }
        if(direction == RIGHT)
        {
            direction = LEFT ;
        }

        y = y + height ;
        speed = speed * 1.10f ;
    }

    public boolean takeAim(float playerX, float playerLength)
    {
        int randomNumber = -1 ;

        //if near the player
        if((playerX + playerLength > x &&
                playerX + playerLength > x + length ) || (playerX > x && playerX < x + length))
        {

            //1 in 500 chance
            randomNumber = rn.nextInt(150);
            if(randomNumber == 0 )
            {
                return true ;
            }
        }
        //fire at random
        randomNumber = rn.nextInt(2000) ;
        if(randomNumber == 0)
        {
            return true ;
        }

        return false ;
    }
}
