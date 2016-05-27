package csie.yuntech.edu.tw.gps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class GPS extends MapActivity {
    private TextView myTextView01;
    //  private MapView myMapView01;
  private  MarkerOptions mselfpoision;
    private   LatLng latLng;
    private GeoPoint currentGeoPoint,markGeopoint;
    private LocationManager myLocationManager01;
    private String strLocationPrivider = "";String[] lunch = {"加油站", "停車場", "修車廠"},Sz={"1","2","3","4","5"};
    private Location mLocation01=null;
    private int intZoomLevel = 12;
    private Button sosbutton,bgass,bdir;
    LatLng lk = new LatLng(0,0 );
     static GoogleMap map;
    private Marker mk;
    static Spinner spinner,zoom;
    private ArrayAdapter<String> lunchList;
    double mLatitude=0, mLongitude=0;
    private boolean diring=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);


        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        bdir=(Button)findViewById(R.id.button2);bgass=(Button)findViewById(R.id.button);
        sosbutton=(Button)findViewById(R.id.button);bgass=(Button)findViewById(R.id.bgass);
        myTextView01 = (TextView)findViewById(R.id.textview);
        myLocationManager01 = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocation01 = getLocationPrivider(myLocationManager01);
        spinner = (Spinner)findViewById(R.id.spinner);
        zoom=(Spinner)findViewById(R.id.spinner2);
