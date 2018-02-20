package stresstest.ntt.kaist.childcare;

/**
 * Created by songseokwoo on 2018. 2. 11..
 */
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomDialogIcon extends Dialog {


    public enum Type {
        BABY_FEED, MOTHER_FOOD, MOTHER_EMOTION, BABY_DIASPER, FATHER_COMMENT
    }


    private Type type;

    private int size_x;
    private int size_y;

    private MyDialogListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        //WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        //lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        //lpWindow.dimAmount = 0.8f;
        //getWindow().setAttributes(lpWindow);

        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        //wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        //wlp.dimAmount = 0.8f;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        } else {
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        getWindow().setAttributes(wlp);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        params.y = size_y * 3;

        getWindow().setAttributes(params);

        setContentView(R.layout.icon_custom_dialog);
        final ImageView [] imageView = new ImageView[5];

        imageView[0] = (ImageView) findViewById(R.id.icon1);
        imageView[1] = (ImageView) findViewById(R.id.icon2);
        imageView[2] = (ImageView) findViewById(R.id.icon3);
        imageView[3] = (ImageView) findViewById(R.id.icon4);
        imageView[4] = (ImageView) findViewById(R.id.icon5);

        for(int i=0 ; i < imageView.length ; i++){
            ViewGroup.LayoutParams imgParams = (ViewGroup.LayoutParams) imageView[i].getLayoutParams();
            imgParams.width = size_x;
            imgParams.height = size_y;
            imageView[i].setLayoutParams(imgParams);

            final int imgId = i;

            imageView[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.userSelectedAValue(imageView[imgId].getDrawable());
                }
            });
        }


        switch(type){
            case BABY_FEED:
                imageView[0].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_food_small, null));
                imageView[1].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_food_medium, null));
                imageView[2].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_food_large, null));
                imageView[3].setVisibility(ImageView.GONE);
                imageView[4].setVisibility(ImageView.GONE);
                break;
            case BABY_DIASPER:
                imageView[0].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.diaper_pee, null));
                imageView[1].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_baby_poop, null));
                imageView[2].setVisibility(ImageView.GONE);
                imageView[3].setVisibility(ImageView.GONE);
                imageView[4].setVisibility(ImageView.GONE);
                break;

            case MOTHER_FOOD:
                imageView[0].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_rise_s, null));
                imageView[1].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_rise_m, null));
                imageView[2].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_rise_l, null));
                imageView[3].setVisibility(ImageView.GONE);
                imageView[4].setVisibility(ImageView.GONE);

                break;

            case MOTHER_EMOTION:
                imageView[0].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_emotion_lv1, null));
                imageView[0].setImageAlpha(253);

                imageView[1].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_emotion_lv2, null));
                imageView[1].setImageAlpha(254);

                imageView[2].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_emotion_lv3, null));
                imageView[3].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_emotion_lv4, null));
                imageView[4].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_emotion_lv5, null));
                break;

            case FATHER_COMMENT:
                imageView[0].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_father_heart, null));
                imageView[1].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_father_rose, null));
                imageView[2].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_father_thumsup, null));
                imageView[3].setImageDrawable(ResourcesCompat.getDrawable( getContext().getResources(), R.drawable.ic_father_smile, null));
                imageView[4].setVisibility(ImageView.GONE);
                break;
        }
    }

    public static interface MyDialogListener
    {
        public void userSelectedAValue(Drawable draw);
    }


    public void setMyDialogListener(MyDialogListener a){
        listener = a;
    }

    // 클릭버튼이 하나일때 생성자 함수로 클릭이벤트를 받는다.
    public CustomDialogIcon(Context context, Type type, int sizex, int sizey) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.type = type;
        size_x = sizex;
        size_y = sizey;

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            this.dismiss();
        }

        return super.dispatchTouchEvent(ev);
    }
}
