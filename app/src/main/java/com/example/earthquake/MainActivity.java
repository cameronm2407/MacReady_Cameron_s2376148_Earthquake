// Name                 Cameron MacReady
// Student ID           s2376148
// Programme of Study   BSc Computing

package com.example.earthquake;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.DatePickerDialog;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EarthquakeAdapter.OnEarthquakeClickListener {
    private final List<EarthquakeItem> fullEarthquakeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EarthquakeAdapter adapter;
    private ArrayList<EarthquakeItem> earthquakeList = new ArrayList<>();
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private final Handler handler = new Handler();
    private final int interval = 2 * 60 * 60 * 1000; // Set to 10 seconds for demo purposes. 2 * 60 * 60 * 1000 for 2 hours
    private Button btnSearch, btnReset, btnSortDate, btnSortMagnitude, btnSortDeepest, btnSortShallowest;
    private TextView lastUpdatedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.post(fetchTask);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EarthquakeAdapter(earthquakeList, this);
        recyclerView.setAdapter(adapter);

        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        btnSortDate = findViewById(R.id.btnSortDate);
        btnSortMagnitude = findViewById(R.id.btnSortMagnitude);
        btnSortDeepest = findViewById(R.id.btnSortDeepest);
        btnSortShallowest = findViewById(R.id.btnSortShallowest);
        btnSearch = findViewById(R.id.btnSearch);
        btnReset = findViewById(R.id.btnReset);

        btnSearch.setOnClickListener(view -> showSearchDialog());

        btnReset.setOnClickListener(view -> resetEarthquakeList());

        btnSortDate.setOnClickListener(v -> showDatePickerDialog());

        btnSortMagnitude.setOnClickListener(v -> {
            Collections.sort(earthquakeList, (e1, e2) ->
                    Double.compare(e2.getMagnitude(), e1.getMagnitude()));
            adapter.notifyDataSetChanged();
        });

        btnSortDeepest.setOnClickListener(v -> {
            Collections.sort(earthquakeList, (e1, e2) ->
                    Double.compare(e2.getDepth(), e1.getDepth()));
            adapter.notifyDataSetChanged();
        });

        btnSortShallowest.setOnClickListener(v -> {
            Collections.sort(earthquakeList, Comparator.comparingDouble(EarthquakeItem::getDepth));
            adapter.notifyDataSetChanged();
        });

        // Start loading XML on app launch
        new Thread(new Task(urlSource)).start();
    }

    private void updateLastUpdatedTime() {
        String formattedTime = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK).format(Calendar.getInstance().getTime());
        lastUpdatedText.setText("Last updated: " + formattedTime);
    }
    private final Runnable fetchTask = new Runnable() {
        @Override
        public void run() {
            new Thread(new Task(urlSource)).start();
            handler.postDelayed(this, interval);
        }
    };

    private void resetEarthquakeList() {
        earthquakeList.clear();
        earthquakeList.addAll(fullEarthquakeList);
        adapter.notifyDataSetChanged();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search by Location");

        final EditText input = new EditText(this);
        input.setHint("Enter location or title");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().toLowerCase();
            List<EarthquakeItem> filtered = new ArrayList<>();
            for (EarthquakeItem item : fullEarthquakeList) {
                if (item.getLocation().toLowerCase().contains(query)) {
                    filtered.add(item);
                }
            }
            earthquakeList.clear();
            earthquakeList.addAll(filtered);
            adapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Format selected date to match the pubDate format
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.UK);
                    String selectedDate = sdf.format(calendar.getTime());

                    filterEarthquakesByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void filterEarthquakesByDate(String selectedDate) {
        List<EarthquakeItem> filteredList = new ArrayList<>();

        for (EarthquakeItem eq : fullEarthquakeList) { // ⚠️ This assumes you're keeping a full list
            if (eq.getPubDate().contains(selectedDate)) {
                filteredList.add(eq);
            }
        }

        earthquakeList.clear();
        earthquakeList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onEarthquakeClick(EarthquakeItem earthquake) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("earthquake", earthquake); // Must make Earthquake Serializable or Parcelable
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchTask);
    }

    private void parseXML(String xmlData) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlData));
            int event = parser.getEventType();
            boolean insideItem = false;
            String title = "", description = "", pubDate = "";
            List<EarthquakeItem> tempList = new ArrayList<>();

            while (event != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            insideItem = true;
                        } else if (insideItem) {
                            if ("title".equalsIgnoreCase(tagName)) {
                                title = parser.nextText();
                            } else if ("description".equalsIgnoreCase(tagName)) {
                                description = parser.nextText();
                            } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                pubDate = parser.nextText();
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            EarthquakeItem eq = new EarthquakeItem(title, description, pubDate);
                            tempList.add(eq);

                            // Reset values
                            title = "";
                            description = "";
                            pubDate = "";
                            insideItem = false;
                        }
                        break;
                }
                event = parser.next();
            }
            runOnUiThread(() -> {
                earthquakeList.clear();
                fullEarthquakeList.clear();

                earthquakeList.addAll(tempList);
                fullEarthquakeList.addAll(tempList);

                adapter.notifyDataSetChanged();
                updateLastUpdatedTime();
            });
        } catch (Exception e) {
            Log.e("XMLParser", "Parsing error: " + e.toString());
        }
    }
    private class Task implements Runnable {
        private final String url;

        public Task(String aurl) {
            url = aurl;
        }

        @Override
        public void run() {
            try {
                URL aurl = new URL(url);
                URLConnection yc = aurl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                StringBuilder xmlResult = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    xmlResult.append(inputLine);
                }
                in.close();

                parseXML(xmlResult.toString());

            } catch (Exception e) {
                Log.e("MainActivity", "Network Error: " + e);
            }
        }
    }
}