///////////////////////////////////////////////SPINNER//////////////////////////////////////////////////////////////////////////////////////////
        spinner.setAdapter(new CustomSpinner(GPS.this, R.layout.row, lunch));
        ArrayAdapter adaptezoom=new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,Sz);
        zoom.setAdapter(adaptezoom);
    //---------------------------------------------------------------------------------------------------------------
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if(status!= ConnectionResult.SUCCESS){ // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();}
        //------------------------------------------location run-------------------------------------------------------------------------------------------------
        if(mLocation01!=null)
        {
            processLocationUpdated(mLocation01);
        }
        else
        {
            myTextView01.setText("error");
        }
        //------------------------------------------map init-------------------------------------------------------------------------------------------------
        myLocationManager01.requestLocationUpdates(strLocationPrivider, 100000, 10, mLocationListener01);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lk, 15));


        //-----------------------------------------button run------------------------------------------------------------------------------------------------
        sosbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    String s=myTextView01.getText().toString();

                    Intent dial = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto::" ));
                    dial.putExtra("sms_body",s);
                    startActivity(dial);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }
        });
        bgass.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type;
               switch(spinner.getSelectedItemPosition())
                {
                    case 0:
                        type="gas_station";
                        break;
                    case 1:
                        type="parking";
                        break;
                    case 2:
                        type="car_repair";
                        break;
                    default:
                        type="";
                        break;
                }

                StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");//
                map.clear();
                showselfmark();
                sb.append("location=" + mLatitude + "," + mLongitude);
                sb.append("&radius=5000");
                sb.append("&types=" + type);
                sb.append("&sensor=true");
                sb.append("&key=AIzaSyC3RdGMjDxFiJv5A86bk2Qeminzzxrsb18");
                NearbysearchTask placesTask = new NearbysearchTask();
                Log.v("URLplace","is "+sb+" -----");
                // Invokes the "doInBackground()" method of the class PlaceTask
                placesTask.execute(sb.toString());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lk, 14));
            }
        });
       bdir.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                  map.clear();
                showselfmark();
                if(diring)
                {  diring=false;
                    bdir.setText("清除地圖");
                }
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final LatLng markerLocation = marker.getPosition();
                double geoLatitude = markerLocation.latitude * 1E6;
                double geoLongitude = markerLocation.longitude * 1E6;
                markGeopoint = new GeoPoint((int) geoLatitude, (int) geoLongitude);
                Drawable[] aicon = new Drawable[4];
                aicon[0] = getResources().getDrawable(R.drawable.biggas4);
                aicon[1] = getResources().getDrawable(R.drawable.parking2);
                aicon[2] = getResources().getDrawable(R.drawable.fix2);
                new AlertDialog.Builder(GPS.this)
                        .setTitle("地標資訊")
                        .setMessage(marker.getTitle() + "\n" + getAddressbyGeoPoint(markGeopoint))
                        .setIcon(aicon[spinner.getSelectedItemPosition()])
                        .setPositiveButton("導航", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bdir.setText("取消導航");
                                diring=true;
                                map.clear();
                                showselfmark();
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(markerLocation);
                                map.addMarker(markerOptions);
                                StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
                                url.append("origin=" + lk.latitude + "," + lk.longitude);
                                url.append("&destination=" + markerLocation.latitude + "," + markerLocation.longitude);
                                url.append("&sensor=false");
                                RouteTask routetask = new RouteTask();
                                routetask.execute(url.toString());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lk, 16));
                            }

                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }

                        })
                        .show();
                return false;
            }
        });
        zoom.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lk, (position+5)*2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public final void showselfmark()
    {
        Log.v("mark","start");
        if(mk!=null)
        {mk.remove();}

        mk = map.addMarker(new MarkerOptions().position(lk).title("目前位置").snippet("目前位置").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));//景點說明
      /*  mselfpoision.position(lk);
        Log.v("mark", "1");
        mselfpoision.title("目前位置");
        mselfpoision.snippet("your position");
        mselfpoision.draggable(false);
        mselfpoision.visible(true);
        mselfpoision.anchor(0.5f, 0.5f);//設為圖片中心
        Log.v("mark", "2");
        mselfpoision.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        Log.v("mark", "3");
       mk= map.addMarker(mselfpoision);*/
        Log.v("mark", "final");
    }
    public final LocationListener mLocationListener01 = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            processLocationUpdated(location);
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }

    };


    public Location getLocationPrivider(LocationManager lm)
    {
        Location retLocation = null;
        try
        {
            Criteria mCriteria01 = new Criteria();
            mCriteria01.setAccuracy(Criteria.ACCURACY_FINE);
            mCriteria01.setAltitudeRequired(false);
            mCriteria01.setBearingRequired(false);
            mCriteria01.setCostAllowed(true);
            mCriteria01.setPowerRequirement(Criteria.POWER_LOW);
            strLocationPrivider = lm.getBestProvider(mCriteria01, true);
            retLocation = lm.getLastKnownLocation(strLocationPrivider);
        }
        catch(Exception e)
        {
            myTextView01.setText(e.toString());
            e.printStackTrace();
        }
        return retLocation;
    }
    private void processLocationUpdated(Location location)
    {
        currentGeoPoint = getGeoByLocation(location);
        Log.v("lookGeopoint"," "+currentGeoPoint);
        // refreshMapViewByGeoPoint(currentGeoPoint, myMapView01, intZoomLevel, true);
        myTextView01.setText
                (
                        "Your location is :" + getAddressbyGeoPoint(currentGeoPoint)+currentGeoPoint
                );
        lk=new LatLng((int)currentGeoPoint.getLatitudeE6()/1E6,(int)currentGeoPoint.getLongitudeE6()/1E6);
        //mk = map.addMarker(new MarkerOptions().position(lk).title("目前位置").snippet("目前位置"));//景點說明
        showselfmark();
        if(diring)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lk, 16));
    }


    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }



    private GeoPoint getGeoByLocation(Location location)
    {
        GeoPoint gp = null;
        try
        {
            if (location != null)
            {
                double geoLatitude = location.getLatitude()*1E6;
                double geoLongitude = location.getLongitude()*1E6;
                gp = new GeoPoint((int) geoLatitude, (int) geoLongitude);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return gp;
    }

    public String getAddressbyGeoPoint(GeoPoint gp)
    {

        String strReturn = "Adress error";String returnAddress="Adress error";
        try
        {
            if (gp != null)
            {
                Geocoder gc = new Geocoder(GPS.this, Locale.getDefault());

                double geoLatitude = (int)gp.getLatitudeE6()/1E6;
                double geoLongitude = (int)gp.getLongitudeE6()/1E6;

                List<Address> lstAddress = gc.getFromLocation(geoLatitude, geoLongitude, 1);

                StringBuilder sb = new StringBuilder();
                returnAddress = lstAddress.get(0).getAddressLine(0);
                if (lstAddress.size() > 0)
                {
                    Address adsLocation = lstAddress.get(0);

                    for (int i = 0; i < adsLocation.getMaxAddressLineIndex(); i++)
                    {
                        sb.append(adsLocation.getAddressLine(i)).append("\n");
                    }
                    sb.append(adsLocation.getCountryName());
                    sb.append(adsLocation.getPostalCode()).append("\n");
                    sb.append(adsLocation.getLocality()).append("\n");




                }

                strReturn = sb.toString();
            }
        }
        catch(Exception e)
        {
            Log.v("addressbegin",e.getMessage());
            strReturn=e.getMessage();
            e.printStackTrace();
        }
        return returnAddress;
    }
    public static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
                Log.v("while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_g, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
