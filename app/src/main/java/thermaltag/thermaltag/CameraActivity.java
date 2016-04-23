package thermaltag.thermaltag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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

public class CameraActivity extends AppCompatActivity {

    // Declarations
    public static final String LOGTAG = "ThermalTag";
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;
    private String username;
    private ImageButton cameraButton, submitButton;
    private EditText shipper_cert, harvest_date, harvest_location, type_of_shellfish, quantity,
            temperature, geo_location, time_of_scan, date_of_scan, scan_id;
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


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

        // set current date of scan

    }

    // set view variables
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
        geo_location = (EditText) findViewById(R.id.geo_location);
        time_of_scan = (EditText) findViewById(R.id.time_of_scan);
        date_of_scan = (EditText) findViewById(R.id.date_of_scan);
        scan_id = (EditText) findViewById(R.id.scan_id);
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
//                    saveData.setVisibility(View.VISIBLE);

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
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            sqlFormattedDate = sdf.format(c.getTime());
//            displayDate.setText(MONTHS[month] + " " + day);
            EditText dateSelected = (EditText)getActivity().findViewById(R.id.harvest_date);
//            dateSelected.setText(MONTHS[month] + " " + day);
            dateSelected.setText(MONTHS[month] + " " + day + " " + year);
            Log.i(LOGTAG, "got Here Boom");
        }
    }
}