package eina.imagine;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SwipeAdapter extends BaseAdapter {

    private List<State> data;
    private Context context;

    public SwipeAdapter(List<State> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            // normally use a viewholder
            v = inflater.inflate(R.layout.tarjeta, parent, false);
        }

        State item = (State)getItem(position);
        ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
        int id = v.getResources().getIdentifier("eina.imagine:drawable/" + item.getPathImage(), null, null);
        Picasso.with(context).load(id).fit().centerCrop().into(imageView);
        TextView textView = (TextView) v.findViewById(R.id.sample_text);
        textView.setText(item.getLine());

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                Log.i("Hardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
            }
        });
        return v;
    }

    public void addCard(State card) {
        data.add(card);
    }
}
