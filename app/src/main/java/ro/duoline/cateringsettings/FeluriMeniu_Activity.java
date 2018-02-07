package ro.duoline.cateringsettings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Paul on 07/02/2018.
 */

public class FeluriMeniu_Activity extends AppCompatActivity implements  GetFelMeniuStrinList.Listener{
    String categorieFiltru;
    private RecyclerView recyclerView, recyclerView1;
    private FelAdapter adapter;
    private IconCategoriiadapter adapter1;
    private RecyclerView.LayoutManager layoutManager, layoutManager1;
    public TextView categorieCurenta;

    public ProgressBar prgBar;
    public ImageView backButton;
    public TextView confirmaComanda, numarProduse, anulare;
    public int[] bucComandateSalvate;
    public String denumireRestaurant;
    public int idRestaurant;
    public ArrayList<String> listaDenumiri, al;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felmeniu);
        getSupportActionBar().hide();
        categorieFiltru = getIntent().getStringExtra("EXTRA_TEXT");
        denumireRestaurant = getIntent().getStringExtra("DENUMIRE");
        idRestaurant = getIntent().getIntExtra("ID", 0);
        listaDenumiri = getIntent().getExtras().getStringArrayList("listaDdenumiri");
        al = getIntent().getExtras().getStringArrayList("dateConectare");
        //String dateconectare = "" + al.get(0) + "," + al.get(1) + "," + al.get(2);


        initViews();
        bucComandateSalvate = null;
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("BUC_COMANDATE_CALLBACK")){
                bucComandateSalvate = savedInstanceState.getIntArray("BUC_COMANDATE_CALLBACK");
            }
        }
        prgBar.setVisibility(View.VISIBLE);
        //new GetFelMeniuStrinList(this).execute(dateconectare, al.get(3));


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                finish();
            }
        });
        //numarProduse.


    }

    @Override
    protected void onResume() {
        super.onResume();
        String dateconectare = "" + al.get(0) + "," + al.get(1) + "," + al.get(2);
        new GetFelMeniuStrinList(this).execute(dateconectare, al.get(3));

    }

    private void initViews(){
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        recyclerView = (RecyclerView)findViewById(R.id.card_recycler_view);
        recyclerView.addItemDecoration(new LineItemdecoration(this));
        recyclerView1 = (RecyclerView)findViewById(R.id.categorii_recycler_view);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView1.setLayoutManager(layoutManager1);
        categorieCurenta = (TextView) findViewById(R.id.titlu_categorie);


        backButton = (ImageView) findViewById((R.id.backButton));

        numarProduse = (TextView) findViewById(R.id.textNumarProduse);

    }



    public void setAdapterFilter(String text){
        adapter.filter(text);
    }



    @Override
    public void onError() {
        Toast.makeText(this, "Erroare conectare server!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<FelMeniuValues> vector = adapter.getVector();
        int[] bucComandateCallback = new int[vector.size()];
        FelMeniuValues elementVector;
        for(int i = 0; i<vector.size(); i++){
            elementVector = vector.get(i);
            bucComandateCallback[i] = elementVector.getBucComandate();
        }
        outState.putIntArray("BUC_COMANDATE_CALLBACK", bucComandateCallback);


    }

    public void createAdapter(List<String> NUME_CATEGORIE, List<String> DENUMIRE_PRODUS, List<String> DESCRIERE_PRODUS, List<Float> PRET_BUCATA,
                              List<Integer> COD, List<String> UM, List<String> POZA ){
        prgBar.setVisibility(View.INVISIBLE);
        ArrayList<FelMeniuValues> felMeniu = new ArrayList<>();
        Set<String> setWithUniqueValue = new HashSet<>(NUME_CATEGORIE);
        listaDenumiri = new ArrayList<>(setWithUniqueValue);
        for(int i=0; i<NUME_CATEGORIE.size(); i++){

            FelMeniuValues felMen = new FelMeniuValues();
            felMen.setCategorie(NUME_CATEGORIE.get(i));
            felMen.setDenumire(DENUMIRE_PRODUS.get(i));
            felMen.setDescriere(DESCRIERE_PRODUS.get(i));
            felMen.setUM(UM.get(i));
            felMen.setPret((Float) PRET_BUCATA.get(i));
            felMen.setCod((Integer) COD.get(i));
            felMen.setPozaURL(POZA.get(i));
            felMen.setCerinte("");
            if(bucComandateSalvate != null) {
                felMen.setBucComandate(bucComandateSalvate[i]);
            } else {
                felMen.setBucComandate(0);
            }
            felMeniu.add(felMen);

        }
        adapter1 = new IconCategoriiadapter(this, listaDenumiri);
        recyclerView1.setAdapter(adapter1);


        adapter = new FelAdapter(getApplicationContext(), categorieCurenta, numarProduse, felMeniu, this, al);
        recyclerView.setAdapter(adapter);

        adapter.filter(listaDenumiri.get(0));
    }
}
