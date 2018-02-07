package ro.duoline.cateringsettings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 07/02/2018.
 */

public class HeaderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<HeaderRecyclerViewItems> itemObjects;
    private Context context;


    public HeaderRecyclerViewAdapter(Context context, List<HeaderRecyclerViewItems> itemObjects){
        this.itemObjects = itemObjects;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
            return new HeaderViewHolder(layoutView);
        } else if (viewType == TYPE_ITEM){
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(layoutView);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderRecyclerViewItems mObject = itemObjects.get(position);
        if(holder instanceof HeaderViewHolder){
            ((HeaderViewHolder) holder).headerTitle.setText(mObject.getHeader());
        } else if (holder instanceof ItemViewHolder){
            ((ItemViewHolder) holder).denumireRestaurant.setText(mObject.getDenumireRestaurant());
            Picasso.with(context).load(mObject.getPoza()).into(((ItemViewHolder) holder).pozaRestaurant);//.error(R.drawable.eating)

        }
    }

    @Override
    public int getItemCount() {
        return itemObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position)) return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position)
    {
        return position == 0;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        public TextView headerTitle;

        public HeaderViewHolder(View itemView){
            super(itemView);
            headerTitle = (TextView)itemView.findViewById(R.id.header_id);
            Typeface face= Typeface.createFromAsset(itemView.getContext().getAssets(), "font/PoiretOne-Regular.ttf");
            headerTitle.setTypeface(face);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        public TextView denumireRestaurant;

        public ImageView pozaRestaurant;


        public ItemViewHolder(View itemView){
            super(itemView);
            denumireRestaurant = (TextView) itemView.findViewById(R.id.denumire_restaurant);

            pozaRestaurant = (ImageView) itemView.findViewById(R.id.poza_restaurant);
            pozaRestaurant.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int restaurantID = itemObjects.get(getAdapterPosition()).getRestaurantId();
                    String denumireRestaurant = itemObjects.get(getAdapterPosition()).getDenumireRestaurant();
                    ArrayList<String> al = itemObjects.get(getAdapterPosition()).getDateConectare();
                    //String livrare = itemObjects.get(getAdapterPosition()).getLocatiiLivrare();
                    Intent i = new Intent(context.getApplicationContext(), FeluriMeniu_Activity.class);
                    // i.putExtra("EXTRA_TEXT", categorie);
                    // i.putStringArrayListExtra("listaDdenumiri", listaDenumiri);
                    i.putExtra("DENUMIRE", denumireRestaurant);
                    i.putExtra("ID", restaurantID);
                    //i.putExtra("LIVRARE", livrare);
                    i.putStringArrayListExtra("dateConectare", al);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(i);
                }
            });


        }
    }
}