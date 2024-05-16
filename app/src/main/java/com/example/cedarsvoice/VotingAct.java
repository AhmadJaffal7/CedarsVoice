package com.example.cedarsvoice;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ParseException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class VotingAct extends AppCompatActivity {
    private TextView remainingTimeTextView;
    private String endTime;
    private CountDownTimer countDownTimer;
    private Spinner spinnerCandidates;
    private HashMap<String, String> candidateNameToIdMap;
    private String voterId;
    private String SECRET_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        SECRET_KEY = getString(R.string.Key);
        spinnerCandidates = findViewById(R.id.candidates);

        // Get the voter_id from the intent extras
        voterId = getIntent().getStringExtra("voter_id");
        endTime = getIntent().getStringExtra("endTime");

        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        // Calculate remaining time
        calculateRemainingTime();
        // Start the countdown timer
        startCountdownTimer();

        fetchCandidateNames();
    }

    private void fetchCandidateNames() {
        String url = "http://10.0.2.2/cedarsvoice/get_candidate_names.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            List<String> candidateNames = new ArrayList<>();
                            candidateNameToIdMap = new HashMap<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String candidateName = jsonObject.getString("candidate_name");
                                String candidateId = jsonObject.getString("candidate_id");
                                candidateNames.add(candidateName);
                                candidateNameToIdMap.put(candidateName, candidateId);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(VotingAct.this, android.R.layout.simple_spinner_item, candidateNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCandidates.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VotingAct.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    public void VoterVote(View view) {
    // Create an AlertDialog.Builder object
    AlertDialog.Builder builder = new AlertDialog.Builder(VotingAct.this);
    builder.setTitle("Confirmation")
        .setMessage("Are you sure you want to vote?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Yes", record the vote and logout
                try {
                    recordVoteAndLogout();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No", dismiss the dialog
                dialog.dismiss();
            }
        });

    // Create and show the AlertDialog
    AlertDialog dialog = builder.create();
    dialog.show();
}

    private void recordVoteAndLogout() throws Exception {
        String selectedCandidateName = spinnerCandidates.getSelectedItem().toString();
        String selectedCandidateId = candidateNameToIdMap.get(selectedCandidateName);
        int candidate_id = Integer.parseInt(selectedCandidateId);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.GET, "http://10.0.2.2/cedarsvoice/get_vote_count.php?candidate_id="+candidate_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Decrypt the received encrypted count
                            int currentVoteCount = decryptVoteCount(response);
                            // Check if decryption was successful
                            if (currentVoteCount == -1) {
                                Toast.makeText(VotingAct.this, "Error decrypting vote count", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Increment the count by 1
                            int newVoteCount = currentVoteCount + 1;
                            // Encrypt the new count
                            String encryptedNewVoteCount = encryptVoteCount(newVoteCount);
                            // Check if encryption was successful
                            if (encryptedNewVoteCount == null) {
                                Toast.makeText(VotingAct.this, "Error encrypting vote count", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Update the encrypted count in the database
                            updateVoteCountInDatabase(encryptedNewVoteCount, candidate_id);

                        } catch (NumberFormatException e) {
                            Log.e("VotingAct", "Error parsing vote count: " + response, e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                error.printStackTrace();
            }
        });

        queue.add(getRequest);
    }

    // Method to encrypt the vote count
    private String encryptVoteCount(int voteCount) {
        try {
            // Convert the vote count to bytes
            byte[] voteCountBytes = String.valueOf(voteCount).getBytes();

            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(voteCountBytes);

            // Encode the encrypted data to Base64 string
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(encryptedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Error occurred
        }
        return null;
    }

    // Method to decrypt the vote count
    private int decryptVoteCount(String encryptedVoteCount) {
        try {
            // Decode the Base64 encoded data
            byte[] encryptedData = android.util.Base64.decode(encryptedVoteCount, android.util.Base64.DEFAULT);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // Convert the decrypted data to integer
            return Integer.parseInt(new String(decryptedData));
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Error occurred
        }
    }

    private void updateVoteCountInDatabase(String newVoteCount, int candidateId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/cedarsvoice/update_vote_count.php";

        StringRequest updateRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(VotingAct.this, response, Toast.LENGTH_SHORT).show();
                        updateHasVotedField();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Create a map with the new vote count and candidate id
                Map<String, String> params = new HashMap<>();
                params.put("candidate_id", String.valueOf(candidateId));
                params.put("vote_count", newVoteCount);
                return params;
            }
        };

        queue.add(updateRequest);
    }

private void updateHasVotedField() {
    String url = "http://10.0.2.2/cedarsvoice/update_has_voted.php";
    RequestQueue queue = Volley.newRequestQueue(this);

    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // After the has_voted field is updated, logout the user and go back to the VoterAct page
                    logout();
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(VotingAct.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("voter_id", voterId);
            return params;
        }
    };

    queue.add(stringRequest);
}

public void logout() {
    // Create an intent to start VoterAct
    Intent intent = new Intent(VotingAct.this, VoterAct.class);
    intent.putExtra("endTime", endTime);
    startActivity(intent);

    // Finish the current activity
    finish();
}
    private void calculateRemainingTime() {
        try {
            // Parse end time to get hours and minutes
            String[] endTimeParts = endTime.split(":");
            int endHour = Integer.parseInt(endTimeParts[0]);
            int endMinute = Integer.parseInt(endTimeParts[1]);

            // Get current time
            Calendar currentTime = Calendar.getInstance();
            int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int currentMinute = currentTime.get(Calendar.MINUTE);

            // Calculate remaining time in minutes
            int remainingMinutes = (endHour - currentHour) * 60 + (endMinute - currentMinute);

            // Check if end time is the next day
            if (remainingMinutes < 0) {
                remainingMinutes += 24 * 60; // Add 24 hours
            }

            // Convert remaining time to hours and minutes
            int remainingHours = remainingMinutes / 60;
            int remainingMinutesAfterHours = remainingMinutes % 60;

            // Display remaining time in TextView
            String remainingTimeString = String.format(Locale.getDefault(), "%d hours %d minutes", remainingHours, remainingMinutesAfterHours);
            remainingTimeTextView.setText(remainingTimeString);
        } catch (NumberFormatException e) {
            Log.e("VotingAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("VotingAct", "Error splitting end time", e);
            remainingTimeTextView.setText("Error splitting end time");
        }
    }
    private void startCountdownTimer() {
        if (endTime == null || endTime.isEmpty()) {
            Log.e("VotingAct", "End time is null or empty");
            remainingTimeTextView.setText("End time is not set");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date endTimeDate = sdf.parse(endTime);
            // Set the date of endTimeDate to the current date
            Calendar endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTime(endTimeDate);
            endTimeCalendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            endTimeCalendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
            endTimeCalendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

            long endTimeMillis = endTimeCalendar.getTimeInMillis();
            long currentTimeMillis = System.currentTimeMillis();
            long remainingTimeMillis = endTimeMillis - currentTimeMillis;

            if (remainingTimeMillis <= 0) {
                Log.e("VotingAct", "End time has already passed");
                remainingTimeTextView.setText("End time has already passed");
                return;
            }

            countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update remaining time on each tick
                    updateRemainingTime(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    // Close VoterActivity and open SupervisorActivity
                    Intent intent = new Intent(VotingAct.this, SupervisorActivity.class);
                    startActivity(intent);
                    finish(); // Close current activity
                }
            }.start();
        } catch (ParseException e) {
            Log.e("VotingAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        } catch (java.text.ParseException e) {
            Log.e("VotingAct", "Error parsing end time", e);
            remainingTimeTextView.setText("Error parsing end time");
        }
    }
    private void updateRemainingTime(long millisUntilFinished) {
        if (millisUntilFinished <= 0) {
            Log.e("VotingAct", "Time has already finished");
            remainingTimeTextView.setText("Time has already finished");
            return;
        }

        try {
            // Convert milliseconds until finished to hours, minutes and seconds
            long totalSeconds = millisUntilFinished / 1000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            // Create a StringBuilder to build the remaining time string
            StringBuilder remainingTimeString = new StringBuilder();
            remainingTimeString.append("Remaining time: ");
            // Append hours to the string if not zero
            if (hours > 0) {
                remainingTimeString.append(hours).append(" hours ");
            }

            // Append minutes to the string if not zero
            if (minutes > 0) {
                remainingTimeString.append(minutes).append(" minutes ");
            }

            // Always append seconds
            remainingTimeString.append(String.format(Locale.getDefault(), "%02d seconds", seconds));

            // Display remaining time in TextView
            remainingTimeTextView.setText(remainingTimeString.toString());
        } catch (Exception e) {
            Log.e("VotingAct", "Error updating remaining time", e);
            remainingTimeTextView.setText("Error updating remaining time");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}