package ro.duoline.cateringsettings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private ImageView mImageView;
    private TextView denumire;
    private EditText numeFileEdit;
    private Bitmap mImageBitmap;
    private Button btnCamera, btnUpload, btnCache;
    private static final int ACTION_TAKE_PHOTO = 100;
    private int cod;
    private ArrayList<String> al;
    private static final String UPLOAD_IMAGE_URL = "https://www.duoline.ro/catering/imagesProduse/uploadImage.php";
    private static final int MULTIPLE_PERMISSION_CODE = 342;
    private Uri fileUri;
    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setContentView(R.layout.activity_upload);
        requestStoragePermission();
        mImageView = (ImageView) findViewById(R.id.imageView);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnCache = (Button) findViewById(R.id.delCache);
        numeFileEdit = (EditText) findViewById(R.id.editText);
        denumire = (TextView) findViewById(R.id.textViewDenumire);
        mImageBitmap = null;
        String den = getIntent().getStringExtra("denumire");
        denumire.setText(den);
        al = getIntent().getStringArrayListExtra("dateconectare");
        cod = getIntent().getIntExtra("cod", 0);
        den = den.replace(" ", "");
        den = den.replace("/","");
        den = den.replace(".", "");
        if (den.length() < 9){
            den = den.substring(0, den.length()).toLowerCase() + Integer.toString(cod);
        } else {
            den = den.substring(0, 8).toLowerCase() + Integer.toString(cod);
        }
        numeFileEdit.setText(den);
        btnUpload.setEnabled(false);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile  = null;
                        try{
                            photoFile = createImageFile();
                        } catch (IOException ex){
                            //Error
                        }
                        if (photoFile != null) {
                            fileUri = FileProvider.getUriForFile(getApplicationContext(),"ro.duoline.cateringsettings",photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
                        }
                    }
                } else {
                    Toast.makeText(getApplication(), "camera not supported", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        btnCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCache(getApplicationContext());
            }
        });
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private File createImageFile() throws IOException{

        String imageFilename = "JPEG_CAMERA_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFilename, //prefix
                ".jpg", //suffix
                storageDir //directory
        );
        picturePath = image.getAbsolutePath();
        btnUpload.setEnabled(true);
        return image;
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap) {
        int targetHeight = 300;
        int targetWidth = 240;
        int height = originalBitmap.getHeight();
        int width = originalBitmap.getWidth();
        float scaleWidth = ((float) targetWidth) / width;
        float scaleHeight = ((float) targetHeight) / height;
        Matrix matrix = new Matrix();
        Bitmap resizedBitmap = null;
        if (scaleWidth > scaleHeight){ //scalez
            matrix.postScale(scaleWidth, scaleWidth);
            resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, false);
            int tempHeight = resizedBitmap.getHeight();
            resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, (int)((tempHeight - targetHeight) / 2), targetWidth, targetHeight); //diferenta de inaltime dupa prima scalare o impart la 2 si cropez
        } else {
            matrix.postScale(scaleHeight, scaleHeight);
            resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, false);
            float tempWidth = resizedBitmap.getWidth();
            resizedBitmap = Bitmap.createBitmap(resizedBitmap,  (int)((tempWidth - targetWidth) / 2), 0, targetWidth, targetHeight);
        }


        // matrix.postScale(scaleWidth, scaleHeight);
        // Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    mImageView.setImageURI(fileUri);
                }
                break;
            } // ACTION_TAKE_PHOTO_B

        }
    }
    public void uploadImage(){
        btnUpload.setEnabled(false);
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        byte[] ba = null;
        if (bm != null) {
            bm = resizeBitmap(bm);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
            ba = bao.toByteArray();
        }
        try {
            if (ba != null) {
                File f = new File(this.getCacheDir(), "captura.jpg");
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(ba);
                fos.flush();
                fos.close();
                int index = al.get(3).lastIndexOf("/");
                String folder = al.get(3).substring(index + 1);
                String uploadid = UUID.randomUUID().toString();
                new MultipartUploadRequest(this, uploadid, UPLOAD_IMAGE_URL)
                        .addFileToUpload(f.getPath(), "image")
                        .addParameter("name", numeFileEdit.getText().toString())
                        .addParameter("folder", folder)
                        .setNotificationConfig(new UploadNotificationConfig())
                        .setMaxRetries(2)
                        .startUpload();
            }
            new HttpAsyncTask().execute(al.get(3));
        }catch(Exception e){
            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }


    private void requestStoragePermission(){
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            return;

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MULTIPLE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MULTIPLE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission NOT granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String numefisier = numeFileEdit.getText() + ".jpg";
            JSONObject codPozaLink = makeJson(cod, numefisier);
            String dateconectare = "" + al.get(0) + "," + al.get(1) + "," + al.get(2);
            POST(urls[0],codPozaLink, dateconectare);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Upload DONE!", Toast.LENGTH_LONG).show();
        }
    }

    public static void POST(String url, JSONObject jarr, String dateconectare){
        //InputStream inputStream = null;
        try{
            String json = URLEncoder.encode(jarr.toString(), "UTF-8");
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // 2. initialize HTTPGet request
            HttpGet request = new HttpGet();


            request.setURI(new URI(url+"/setPozaProdus.php?sirjson="+json+"&dateconectare="+dateconectare));
            // 2. Execute GET request to the given URL
            HttpResponse response = httpclient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject makeJson(Integer cod, String numefis){
        try {
            // JSONArray jarrFinal = new JSONArray();
            JSONObject jo = new JSONObject();
            jo.accumulate("cod", cod);
            jo.accumulate("link", numefis);

            //  jarrFinal.put(jo);

            return jo;

        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }

    }
}
