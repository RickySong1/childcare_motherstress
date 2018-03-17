package stresstest.ntt.kaist.childcare;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import stresstest.ntt.mymanager.MyFileManager;
import stresstest.ntt.mymanager.MySocketManager;
import stresstest.ntt.smartband.LoginSettingActivity;

import static stresstest.ntt.mymanager.MySocketManager.SOCKET_MSG.GET_BABYACTIVITY_UP;
import static stresstest.ntt.mymanager.MySocketManager.SOCKET_MSG.SET_OPENAPP;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    public static final int BABY = 0;
    public static final int MOTHER = 1;
    public static final int MOTHER_EMOTION = 2;
    public static final int FATHER_COMMENT = 3;  // Feedback
    public static final int FATHER_ASK = 4;  // Before time slot ,  ASK
    public static final int FATHER_SUGGESTION = 5;  // After time slot  ,  SUGGESTION
    public static int TOTAL_PAGE; // TOTAL_PAGE
    public static Date NOW_TIME;

    public enum USER_TYPE_ENUM {
        MOTHER, FATHER
    }

    public enum ICON_GROUP {
        BABY_ACTIVITY, MOTHER_EMOTION, MOTHER_ACTIVITY, FATHER_COMMENT, FATHER_ASK ,FATHER_SUGGESTION
    }

    public static String USER_ID = "";
    public static  USER_TYPE_ENUM USER_TYPE;

    private ViewPager mViewPager;
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

    public enum Type {
        MOTHER_STRESS, FATHER_BEFORE, FATHER_AFTER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //intent = this.getPackageManager().getLaunchIntentForPackage("com.garmin.android.apps.connectmobile");
        //MainActivity.this.startActivity(intent);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            TOTAL_PAGE = Integer.parseInt(socketM.getDataFromServer(MySocketManager.SOCKET_MSG.GET_PAGE_COUNT , null));
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

        GraphView stressgraph;
        int nowHour;

        float[] values;

        TableLayout icon_table;

        Date THIS_TIME;
        SimpleDateFormat save_date;

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
            values = new float[240];
            String[] verlabels = new String[] { "", "High", "Medium" , "Low" , ""};

            Random a = new Random();
            for(int i=0 ; i<240 ;i++){
                values[ i] = a.nextInt(100);
            }

            THIS_TIME = new Date();
            THIS_TIME.setTime(NOW_TIME.getTime() - ( (12 * 60 * 60 * 1000)* (TOTAL_PAGE - page_num) ));


            SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMMM (a)");
            SimpleDateFormat time = new SimpleDateFormat("hh");
            save_date = new SimpleDateFormat("yyyyMMdda");


            ((TextView)rootView.findViewById(R.id.text_date)).setText(date.format(THIS_TIME));

            String now = time.format(THIS_TIME);
            if(now.compareTo("01") == 0) nowHour =0; else if(now.compareTo("02") == 0) nowHour = 1;
            else if (now.compareTo("03") == 0) nowHour = 2; else if (now.compareTo("04") == 0) nowHour = 3;
            else if (now.compareTo("05") == 0) nowHour = 4; else if (now.compareTo("06") == 0) nowHour = 5;
            else if (now.compareTo("07") == 0) nowHour = 6; else if (now.compareTo("08") == 0) nowHour = 7;
            else if (now.compareTo("09") == 0) nowHour = 8; else if (now.compareTo("10") == 0) nowHour = 9;
            else if (now.compareTo("11") == 0) nowHour = 10; else if(now.compareTo("11") == 0) nowHour = 11;

            stressgraph = new GraphView(getContext() , values, "", null , verlabels);
            stressgraph.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT , 250 ));

            LinearLayout graphView = ((LinearLayout)rootView.findViewById(R.id.graph_view));
            graphView.addView(stressgraph);

            babyViewList.clear(); motherViewList.clear(); motherEmotionList.clear(); ; motherEmotionListTemp.clear();
            fatherViewListA.clear(); fatherViewListB.clear(); babyIconList.clear(); motherIconList.clear();
            motherEmotionIconList.clear(); fatherIconListComment.clear(); fatherIconListAsk.clear(); fatherIconListSuggest.clear();

            icon_table = rootView.findViewById(R.id.icon_table);
            List<Boolean> stressfulBox = findStressfullBox();

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

            for(int i=0 ; i<stressfulBox.size() ; i++){
                if(stressfulBox.get(i) == true){
                    motherEmotionListTemp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape, null));
                    motherEmotionList.add(motherEmotionListTemp.get(i));
                    motherEmotionList.get(motherEmotionList.size()-1).setLabelFor(i); // just added value
                }
                else
                    motherEmotionListTemp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_impossible, null));
            }

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
                babyViewList.get(i).setOnClickListener(new MyTableClearListener());
                babyViewList.get(i).setLabelFor(i);
            }
            for(int i=0 ; i< motherViewList.size() ; i++){
                motherViewList.get(i).setOnDragListener(new MyDragListener(MOTHER,i));
                motherViewList.get(i).setOnClickListener(new MyTableClearListener());
                motherViewList.get(i).setLabelFor(i);
            }
            for(int i=0 ; i < motherEmotionList.size() ; i++){
                motherEmotionList.get(i).setOnDragListener(new MyDragListener(MOTHER_EMOTION, i));
                motherEmotionList.get(i).setOnClickListener(new MyTableClearListener());
                //motherEmotionList.get(i).setLabelFor(i);  // already setup before
            }

            for(int i=0 ; i< fatherViewListTemp.size() ; i++){
                fatherViewListTemp.get(i).setLabelFor(i);
            }

            for(int i=0 ; i< fatherViewListA.size() ; i++){
                fatherViewListA.get(i).setOnDragListener(new MyDragListener(FATHER_ASK,i));
                fatherViewListA.get(i).setOnClickListener(new MyTableClearListener());
            }
            for(int i=0 ; i< fatherViewListB.size() ; i++){
                fatherViewListB.get(i).setOnDragListener(new MyDragListener(FATHER_SUGGESTION,i));
                fatherViewListB.get(i).setOnClickListener(new MyTableClearListener());
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


            }

            public boolean onTouch(View v, MotionEvent event) {
                int NONE = 0;
                int DRAG = 1;
                int MAX_MOVE = 150;
                int m_mode = NONE;

                final int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        m_mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
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
            Type type;

            public FatherAlertListener(Type _type){
                type = _type;
            }

            public void onClick(View v) {

                    final ImageView tt = (ImageView) v;

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                switch(type){
                        case MOTHER_STRESS:
                            alertDialogBuilder
                                    .setIcon(R.drawable.ic_emotion_lv1)
                                    .setTitle("Mother is tired")
                                    .setMessage("Mother looks very stressful. \n\nAsking why she does makes her be happy. \n\n Will you contact her soon?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes, I will", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            showIcon( tt , Type.FATHER_BEFORE  );  // pass ImageView
                                        }
                                    });
                            break;

                        case FATHER_BEFORE:
                            alertDialogBuilder
                                    .setIcon(R.drawable.ic_father_heart)
                                    .setMessage("Mother is waiting for your attention. \n\nAsking why she does makes her be happy.")
                                    .setCancelable(false)
                                    .setPositiveButton("Okay, I will ask", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            showIcon( tt , Type.FATHER_AFTER  );  // pass ImageView
                                        }
                                    });
                            break;

                    case FATHER_AFTER:
                        alertDialogBuilder
                                .setIcon(R.drawable.ic_father_heart)
                                .setMessage("Father checked your stressful emotion. \n\n He is wondering and will contact you.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        tt.clearAnimation();
                                    }
                                });
                        break;
                    }

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

            }
        }


        private final class MyTableClearListener implements View.OnClickListener{
                public void onClick(View v) {
                    final LinearLayout container = (LinearLayout) v;
                    if (((ViewGroup) container).getChildCount() > 0) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder
                                .setMessage("기록 된 내용을 삭제하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int index = 0; index < ((ViewGroup) container).getChildCount(); ++index) {
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

        public void showIcon(View parent, Type a){
            switch(a){
                case MOTHER_STRESS:  // Pass LinearLayout
                    LinearLayout parent_view = (LinearLayout) parent;
                    ImageView addImage = new ImageView(getContext());
                    addImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_warning, null)  );
                    AlphaAnimation blinkanimation= new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                    blinkanimation.setDuration(800); // duration
                    blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                    blinkanimation.setRepeatCount(-1); // Repeat animation infinitely
                    blinkanimation.setRepeatMode(Animation.REVERSE);
                    addImage.startAnimation(blinkanimation);
                    addImage.setOnClickListener(new FatherAlertListener(Type.MOTHER_STRESS));
                    parent_view.addView(addImage);
                break;

                case FATHER_BEFORE:   // Pass ImageView
                    ((ImageView)parent).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sms, null));
                    parent.setOnClickListener(new FatherAlertListener(Type.FATHER_BEFORE));
                    parent.clearAnimation();
                    break;

                case FATHER_AFTER:
                    ((ImageView)parent).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sms, null));
                    AlphaAnimation blinkanimation2= new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                    blinkanimation2.setDuration(800); // duration
                    blinkanimation2.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                    blinkanimation2.setRepeatCount(-1); // Repeat animation infinitely
                    blinkanimation2.setRepeatMode(Animation.REVERSE);
                    parent.startAnimation(blinkanimation2);
                    parent.setOnClickListener(new FatherAlertListener(Type.FATHER_AFTER));
                    break;
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

                            // Mother emotion view list's index is different
                            if( getDrawableString( getResources()  ,newView.getDrawable()).compareTo("E1") == 0 || getDrawableString( getResources()  ,newView.getDrawable()).compareTo("E2") == 0 ){
                                if(((ViewGroup)fatherViewListTemp.get(v.getLabelFor())).getChildCount() > 0){
                                    ((ViewGroup)fatherViewListTemp.get(v.getLabelFor())).removeAllViews();
                                }
                                showIcon(((ViewGroup)fatherViewListTemp.get(v.getLabelFor())), Type.MOTHER_STRESS  );
                            }

                            String save_icon_string = getDrawableString( getResources()  ,newView.getDrawable());

                            switch(boxOwner){
                                case BABY:
                                    socketM = new MySocketManager(USER_ID);
                                    //socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET);
                                    break;
                                case MOTHER:
                                    socketM = new MySocketManager(USER_ID);
                                    socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHERACTIVITY, save_date.format(THIS_TIME) , id , save_icon_string) ;
                                    break;
                                case MOTHER_EMOTION:
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
                return "H0";
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

                if(data==null){
                    Log.e("onPostExecute", msg.toString());
                    Log.e("onPostExecute", data);
                }

                String[] pieces = data.split(" ");

                for(int i=0 ; i < pieces.length ; i++){
                    Drawable icon=null;
                    if( pieces[i].equals("B1")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_small, null);
                    } else if ( pieces[i].equals("B2")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_medium, null);
                    }else if ( pieces[i].equals("B3")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_food_large, null);
                    }else if ( pieces[i].equals("DI")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.diaper_pee, null);
                    }else if ( pieces[i].equals("DU")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baby_poop, null);
                    }else if ( pieces[i].equals("SL")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sleeping_baby, null);
                    }else if ( pieces[i].equals("BA")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bath, null);
                    }else if ( pieces[i].equals("HO")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_motherhood, null);
                    }else if ( pieces[i].equals("M1")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_s, null);
                    }else if ( pieces[i].equals("M2")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_m, null);
                    }else if ( pieces[i].equals("M3")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rise_l, null);
                    }else if ( pieces[i].equals("RE")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rest, null);
                    }else if ( pieces[i].equals("E1")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv1, null);
                    }else if ( pieces[i].equals("E2")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv2, null);
                    }else if ( pieces[i].equals("E3")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv3, null);
                    }else if ( pieces[i].equals("E4")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv4, null);
                    }else if ( pieces[i].equals("E5")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_emotion_lv5, null);
                    }else if ( pieces[i].equals("C1")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_heart, null);
                    }else if ( pieces[i].equals("C2")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rose, null);
                    }else if ( pieces[i].equals("C3")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_thumsup, null);
                    }else if ( pieces[i].equals("C4")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_smile, null);
                    }else if ( pieces[i].equals("AS")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_ask, null);
                    }else if ( pieces[i].equals("S1")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rise_sug, null);
                    }else if ( pieces[i].equals("S2")){
                        icon =ResourcesCompat.getDrawable(getResources(), R.drawable.ic_father_rest_sug, null);
                    }

                    if(icon !=null) {
                        ImageView addImage = new ImageView(getContext());
                        addImage.setImageDrawable(icon);
                        ((LinearLayout)viewList.get(i)).addView(addImage);
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
