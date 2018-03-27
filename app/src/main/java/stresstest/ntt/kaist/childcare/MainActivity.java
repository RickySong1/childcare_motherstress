package stresstest.ntt.kaist.childcare;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import stresstest.ntt.Garmin.MyOAuthConnect;
import stresstest.ntt.mymanager.MyFileManager;
import stresstest.ntt.mymanager.MySocketManager;
import stresstest.ntt.smartband.LoginSettingActivity;

import static stresstest.ntt.mymanager.MySocketManager.SOCKET_MSG.SET_OPENAPP;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final int BABY = 0;
    public static final int MOTHER = 1;
    public static final int MOTHER_EMOTION = 2;
    public static final int FATHER_COMMENT = 3;  // Feedback
    public static final int FATHER_ASK = 4;  // Before time slot ,  ASK
    public static final int FATHER_SUGGESTION = 5;  // After time slot  ,  SUGGESTION
    public static int TOTAL_PAGE; // TOTAL_PAGE
    public static Date NOW_TIME;
    public static boolean running = false;
    public static Resources res;
    public enum USER_TYPE_ENUM {
        MOTHER, FATHER
    }

    public enum ICON_GROUP {
        BABY_ACTIVITY, MOTHER_EMOTION, MOTHER_ACTIVITY, FATHER_COMMENT, FATHER_ASK ,FATHER_SUGGESTION
    }

    public static String USER_ID = "";
    public static  USER_TYPE_ENUM USER_TYPE;

    public ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static MySocketManager socketM;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("DOWN", Integer.toString(x) +" "+ Integer.toString(y));
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("MOVE", Integer.toString(x) +" "+ Integer.toString(y));
                break;
            case MotionEvent.ACTION_UP:
                Log.e("UP", Integer.toString(x) +" "+ Integer.toString(y));
                break;
        }
        return false;
    }

    public enum communicationType {
        MOTHER_STRESS_START, FATHER_WAITING, MOTHER_MSG
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.e("zz","start");
        running = true;
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.e("zz","stop");
        running = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getResources();

        //intent = this.getPackageManager().getLaunchIntentForPackage("com.garmin.android.apps.connectmobile");
        //MainActivity.this.startActivity(intent);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Start Garmin application to initiate Sync.
        Intent garminintent = this.getPackageManager().getLaunchIntentForPackage("com.garmin.android.apps.connectmobile");
        if(garminintent != null) {
            MainActivity.this.startActivity(garminintent);
        }else{
            Toast.makeText(getApplicationContext(), "GARMIN Connector를 설치해주세요." ,  Toast.LENGTH_SHORT).show();
        }

        // Also start my application running on the background.
        new Thread(new Runnable() { @Override public void run() {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e("new",e.toString());
            }

            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTask = am.getAppTasks();
            for(ActivityManager.AppTask task : appTask ) {
                ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
                String packageName = taskInfo.baseIntent.getComponent().getPackageName();
                if(packageName.equals(getPackageName()));
                    am.moveTaskToFront(task.getTaskInfo().id, 0);
            }
        }
        }).start();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        MyFileManager mFile = new MyFileManager();

        // If the user is already login
        String userID = mFile.getUserIdFromFile();
        if(mFile.getUserIdFromFile() != null){
            MainActivity.USER_ID = userID;

            if(userID.contains("MOTHER")){
                MainActivity.USER_TYPE = USER_TYPE_ENUM.MOTHER;
            }else if (userID.contains("FATHER")){
                MainActivity.USER_TYPE = USER_TYPE_ENUM.FATHER;
            }
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.nav_userid)).setText(userID);
        }else { // move login page
            Intent intent=new Intent(MainActivity.this, LoginSettingActivity.class);
            startActivity(intent);
            finish();
        }

        NOW_TIME = new Date();
        socketM = new MySocketManager(USER_ID);
        SimpleDateFormat save_date = new SimpleDateFormat("yyyyMMdda");
        socketM.setDataFromServer(SET_OPENAPP, save_date.format(NOW_TIME) , 0 , null);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        View content_main = findViewById(R.id.app_bar_main);

        mViewPager = (ViewPager) content_main.findViewById(R.id.content_viewpager);

        UpdateViewPager updateViewPager = new UpdateViewPager (mViewPager, mSectionsPagerAdapter);
        updateViewPager.execute((Void) null);

        //mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setCurrentItem(TOTAL_PAGE);
    }

    public class UpdateViewPager extends AsyncTask<Void, Void, Boolean> {

        ViewPager mViewPager;
        SectionsPagerAdapter mSectionsPagerAdapter;

        UpdateViewPager(ViewPager _a, SectionsPagerAdapter _b) {
            mViewPager = _a;
            mSectionsPagerAdapter = _b;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            socketM = new MySocketManager(USER_ID);
            SimpleDateFormat save_date = new SimpleDateFormat("yyyyMMdda");
            TOTAL_PAGE = Integer.parseInt(socketM.getDataFromServer(MySocketManager.SOCKET_MSG.GET_PAGE_COUNT, save_date.format(NOW_TIME)));
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(TOTAL_PAGE);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    */

    /*
    @Overrideaction_setting
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login){
            Intent intent=new Intent(MainActivity.this, LoginSettingActivity.class);
            startActivity(intent);
        }else if( id == R.id.nav_smartband){

            // Start garmin application to initiate Sync.
            Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.garmin.android.apps.connectmobile");
            MainActivity.this.startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        List <View> babyViewList = new ArrayList();
        List <View> motherViewList = new ArrayList();
        List <View> motherEmotionList = new ArrayList();
        List <View> motherEmotionListTemp = new ArrayList();
        List <View> fatherViewListA = new ArrayList();
        List <View> fatherViewListB = new ArrayList();
        List <View> fatherViewListTemp = new ArrayList();

        List <View> babyIconList = new ArrayList();
        List <View> motherIconList = new ArrayList();
        List <View> motherEmotionIconList = new ArrayList();
        List <View> fatherIconListComment = new ArrayList();
        List <View> fatherIconListAsk = new ArrayList();
        List <View> fatherIconListSuggest = new ArrayList();

        String motherEmotionString = "0 0 0 0 0 E1 0 0 0 E2 E4 E5";
        String motherActivityString = "M1 0 M2 0 0 0 M3 0 0 RE 0 0";
        String fatherCommentString = "C1 0 0 AS 0 C2 0 S1 0 C3 0 0";
        String babyActivityUpString = "B1 DI B2 0 0 SL B3 B3 B3 0 B1 0";
        String babyActivityDownString = "B1 SL B3 0 SL 0 0 0 HO B2 B2 0";
        String[] verlabels = new String[] { "", "High", "Medium" , "Low" , ""};

        int nowHour;
        TableLayout icon_table;
        Date THIS_TIME;
        SimpleDateFormat save_date;

        int [] values;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.content_main, container, false);
            int page_num = getArguments().getInt(ARG_SECTION_NUMBER);

            values = new int[240];

            /*
            Random a = new Random();
            for(int i=0 ; i<240 ;i++){
                values[ i] = a.nextInt(100);
            }
            */

            SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMMM (a)");
            SimpleDateFormat time = new SimpleDateFormat("HH");
            SimpleDateFormat real_time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            save_date = new SimpleDateFormat("yyyyMMdda");

            Date ttoday = new Date();

            THIS_TIME = new Date();
            THIS_TIME.setTime(NOW_TIME.getTime() - ( (12 * 60 * 60 * 1000)* (TOTAL_PAGE - page_num) ));

            final Calendar cal_for_stress_request_start = Calendar.getInstance();
            final Calendar cal_for_stress_request_end = Calendar.getInstance();

            cal_for_stress_request_start.setTime(THIS_TIME);
            cal_for_stress_request_end.setTime(THIS_TIME);

            if ( save_date.format(THIS_TIME).contains("AM")){ //
                ((TableRow)rootView.findViewById(R.id.time_am)).setVisibility(TableRow.VISIBLE);
                ((TableRow)rootView.findViewById(R.id.time_pm)).setVisibility(TableRow.GONE);
                cal_for_stress_request_start.set(cal_for_stress_request_start.get(Calendar.YEAR) , cal_for_stress_request_start.get(Calendar.MONTH) , cal_for_stress_request_start.get(Calendar.DAY_OF_MONTH) ,
                        0 , 0 , 0 );
                cal_for_stress_request_end.set(cal_for_stress_request_end.get(Calendar.YEAR) , cal_for_stress_request_end.get(Calendar.MONTH) , cal_for_stress_request_end.get(Calendar.DAY_OF_MONTH) ,
                        23 , 59, 59);
                cal_for_stress_request_end.setTimeInMillis( cal_for_stress_request_end.getTimeInMillis()+1000 );
            }else { // PM
                ((TableRow)rootView.findViewById(R.id.time_am)).setVisibility(TableRow.GONE);
                ((TableRow)rootView.findViewById(R.id.time_pm)).setVisibility(TableRow.VISIBLE);
                cal_for_stress_request_start.set(cal_for_stress_request_start.get(Calendar.YEAR) , cal_for_stress_request_start.get(Calendar.MONTH) , cal_for_stress_request_start.get(Calendar.DAY_OF_MONTH) ,
                        0 , 0 , 0 );
                cal_for_stress_request_end.set(cal_for_stress_request_end.get(Calendar.YEAR) , cal_for_stress_request_end.get(Calendar.MONTH) , cal_for_stress_request_end.get(Calendar.DAY_OF_MONTH) ,
                        23 , 59 , 59 );
                cal_for_stress_request_end.setTimeInMillis( cal_for_stress_request_end.getTimeInMillis()+1000 );
            }

            ((TextView)rootView.findViewById(R.id.text_date)).setText(date.format(THIS_TIME));

            String now = time.format(THIS_TIME);

            if(now.compareTo("00") == 0) nowHour =0; else if(now.compareTo("01") == 0) nowHour = 1;
            else if (now.compareTo("02") == 0) nowHour = 2; else if (now.compareTo("03") == 0) nowHour = 3;
            else if (now.compareTo("04") == 0) nowHour = 4; else if (now.compareTo("05") == 0) nowHour = 5;
            else if (now.compareTo("06") == 0) nowHour = 6; else if (now.compareTo("07") == 0) nowHour = 7;
            else if (now.compareTo("08") == 0) nowHour = 8; else if (now.compareTo("09") == 0) nowHour = 9;
            else if (now.compareTo("10") == 0) nowHour = 10; else if(now.compareTo("11") == 0) nowHour = 11;
            else if(now.compareTo("12") == 0) nowHour = 0; else if(now.compareTo("13") == 0) nowHour = 1;
            else if(now.compareTo("14") == 0) nowHour = 2; else if(now.compareTo("15") == 0) nowHour = 3;
            else if(now.compareTo("16") == 0) nowHour = 4; else if(now.compareTo("17") == 0) nowHour = 5;
            else if(now.compareTo("18") == 0) nowHour = 6; else if(now.compareTo("19") == 0) nowHour = 7;
            else if(now.compareTo("20") == 0) nowHour = 8; else if(now.compareTo("21") == 0) nowHour = 9;
            else if(now.compareTo("22") == 0) nowHour = 10; else if(now.compareTo("23") == 0) nowHour = 11;

            final ProgressBar loading = ((ProgressBar)rootView.findViewById(R.id.stress_progress));
            final RelativeLayout graphView = ((RelativeLayout)rootView.findViewById(R.id.graph_view));
            final GraphView stressgraph;
            stressgraph = new GraphView(getContext() , values, "", null , verlabels , 60);

            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            stressgraph.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT , height/9 ));
            graphView.addView(stressgraph);


            if( MainActivity.USER_TYPE == USER_TYPE_ENUM.MOTHER){
                ((TextView)rootView.findViewById(R.id.father_text1)).setTextColor(Color.parseColor("#cacaca"));
                ((TextView)rootView.findViewById(R.id.father_text2)).setTextColor(Color.parseColor("#cacaca"));
                ((TextView)rootView.findViewById(R.id.father_text3)).setTextColor(Color.parseColor("#cacaca"));
                ((TextView)rootView.findViewById(R.id.mother_text1)).setTextColor(Color.parseColor("#000000"));
                ((TextView)rootView.findViewById(R.id.mother_text2)).setTextColor(Color.parseColor("#000000"));
                ((TextView)rootView.findViewById(R.id.mother_text3)).setTextColor(Color.parseColor("#000000"));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m4))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_thumsup_grey, null));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m5))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_ask_grey, null));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m6))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rise_sug_grey, null));

            }else if (USER_TYPE == USER_TYPE_ENUM.FATHER){
                ((TextView)rootView.findViewById(R.id.father_text1)).setTextColor(Color.parseColor("#000000"));
                ((TextView)rootView.findViewById(R.id.father_text2)).setTextColor(Color.parseColor("#000000"));
                ((TextView)rootView.findViewById(R.id.father_text3)).setTextColor(Color.parseColor("#000000"));
                ((TextView)rootView.findViewById(R.id.mother_text1)).setTextColor(Color.parseColor("#cacaca"));
                ((TextView)rootView.findViewById(R.id.mother_text2)).setTextColor(Color.parseColor("#cacaca"));
                ((TextView)rootView.findViewById(R.id.mother_text3)).setTextColor(Color.parseColor("#cacaca"));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m1))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_m_grey, null));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m2))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rest_grey, null));
                ((ImageView)(rootView.findViewById(R.id.activity_btn_m3))).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv3_grey, null));
            }


            babyViewList.clear(); motherViewList.clear(); motherEmotionList.clear(); ; motherEmotionListTemp.clear();
            fatherViewListA.clear(); fatherViewListB.clear(); babyIconList.clear(); motherIconList.clear(); fatherViewListTemp.clear();
            motherEmotionIconList.clear(); fatherIconListComment.clear(); fatherIconListAsk.clear(); fatherIconListSuggest.clear();

            icon_table = (TableLayout) rootView.findViewById(R.id.icon_table);
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t1));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t2));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t3));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t4));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t5));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t6));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t7));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t8));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t9));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t10));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t11));
            motherEmotionListTemp.add(rootView.findViewById(R.id.emotion_t12));

            motherViewList.add(rootView.findViewById(R.id.schedule_t1));
            motherViewList.add(rootView.findViewById(R.id.schedule_t2));
            motherViewList.add(rootView.findViewById(R.id.schedule_t3));
            motherViewList.add(rootView.findViewById(R.id.schedule_t4));
            motherViewList.add(rootView.findViewById(R.id.schedule_t5));
            motherViewList.add(rootView.findViewById(R.id.schedule_t6));
            motherViewList.add(rootView.findViewById(R.id.schedule_t7));
            motherViewList.add(rootView.findViewById(R.id.schedule_t8));
            motherViewList.add(rootView.findViewById(R.id.schedule_t9));
            motherViewList.add(rootView.findViewById(R.id.schedule_t10));
            motherViewList.add(rootView.findViewById(R.id.schedule_t11));
            motherViewList.add(rootView.findViewById(R.id.schedule_t12));

            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m1));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m2));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m3));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m4));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m5));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m6));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m7));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m8));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m9));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m10));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m11));
            fatherViewListTemp.add(rootView.findViewById(R.id.schedule_m12));

            babyViewList.add(rootView.findViewById(R.id.schedule_b1));
            babyViewList.add(rootView.findViewById(R.id.schedule_b2));
            babyViewList.add(rootView.findViewById(R.id.schedule_b3));
            babyViewList.add(rootView.findViewById(R.id.schedule_b4));
            babyViewList.add(rootView.findViewById(R.id.schedule_b5));
            babyViewList.add(rootView.findViewById(R.id.schedule_b6));
            babyViewList.add(rootView.findViewById(R.id.schedule_b7));
            babyViewList.add(rootView.findViewById(R.id.schedule_b8));
            babyViewList.add(rootView.findViewById(R.id.schedule_b9));
            babyViewList.add(rootView.findViewById(R.id.schedule_b10));
            babyViewList.add(rootView.findViewById(R.id.schedule_b11));
            babyViewList.add(rootView.findViewById(R.id.schedule_b12));

            babyIconList.add(rootView.findViewById(R.id.activity_btn_t1));
            babyIconList.add(rootView.findViewById(R.id.activity_btn_t2));
            babyIconList.add(rootView.findViewById(R.id.activity_btn_t3));
            babyIconList.add(rootView.findViewById(R.id.activity_btn_t4));
            babyIconList.add(rootView.findViewById(R.id.activity_btn_t5));
            babyIconList.add(rootView.findViewById(R.id.activity_btn_t6));

            motherIconList.add(rootView.findViewById(R.id.activity_btn_m1));
            motherIconList.add(rootView.findViewById(R.id.activity_btn_m2));
            motherEmotionIconList.add(rootView.findViewById(R.id.activity_btn_m3));
            fatherIconListComment.add(rootView.findViewById(R.id.activity_btn_m4));
            fatherIconListAsk.add(rootView.findViewById(R.id.activity_btn_m5));
            fatherIconListSuggest.add(rootView.findViewById(R.id.activity_btn_m6));

            for(int i=0 ; i< fatherViewListTemp.size() ; i++){
                if( i < nowHour){ // Before time slot
                    fatherViewListA.add(fatherViewListTemp.get(i));
                }
                else{ // After time slot
                    fatherViewListB.add(fatherViewListTemp.get(i));
                }
            }

            // ONLY RIGHT_NOW PAGE shows this time
            if(page_num == TOTAL_PAGE) {
                motherViewList.get(nowHour).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));
                fatherViewListB.get(0).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));
                babyViewList.get(nowHour).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));
            }

            for(int i=0 ; i< babyViewList.size() ; i++){
                babyViewList.get(i).setOnDragListener(new MyDragListener(BABY,i));
                babyViewList.get(i).setOnClickListener(new MyTableClearListener(BABY,i));
                babyViewList.get(i).setOnLongClickListener(new MyTableLongListener());
                babyViewList.get(i).setLabelFor(i);
            }
            for(int i=0 ; i< motherViewList.size() ; i++){
                motherViewList.get(i).setOnDragListener(new MyDragListener(MOTHER,i));
                motherViewList.get(i).setOnClickListener(new MyTableClearListener(MOTHER,i));
                motherViewList.get(i).setOnLongClickListener(new MyTableLongListener());
                motherViewList.get(i).setLabelFor(i);
            }

            for(int i=0 ; i< fatherViewListTemp.size() ; i++){
                fatherViewListTemp.get(i).setLabelFor(i);
                fatherViewListTemp.get(i).setOnClickListener(new MyTableClearListener(FATHER_COMMENT,i));
                fatherViewListTemp.get(i).setOnLongClickListener(new MyTableLongListener());
            }

            for(int i=0 ; i< fatherViewListA.size() ; i++){
                fatherViewListA.get(i).setOnDragListener(new MyDragListener(FATHER_ASK,i));

            }
            for(int i=0 ; i< fatherViewListB.size() ; i++){
                fatherViewListB.get(i).setOnDragListener(new MyDragListener(FATHER_SUGGESTION,i));
            }



            UpdateInterface mMotherActTask = new UpdateInterface(motherViewList, MySocketManager.SOCKET_MSG.GET_MOTHERACTIVITY , null, save_date.format(THIS_TIME));
            mMotherActTask.execute((Void) null);

            UpdateInterface mMotherEmotionTask = new UpdateInterface(motherEmotionListTemp, MySocketManager.SOCKET_MSG.GET_MOTHEREMOTION , null, save_date.format(THIS_TIME));
            mMotherEmotionTask.execute((Void) null);

            UpdateInterface mFatherCommentTask = new UpdateInterface(fatherViewListTemp, MySocketManager.SOCKET_MSG.GET_FATHERCOMMENT, null, save_date.format(THIS_TIME));
            mFatherCommentTask.execute((Void) null);

            UpdateInterface mBabyActTask = new UpdateInterface(babyViewList, MySocketManager.SOCKET_MSG.GET_BABYACTIVITY_UP, null , save_date.format(THIS_TIME));
            mBabyActTask.execute((Void) null);

            mBabyActTask = new UpdateInterface(babyViewList, MySocketManager.SOCKET_MSG.GET_BABYACTIVITY_DOWN, null , save_date.format(THIS_TIME));
            mBabyActTask.execute((Void) null);


            new Thread(new Runnable() { @Override public void run() {
                try {
                    UpdateStressData mStressUpdate = new UpdateStressData(getContext(), graphView , stressgraph , loading, Long.toString(cal_for_stress_request_start.getTimeInMillis() / 1000) , Long.toString(cal_for_stress_request_end.getTimeInMillis() / 1000)) ;
                    mStressUpdate.execute((Void) null);

                    /*
                    Thread.sleep(1000 * 20 );  // wait 20 seconds to update stressGraph again
                    if(running){
                        mStressUpdate = new UpdateStressData(getContext(), graphView , stressgraph , loading, Long.toString(cal_for_stress_request_start.getTimeInMillis() / 1000) , Long.toString(cal_for_stress_request_end.getTimeInMillis() / 1000)) ;
                        mStressUpdate.execute((Void) null);
                    }
                    */
                } catch (Exception e) {
                    Log.e("stressUpdateThread",e.toString());
                }
            }
            }).start();


            //textView.setText( Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        // To show only proper schedule box, otherwise to show X
        private int findScheduleBoxGroup(View id){
            for(int i=0 ; i < babyViewList.size() ; i++){
                if( babyViewList.get(i) == id){
                    return BABY;
                }
            }
            for(int i=0 ; i < motherViewList.size() ; i++){
                if( motherViewList.get(i) == id){
                    return MOTHER;
                }
            }
            for(int i=0 ; i < motherEmotionList.size() ; i++){
                if ( motherEmotionList.get(i) == id){
                    return MOTHER_EMOTION;
                }
            }
            for(int i=0 ; i < fatherViewListA.size() ; i++){
                if( fatherViewListA.get(i) == id){
                    return FATHER_ASK;
                }
            }
            for(int i=0 ; i < fatherViewListB.size() ; i++){
                if( fatherViewListB.get(i) == id){
                    return FATHER_SUGGESTION;
                }
            }
            return -1;
        }

        // To show only proper schedule box, otherwise to show X
        private int findIconGroup(View id){
            for(int i=0 ; i < babyIconList.size() ; i++){
                if( babyIconList.get(i) == id){
                    return BABY;
                }
            }
            for(int i=0 ; i < motherIconList.size() ; i++){
                if( motherIconList.get(i) == id){
                    return MOTHER;
                }
            }
            for(int i=0 ; i < motherEmotionIconList.size() ; i++){
                if ( motherEmotionIconList.get(i) == id){
                    return MOTHER_EMOTION;
                }
            }
            for(int i=0 ; i < fatherIconListComment.size() ; i++){
                if( fatherIconListComment.get(i) == id){
                    return FATHER_COMMENT;
                }
            }
            for(int i=0 ; i < fatherIconListAsk.size() ; i++){
                if( fatherIconListAsk.get(i) == id){
                    return FATHER_ASK;
                }
            }
            for(int i=0 ; i < fatherIconListSuggest.size() ; i++){
                if( fatherIconListSuggest.get(i) == id){
                    return FATHER_SUGGESTION;
                }
            }
            return -1;
        }

        private List<Boolean> findStressfullBox(){
            List <Boolean> motherEmotionBox = new ArrayList<>();
            int count = 0;
            boolean angry = false;
            for(int i=0 ; i<values.length ; i++){
                count ++;
                if(values[i] >= GraphView.HIGH_STRESS_SCORE){
                    angry = true;
                }
                if ( count % 20 == 0){
                    motherEmotionBox.add(angry);
                    angry = false;
                }
            }
            return motherEmotionBox;
        }

        private final class MyIconTouchClickListener implements View.OnTouchListener {

            ICON_GROUP boxOwner;
            int id;

            MyIconTouchClickListener(ICON_GROUP _boxO , int _id){
                boxOwner = _boxO;
            }

            public boolean onTouch(View v, MotionEvent event) {
                int NONE = 0;
                int DRAG = 1;
                int MAX_MOVE = 170;
                int m_mode = NONE;

                final int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        m_mode = NONE;
                        boolean isPossible = false;
                        if(USER_TYPE == USER_TYPE_ENUM.MOTHER){
                            for(int i=0 ;i<motherEmotionList.size() ; i++){
                                if ( ((ViewGroup)motherEmotionList.get(i)).getChildCount() <= 0 ){
                                    isPossible= true;
                                    break;
                                }
                            }
                            if(isPossible && boxOwner != ICON_GROUP.MOTHER_EMOTION){
                                Toast.makeText(getContext(), "'엄마 감정' 란을 모두 채워주세요." ,  Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if(USER_TYPE == USER_TYPE_ENUM.FATHER &&  (boxOwner == ICON_GROUP.MOTHER_ACTIVITY || boxOwner == ICON_GROUP.MOTHER_EMOTION)) {
                            Toast.makeText(getContext(), "Can't use mother's icon" ,  Toast.LENGTH_SHORT).show();
                            break;
                        }else if (USER_TYPE == USER_TYPE_ENUM.MOTHER &&  (boxOwner == ICON_GROUP.FATHER_ASK || boxOwner == ICON_GROUP.FATHER_COMMENT || boxOwner == ICON_GROUP.FATHER_SUGGESTION) ) {
                            Toast.makeText(getContext(), "Can't use father's icon" ,  Toast.LENGTH_SHORT).show();
                            break;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if(USER_TYPE == USER_TYPE_ENUM.MOTHER){
                            isPossible = false;
                            for(int i=0 ;i<motherEmotionList.size() ; i++){
                                if ( ((ViewGroup)motherEmotionList.get(i)).getChildCount() <= 0 ){
                                    isPossible= true;
                                    break;
                                }
                            }
                            if(isPossible && boxOwner != ICON_GROUP.MOTHER_EMOTION){
                                break;
                            }
                        }
                        if(USER_TYPE == USER_TYPE_ENUM.FATHER &&  (boxOwner == ICON_GROUP.MOTHER_ACTIVITY || boxOwner == ICON_GROUP.MOTHER_EMOTION)) {
                            break;
                        }else if (USER_TYPE == USER_TYPE_ENUM.MOTHER &&  (boxOwner == ICON_GROUP.FATHER_ASK || boxOwner == ICON_GROUP.FATHER_COMMENT || boxOwner == ICON_GROUP.FATHER_SUGGESTION) ) {
                            break;
                        }

                        if (Math.abs(event.getX()) + Math.abs(event.getY()) > MAX_MOVE ) {
                            m_mode = DRAG;
                            ClipData data = ClipData.newPlainText("", "");
                            View.DragShadowBuilder shadowBuilder = new CanvasShadowCustom(v);
                            v.startDrag(data, shadowBuilder, v, 0);
                            Drawable noShape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_no, null);
                            Drawable Draging = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_draging, null);

                            icon_table.setForeground(Draging);

                            switch (findIconGroup(v)) {
                                case BABY:
                                    for (int i = 0; i < motherViewList.size(); i++) {
                                        motherViewList.get(i).setForeground(noShape);//.setBackground(noShape);
                                    }
                                    for (int i = 0; i < motherEmotionList.size(); i++) {
                                        motherEmotionList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < fatherViewListTemp.size(); i++) {
                                        fatherViewListTemp.get(i).setForeground(noShape);
                                    }
                                    break;
                                case MOTHER:
                                    for (int i = 0; i < babyViewList.size(); i++) {
                                        babyViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherEmotionList.size(); i++) {
                                        motherEmotionList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < fatherViewListTemp.size(); i++) {
                                        fatherViewListTemp.get(i).setForeground(noShape);
                                    }
                                    break;
                                case MOTHER_EMOTION:
                                    for (int i = 0; i < babyViewList.size(); i++) {
                                        babyViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherViewList.size(); i++) {
                                        motherViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < fatherViewListTemp.size(); i++) {
                                        fatherViewListTemp.get(i).setForeground(noShape);
                                    }
                                    break;
                                case FATHER_COMMENT:
                                    for (int i = 0; i < babyViewList.size(); i++) {
                                        babyViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherViewList.size(); i++) {
                                        motherViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherEmotionList.size(); i++) {
                                        motherEmotionList.get(i).setForeground(noShape);
                                    }
                                    break;
                                case FATHER_ASK:
                                    for (int i = 0; i < babyViewList.size(); i++) {
                                        babyViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherViewList.size(); i++) {
                                        motherViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherEmotionList.size(); i++) {
                                        motherEmotionList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < fatherViewListB.size(); i++) {
                                        fatherViewListB.get(i).setForeground(noShape);
                                    }
                                    break;
                                case FATHER_SUGGESTION:
                                    for (int i = 0; i < babyViewList.size(); i++) {
                                        babyViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherViewList.size(); i++) {
                                        motherViewList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < motherEmotionList.size(); i++) {
                                        motherEmotionList.get(i).setForeground(noShape);
                                    }
                                    for (int i = 0; i < fatherViewListA.size(); i++) {
                                        fatherViewListA.get(i).setForeground(noShape);
                                    }
                                    break;
                                default:
                                    Log.e("ERROR", "Group Error happen");
                                    break;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (m_mode == DRAG) {
                            break;
                        }
                        v.performClick();
                        break;
                }
                return true;
            }
        }

        private final class MyIconCustomDialog implements View.OnClickListener{

            private CustomDialogIcon.Type id;

            MyIconCustomDialog(CustomDialogIcon.Type i){
                id = i;
            }

            public void onClick(View v) {
                final CustomDialogIcon mCustomDialog;
                final ImageView vv = (ImageView)v;
                mCustomDialog = new CustomDialogIcon(getContext(), id /* CustomDialogIcon.BABY_FEED */, v.getMeasuredWidth(), v.getMeasuredHeight());
                mCustomDialog.setMyDialogListener(new CustomDialogIcon.MyDialogListener() {
                    public void userSelectedAValue(Drawable draw) {
                        ((ImageView)vv).setImageDrawable(draw);
                        mCustomDialog.dismiss();
                    }
                });
                mCustomDialog.show();

            }
        }

        private final class FatherAlertListener implements View.OnClickListener{
            communicationType type;
            int listid;
            LinearLayout view;

            public FatherAlertListener(LinearLayout _view , communicationType _type, int i){
                view = _view ;type = _type; listid = i;
            }

            public void onClick(View v) {

                final ImageView tt = (ImageView) v;

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                switch(type){
                    case MOTHER_STRESS_START:
                            alertDialogBuilder
                                    .setIcon(R.drawable.ic_emotion_lv1)
                                    .setTitle("Mother is stressful")
                                    //.setMessage("Mother looks very stressful. \n\nAsking why she does makes her be happy. \n\n Will you contact her soon?")
                                    .setMessage("엄마의 기분이 좋지 않은 상태입니다. \n\n 엄마의 기분이 왜 안좋았는지 물어 보는 것이 스트레스 해소에 큰 도움이 됩니다. \n\n 엄마의 기분이 왜 안좋은지 물어 볼 것인가요?")
                                    .setCancelable(false)
                                    .setNegativeButton("아니오, 아직입니다", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Toast.makeText(getContext(), "배우자를 위해서 관심 가져보는 것을 추천드립니다." ,  Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            view.setOnClickListener(null);
                                            Toast.makeText(getContext(), "감사합니다." ,  Toast.LENGTH_SHORT).show();
                                            tt.setOnClickListener(new FatherAlertListener(view,communicationType.FATHER_WAITING,listid));
                                            view.getChildAt(0).clearAnimation();
                                            MySocketManager socketM = new MySocketManager(USER_ID);
                                            socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT , save_date.format(THIS_TIME) , listid , "SM") ;  // Mother emoticon index is different
                                        }
                                    });
                            break;

                    case FATHER_WAITING:
                        alertDialogBuilder
                                .setIcon(R.drawable.ic_emotion_lv1)
                                .setTitle("Mother is stressful")
                                //.setMessage("Mother looks very stressful. \n\nAsking why she does makes her be happy. \n\n Will you contact her soon?")
                                .setMessage("엄마가 연락을 기다리고 있습니다. 전화나 문자를 통해서 물어보세요. 바쁘시다면 집에 가서 물어보셔도 좋습니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        break;


                    case MOTHER_MSG:
                        alertDialogBuilder
                                .setIcon(R.drawable.ic_father_heart)
                                .setMessage("배우자가 당신의 높은 스트레스 지수에 대해서 왜 그런지 궁금해 하고 있습니다. \n\n 배우자에게서 연락을 받으셨나요?")
                                .setCancelable(false)
                                .setNegativeButton("아니오, 아직입니다.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        view.getChildAt(0).clearAnimation();
                                        MySocketManager socketM = new MySocketManager(USER_ID);
                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT , save_date.format(THIS_TIME) , listid , "SW") ;  // Mother emoticon index is different
                                    }
                                })
                                .setPositiveButton("네, 받았습니다.", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        view.getChildAt(0).clearAnimation();
                                        view.setOnClickListener(null);
                                        view.removeView(view.getChildAt(0));
                                        ImageView addImage = new ImageView(getContext());
                                        addImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_couple_heart, null));
                                        view.addView(addImage);
                                        MySocketManager socketM = new MySocketManager(USER_ID);
                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT , save_date.format(THIS_TIME) , listid , "CO") ;  // Mother emoticon index is different
                                    }
                                });
                        break;


                    }
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

            }
        }

        private final class MyTableLongListener implements View.OnLongClickListener {

            @Override
            public boolean onLongClick(View v) {
                String iconText = null;
                String piece = null;

                if(res !=null && ((LinearLayout) v).getChildCount() > 0){
                    piece = getDrawableString(res, ((ImageView) ((LinearLayout) v).getChildAt(0)).getDrawable());

                    if (piece.equals("B1")) {
                        iconText="'적은양'의 식사를 하였습니다.";
                    } else if (piece.equals("B2")) {
                        iconText="'중간양'의 식사를 하였습니다.";
                    } else if (piece.equals("B3")) {
                        iconText="'많은양'의 식사를 하였습니다.";
                    } else if (piece.equals("DI")) {
                        iconText="기저귀를 갈았습니다.";
                    } else if (piece.equals("DU")) {
                        iconText="대변을 보았습니다.";
                    } else if (piece.equals("SL")) {
                        iconText="잠을 잤습니다.";
                    } else if (piece.equals("BA")) {
                        iconText="목욕을 하였습니다.";
                    } else if (piece.equals("HO")) {
                        iconText="아기를 보거나 달래고 있습니다.";
                    } else if (piece.equals("M1")) {
                        iconText="'적은양'의 식사를 하였습니다.";
                    } else if (piece.equals("M2")) {
                        iconText="'중간양'의 식사를 하였습니다.";
                    } else if (piece.equals("M3")) {
                        iconText="'많은양'의 식사를 하였습니다.";
                    } else if (piece.equals("RE")) {
                        iconText="휴식 시간입니다.";
                    } else if (piece.equals("E1")) {
                        iconText="기분 아주 나쁨";
                    } else if (piece.equals("E2")) {
                        iconText="기분 나쁨";
                    } else if (piece.equals("E3")) {
                        iconText="기분 보통";
                    } else if (piece.equals("E4")) {
                        iconText="기분 좋음";
                    } else if (piece.equals("E5")) {
                        iconText="기분 아주 좋음";
                    } else if (piece.equals("C1")) {
                        iconText="하트로 답을 하였습니다.";
                    } else if (piece.equals("C2")) {
                        iconText="장미꽃으로 답을 하였습니다.";
                    } else if (piece.equals("C3")) {
                        iconText="따봉으로 답을 하였습니다.";
                    } else if (piece.equals("C4")) {
                        iconText="스마일로 답을 하였습니다.";
                    } else if (piece.equals("AS")) {
                        iconText="이때 아기가 뭐했는지 궁금해합니다.";
                    } else if (piece.equals("S1")) {
                        iconText="이때 식사를 하면 어떨지 권하고 있습니다.";
                    } else if (piece.equals("S2")) {
                        iconText="이때 휴식시간을 가지면 어떨지 권하고 있습니다.";
                    } else if (piece.equals("WN") && USER_TYPE_ENUM.FATHER == USER_TYPE) {

                    } else if (piece.equals("SM")) {

                    } else if (piece.equals("SW")) {

                    } else if (piece.equals("CO")) {
                        iconText="엄마의 스트레스 받는 일에 대하여 공유하였습니다.";
                    }
                    Toast.makeText(getContext(), iconText ,  Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        private final class MyTableClearListener implements View.OnClickListener{

            int boxOwner;
            int array_id;

            MyTableClearListener(int _boxOwner , int _id){
                boxOwner = _boxOwner;
                array_id = _id;
            }

            public void onClick(final View v) {

                if(boxOwner == MOTHER && USER_TYPE == USER_TYPE_ENUM.FATHER){
                    return ;
                }
                if(boxOwner == MOTHER_EMOTION && USER_TYPE == USER_TYPE_ENUM.FATHER){
                    return ;
                }
                if(boxOwner == FATHER_COMMENT && USER_TYPE == USER_TYPE_ENUM.MOTHER){
                    return;
                }

                    final LinearLayout container = (LinearLayout) v;
                    if (((ViewGroup) container).getChildCount() > 0) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder
                                .setMessage("기록 된 내용을 삭제하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int index = 0; index < ((ViewGroup) container).getChildCount(); ++index) {

                                            switch(boxOwner){
                                                case BABY:
                                                    socketM = new MySocketManager(USER_ID);
                                                    if( ((ViewGroup) container).getChildCount() > 1){
                                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_BABYACTIVITY_DOWN, save_date.format(THIS_TIME) , array_id, "0");
                                                    }else {
                                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_BABYACTIVITY_UP, save_date.format(THIS_TIME) , array_id ,  "0");
                                                    }
                                                    break;
                                                case MOTHER:
                                                    socketM = new MySocketManager(USER_ID);
                                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHERACTIVITY, save_date.format(THIS_TIME) , array_id , "0") ;
                                                    break;
                                                case MOTHER_EMOTION:
                                                    socketM = new MySocketManager(USER_ID);
                                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHEREMOTION, save_date.format(THIS_TIME) , v.getLabelFor() , "0") ;  // Mother emoticon index is different
                                                    break;
                                                case FATHER_COMMENT:
                                                    socketM = new MySocketManager(USER_ID);
                                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT, save_date.format(THIS_TIME) , v.getLabelFor() , "0") ;
                                                    break;
                                            }

                                            ((ViewGroup) container).removeView(((ViewGroup) container).getChildAt(index));
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
        }

        class MyDragListener implements View.OnDragListener {
            Drawable enterShape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_drop, null);
            Drawable normalShape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape, null);
            Drawable normalNowShape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null);

            int boxOwner;
            int id;

            MyDragListener(int _boxOwner , int _id){
                boxOwner = _boxOwner;
                id = _id;
            }

            @Override
            public boolean onDrag(View v, DragEvent event) {

                boolean checkProper = false;
                boolean drag_start = false;
                if(findScheduleBoxGroup(v) == findIconGroup((View)event.getLocalState()) || (findScheduleBoxGroup(v) == FATHER_ASK && findIconGroup((View)event.getLocalState()) == FATHER_COMMENT) ||  (findScheduleBoxGroup(v) == FATHER_SUGGESTION && findIconGroup((View)event.getLocalState()) == FATHER_COMMENT) ){
                    checkProper = true;
                }

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        //v.setVisibility(View.INVISIBLE);
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (checkProper) {
                            v.setBackground(enterShape);
                        }
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        if (checkProper) {
                            if(nowHour == v.getLabelFor() && boxOwner != MOTHER_EMOTION )
                                v.setBackground(normalNowShape);
                            else
                                v.setBackground(normalShape);
                        }
                        break;

                    case DragEvent.ACTION_DROP:
                        if(checkProper) {
                            // Dropped, reassign View to ViewGroup
                            View view = (View) event.getLocalState();
                            //ViewGroup owner = (ViewGroup) view.getParent();
                            //owner.removeView(view);
                            LinearLayout container = (LinearLayout) v;
                            int maxIcon=0;

                            if(boxOwner == BABY){
                                maxIcon = 2;
                            }else{
                                maxIcon = 1;
                            }

                            if (((ViewGroup) container).getChildCount() >= maxIcon) {
                                for (int index = 0; index < ((ViewGroup) container).getChildCount(); ++index) {
                                    //View nextChild = ((ViewGroup)container).getChildAt(index).setVisibility(View.GONE);
                                    ((ViewGroup) container).removeView(((ViewGroup) container).getChildAt(index));
                                    break;
                                }
                            }
                            // Added the following to copy the old view's bitmap to a new ImageView:
                            ImageView oldView = (ImageView) view;
                            ImageView newView = new ImageView(getContext());
                            newView.setImageDrawable(oldView.getDrawable());
                            container.addView(newView);                       // Changed to add new view instead

                            // if( newView.getDrawable().getAlpha() < 255){
                            // 253 - very bad , 254 - bad

                            String save_icon_string = getDrawableString( getResources()  ,newView.getDrawable());
                            switch(boxOwner){
                                case BABY:
                                    socketM = new MySocketManager(USER_ID);
                                    if( ((ViewGroup) container).getChildCount() > 1){
                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_BABYACTIVITY_DOWN, save_date.format(THIS_TIME) , id ,  save_icon_string);
                                    }else {
                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_BABYACTIVITY_UP, save_date.format(THIS_TIME) , id ,  save_icon_string);
                                    }
                                    break;
                                case MOTHER:
                                    socketM = new MySocketManager(USER_ID);
                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHERACTIVITY, save_date.format(THIS_TIME) , id , save_icon_string) ;
                                    break;
                                case MOTHER_EMOTION:
                                    // When mother is stressful
                                    if( getDrawableString( getResources()  ,newView.getDrawable()).compareTo("E1") == 0 || getDrawableString( getResources()  ,newView.getDrawable()).compareTo("E2") == 0 ){
                                        socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT , save_date.format(THIS_TIME) , v.getLabelFor() , "WN") ;  // Mother emoticon index is different
                                    }

                                    socketM = new MySocketManager(USER_ID);
                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHEREMOTION, save_date.format(THIS_TIME) , v.getLabelFor() , save_icon_string) ;  // Mother emoticon index is different
                                    break;
                                case FATHER_COMMENT:
                                case FATHER_ASK:
                                case FATHER_SUGGESTION:
                                    socketM = new MySocketManager(USER_ID);
                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHERCOMMENT, save_date.format(THIS_TIME) , v.getLabelFor() , save_icon_string) ;
                                    break;
                            }
                        }
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        if(nowHour == v.getLabelFor() && boxOwner != MOTHER_EMOTION)
                            v.setBackground(normalNowShape);
                        else
                            v.setBackground(normalShape);

                        if(icon_table.getForeground() != null)
                            icon_table.setForeground(null);

                        v.setForeground(null);
                        View originalview = (View) event.getLocalState();
                        ImageView oldImage = (ImageView) originalview;
                        oldImage.setVisibility(ImageView.VISIBLE);
                    default:
                        break;
                }
                return true;
            }
        }

        public static String getDrawableString(Resources a , Drawable b) {
            if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_food_small, null), b)) {
                return "B1";
            } else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_food_medium, null), b)){
                return "B2";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_food_large, null), b)){
                return "B3";
            } else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.diaper_pee, null), b)){
                return "DI";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_baby_poop, null), b)){
                return "DU";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_sleeping_baby, null), b)){
                return "SL";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_bath, null), b)){
                return "B1";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_motherhood, null), b)){
                return "HO";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_rise_s, null), b)){
                return "M1";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_rise_m, null), b)){
                return "M2";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_rise_l, null), b)){
                return "M3";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_rest, null), b)){
                return "RE";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_emotion_lv1, null), b)){
                return "E1";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_emotion_lv2, null), b)){
                return "E2";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_emotion_lv3, null), b)){
                return "E3";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_emotion_lv4, null), b)){
                return "E4";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_emotion_lv5, null), b)){
                return "E5";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_heart, null), b)){
                return "C1";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_rose, null), b)){
                return "C2";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_thumsup, null), b)){
                return "C3";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_smile, null), b)){
                return "C4";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_ask, null), b)){
                return "AS";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_rise_sug, null), b)){
                return "S1";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_rest_sug, null), b)){
                return "S2";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_father_warning, null), b)){
                return "WN";
            } else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_sms, null), b)){
                return "SM";
            }else if (areDrawablesIdentical(ResourcesCompat.getDrawable(a, R.drawable.ic_couple_heart, null), b)){
                return "CO";
            }
            return "NULL";
        }

        public static boolean areDrawablesIdentical(Drawable drawableA, Drawable drawableB) {
            Drawable.ConstantState stateA = drawableA.getConstantState();
            Drawable.ConstantState stateB = drawableB.getConstantState();
            // If the constant state is identical, they are using the same drawable resource.
            // However, the opposite is not necessarily true.
            return (stateA != null && stateB != null && stateA.equals(stateB)) || getBitmap(drawableA).sameAs(getBitmap(drawableB));
        }

        public static Bitmap getBitmap(Drawable drawable) {
            Bitmap result;
            if (drawable instanceof BitmapDrawable) {
                result = ((BitmapDrawable) drawable).getBitmap();
            } else {
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                // Some drawables have no intrinsic width - e.g. solid colours.
                if (width <= 0) {
                    width = 1;
                }
                if (height <= 0) {
                    height = 1;
                }
                result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(result);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
            return result;
        }

        public class UpdateStressData extends AsyncTask<Void,Void,Boolean> {
            RelativeLayout graphView;
            GraphView stressgraph;
            ProgressBar loading;
            String start;
            String end;
            int thres = 50;
            Context context;

            UpdateStressData(Context _context, RelativeLayout _graphView , GraphView _stressgraph , ProgressBar _loading , String _start , String _end) {
                graphView = _graphView;
                stressgraph = _stressgraph;
                loading = _loading;
                start = _start;
                end = _end;
                context = _context;
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    MyOAuthConnect a = new MyOAuthConnect(save_date.format(THIS_TIME));
                    values = a.sendGet(start , end);

                    //MySocketManager socketM = new MySocketManager(USER_ID);
                    //thres = Integer.parseInt(socketM.getDataFromServer(MySocketManager.SOCKET_MSG.GET_THRESHOLD , save_date.format(THIS_TIME)));

                } catch (Exception e) {
                    Log.e("doInBackground",e.toString());
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                // LOAD MOTHER STRESS VALUE
                if(running) {

                    if(context != null && context.getResources()!=null) {
                        graphView.removeView(stressgraph);
                        stressgraph = new GraphView(context, values, "", null, verlabels, thres);
                        DisplayMetrics dm = context.getResources().getDisplayMetrics();
                        int width = dm.widthPixels;
                        int height = dm.heightPixels;
                        stressgraph.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, height / 9));
                        graphView.addView(stressgraph);
                        loading.setVisibility(ProgressBar.GONE);

                        // SET MOTHERS STRESS BOX
                        List<Boolean> stressfulBox = findStressfullBox();
                        for (int i = 0; i < stressfulBox.size(); i++) {
                            if (stressfulBox.get(i) == true) {
                                motherEmotionListTemp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape, null));
                                motherEmotionList.add(motherEmotionListTemp.get(i));
                                motherEmotionList.get(motherEmotionList.size() - 1).setLabelFor(i); // just added value
                            } else
                                motherEmotionListTemp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_impossible, null));
                        }

                        for (int i = 0; i < motherEmotionList.size(); i++) {
                            motherEmotionList.get(i).setOnDragListener(new MyDragListener(MOTHER_EMOTION, i));
                            motherEmotionList.get(i).setOnClickListener(new MyTableClearListener(MOTHER_EMOTION, i));
                            motherEmotionList.get(i).setOnLongClickListener(new MyTableLongListener());
                            //motherEmotionList.get(i).setLabelFor(i);  // already setup before
                        }
                    }
                }
            }
        }

        public class UpdateInterface extends AsyncTask<Void, Void, Boolean> {

            private List <View> viewList;
            private String data;
            private String date;
            private MySocketManager.SOCKET_MSG msg;
            // Update Interface
            //
            // B1 DI DU SL BA HO
            // M1 RE E1 C1 AS S1

            UpdateInterface(List <View> _viewList, MySocketManager.SOCKET_MSG _msg,  String _data, String _date) {
                viewList = _viewList;
                data = _data;
                date = _date;
                msg = _msg;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.

                if(data == null) {
                    socketM = new MySocketManager(USER_ID);
                    data = socketM.getDataFromServer(msg, date);
                }

                // TODO: register the new account here.
                return false;
            }


            @Override
            protected void onPostExecute(final Boolean success) {
                // B1 DI DU SL BA HO
                // M1 RE E1 C1 AS S1
                if(running) {
                    if (data == null) {
                        Log.e("onPostExecute", msg.toString());
                        Log.e("onPostExecute", data);
                    }

                    for(int i=0 ; i <  babyIconList.size() ; i++){
                        babyIconList.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.BABY_ACTIVITY , i));
                        if(i==0)
                            babyIconList.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.BABY_FEED));
                        else {
                            babyIconList.get(i).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getContext(), "변경 가능한 아이콘이 없습니다." ,  Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    for(int i=0 ; i < motherIconList.size() ; i++){
                        motherIconList.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.MOTHER_ACTIVITY,i));
                        if(i==0)
                            motherIconList.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.MOTHER_FOOD));
                        else {
                            motherIconList.get(i).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getContext(), "변경 가능한 아이콘이 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    for(int i=0 ; i < motherEmotionIconList.size() ; i++){
                        motherEmotionIconList.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.MOTHER_EMOTION,i));
                        if(i==0)
                            motherEmotionIconList.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.MOTHER_EMOTION));
                        else {
                            motherEmotionIconList.get(i).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getContext(), "변경 가능한 아이콘이 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    for(int i=0 ; i < fatherIconListComment.size() ; i++) {
                        fatherIconListComment.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.FATHER_COMMENT,i));
                        fatherIconListComment.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.FATHER_COMMENT));
                    }
                    for(int i=0 ; i < fatherIconListAsk.size() ; i++) {
                        fatherIconListAsk.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.FATHER_ASK,i));
                        fatherIconListAsk.get(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getContext(), "변경 가능한 아이콘이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    for(int i=0 ; i < fatherIconListSuggest.size() ; i++) {
                        fatherIconListSuggest.get(i).setOnTouchListener(new MyIconTouchClickListener(ICON_GROUP.FATHER_SUGGESTION,i));
                        fatherIconListSuggest.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.FATHER_SUGGESTION));
                    }

                    String[] pieces = data.split(" ");

                    for (int i = 0; i < pieces.length; i++) {
                        Drawable icon = null;

                        boolean warning_icon = false;
                        boolean sms_icon = false;
                        String iconText ="";
                        if (pieces[i].equals("B1")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_small, null);
                            iconText="'적은양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("B2")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_medium, null);
                            iconText="'중간양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("B3")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_large, null);
                            iconText="'많은양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("DI")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.diaper_pee, null);
                            iconText="기저귀를 갈았습니다.";
                        } else if (pieces[i].equals("DU")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baby_poop, null);
                            iconText="대변을 보았습니다.";
                        } else if (pieces[i].equals("SL")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sleeping_baby, null);
                            iconText="잠을 잤습니다.";
                        } else if (pieces[i].equals("BA")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bath, null);
                            iconText="목욕을 하였습니다.";
                        } else if (pieces[i].equals("HO")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_motherhood, null);
                            iconText="아기를 보거나 달래고 있습니다.";
                        } else if (pieces[i].equals("M1")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_s, null);
                            iconText="'적은양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("M2")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_m, null);
                            iconText="'중간양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("M3")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_l, null);
                            iconText="'많은양'의 식사를 하였습니다.";
                        } else if (pieces[i].equals("RE")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rest, null);
                            iconText="휴식 시간입니다.";
                        } else if (pieces[i].equals("E1")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv1, null);
                            iconText="기분 아주 나쁨";
                        } else if (pieces[i].equals("E2")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv2, null);
                            iconText="기분 나쁨";
                        } else if (pieces[i].equals("E3")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv3, null);
                            iconText="기분 보통";
                        } else if (pieces[i].equals("E4")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv4, null);
                            iconText="기분 좋음";
                        } else if (pieces[i].equals("E5")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv5, null);
                            iconText="기분 아주 좋음";
                        } else if (pieces[i].equals("C1")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_heart, null);
                            iconText="하트로 답을 하였습니다.";
                        } else if (pieces[i].equals("C2")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rose, null);
                            iconText="장미꽃으로 답을 하였습니다.";
                        } else if (pieces[i].equals("C3")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_thumsup, null);
                            iconText="따봉으로 답을 하였습니다.";
                        } else if (pieces[i].equals("C4")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_smile, null);
                            iconText="스마일로 답을 하였습니다.";
                        } else if (pieces[i].equals("AS")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_ask, null);
                            iconText="이때 아기가 뭐했는지 궁금해합니다.";
                        } else if (pieces[i].equals("S1")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rise_sug, null);
                            iconText="이때 식사를 하면 어떨지 권하고 있습니다.";
                        } else if (pieces[i].equals("S2")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rest_sug, null);
                            iconText="이때 휴식시간을 가지면 어떨지 권하고 있습니다.";
                        } else if (pieces[i].equals("WN") && USER_TYPE_ENUM.FATHER == USER_TYPE) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_warning, null);
                            warning_icon = true;
                        } else if (pieces[i].equals("SM")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sms, null);
                            sms_icon = true;
                        } else if (pieces[i].equals("SW")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sms, null);
                            sms_icon = true;
                        } else if (pieces[i].equals("CO")) {
                            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_couple_heart, null);
                            iconText="엄마의 스트레스 받는 일에 대하여 공유하였습니다.";
                        }

                        final String iconText2 = iconText;

                        if (icon != null) {
                            ImageView addImage = new ImageView(getContext());
                            if (warning_icon) {
                                AlphaAnimation blinkanimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                                blinkanimation.setDuration(800); // duration
                                blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                                blinkanimation.setRepeatCount(-1); // Repeat animation infinitely
                                blinkanimation.setRepeatMode(Animation.REVERSE);
                                addImage.setImageDrawable(icon);
                                addImage.startAnimation(blinkanimation);
                                addImage.setOnClickListener(new FatherAlertListener(((LinearLayout) viewList.get(i)), communicationType.MOTHER_STRESS_START, i));
                                ((LinearLayout) viewList.get(i)).addView(addImage);
                                ((LinearLayout) viewList.get(i)).setOnClickListener(null);
                            } else if (sms_icon) {
                                if (USER_TYPE == USER_TYPE_ENUM.FATHER) {
                                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_warning, null);
                                    addImage.setImageDrawable(icon);
                                    addImage.setOnClickListener(new FatherAlertListener(((LinearLayout) viewList.get(i)), communicationType.FATHER_WAITING, i));
                                    ((LinearLayout) viewList.get(i)).addView(addImage);
                                    ((LinearLayout) viewList.get(i)).setOnClickListener(null);
                                } else {
                                    AlphaAnimation blinkanimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                                    blinkanimation.setDuration(800); // duration
                                    blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                                    blinkanimation.setRepeatCount(-1); // Repeat animation infinitely
                                    blinkanimation.setRepeatMode(Animation.REVERSE);
                                    addImage.setImageDrawable(icon);

                                    //if( pieces[i].equals("SM"))
                                    addImage.startAnimation(blinkanimation);
                                    addImage.setOnClickListener(new FatherAlertListener(((LinearLayout) viewList.get(i)), communicationType.MOTHER_MSG, i));
                                    ((LinearLayout) viewList.get(i)).addView(addImage);
                                    ((LinearLayout) viewList.get(i)).setOnClickListener(null);
                                }
                            } else {
                                /*
                                addImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(getContext(), iconText2 ,  Toast.LENGTH_SHORT).show();
                                    }
                                });
                                */
                                addImage.setImageDrawable(icon);
                                ((LinearLayout) viewList.get(i)).addView(addImage);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return TOTAL_PAGE;
        }
    }
}
