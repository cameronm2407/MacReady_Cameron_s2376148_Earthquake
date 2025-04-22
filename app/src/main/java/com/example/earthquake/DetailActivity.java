// Name                 Cameron MacReady
// Student ID           s2376148
// Programme of Study   BSc Computing

package com.example.earthquake;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView detailTitle;
    private TextView detailDescription;
    private TextView detailDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailTitle = findViewById(R.id.detailTitle);
        detailDescription = findViewById(R.id.detailDescription);
        detailDate = findViewById(R.id.detailDate);

        EarthquakeItem earthquake = (EarthquakeItem) getIntent().getSerializableExtra("earthquake");

        if (earthquake != null) {
            detailTitle.setText(earthquake.getLocation());
            detailDescription.setText(earthquake.getDescription());
            detailDate.setText(earthquake.getPubDate());
        }
    }
}
