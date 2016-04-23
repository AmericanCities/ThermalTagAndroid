package thermaltag.thermaltag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements LocationListener {

    // Declarations
    public static final String LOGTAG = "ThermalTag";
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;
    private String username;
    private ImageButton cameraButton, submitButton;
    private EditText shipper_cert, harvest_date, harvest_location, type_of_shellfish, quantity,
            temperature, scan_id;
    private TextView date_of_scan, time_of_scan, geo_location;
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                                            "Oct", "Nov", "Dec"};
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

//    // change geo location to string
//    public static String locationStringFromLocation(final Location location) {
//        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
//    }

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

        date_of_scan.setText(MONTHS[month] + " " + day + " " + year);
        time_of_scan.setText(hour + ":" + minute + ":" +second + " " + amPm);
    }


    // This method sets all of our view variables
    private void createViewVariables() {
        //buttons
        cameraButton = (ImageButton) findViewById(R.id.button_camera);
        submitButton = (ImageButton) findViewById(R.id.button_submit);

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
    }

    private void setClickListners(){
        cameraButton.setOnClickListener(cameraListener);
        submitButton.setOnClickListener(submitListner);

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
        }
    };


    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);
        }
    };

    private void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);

            ImageView imageView = (ImageView) findViewById(R.id.image_camera);
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
    }


    // location Listener Interface (Required)

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }


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


    /*
     DATE PICKER FRAGMENT
     ---------------------------------------------------------------
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
            dateSelected.setText(MONTHS[month] + " " + day + " " + year);

//            In case we have time to implement local SQL-LITE
//            Calendar c = Calendar.getInstance();
//            c.set(year, month, day);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            sqlFormattedDate = sdf.format(c.getTime());
        }
    }
}