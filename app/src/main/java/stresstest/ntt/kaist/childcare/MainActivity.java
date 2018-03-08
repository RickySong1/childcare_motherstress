package stresstest.ntt.kaist.childcare;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.Layout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import stresstest.ntt.Garmin.MyOAuthConnect;
import stresstest.ntt.smartband.SmartbandSettingActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int BABY = 0;
    public static final int MOTHER = 1;
    public static final int MOTHER_EMOTION = 2;
    public static final int FATHER_COMMENT = 3;  // Feedback
    public static final int FATHER_ASK = 4;  // Before time slot ,  ASK
    public static final int FATHER_SUGGESTION = 5;  // After time slot  ,  SUGGESTION

    public static int USER = MOTHER;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public enum Type {
        MOTHER_STRESS, FATHER_BEFORE, FATHER_AFTER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            MyOAuthConnect a = new MyOAuthConnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        View content_main = findViewById(R.id.app_bar_main);

        mViewPager = (ViewPager) content_main.findViewById(R.id.content_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(2);
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
            Log.e("Login", "dd");
        }else if( id == R.id.nav_smartband){

            Intent intent=new Intent(MainActivity.this, SmartbandSettingActivity.class);
            startActivity(intent);
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
        List <View> fatherViewListA = new ArrayList();
        List <View> fatherViewListB = new ArrayList();
        List <View> fatherViewListTemp = new ArrayList();

        List <View> babyIconList = new ArrayList();
        List <View> motherIconList = new ArrayList();
        List <View> motherEmotionIconList = new ArrayList();
        List <View> fatherIconListComment = new ArrayList();
        List <View> fatherIconListAsk = new ArrayList();
        List <View> fatherIconListSuggest = new ArrayList();

        GraphView stressgraph;
        int nowHour;

        float[] values;

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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.content_main, container, false);

            values = new float[72];
            String[] verlabels = new String[] { "", "High", "Medium" , "Low" , ""};

            Random a = new Random();
            for(int i=0 ; i<72 ;i++){
                values[ i] = a.nextInt(100);
            }

            Date today = new Date();

            SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMMM (a)");
            SimpleDateFormat time = new SimpleDateFormat("hh");
            ((TextView)rootView.findViewById(R.id.text_date)).setText(date.format(today));

            String now = time.format(today);
            if(now.compareTo("01") == 0) nowHour =0; else if(now.compareTo("02") == 0) nowHour = 1;
            else if (now.compareTo("03") == 0) nowHour = 2; else if (now.compareTo("04") == 0) nowHour = 3;
            else if (now.compareTo("05") == 0) nowHour = 4; else if (now.compareTo("06") == 0) nowHour = 5;
            else if (now.compareTo("07") == 0) nowHour = 6; else if (now.compareTo("08") == 0) nowHour = 7;
            else if (now.compareTo("09") == 0) nowHour = 8; else if (now.compareTo("10") == 0) nowHour = 9;
            else if (now.compareTo("11") == 0) nowHour = 10; else nowHour = 11;

            stressgraph = new GraphView(getContext() , values, "", null , verlabels);
            stressgraph.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT , 300 ));

            LinearLayout graphView = ((LinearLayout)rootView.findViewById(R.id.graph_view));
            graphView.addView(stressgraph);

            babyViewList.clear(); motherViewList.clear(); motherEmotionList.clear();
            fatherViewListA.clear(); fatherViewListB.clear(); babyIconList.clear(); motherIconList.clear();
            motherEmotionIconList.clear(); fatherIconListComment.clear(); fatherIconListAsk.clear(); fatherIconListSuggest.clear();

            List <View> motherEmotion_temp = new ArrayList();
            List<Boolean> stressfulBox = findStressfullBox();

            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t1));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t2));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t3));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t4));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t5));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t6));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t7));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t8));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t9));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t10));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t11));
            motherEmotion_temp.add(rootView.findViewById(R.id.emotion_t12));

            for(int i=0 ; i<stressfulBox.size() ; i++){
                if(stressfulBox.get(i) == true){
                    motherEmotion_temp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape, null));
                    motherEmotionList.add(motherEmotion_temp.get(i));
                    motherEmotionList.get(motherEmotionList.size()-1).setLabelFor(i); // just added value
                }
                else
                    motherEmotion_temp.get(i).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_impossible, null));
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
                    Log.e("AA",Integer.toString(i));
                }
                else{ // After time slot
                    fatherViewListB.add(fatherViewListTemp.get(i));
                    Log.e("BB",Integer.toString(i));
                }
            }

            motherViewList.get(nowHour).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));
            fatherViewListB.get(0).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));
            babyViewList.get(nowHour).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_nowtime, null));

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
                babyIconList.get(i).setOnTouchListener(new MyIconTouchClickListener());
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
                motherIconList.get(i).setOnTouchListener(new MyIconTouchClickListener());
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
                motherEmotionIconList.get(i).setOnTouchListener(new MyIconTouchClickListener());
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
                fatherIconListComment.get(i).setOnTouchListener(new MyIconTouchClickListener());
                fatherIconListComment.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.FATHER_COMMENT));
            }
            for(int i=0 ; i < fatherIconListAsk.size() ; i++) {
                fatherIconListAsk.get(i).setOnTouchListener(new MyIconTouchClickListener());
                fatherIconListAsk.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "변경 가능한 아이콘이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            for(int i=0 ; i < fatherIconListSuggest.size() ; i++) {
                fatherIconListSuggest.get(i).setOnTouchListener(new MyIconTouchClickListener());
                fatherIconListSuggest.get(i).setOnClickListener(new MyIconCustomDialog(CustomDialogIcon.Type.FATHER_SUGGESTION));
            }

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
                if ( count % 6 == 0){
                    motherEmotionBox.add(angry);
                    angry = false;
                }
            }
            return motherEmotionBox;
        }

        private final class MyIconTouchClickListener implements View.OnTouchListener {

            public boolean onTouch(View v, MotionEvent event) {
                int NONE = 0;
                int DRAG = 1;
                int MAX_X_MOVE = 80;
                int MAX_Y_MOVE = 80;
                int m_mode = NONE;

                final int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        m_mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (Math.abs(event.getX()) > MAX_X_MOVE || Math.abs(event.getY()) > MAX_Y_MOVE) {

                            Log.e("xx", Float.toString(event.getX()));
                            Log.e("yy", Float.toString(event.getY()));

                            m_mode = DRAG;
                            Drawable noShape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_no, null);
                            ClipData data = ClipData.newPlainText("", "");
                            View.DragShadowBuilder shadowBuilder = new CanvasShadowCustom(v);
                            Log.e("zz","111");
                            v.startDrag(data, shadowBuilder, v, 0);
                            Log.e("zz","222");
                            v.setVisibility(View.INVISIBLE);

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
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (checkProper)
                            v.setBackground(enterShape);
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

                            if( newView.getDrawable().getAlpha() < 255){
                             // 253 - very bad , 254 - bad
                                if(((ViewGroup)fatherViewListTemp.get(v.getLabelFor())).getChildCount() > 0){
                                    ((ViewGroup)fatherViewListTemp.get(v.getLabelFor())).removeAllViews();
                                }
                                showIcon(((ViewGroup)fatherViewListTemp.get(v.getLabelFor())), Type.MOTHER_STRESS  );
                            }
                        }
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        if(nowHour == v.getLabelFor() && boxOwner != MOTHER_EMOTION)
                            v.setBackground(normalNowShape);
                        else
                            v.setBackground(normalShape);

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
            // Show 3 total pages.
            return 3;
        }
    }
}
