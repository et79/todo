package com.et79.todo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.et79.todo.Constants;
import com.et79.todo.R;
import com.et79.todo.models.TodoTask;
import com.et79.todo.adapters.FirebaseTaskListAdapter;
import com.et79.todo.adapters.FirebaseTaskListEventListener;
import com.et79.todo.adapters.SimpleItemTouchHelperCallback;
import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        FirebaseTaskListEventListener {

    private static final String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;

    // RecyclerView instance variables
    private RecyclerView mTaskRecyclerView;
    private FirebaseTaskListAdapter mFirebaseAdapter;
    private ItemTouchHelper mItemTouchHelper;

    // ProgressBar instance variables
    private ProgressBar mProgressBar;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private LinearLayout mSplash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskRecyclerView = (RecyclerView) findViewById(R.id.taskRecyclerView);
        mSplash = (LinearLayout) findViewById(R.id.splash);
        splashVisible(View.VISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTaskEditActivity(new TodoTask(), Constants.NUM_UNDEFINED);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Initialize ProgressBar.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Initialize Google Api Client.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        if (setUpFirebaseAuth()) {
            setUpFirebaseAdapter();
            setNavHeader();
        }
    }

    private boolean setUpFirebaseAuth() {
        Log.d(TAG, "setUpFirebaseAuth");

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();

            return false;
        }

        return true;
    }

    private void setUpFirebaseAdapter() {
        Log.d(TAG, "setUpFirebaseAdapter");

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference(mFirebaseUser.getUid())
                .child(Constants.FIREBASE_DB_TASKS_CHILD);

        dbRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {
                    progressBarVisible(ProgressBar.INVISIBLE);
                    splashVisible(View.GONE);
                }

                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });

        Query query = dbRef.orderByChild(Constants.FIREBASE_QUERY_INDEX);

        mFirebaseAdapter = new FirebaseTaskListAdapter(
                TodoTask.class,
                R.layout.item_task,
                FirebaseTaskViewHolder.class,
                query,
                this, this);

        mTaskRecyclerView.setHasFixedSize(true);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTaskRecyclerView.setAdapter(mFirebaseAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFirebaseAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mTaskRecyclerView);
    }

    /**
     * NavigationView の設定
     */
    public void setNavHeader() {
        Log.d(TAG, "setNavHeader");

        // Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        // image
        CircleImageView navheaderImage = (CircleImageView) navHeaderView.findViewById(R.id.nav_header_image);
        if (mFirebaseUser.getPhotoUrl() != null) {
            Glide.with(MainActivity.this)
                    .load(mFirebaseUser.getPhotoUrl().toString())
                    .into(navheaderImage);
        } else {
            navheaderImage.setImageDrawable(ContextCompat
                    .getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
        }

        // name
        TextView navheaderName = (TextView) navHeaderView.findViewById(R.id.nav_header_name);
        navheaderName.setText(mFirebaseUser.getDisplayName());

        // email
        TextView navheaderEmail = (TextView) navHeaderView.findViewById(R.id.nav_header_email);
        navheaderEmail.setText(mFirebaseUser.getEmail());

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

        if (mFirebaseAdapter != null)
            mFirebaseAdapter.setIndexInFirebase();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if (mFirebaseAdapter != null)
            mFirebaseAdapter.cleanup();
    }

    // RecyclerView Event
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "onStartDrag");
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onClickItem(RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "onClickItem");
        TodoTask task = ((FirebaseTaskViewHolder) viewHolder).getTodoTask();
        startTaskEditActivity(task, viewHolder.getAdapterPosition());
    }

    @Override
    public void onAddItem() {
        Log.d(TAG, "onAddItem");

        // 先頭にフォーカスを移す
        mTaskRecyclerView.getLayoutManager().scrollToPosition(0);
    }

    @Override
    public void onPopulateViewHolder() {
        progressBarVisible(ProgressBar.INVISIBLE);
        splashVisible(View.GONE);
    }

    /**
     * TaskEditActivity を起動
     *
     * @param task     起動するタスク
     * @param position RecyclerView での表示位置
     */
    private void startTaskEditActivity(TodoTask task, int position) {
        Log.d(TAG, "startTaskEditActivity");

        Intent intent = new Intent(getApplication(), TaskEditActivity.class);
        intent.putExtra(Constants.STR_TASK, task);
        intent.putExtra(Constants.STR_POSITION, position);

        int requestCode = Constants.RESULT_TASKEDITACTIVITY;
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == Constants.RESULT_TASKEDITACTIVITY && data != null) {
            TodoTask task = (TodoTask) data.getSerializableExtra(Constants.STR_TASK);
            int position = data.getIntExtra(Constants.STR_POSITION, Constants.NUM_UNDEFINED);

            if (position == Constants.NUM_UNDEFINED) {
                // 新規追加
                DatabaseReference dbRef = FirebaseDatabase
                        .getInstance()
                        .getReference(mFirebaseUser.getUid())
                        .child(Constants.FIREBASE_DB_TASKS_CHILD);

                if (dbRef != null)
                    dbRef.push().setValue(task);
            } else {
                // 更新
                mFirebaseAdapter.getRef(position).setValue(task);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.sign_out_menu: {
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            }
            case R.id.action_settings:
                Toast.makeText(this, "Sorry. Now preparing...", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected");

        switch (item.getItemId()) {
            case R.id.nav_today:
            case R.id.nav_important:
                Toast.makeText(this, "Sorry. Now preparing...", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void progressBarVisible(int visible) {
        Log.d(TAG, "progressBarVisible");
        mProgressBar.setVisibility(visible);
    }

    private void splashVisible(int visible) {

        if (mSplash == null)
            return;

        AlphaAnimation animation;

        if (visible == View.GONE) {
            animation = new AlphaAnimation(1, 0);
            animation.setDuration(100);
            mSplash.startAnimation(animation);
        }

        mSplash.setVisibility(visible);
    }

}
