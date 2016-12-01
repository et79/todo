package com.et79.todo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.et79.todo.Constants;
import com.et79.todo.R;
import com.et79.todo.TodoApplication;
import com.et79.todo.models.TodoTask;
import com.et79.todo.adapters.FirebaseTaskListAdapter;
import com.et79.todo.adapters.FirebaseTaskListEventListener;
import com.et79.todo.adapters.SimpleItemTouchHelperCallback;
import com.et79.todo.util.util;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
    private TodoApplication mApplication;

    // RecyclerView instance variables
    private RecyclerView mTaskRecyclerView;
    private FirebaseTaskListAdapter mFirebaseAdapter;
    private ItemTouchHelper mItemTouchHelper;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // UI references.
    private LinearLayout mSplashView;
    private View mNavHeaderView;
    private ShowcaseView mShowcaseView;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplication = (TodoApplication) getApplication();

        mTaskRecyclerView = (RecyclerView) findViewById(R.id.taskRecyclerView);
        mTaskRecyclerView.setHasFixedSize(true);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // プログレスバーは今は使ってないけど、一応残してます
        progressBarVisible(View.INVISIBLE);
        setUpSplash();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNavHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        if (getIsFirst()) {
            // アプリ初回起動時はコーチマークを表示するので、スプラッシュは表示しない
        } else {
            // スプラッシュ表示
            setSplashVisible(View.VISIBLE);
        }

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // タスクを追加
                startTaskEditActivity(new TodoTask(), Constants.NUM_UNDEFINED);

                // コールマーク非表示
                hideCoachMark();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Initialize Google Api Client.
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mApplication.setGoogleApiClient(mGoogleApiClient);

        // Firebase Authentication のセットアップ
        setUpFirebaseAuth();

        // Firebase database / FirebaseTaskListAdapter のセットアップ
        setUpFirebaseAdapter();

        // Navigation Header のセットアップ
        setNavHeader();
    }

    /**
     * 初回起動か否か？
     * @return true: 初回起動 false: ２回目以降
     */
    private boolean getIsFirst() {
        Log.d(TAG, "getIsFirst");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean(Constants.STR_IS_FIRST, true);
    }

    /**
     * 初回起動フラグを設定
     * @param isFirst
     */
    private void setIsFirst(boolean isFirst) {
        Log.d(TAG, "setIsFirst");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.STR_IS_FIRST, isFirst);
        editor.apply();
    }

    private void setUpSplash() {
        Log.d(TAG, "setUpSplash");

        mSplashView = (LinearLayout) findViewById(R.id.splash);

        // バージョン名表示
        TextView splushVerStrTextView = (TextView) findViewById(R.id.splash_ver_str);
        splushVerStrTextView.setText(getString(R.string.version_str, util.getVersionName(this)));
    }

    private void setUpFirebaseAuth() {
        Log.d(TAG, "setUpFirebaseAuth");

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mApplication.setFirebaseAuth(mFirebaseAuth);

        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // ログインしていない場合は、ログイン画面を表示
            startLoginActivity();
        }
    }

    private void setUpFirebaseAdapter() {
        Log.d(TAG, "setUpFirebaseAdapter");

        if (mFirebaseUser == null)
            return;

        // Firebase DB取得
        // uid/tasks
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
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);

                // データ数を取得
                long taskCount = dataSnapshot.getChildrenCount();

                // データが0個だった場合
                if (taskCount == 0) {
                    setSplashVisible(View.GONE);
//                    progressBarVisible(ProgressBar.INVISIBLE);
                }
            }
        });

        Query query = dbRef.orderByChild(Constants.FIREBASE_QUERY_INDEX);

        // FirebaseTaskListAdapter をセット
        mFirebaseAdapter = new FirebaseTaskListAdapter(
                TodoTask.class,
                R.layout.item_task,
                FirebaseTaskViewHolder.class,
                query,
                this, this);

        mTaskRecyclerView.setAdapter(mFirebaseAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFirebaseAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mTaskRecyclerView);
    }

    public void setNavHeader() {
        Log.d(TAG, "setNavHeader");

        if (mFirebaseUser == null)
            return;

        // image
        CircleImageView navheaderImage = (CircleImageView) mNavHeaderView.findViewById(R.id.nav_header_image);
        if (mFirebaseUser.getPhotoUrl() != null) {
            Glide.with(MainActivity.this)
                    .load(mFirebaseUser.getPhotoUrl().toString())
                    .into(navheaderImage);
        } else {
            navheaderImage.setImageDrawable(ContextCompat
                    .getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
        }

        // name を設定
        TextView navheaderName = (TextView) mNavHeaderView.findViewById(R.id.nav_header_name);
        navheaderName.setText(mFirebaseUser.getDisplayName());

        // email を設定
        TextView navheaderEmail = (TextView) mNavHeaderView.findViewById(R.id.nav_header_email);
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

        if (mFirebaseAdapter != null) {
            mFirebaseAdapter.cleanup();
            mItemTouchHelper = null;
        }
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

        // タスクの詳細を表示
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
        Log.d(TAG, "onPopulateViewHolder");

        // アイテムを表示したので、スプラッシュを非表示にする
        setSplashVisible(View.GONE);
//        progressBarVisible(ProgressBar.INVISIBLE);
    }

    @Override
    public void onItemDismiss() {
        Log.d(TAG, "onItemDismiss");

        // スナックバーを表示
        // TODO: Undoの実装
        FrameLayout layout = (FrameLayout) findViewById(R.id.mainActivityLayout);
        Snackbar.make(layout, getString(R.string.snackbar_text), Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * LoginActivity の起動
     */
    private void startLoginActivity() {
        Log.d(TAG, "startLoginActivity");

        Intent intent = new Intent(getApplication(), LoginActivity.class);
        startActivityForResult(intent, Constants.RESULT_LOGINACTIVITY);
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

        startActivityForResult(intent, Constants.RESULT_TASKEDITACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.RESULT_TASKEDITACTIVITY:
                    TodoTask task = (TodoTask) data.getSerializableExtra(Constants.STR_TASK);
                    int position = data.getIntExtra(Constants.STR_POSITION, Constants.NUM_UNDEFINED);
                    UpdateTask(task, position);
                    break;
                case Constants.RESULT_LOGINACTIVITY:
                    // コーチマークを表示
                    if (getIsFirst()) {
                        showCoachMark(mFloatingActionButton);
                        setIsFirst(false);
                    }
                    changeLoginUser();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * タスクの作成／更新
     *
     * @param task     更新するタスク
     * @param position 更新するタスクの RecyclerView 表示位置
     */
    private void UpdateTask(TodoTask task, int position) {
        Log.d(TAG, "UpdateTask");

        if (position != Constants.NUM_UNDEFINED) {
            // 更新
            mFirebaseAdapter.getRef(position).setValue(task);
        } else {
            // 新規追加
            DatabaseReference dbRef = FirebaseDatabase
                    .getInstance()
                    .getReference(mFirebaseUser.getUid())
                    .child(Constants.FIREBASE_DB_TASKS_CHILD);

            if (dbRef != null)
                dbRef.push().setValue(task);
        }
    }

    /**
     * ログインユーザが変わった場合
     */
    private void changeLoginUser() {
        Log.d(TAG, "changeLoginUser");

        // ユーザ／UI表示／ナビゲーションを更新
        setUpFirebaseAuth();
        setUpFirebaseAdapter();
        setNavHeader();
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
                startLoginActivity();
                return true;
            }
            case R.id.action_settings:  // TODO
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

        switch (item.getItemId()) { // TODO:
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
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(visible);
    }

    /**
     * Splashの表示切り替え
     *
     * @param visible 表示／非表示
     */
    private void setSplashVisible(int visible) {
        Log.d(TAG, "setSplashVisible");

        if (mSplashView == null || mSplashView.getVisibility() == visible)
            return;

        // 消すときにフェードアウトのアニメーションをつける
        if (visible == View.GONE) {
            AlphaAnimation animation;
            animation = new AlphaAnimation(1, 0);
            animation.setDuration(100);
            mSplashView.startAnimation(animation);
        }

        mSplashView.setVisibility(visible);
    }

    /**
     * コーチマークを表示
     *
     * @param view
     */
    private void showCoachMark(View view) {
        Log.d(TAG, "showCoachMark");

        mShowcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(view))
                .setContentTitle(getString(R.string.coach_mark_title))
                .setContentText(getString(R.string.coach_mark_text))
                .hideOnTouchOutside()
                .setStyle(R.style.CustomShowcaseTheme)
                .withMaterialShowcase()
                .doNotBlockTouches() //ShowcaseView下のボタンを触れるように。
                .build();
        mShowcaseView.hideButton(); // Showcase上のボタンを隠す。
    }

    /**
     * コーチマークを非表示
     */
    private void hideCoachMark() {

        if (mShowcaseView != null)
            mShowcaseView.hide();
    }
}
