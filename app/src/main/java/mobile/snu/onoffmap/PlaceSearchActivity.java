package mobile.snu.onoffmap;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentSender;
        import android.location.Address;
        import android.location.Geocoder;
        import android.media.Image;
        import android.os.AsyncTask;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.places.Place;
        import com.google.android.gms.location.places.Places;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.UnsupportedEncodingException;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.net.URLConnection;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Scanner;

public class PlaceSearchActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String requestURL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String googleApiKey = "AIzaSyCL0fSGfcZYrnW3GsQfvQ4Z8YtoBP_3vsQ";
    private static final int GET_PLACE_CODE = 0;

    // UI
    private EditText srcEditText;
    private EditText destEditText;
    private ImageButton srcSearchButton;
    private ImageButton destSearchButton;
    private Button searchButton;
    private ListView resultListView;

    // Search information
    private String srcInput;
    private String destInput;
    private PlaceBean srcPlace;
    private PlaceBean destPlace;
    private int searchType;             // 0: SRC, 1: DEST

    private ArrayAdapter<String> resultListAdapter;
    ArrayList<PlaceBean> placeList;

    private Context mContext;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_manager);

        // UI
        srcEditText = (EditText) findViewById(R.id.editText1);
        destEditText = (EditText) findViewById(R.id.editText2);
        srcSearchButton = (ImageButton) findViewById(R.id.edt_b1);
        destSearchButton = (ImageButton) findViewById(R.id.edt_b2);
        searchButton = (Button) findViewById(R.id.r_search_b);
        resultListView = (ListView) findViewById(R.id.placeList);

        srcSearchButton.setOnClickListener(this);
        destSearchButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);

        resultListAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        resultListView.setOnItemClickListener(mPlaceClickListener);
        resultListView.setAdapter(resultListAdapter);

        mContext = this;
    }

    /**
     *  ListViews' ItemClickListener
     *  Through this, we can set dest and src information
     */
    private AdapterView.OnItemClickListener mPlaceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            PlaceBean selPlace = placeList.get(position);
            if (searchType == 0) {
                srcPlace = selPlace;
            } else {
                destPlace = selPlace;
            }

            resultListAdapter.clear();
            //Toast.makeText(mContext, "lat:"+selPlace.getLatitude() +" long:"+selPlace.getLongitude(), Toast.LENGTH_LONG).show();
        }
    } ;


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edt_b1:
                // Reset
                searchType = 0;     // Set the type as source
                srcPlace = null;

                resultListAdapter.clear();
                ArrayList<String> results;
                srcInput = srcEditText.getText().toString();
                if(srcInput != "" && srcInput != null)
                     new LocListProcessingThread(this).execute(srcInput);
                break;
            case R.id.edt_b2:
                // Reset
                searchType = 1;     // Set the type as dest
                destPlace = null;

                resultListAdapter.clear();
                destInput = destEditText.getText().toString();
                if(destInput != "" && destInput != null)
                    new LocListProcessingThread(this).execute(destInput);
                break;
            case R.id.r_search_b:
                if (destPlace != null && srcPlace != null) {
                    Intent placePack = new Intent();
                    placePack.putExtra("srcPlace", srcPlace);
                    placePack.putExtra("destPlace", destPlace);
                    setResult(GET_PLACE_CODE, placePack);
                    finish();
                } else {
                    Toast.makeText(mContext, "You need to select source and destination place!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     *  LocListProcessingThread
     */
    class LocListProcessingThread extends AsyncTask<String
                                                        , Void
                                                        , ArrayList<PlaceBean>> {
        private Context mContext;
        private ArrayList<String> expressList;

        public LocListProcessingThread(Context context) {
            mContext = context;
            expressList = new ArrayList<String>();
        }

        /**
         * URLBuilder
         *
         * This method builds the URL for getting information
         * @param place
         * @return
         */
        private String URLBuilder(String place) {
            StringBuilder urlString;

            urlString = new StringBuilder(requestURL);
            urlString.append("query=");
            urlString.append(place);
            urlString.append("&sensor=false&key=" + googleApiKey);

            return urlString.toString();
        }

        private String getJSON(String theURL) {
            return getURLContents(theURL);
        }

        private String getURLContents(String theURL) {
            URL url;
            URLConnection urlConn;
            BufferedReader buffReader;

            StringBuilder contents = new StringBuilder();

            try {
                url = new URL(theURL);
                urlConn = url.openConnection();
                buffReader = new BufferedReader(
                        new InputStreamReader(urlConn.getInputStream()), 8);
                String line;

                while ((line = buffReader.readLine()) != null) {
                    contents.append(line + "\n");
                }

                buffReader.close();
            } catch (Exception e){
                e.printStackTrace();
            }

            return contents.toString();
        }

        /**
         * getPlaceInformation
         *
         * This method parses all the json data
         * @param place
         * @return
         */
        private ArrayList<PlaceBean> getPlaceInformation(String place) {
            placeList = new ArrayList<PlaceBean>();
            String urlString;
            String json;

            try {
                // Get Full URL of searching
                urlString = URLBuilder(place);
                json = getJSON(urlString);

                JSONObject jsonObj = new JSONObject(json);

                JSONArray jsonArr = jsonObj.getJSONArray("results");

                PlaceBean elePlace = new PlaceBean();
                JSONObject placeObj;
                JSONObject geomtryObj;
                JSONObject locationObj;

                for(int i = 0; i < jsonArr.length(); i++) {
                    placeObj = jsonArr.getJSONObject(i);
                    geomtryObj = (JSONObject) placeObj.get("geometry");
                    locationObj = (JSONObject) geomtryObj.get("location");

                    elePlace.setName(placeObj.optString("name"));
                    elePlace.setAddress(placeObj.optString("formatted_address"));
                    elePlace.setId(placeObj.optString("place_id"));
                    elePlace.setRating(placeObj.optString("rating"));
                    elePlace.setLatitude(locationObj.optDouble("lat"));
                    elePlace.setLongitude(locationObj.optDouble("lng"));

                    placeList.add(elePlace);
                    expressList.add(elePlace.getName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return placeList;
        }

        @Override
        protected ArrayList<PlaceBean> doInBackground(String... params) {
            ArrayList<PlaceBean> results = new ArrayList<PlaceBean>();

            // Get the results of searching
            results = getPlaceInformation(params[0]);

            return results;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(ArrayList<PlaceBean> results) {
            //Toast.makeText(mContext, results.get(0).getLatitude() + ":" + results.get(0).getLongitude(), Toast.LENGTH_LONG).show();
            resultListAdapter.addAll(expressList);
        }
    }
}
