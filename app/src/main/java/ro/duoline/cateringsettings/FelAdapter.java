package ro.duoline.cateringsettings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Paul on 07/02/2018.
 */

public class FelAdapter extends RecyclerView.Adapter<FelAdapter.ViewHolder> {
    public ArrayList<FelMeniuValues> felMeniu;
    private ArrayList<FelMeniuValues> felMeniuFitrata;
    private Context context;
    //private TextView total;

    private TextView categorieCurenta, numarProduse;
    private AppCompatActivity mInstance;
    private Boolean viewCod = false;
    public ArrayList<String> al;

    public FelAdapter(Context context,  TextView categorie, TextView numarProduse, ArrayList<FelMeniuValues> felMeniu, AppCompatActivity parentActivity, ArrayList<String> dateconectare){
        this.felMeniu = new ArrayList<FelMeniuValues>();
        this.felMeniuFitrata = new ArrayList<FelMeniuValues>();
        this.felMeniu.addAll(felMeniu);
        this.context = context;
        this.felMeniuFitrata.addAll(this.felMeniu); //copiez lista originala
        //this.total = total;
        this.categorieCurenta = categorie;
        this.numarProduse = numarProduse;
        this.mInstance = parentActivity;
        this.al = dateconectare;
    }

    @Override
    public FelAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_card_produse, viewGroup, false);
        return new FelAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FelAdapter.ViewHolder viewHolder, int pos) {

        viewHolder.denumire_produs.setText(felMeniuFitrata.get(pos).getDenumire());
        viewHolder.descriere_produs.setText(felMeniuFitrata.get(pos).getDescriere());
        viewHolder.pret.setText("Pret: " + felMeniuFitrata.get(pos).getPret().toString() + " RON");
        String temp = "x " + felMeniuFitrata.get(pos).getBucComandate().toString();
        viewHolder.comandaCurenta.setText(temp);


        if(felMeniuFitrata.get(pos).getBucComandate() > 0){
            viewHolder.comandaCurenta.setVisibility(View.VISIBLE);
        } else {
            viewHolder.comandaCurenta.setVisibility(View.INVISIBLE);
        }
        if(felMeniuFitrata.get(pos).getPozaURL().equals("")) {
            viewHolder.img_produs.setImageResource(R.drawable.no_photo);
        }else{
            Picasso.with(context).load(felMeniuFitrata.get(pos).getPozaURL()).error(R.drawable.no_photo).into(viewHolder.img_produs);
        }



       // total.setText("TOTAL: " + viewHolder.calculTotal() + " RON");
    }

    @Override
    public int getItemCount() {
        return (null != felMeniuFitrata ? felMeniuFitrata.size() : 0);
    }



    public void filter(final String categorieAfisata){

        felMeniuFitrata.clear(); // golim lista
        if(TextUtils.isEmpty(categorieAfisata)){ //Daca categoria nu e specificata se restaureaza lista originala
            felMeniuFitrata.addAll(felMeniu);
            categorieCurenta.setText("Toate");
        } else {
            //se itereaza lista originala si se adauga la lista filtrata
            for(int i=0; i<felMeniu.size(); i++){
                if(felMeniu.get(i).getCategorie().equals(categorieAfisata)){
                    felMeniuFitrata.add(felMeniu.get(i));
                }
            }
            categorieCurenta.setText(categorieAfisata);
            numarProduse.setText(Integer.toString(felMeniuFitrata.size()) + " produse");
        }


        notifyDataSetChanged();
    }

    public ArrayList<FelMeniuValues> getVector(){return felMeniu;}

    public void setVector(ArrayList<FelMeniuValues> vec){
        felMeniu = vec;
        filter(categorieCurenta.getText().toString());
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView denumire_produs;
        private TextView descriere_produs;
        private TextView pret;
        private TextView comandaCurenta;
        private ImageView img_produs;
        private Button addBtn;
        public ViewHolder(View view) {
            super(view);

            denumire_produs = (TextView)view.findViewById(R.id.denumire);
            descriere_produs = (TextView) view.findViewById(R.id.descriere_produs);
            pret = (TextView) view.findViewById(R.id.pret);
            addBtn = (Button) view.findViewById(R.id.addImage);
            comandaCurenta = (TextView) view.findViewById(R.id.comanda_curenta);
            img_produs = (ImageView) view.findViewById(R.id.poza);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    Intent i = new Intent(context.getApplicationContext(), UploadActivity.class);

                    i.putStringArrayListExtra("dateconectare", al);
                    i.putExtra("cod", felMeniuFitrata.get(getAdapterPosition()).getCod());
                    i.putExtra("denumire", felMeniuFitrata.get(getAdapterPosition()).getDenumire());
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(i);
                }
            });

        }




        public float calculTotal() {
            float res = 0;
            for(int i=0; i<felMeniu.size(); i++){
                res += (felMeniu.get(i).getBucComandate() * felMeniu.get(i).getPret());
            }
            return round2(res, 2);
        }
        public float round2(float number, int scale) {
            int pow = 10;
            for (int i = 1; i < scale; i++)
                pow *= 10;
            float tmp = number * pow;
            return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
        }
    }
}
