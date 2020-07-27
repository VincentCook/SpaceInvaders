package com.nodomain.spaceinvaders;


import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SpaceInvadersActivity extends Activity
    //main class for the game
{


    //declare the game view
    SpaceInvadersView spaceInvadersView ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //get display object
        Display display = getWindowManager().getDefaultDisplay() ;
        //load resolution to point
        Point size = new Point() ;
        display.getSize(size);

        //initialize the view and set
        spaceInvadersView = new SpaceInvadersView(this, size.x,size.y);
        setContentView(spaceInvadersView);
    }

    @Override
    protected void onResume()
    {

        super.onResume();

        spaceInvadersView.resume() ;

    }

    @Override
    protected void onPause()
    {

        super.onPause();

        spaceInvadersView.pause() ;

    }
}