package br.com.bossini.perctemp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.SystemClock;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_CODE_GPS = 1001;
    private double latitudeAtual = 0;
    private double longitudeAtual = 0;
    public EditText txtInpSea;
    public Button btnConPer;
    Chronometer lblTemPas2;
    public TextView lblDisPer2;
    public Location location2;
    public int totalMeters = 0;
    public Boolean gpsStatus;
    public Boolean perStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtInpSea = findViewById(R.id.txtInpSea2);
        btnConPer = findViewById(R.id.btnConPer);
        lblTemPas2 = findViewById(R.id.lblTemPas2);
        lblDisPer2 = findViewById(R.id.lblDisPer2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gpsStatus = false;
        perStatus = false;
        lblDisPer2.setText(totalMeters+getString(R.string.disTyp));
        txtInpSea.clearFocus();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                latitudeAtual = location.getLatitude();
                longitudeAtual = location.getLongitude();
                if(location2 != null){
                    totalMeters += location.distanceTo(location2);
                }
                location2 = new Location(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }

        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            btnConPer.setText(R.string.txtPerCon);
            btnConPer.setEnabled(false);
        }

        lblTemPas2.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                lblDisPer2.setText(totalMeters+getString(R.string.disTyp));
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickSearch(View view){
        Uri gmmIntentUri =
                Uri.parse(String.format("geo:%f,%f?q="+txtInpSea.getText().toString(), latitudeAtual, longitudeAtual));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void onClickConPer(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    public void onClickAtvGps(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(!gpsStatus){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                gpsStatus = true;
            }
        }else{
            alert(getString(R.string.perGps), getString(R.string.perAnt));
        }
    }

    public void onClickDesGps(View view){
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        Boolean isOn = manager.isProviderEnabled( LocationManager.GPS_PROVIDER);
        if(isOn){
            if(gpsStatus){
                locationManager.removeUpdates(locationListener);
                gpsStatus = false;
                if(perStatus){
                    endPer();
                }
            }else{
                alert("GPS", getString(R.string.gpsDes));
            }
        }else{
            alert("GPS", getString(R.string.gpsDes));
        }
    }

    public void onClickIniPer(View view){
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        Boolean isOn = manager.isProviderEnabled( LocationManager.GPS_PROVIDER);
        if(isOn){
            if(!gpsStatus){
                alert("GPS", getString(R.string.gpsAtv));
            }else{
                // liga o cronometro
                lblTemPas2.setBase(SystemClock.elapsedRealtime());
                lblTemPas2.start();
                perStatus = true;
            }
        }else{
            alert("GPS", getString(R.string.gpsAtv));
        }

    }

    public void onClickTerPer(View view){
        if(perStatus){
            endPer();
            perStatus = false;
        }
    }

    public void endPer(){
        lblTemPas2.stop();
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.disPer), lblDisPer2.getText(), lblTemPas2.getText()),
                Toast.LENGTH_LONG).show();
        lblTemPas2.setBase(SystemClock.elapsedRealtime());
        totalMeters = 0;
        lblDisPer2.setText("0"+getString(R.string.disTyp));
    }

    public void alert(String title, String text){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS){
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                //permissão concedida, ativamos o GPS
                alert(getString(R.string.perGps), getString(R.string.perCon));
                btnConPer.setText(getString(R.string.txtPerCon));
                btnConPer.setEnabled(false);
            }
            else{
                alert(getString(R.string.perGps), getString(R.string.perNCon));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //locationManager.removeUpdates(locationListener);
    }


}
