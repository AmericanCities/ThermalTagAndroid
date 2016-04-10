package thermaltag.thermaltag;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class CameraActivity extends AppCompatActivity {


    private static String logtag="ThermalTag";
    private static int TAKE_PICTURE=1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);

        Button cameraButton =(Button)findViewById(R.id.button_camera);
        cameraButton.setOnClickListener(cameraListener);
    }

    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);
        }
    };

    private void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent,TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode== Activity.RESULT_OK){
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage,null);

            ImageView imageView = (ImageView)findViewById(R.id.image_camera);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;

            try{
                bitmap = MediaStore.Images.Media.getBitmap(cr,selectedImage);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(CameraActivity.this,selectedImage.toString(),Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Log.e(logtag, e.toString());
            }
        }
    }
}