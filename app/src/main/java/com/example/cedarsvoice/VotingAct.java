package com.example.cedarsvoice;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class VotingAct extends AppCompatActivity {

    private Spinner spinner;
    private int loggedInUserId; // Assuming you have a way to get the logged-in user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        // Assuming you have a Spinner with id candidates in your layout
        spinner = findViewById(R.id.candidates);

        // Execute FillSpinnerTask to fill the spinner
        new FillSpinnerTask().execute();
    }

    public void VoterVote(View view) {
        // Get the selected candidate name from the spinner
        String selectedCandidateName = spinner.getSelectedItem().toString();

        // Execute a background task to get the candidate ID from the database
        new GetCandidateIdTask(selectedCandidateName).execute();
    }

    private class GetCandidateIdTask extends AsyncTask<Void, Void, Integer> {
        private final String candidateName;

        public GetCandidateIdTask(String candidateName) {
            this.candidateName = candidateName;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            // Perform network operation to retrieve the candidate ID from the database
            int candidateId = fetchCandidateId(candidateName);
            return candidateId;
        }

        @Override
        protected void onPostExecute(Integer candidateId) {
            // Once the candidate ID is retrieved, insert a new record into the votes table
            int voterId = getLoggedInUserId(); // Get the logged-in user ID
            if (candidateId != -1 && voterId != -1) {
                new RecordVoteTask(voterId, candidateId).execute();
            } else {
                // Handle error (e.g., candidate not found)
            }
        }

        private int fetchCandidateId(String candidateName) {
            // Implement code to fetch the candidate ID from the database based on the candidate name
            // Return -1 if the candidate is not found
            // Otherwise, return the candidate ID
            // This is a placeholder and needs to be replaced with actual implementation
            return -1; // Placeholder
        }

        private int getLoggedInUserId() {
            // Return the ID of the logged-in user (voter)
            return loggedInUserId;
        }
    }

    private class RecordVoteTask extends AsyncTask<Void, Void, Void> {
        private final int voterId;
        private final int candidateId;

        public RecordVoteTask(int voterId, int candidateId) {
            this.voterId = voterId;
            this.candidateId = candidateId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://10.0.2.2/cedarsvoice/record_vote.php");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("voterId", String.valueOf(voterId))
                        .appendQueryParameter("candidateId", String.valueOf(candidateId));
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Vote recorded successfully
                } else {
                    // Handle server error
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Handle post-execution tasks if needed
        }
    }

    private class FillSpinnerTask extends AsyncTask<Void, Void, ArrayList<String>> {
        private ArrayAdapter<String> adapter;

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
                    String name = jsonObject.getString("candidate_name"); // Updated to match the database schema
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