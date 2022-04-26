package com.example.museum_ocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.CAMERA;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private SurfaceView surfaceView;

    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;

    private TextToSpeech textToSpeech;
    private String stringResult = null;

    private Bitmap bp;

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        Button photoButton = (Button) this.findViewById(R.id.capture_btn);
        Button convertButton = (Button) this.findViewById(R.id.convert_btn);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Frame frame = new Frame.Builder().setBitmap(bp).build();
                SparseArray<TextBlock> sparseArray = textRecognizer.detect(frame);

                StringBuilder stringBuilder = new StringBuilder();

                for(int i=0; i<sparseArray.size(); i++){
                    TextBlock tx = sparseArray.get(i);
                    String str = tx.getValue();

                    stringBuilder.append(str);
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Text in the Image is:")
                        .setMessage(stringBuilder)
                        .setPositiveButton(R.string.speak, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                textToSpeech.speak(stringBuilder, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(R.string.close, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bp = photo;
            imageView.setImageBitmap(photo);
        }
    }
}