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

import com.beastpotato.cast.chromcastscheduler.adapters.LayoutItemRowAdapter;
import com.beastpotato.cast.chromcastscheduler.fragments.CreateItemFragment;
import com.beastpotato.cast.chromcastscheduler.managers.DatabaseManager;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;
import com.beastpotato.cast.chromcastscheduler.utils.Utils;

public class MainActivity extends AppCompatActivity implements CreateItemFragment.OnAddItemDialogDone, LayoutItemRowAdapter.OnItemDeleteClickListener, LayoutItemRowAdapter.OnItemClickListener {
    private CoordinatorLayout root;
    private RecyclerView itemsView;
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

        LayoutItemRowAdapter rowAdapter = new LayoutItemRowAdapter(DatabaseManager.getInstance(this).getScheduledItems(), this);
        rowAdapter.setDeleteClickListener(this);
        rowAdapter.setOnItemClickListener(this);
        itemsView.setAdapter(rowAdapter);
        itemsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void showAddItemDialog() {
        CreateItemFragment itemFragment = new CreateItemFragment();
        itemFragment.show(getSupportFragmentManager(), "add_item_frag");
    }

    private void showAddItemDialog(ScheduledItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CreateItemFragment.EXTRA_SCHEDULED_ITEM, item);
        CreateItemFragment itemFragment = new CreateItemFragment();
        itemFragment.setArguments(bundle);
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
        DatabaseManager.getInstance(this).setScheduledItem(item);
        ((LayoutItemRowAdapter) itemsView.getAdapter()).setData(DatabaseManager.getInstance(this).getScheduledItems());
        Snackbar.make(root, "Item added to schedule", Snackbar.LENGTH_LONG)
                .setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseManager.getInstance(MainActivity.this).deleteScheduledItem(item);
                        ((LayoutItemRowAdapter) itemsView.getAdapter()).setData(DatabaseManager.getInstance(MainActivity.this).getScheduledItems());
                    }
                }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (event != DISMISS_EVENT_ACTION) {
                    Utils.cancelAlarm(MainActivity.this, item);
                    Utils.setAlarmForScheduledItem(MainActivity.this, item);
                }
            }
        }).show();
    }

    @Override
    public void onDelete(ScheduledItem item) {
        DatabaseManager.getInstance(this).deleteScheduledItem(item);
        ((LayoutItemRowAdapter) itemsView.getAdapter()).setData(DatabaseManager.getInstance(this).getScheduledItems());
    }

    @Override
    public void onItemClick(ScheduledItem item, View view) {
        try {
            showAddItemDialog(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
