


package com.example.mapboxtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class uploadimagepage extends AppCompatActivity {

    private Button btnchoose,btnupload;
    private ImageView imageview;



    public static Bitmap bitmap;
    public static double lat ;
    public static double lng;
    public static final String BITMAP="com.example.application.example.BITMAP";
    public Uri filepath=null;
    private final int PICK_IMAGE_REQUEST=71;
    public boolean askscreenshot=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadimagepage);


        btnchoose=findViewById(R.id.chooseimage);
        btnupload=findViewById(R.id.buttonupload);
        imageview=findViewById(R.id.imageView);
        btnchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askscreenshot();
            }
        });


        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filepath!=null){

                }else{

                }
                storedestination();
                returnbitmap();
                MainActivity.uploadimagedatabase();
                finish();

            }
        });
    }


    private void storedestination(){
        Intent intent =getIntent();

        double value1=1;

        lat = intent.getDoubleExtra(MainActivity.EXTRA_LAT,value1);
        lng = intent.getDoubleExtra(MainActivity.EXTRA_LNG,value1);
        //bitmap
        MainActivity.latarraylist .add(lat);
        MainActivity.lngarraylist .add(lng);
        MainActivity.bitmaplist.add(bitmap);


    }

    private  void askscreenshot(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setMessage("take photo now?").setCancelable(false)
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseimage();
                        Toast toast=Toast. makeText(getApplicationContext(),"open file",Toast. LENGTH_SHORT);
                        toast.show();
                        askscreenshot=false;

                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent,0);
                        Toast toast=Toast. makeText(getApplicationContext(),"open camera",Toast. LENGTH_SHORT);
                        toast.show();
                        askscreenshot=true;
                    }
                });
        AlertDialog alertDialog= builder.create();
        alertDialog.show();

    }

    private void chooseimage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select Pictures"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (askscreenshot==true){
            bitmap = (Bitmap) data.getExtras().get("data");

            imageview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 1000, 1200, false));

        }
        else {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                filepath = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                    imageview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 1000, 1200, false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public static Bitmap returnbitmap(){
        if(bitmap!=null) {
            MainActivity.userinputbitmap=bitmap;
            return bitmap;
        }
        return null;
    }
}
