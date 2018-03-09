package stresstest.ntt.kaist.childcare;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

/**
 * Created by songseokwoo on 2018. 2. 8..
 */

//커스텀 섀도우 빌더

public class CanvasShadowCustom extends View.DragShadowBuilder{

    int mWidth, mHeight;

    public CanvasShadowCustom(View v){

        super(v);
        mWidth = v.getWidth();//좌표를 가져와서 멤버 변수에 넣어둠.
        mHeight = v.getHeight();//좌표를 가져와서 멤버 변수에 넣어둠.
    }

    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint){

        //섀도우의 크기와 중심점의 좌표를 지정하는 메소드임
        shadowSize.set(mWidth, mHeight);//사이즈 지정
        shadowTouchPoint.set(mWidth, mHeight+100);//중심점 지정
    }
}


