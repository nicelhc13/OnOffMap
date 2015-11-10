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
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
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

    private ArrayAdapter<PlaceBean> resultListAdapter;

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

        resultListAdapter = new ArrayAdapter<PlaceBean>(getApplicationContext(), android.R.layout.simple_list_item_1);
        resultListView.setAdapter(resultListAdapter);
    }



    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edt_b1:
                resultListAdapter.clear();
                ArrayList<String> results;
                srcInput = srcEditText.getText().toString();
                if(srcInput != "" && srcInput != null)
                     new LocListProcessingThread(this).execute(srcInput);
                break;
            case R.id.edt_b2:
                resultListAdapter.clear();
                destInput = destEditText.getText().toString();
                if(destInput != "" && destInput != null)
                    new LocListProcessingThread(this).execute(destInput);
                break;
            case R.id.r_search_b:
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

        public LocListProcessingThread(Context context) {
            mContext = context;
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
            ArrayList<PlaceBean> placeNameList = new ArrayList<PlaceBean>();
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
                    elePlace.setName(placeObj.getString("name"));
                    elePlace.setAddress(placeObj.getString("formatted_address"));
                    elePlace.setId(placeObj.getString("place_id"));
                    elePlace.setRating(placeObj.getString("rating"));
                    elePlace.setLatitude(locationObj.getDouble("lat"));
                    elePlace.setLongitude(locationObj.getDouble("lng"));

                    placeNameList.add(elePlace);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return placeNameList;
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
            Toast.makeText(mContext, results.get(0).getLatitude() + ":" + results.get(0).getLongitude(), Toast.LENGTH_LONG).show();
            resultListAdapter.addAll(results);
        }
    }
}
