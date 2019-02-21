package sarath.com.cantread;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

    private Context mContext;
    private GestureDetector gestureDetector;

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 150;

    SwipeDetector(Context mContext) {
        this.mContext = mContext.getApplicationContext();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {

        // Check movement along the Y-axis. If it exceeds SWIPE_MAX_OFF_PATH,
        // then dismiss the swipe.
        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
        {
            return false;
        }

        // From left to right
        if( e2.getX() > e1.getX() )
        {
            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                return true;
            }
        }

        // From right to left
        if( e1.getX() > e2.getX() )
        {
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                Intent intent = new Intent(mContext, RecogniserActivity.class);
                mContext.startActivity(intent);
                return true;
            }
        }

        return false;
    }
}
