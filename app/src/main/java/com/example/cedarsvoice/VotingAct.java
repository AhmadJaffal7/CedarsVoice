package com.example.cedarsvoice;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cedarsvoice.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class VotingAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        // Assuming you have a Spinner with id candidates in your layout
        Spinner spinner = findViewById(R.id.candidates);

        // Execute FillSpinnerTask to fill the spinner
        new FillSpinnerTask(spinner).execute();
    }

    public void VoterVote(View view) {

    }

    private static class FillSpinnerTask extends AsyncTask<Void, Void, ArrayList<String>> {
        private final Spinner spinner;
        private ArrayAdapter<String> adapter;

        public FillSpinnerTask(Spinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> candidateNames = fetchCandidateNames();
            return candidateNames;
        }

        @Override
        protected void onPostExecute(ArrayList<String> candidateNames) {
            adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, candidateNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }


        private ArrayList<String> fetchCandidateNames() {
            ArrayList<String> candidateNames = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("http://10.0.2.2/cedarsvoice/cedars.php");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONArray jsonArray = new JSONArray(builder.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    candidateNames.add(name);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return candidateNames;
        }
    }
}
