package eina.imagine;

import android.view.View;

public class CardContainer {

    private View view;
    int positionWithinViewGroup = -1;
    int positionWithinAdapter = -1;
    private SwipeListener swipeListener;
    private SwipeCallback callback;
    private SwipeDeck parent;
    private long id;

    public CardContainer(View view, SwipeDeck parent, SwipeCallback callback) {
        this.view = view;
        this.parent = parent;
        this.callback = callback;

        setupSwipeListener();
    }

    public void setPositionWithinViewGroup(int pos) {
        this.positionWithinViewGroup = pos;
    }

    public int getPositionWithinViewGroup() {
        return positionWithinViewGroup;
    }

    public View getCard() {
        return this.view;
    }

    public void cleanupAndRemoveView() {
        //wait for card to render off screen, do cleanup and remove from viewgroup
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                deleteViewFromSwipeDeck();
            }
        }, SwipeDeck.ANIMATION_DURATION);
    }

    private void deleteViewFromSwipeDeck() {
        parent.removeView(view);
        parent.removeFromBuffer(this);
    }

    public void setSwipeEnabled(boolean enabled) {
        //also checks in case user doesn't want to be able to swipe the card freely
        if (enabled && parent.SWIPE_ENABLED) {
            view.setOnTouchListener(swipeListener);
        } else {
            view.setOnTouchListener(null);
        }
    }

    public SwipeListener getSwipeListener() {
        return swipeListener;
    }

    public void setLeftImageResource(int leftImageResource) {
        View left = view.findViewById(leftImageResource);
        left.setAlpha(0);
        swipeListener.setLeftView(left);

    }

    public void setRightImageResource(int rightImageResource) {
        View right = view.findViewById(rightImageResource);
        right.setAlpha(0);
        swipeListener.setRightView(right);
    }

    public void setupSwipeListener() {
        this.swipeListener = new SwipeListener(view, callback, parent.getPaddingLeft(), parent.getPaddingTop(), parent.ROTATION_DEGREES, parent.OPACITY_END, parent);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPositionWithinAdapter(int position){
        this.positionWithinAdapter = position;
    }

    public int getPositionWithinAdapter(){
        return positionWithinAdapter;
    }
}
