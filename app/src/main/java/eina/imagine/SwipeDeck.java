package eina.imagine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class SwipeDeck extends FrameLayout {

    private static final String TAG = "SwipeDeck MainActivity";
    private int NUMBER_OF_SIMULTANEOUS_CARDS;
    public float OPACITY_END;
    public float ROTATION_DEGREES;
    private float CARD_SPACING;
    public static int ANIMATION_DURATION = 200;
    public boolean RENDER_ABOVE;
    public boolean SWIPE_ENABLED;
    private boolean mHasStableIds;

    private Adapter mAdapter;
    private DataSetObserver observer;
    private Deck<CardContainer> deck;
    private SwipeDeckCallback callback;
    private ArrayList<CardContainer> buffer = new ArrayList<>();

    private int leftImageResource;
    private int rightImageResource;

    private int adapterIndex = 0;

    public SwipeDeck(final Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SwipeDeck2, 0, 0);
        NUMBER_OF_SIMULTANEOUS_CARDS = 1;
        OPACITY_END = a.getFloat(R.styleable.SwipeDeck2_opacity_end, 0.33f);
        ROTATION_DEGREES = a.getFloat(R.styleable.SwipeDeck2_rotation_degrees, 15f);
        CARD_SPACING = a.getDimension(R.styleable.SwipeDeck2_card_spacing, 15f);
        RENDER_ABOVE = a.getBoolean(R.styleable.SwipeDeck2_render_above, true);
        SWIPE_ENABLED = a.getBoolean(R.styleable.SwipeDeck2_swipe_enabled, true);

        deck = new Deck<>(new Deck.DeckEventListener() {

            @Override
            public void itemAddedFront(Object item) {
                deck.getFront().setSwipeEnabled(true);
                if (deck.size() > NUMBER_OF_SIMULTANEOUS_CARDS) {
                    deck.removeBack();
                    adapterIndex--;
                }
                renderDeck();
            }

            @Override
            public void itemAddedBack(Object item) {
                deck.getFront().setSwipeEnabled(true);
                renderDeck();
            }

            @Override
            public void itemRemovedFront(Object item) {
                CardContainer container = (CardContainer) item;
                buffer.add(container);
                //enable swipe in the next cardContainer
                if (deck.size() > 0) {
                    deck.getFront().setSwipeEnabled(true);
                }
                container.cleanupAndRemoveView();
                //pull in the next view (if available)
                addNextView();
                renderDeck();
            }

            @Override
            public void itemRemovedBack(Object item) {
                ((CardContainer) item).getCard().animate().setDuration(100).alpha(0);
            }
        });

        //set clipping of view parent to false so cards render outside their view boundary
        //make sure not to clip to padding
        setClipToPadding(false);
        setClipChildren(false);
        this.setWillNotDraw(false);

        //if render above is set make sure everything in this view renders above other views
        //outside of it.
        if (RENDER_ABOVE) {
            ViewCompat.setTranslationZ(this, Float.MAX_VALUE);
        }//todo: make an else here possibly

    }

    public void setAdapter(final Adapter adapter) {

        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(observer);
        }
        mHasStableIds = adapter.hasStableIds();
        mAdapter = adapter;
        observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //handle data set changes
                //if we need to add any cards at this point (ie. the amount of cards in the deck
                //is less than the max number of cards to display) add the cards.

                int deckSize = deck.size();
                //only perform action if there are less cards on screen than NUMBER_OF_CARDS
                if (deckSize < NUMBER_OF_SIMULTANEOUS_CARDS) {
                    for (int i = deckSize; i < NUMBER_OF_SIMULTANEOUS_CARDS; ++i) {
                        addNextView();
                    }
                }
                //if the adapter has been emptied empty the view and reset adapterIndex
                if (adapter.getCount() == 0) {
                    deck.clear();
                    adapterIndex = 0;
                }
            }

            @Override
            public void onInvalidated() {
                //reset state, remove views and request layout
                //nextAdapterCard = 0;
                deck.clear();
                removeAllViews();
                requestLayout();
            }
        };
        adapter.registerDataSetObserver(observer);
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // if we don't have an adapter, we don't need to do anything
        if (mAdapter == null || mAdapter.getCount() == 0) {
            //nextAdapterCard = 0;
            removeAllViewsInLayout();
            return;
        }
        //pull in views from the adapter at the position the top of the deck is set to
        //stop when you get to for cards or the end of the adapter
        int deckSize = deck.size();
        for (int i = deckSize; i < NUMBER_OF_SIMULTANEOUS_CARDS; ++i) {
            addNextView();
        }
    }

    private void addNextView() {
        if (adapterIndex < mAdapter.getCount()) {
            View newBottomChild = mAdapter.getView(adapterIndex, null, this);
            newBottomChild.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            //todo: i'm setting the card to invisible initially and making it visible when i animate
            //later
            newBottomChild.setAlpha(0);
            newBottomChild.setY(getPaddingTop());
            final long viewId = mAdapter.getItemId(adapterIndex);

            CardContainer card = new CardContainer(newBottomChild, this, new SwipeCallback() {
                @Override
                public void cardSwipedLeft(View card) {
                    Log.d(TAG, "card swiped left");
                    if (!(deck.getFront().getCard() == card)) {
                        Log.e("SWIPE ERROR: ", "card on top of deck not equal to card swiped");
                    }
                    deck.removeFront();
                    if (callback != null) {
                        callback.cardSwipedLeft(viewId);
                    }
                }

                @Override
                public void cardSwipedRight(View card) {
                    Log.d(TAG, "card swiped right");
                    if (!(deck.getFront().getCard() == card)) {
                        Log.e("SWIPE ERROR: ", "card on top of deck not equal to card swiped");
                    }
                    deck.removeFront();
                    if (callback != null) {
                        callback.cardSwipedRight(viewId);
                    }
                }

                @Override
                public void cardOffScreen(View card) {

                }

                @Override
                public void cardActionDown() {

                }

                @Override
                public void cardActionUp() {

                }
            });

            card.setPositionWithinAdapter(adapterIndex);

            if (leftImageResource != 0) {
                card.setLeftImageResource(leftImageResource);
            }
            if (rightImageResource != 0) {
                card.setRightImageResource(rightImageResource);
            }

            card.setId(viewId);

            deck.addBack(card);
            adapterIndex++;
        }
    }


    private void addLastView() {
        //get the position of the card prior to the card atop the deck
        int positionOfLastCard;

        //if there's a card on the deck get the card before it, otherwise the last card is one
        //before the adapter index.
        if (deck.size() > 0) {
            positionOfLastCard = deck.getFront().getPositionWithinAdapter() - 1;
        } else {
            positionOfLastCard = adapterIndex - 1;
        }
        if (positionOfLastCard >= 0) {
            View newBottomChild = mAdapter.getView(positionOfLastCard, null, this);
            newBottomChild.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            //todo: i'm setting the card to invisible initially and making it visible when i animate
            //later
            newBottomChild.setAlpha(0);
            newBottomChild.setY(getPaddingTop());

            final long viewId = mAdapter.getItemId(positionOfLastCard);

            CardContainer card = new CardContainer(newBottomChild, this, new SwipeCallback() {
                @Override
                public void cardSwipedLeft(View card) {
                    Log.d(TAG, "card swiped left");
                    if (!(deck.getFront().getCard() == card)) {
                        Log.e("SWIPE ERROR: ", "card on top of deck not equal to card swiped");
                    }
                    deck.removeFront();
                    if (callback != null) {
                        callback.cardSwipedLeft(viewId);
                    }
                }

                @Override
                public void cardSwipedRight(View card) {
                    Log.d(TAG, "card swiped right");
                    if (!(deck.getFront().getCard() == card)) {
                        Log.e("SWIPE ERROR: ", "card on top of deck not equal to card swiped");
                    }
                    deck.removeFront();
                    if (callback != null) {
                        callback.cardSwipedRight(viewId);
                    }
                }

                @Override
                public void cardOffScreen(View card) {

                }

                @Override
                public void cardActionDown() {

                }

                @Override
                public void cardActionUp() {

                }
            });

            if (leftImageResource != 0) {
                card.setLeftImageResource(leftImageResource);
            }
            if (rightImageResource != 0) {
                card.setRightImageResource(rightImageResource);
            }

            card.setId(viewId);

            deck.addFront(card);
            card.setPositionWithinAdapter(positionOfLastCard);
        }
    }

    private void renderDeck() {
        //we remove all the views and re add them so that the Z translation is correct
        removeAllViews();
        for (int i = deck.size() - 1; i >= 0; --i) {
            CardContainer container = deck.get(i);
            View card = container.getCard();
            ViewGroup.LayoutParams params = card.getLayoutParams();

            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }

            addViewInLayout(card, -1, params, true);
            int itemWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
            int itemHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
            card.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.EXACTLY | itemHeight);
        }
        //if there's still a card animating in the buffer, make sure it's re added after removing all views
        if (buffer != null) {
            for (CardContainer c : buffer) {
                View card = c.getCard();
                ViewGroup.LayoutParams params = card.getLayoutParams();

                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                }
                addViewInLayout(card, -1, params, true);
                int itemWidth = getWidth() - (getPaddingLeft() + getPaddingRight());
                int itemHeight = getHeight() - (getPaddingTop() + getPaddingBottom());
                card.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.EXACTLY | itemHeight);
            }
        }
        positionCards();
    }

    private void positionCards() {

        setZTranslations();
        for (int i = 0; i < deck.size(); ++i) {
            View card = deck.get(i).getCard();
            float offset = (int) (deck.get(i).getPositionWithinViewGroup() * CARD_SPACING);
            card.animate()
                    .setDuration(ANIMATION_DURATION)
                    .y(getPaddingTop() + offset)
                    .alpha(1.0f);
        }
    }

    public void setCallback(SwipeDeckCallback callback) {
        this.callback = callback;
    }

    public void swipeTopCardLeft(int duration) {
        if (deck.size() > 0) {
            deck.get(0).getSwipeListener().swipeCardLeft(duration);
            if (callback != null) {
                callback.cardSwipedLeft(deck.get(0).getId());
            }
            deck.removeFront();
        }


    }

    public void swipeTopCardRight(int duration) {
        if (deck.size() > 0) {
            deck.get(0).getSwipeListener().swipeCardRight(duration);
            if (callback != null) {
                callback.cardSwipedRight(deck.get(0).getId());
            }
            deck.removeFront();
        }
    }

    public void unSwipeCard() {
        addLastView();
    }

    public void setAdapterIndex(int index) {
        this.adapterIndex = index;
    }

    public int getAdapterIndex() {
        return this.adapterIndex;
    }

    public void removeFromBuffer(CardContainer container) {
        this.buffer.remove(container);
    }

    public void setLeftImage(int imageResource) {
        leftImageResource = imageResource;
    }

    public void setRightImage(int imageResource) {
        rightImageResource = imageResource;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setZTranslations() {
        //this is only needed to add shadows to cardviews on > lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int count = getChildCount();
            for (int i = 0; i < count; ++i) {
                getChildAt(i).setTranslationZ(i * 10);
            }
        }
    }

    public interface SwipeDeckCallback {
        void cardSwipedLeft(long stableId);

        void cardSwipedRight(long stableId);
    }
}

