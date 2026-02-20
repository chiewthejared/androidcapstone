package com.example.test_v2.homeIntroLogin; // change if your file is in a subpackage

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_v2.R;
import com.example.test_v2.tiles.TileItem;
import com.example.test_v2.tiles.TilesAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private RecyclerView rvTiles;
    private TextView tvName, tvSubtitle;
    private ImageButton btnBack, btnCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        rvTiles = findViewById(R.id.rv_tiles);
        tvName = findViewById(R.id.tv_name);
        tvSubtitle = findViewById(R.id.tv_sub);
        btnBack = findViewById(R.id.btn_back);
        btnCompose = findViewById(R.id.btn_share);

        // populate tiles using the new vector drawables you created above
        List<TileItem> items = new ArrayList<>();
        items.add(new TileItem(R.drawable.ic_calendar, "Calendar"));
        items.add(new TileItem(R.drawable.ic_notes, "Notes"));
        items.add(new TileItem(R.drawable.ic_medication, "Meds & Equipment"));
        items.add(new TileItem(R.drawable.ic_doctor, "Doctor Info"));
        items.add(new TileItem(R.drawable.ic_analytics, "Analytics"));
        items.add(new TileItem(R.drawable.ic_articles, "Articles & Videos"));
        items.add(new TileItem(R.drawable.ic_timer, "Timer"));

        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rvTiles.setLayoutManager(glm);

        TilesAdapter adapter = new TilesAdapter(items, this, position -> {
            // handle tile clicks
            TileItem tile = items.get(position);
            // TODO: replace with intents to open activities
        });
        rvTiles.setAdapter(adapter);

        tvName.setText(getString(R.string.john_doe));
        tvSubtitle.setText(getString(R.string.subtitle));

        btnBack.setOnClickListener(v -> onBackPressed());
        btnCompose.setOnClickListener(v -> {
            // TODO: implement compose/share
        });
    }
}