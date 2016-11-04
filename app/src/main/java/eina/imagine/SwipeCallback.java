package eina.imagine;

import android.view.View;

public interface SwipeCallback {
    void cardSwipedLeft(View card);
    void cardSwipedRight(View card);
    void cardOffScreen(View card);
    void cardActionDown();
    void cardActionUp();
}
