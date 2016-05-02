package thermaltag.thermaltag;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity implements LocationListener , Device.Delegate, FrameProcessor.Delegate{

    // Declarations
    public static final String LOGTAG = "ThermalTag";
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;
    private String username;
    private ImageButton cameraButton, submitButton, flirCameraButton, camTempButton, camOCRButton;
    private Button tempButton, tagButton;
    private ImageView imageView, tempImageView, thermalImage;
    private EditText shipper_cert, harvest_date, harvest_location, type_of_shellfish, quantity,
            temperature, scan_id;
    private TextView date_of_scan, time_of_scan, geo_location;
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                                            "Oct", "Nov", "Dec"};
    private LocationManager locationManager;
    private String provider;
    private Boolean tempTaken = false;
    private Boolean flirConnected = false;
    private Boolean flirImageRequested = false;
    private String lastSavedPath;
    // Thermal Tag variables
    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;
    private String thermalImageFile;
    private int cameraMode =1;

    //OCR variables
    public static final String PACKAGE_NAME = "thermaltag.thermaltag";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    public static final String lang = "eng";

    private static final String TAG = "SimpleAndroidOCR.java";

    protected Button _button;
    // protected ImageView _image;
    protected EditText _field;
    protected String _path;
    protected boolean _taken;

    protected static final String PHOTO_TAKEN = "photo_taken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        super.onCreate(savedInstanceState);
        _path = DATA_PATH + "/ocr.jpg";

        //initialize frameProcessor for FlirOne
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.BlendedMSXRGBA8888Image));
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Could be another way to do this...
        setSupportActionBar(toolbar);

        // Get "extra" username passed from Login Activity
        username = getIntent().getExtras().getString("username");

        // set view variables from layout
        createViewVariables();

        // set click listeners
        setClickListners();

        // check if Location services is turned on
        checkGpsOn();

        // set geo location
        getGeoLocation();
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();

        //START Google API GPS location
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        //START FlirOne device discovery
        Device.startDiscovery(this,this);
    }


    // remember to prompt this if GPS value is empty
    private void checkGpsOn() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        final boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // check if enabled and if not send user to the Location settings with pop-up
        if (!enabled) {
            // pop up dialogue for location services - sets listeners to the Y/N buttons within the dialogue.
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
            builder.setMessage("This App requires location Services.  Turn on?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void getGeoLocation() {

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider
        //http://developer.android.com/reference/android/location/Criteria.html
        Criteria criteria = new Criteria();

        //http://developer.android.com/reference/android/location/LocationManager.html#getBestProvider(android.location.Criteria, boolean)
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            Log.i(LOGTAG,"Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            geo_location.setText("Location not available");
        }
    }

    // set current date of scan
    //setDateAndTime();
    private void setDateAndTime() {
        final Calendar c = Calendar.getInstance(Locale.getDefault());
        int year         = c.get(Calendar.YEAR);
        int month        = c.get(Calendar.MONTH);
        int day          = c.get(Calendar.DAY_OF_MONTH);
        int hour         = c.get(Calendar.HOUR_OF_DAY);
        int minute       = c.get(Calendar.MINUTE);
        int second       = c.get(Calendar.SECOND);
        String amPm ="AM";

        if (hour > 12){
            hour-=12;
            amPm="PM";
        }

        String scanDate = MONTHS[month] + " " + day + " " + year;
        String scanTime = hour + ":" + minute + ":" +second + " " + amPm;
        date_of_scan.setText(scanDate);
        time_of_scan.setText(scanTime);
    }


    // This method creates all of our view variables
    private void createViewVariables() {
        //buttons
        cameraButton = (ImageButton) findViewById(R.id.button_ocr);
        submitButton = (ImageButton) findViewById(R.id.button_submit);
        camTempButton = (ImageButton)findViewById(R.id.tempButton);
        flirCameraButton = (ImageButton) findViewById(R.id.button_flir_takePicture);
        tempButton = (Button) findViewById(R.id.button_temp);
        tagButton = (Button) findViewById(R.id.button_tag);

        //editText fields
        shipper_cert = (EditText) findViewById(R.id.origin_shipper_cert);
        harvest_date = (EditText) findViewById(R.id.harvest_date);
        harvest_location = (EditText) findViewById(R.id.harvest_location);
        type_of_shellfish = (EditText) findViewById(R.id.type_of_shellfish);
        quantity = (EditText) findViewById(R.id.quantity);
        temperature = (EditText) findViewById(R.id.temperature);
        scan_id = (EditText) findViewById(R.id.scan_id);

        //TextView Fields -- populated automatically by phone - doesn't need to be edited.
        date_of_scan = (TextView) findViewById(R.id.date_of_scan);
        time_of_scan = (TextView) findViewById(R.id.time_of_scan);
        geo_location = (TextView) findViewById(R.id.geo_location);

        //imageView Fields
        thermalImage = (ImageView) findViewById(R.id.thermalImage);
        tempImageView = (ImageView) findViewById(R.id.tempImageView);
        imageView = (ImageView) findViewById(R.id.image_camera);
    }

    private void setClickListners(){
        cameraButton.setOnClickListener(cameraListener);
        flirCameraButton.setOnClickListener(flirCameraListener);
        submitButton.setOnClickListener(submitListner);
        tempButton.setOnClickListener(tempListner);
        camTempButton.setOnClickListener(tempListner);
        tagButton.setOnClickListener(cameraListener);

        //Date Picker button listener
        TextView datePickerBtn = (TextView)findViewById(R.id.harvest_date);
        if (datePickerBtn !=null) {
            datePickerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   DatePickerFragment datePicker = new DatePickerFragment();
                   datePicker.show(getSupportFragmentManager(), "datePicker");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(CameraActivity.this, ScanLogActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            i.putExtras(bundle);
            startActivity(i);
            return true;
        }
        else if (id == R.id.ocr_settings) {
            Intent i = new Intent(CameraActivity.this, OCRActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            i.putExtras(bundle);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener submitListner = new View.OnClickListener() {
        public void onClick(View v) {
            // this could probably be refactored as an array of textViews
            String entered_shipper_cert = shipper_cert.getText().toString();
            String entered_harvest_date = harvest_date.getText().toString();
            String entered_harvest_location = harvest_location.getText().toString();
            String entered_type_of_shellfish = type_of_shellfish.getText().toString();
            String entered_quantity = quantity.getText().toString();
            String entered_temperature = temperature.getText().toString();
            String entered_geo_location = geo_location.getText().toString();
            String entered_time_of_scan = time_of_scan.getText().toString();
            String entered_date_of_scan = date_of_scan.getText().toString();
            // not sure if we need scan_id

            if (entered_shipper_cert.equals("")) {
                Snackbar.make(v, "Shipper Certificate ID must be filled out", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } // end if
            if (entered_harvest_date.equals("")) {
                Snackbar.make(v, "Harvest date must be set", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } // end if
            if (entered_harvest_location.equals("")) {
                Snackbar.make(v, "Harvest location must be entered", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } // end if
            if (entered_type_of_shellfish.equals("")) {
                Snackbar.make(v, "Harvest location must be entered", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } // end if
            if (entered_quantity.equals("")) {
                Snackbar.make(v, "Quantity must be entered", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } // end if

        }
    };


    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {

            Log.v(LOGTAG, "Starting Camera app");
            startCameraActivity();

//            if(cameraMode==1)
//            {
//                Log.v(TAG, "Starting Camera app");
//                startCameraActivity();
//            }
//            else
//            {
//                if(flirConnected)
//                    takeTemp();
//                else
//                    Toast.makeText(CameraActivity.this,"Flir is not connected", Toast.LENGTH_LONG).show();
//            }
        }
    };

    private View.OnClickListener tagListener = new View.OnClickListener(){
        public void onClick(View v){
//            TextView mode =(TextView)findViewById(R.id.textView7);
//            mode.setText("Mode: Tag");
            cameraMode = 1;
        }
    };




    private View.OnClickListener flirCameraListener = new View.OnClickListener(){
        public void onClick(View v){
            flirImageRequested = true;
            setDateAndTime();
            setThermalTagImage();
        }
    };

    private View.OnClickListener tempListner = new View.OnClickListener(){
        public void onClick(View v){
//            TextView mode =(TextView)findViewById(R.id.textView7);
//            mode.setText("Mode: Temp");
            tempImageView.setVisibility(View.VISIBLE);
            flirCameraButton.setVisibility(View.VISIBLE);
            cameraMode = 2;
            if (flirConnected) {
                takeTemp();
            }
        }
    };

    private void takeTemp() {
        flirOneDevice.startFrameStream(new Device.StreamDelegate() {
            @Override
            public void onFrameReceived(Frame frame) {
                frameProcessor.processFrame(frame);
            }
        });
    }

    private void setThermalTagImage(){
        try {
            Log.i(LOGTAG, "image view path is: " + thermalImageFile);
            imageView.setImageURI(Uri.parse(thermalImageFile));
        }catch (Exception e) {
            Log.e(LOGTAG, e.toString());
        }

        tempImageView.setVisibility(View.INVISIBLE);
        flirCameraButton.setVisibility(View.INVISIBLE);
    }

   /* private void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }*/

    //Start OCR Camera Code
    protected void startCameraActivity() {
        File file = new File(_path);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            onPhotoTaken();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(OCRActivity.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(OCRActivity.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }

    protected void onPhotoTaken() {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        String originalShipper[]=recognizedText.split(" ");
        int originalShipperWhere= Arrays.asList(originalShipper).indexOf("Original");

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        TextView origin_shipper_cert =(TextView)findViewById(R.id.origin_shipper_cert);
        origin_shipper_cert.setText(originalShipper[originalShipperWhere+2]);

       /* if (lang.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if (recognizedText.length() != 0) {
           _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
          _field.setSelection(_field.getText().toString().length());
        } */

        // Cycle done.
    }
    //End OCR Camera Code


   /* @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);


            ContentResolver cr = getContentResolver();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(CameraActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(LOGTAG, e.toString());
            }
        }
    }*/




    /*
        ------------------------------------------------------------
         DATE PICKER FRAGMENT
         -----------------------------------------------------------
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public static String sqlFormattedDate;
        TextView displayDate;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            Log.i(LOGTAG, "Year: " + year + " Month: " + month + " Day: " + day);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            EditText dateSelected = (EditText)getActivity().findViewById(R.id.harvest_date);
            String harvestDate = MONTHS[month] + " " + day + " " + year;
            dateSelected.setText(harvestDate);

//            In case we have time to implement local SQL-LITE
//            Calendar c = Calendar.getInstance();
//            c.set(year, month, day);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            sqlFormattedDate = sdf.format(c.getTime());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Methods to stop battery drain

        // STOP GOOGLE API GPS updates when Activity is paused
        locationManager.removeUpdates(this);

        // STOP FLIR Device updates when Activity is paused
        Device.stopDiscovery();
        flirConnected =false;
        Toast.makeText(CameraActivity.this,"Flir is acting up :(", Toast.LENGTH_LONG).show();
    }




    /*
       ------------------------------------------------------------
       INTERFACE IMPLANTATION
       ------------------------------------------------------------
    */


    /* -----------------------------
       Google API for Location
      -----------------------------
    */
    @Override
    public void onLocationChanged(Location location) {
        String lat = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
        String lng= location.convert(location.getLatitude(), location.FORMAT_DEGREES);

        Log.i(LOGTAG,"The location is: " + lng + ", " + lat);
        String locationString = "LNG: " + lng + " LAT: " + lat;
        geo_location.setText(String.valueOf(locationString));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    /* -----------------------------
       FLIR  INTERFACE METHODS
      -----------------------------
    */

    // We don't need this
    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {

    }

    // We don't need this
    @Override
    public void onAutomaticTuningChanged(boolean b) {

    }

    @Override
    public void onDeviceConnected(Device device) {
    /*
        From Flir Docs: device - The object representing the connected device. You should
        use this object to perform device operations such as starting frame streaming and
        controlling tuning.
     */
        flirOneDevice = device;
        flirConnected = true;
        Log.i(LOGTAG, "FLIR Device connected!  Boom");
    }

    @Override
    public void onFrameProcessed(final RenderedImage renderedImage) {
        final Bitmap imageBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(),Bitmap.Config.ARGB_8888);
        imageBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempImageView.setImageBitmap(imageBitmap);
                thermalImage.setImageBitmap(imageBitmap);
            }
        });

        if (flirImageRequested) {
            flirImageRequested = false;
            final Context context = this;
            new Thread(new Runnable() {
                public void run() {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
                    String formatedDate = sdf.format(new Date());
                    String fileName = "FLIROne-" + formatedDate + ".jpg";
                    try {
                        lastSavedPath = path + "/" + fileName;
                        renderedImage.getFrame().save(new File(lastSavedPath), RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);
                        thermalImageFile = lastSavedPath;
                        MediaScannerConnection.scanFile(context,
                                new String[]{path + "/" + fileName}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i(LOGTAG, "Scanned " + path + ":");
                                        Log.i(LOGTAG, "-> uri=" + uri);
                                    }

                                });

                        imageView.setImageURI(Uri.parse(thermalImageFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            tempImageView.animate().setDuration(50).scaleY(0).withEndAction((new Runnable() {
                                public void run() {
                                    tempImageView.animate().setDuration(50).scaleY(1);
                                }
                            }));
                        }
                    });
                }
            }).start();
        } // end if flir image requested
    }

    @Override
    public void onDeviceDisconnected(Device device) {
    // Called when the device has disconnected for any reason

    }
}