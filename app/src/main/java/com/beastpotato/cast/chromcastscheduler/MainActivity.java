package com.beastpotato.cast.chromcastscheduler;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.beastpotato.cast.chromcastscheduler.fragments.CreateItemFragment;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CreateItemFragment.OnAddItemDialogDone, LayoutItemRowAdapter.OnItemDeleteClickListener {
    private CoordinatorLayout root;
    private RecyclerView itemsView;
    private List<ScheduledItem> items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        root = (CoordinatorLayout) findViewById(R.id.home_root);
        itemsView = (RecyclerView) findViewById(R.id.scheduled_items_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
            }
        });

        items = new ArrayList<>();//todo load from database
        itemsView.setAdapter(new LayoutItemRowAdapter(items, this));
        itemsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void showAddItemDialog() {
        CreateItemFragment itemFragment = new CreateItemFragment();
        itemFragment.show(getSupportFragmentManager(), "add_item_frag");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDone(final ScheduledItem item) {
        items.add(item);
        itemsView.getAdapter().notifyDataSetChanged();
        Snackbar.make(root, "Item added to schedule", Snackbar.LENGTH_LONG)
                .setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        items.remove(items.size() - 1);
                        itemsView.getAdapter().notifyDataSetChanged();
                    }
                }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (event != DISMISS_EVENT_ACTION) {
                    //todo start service
                }
            }
        }).show();
    }

    @Override
    public void onDelete(ScheduledItem item) {
        items.remove(item);
        itemsView.getAdapter().notifyDataSetChanged();
        //todo remove from db
    }
}
