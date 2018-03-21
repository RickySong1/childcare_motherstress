package stresstest.ntt.kaist.childcare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.View;
import android.widget.TableRow;

/**
 * GraphView creates a scaled line or bar graph with x and y axis labels.
 * @author Arno den Hond
 *
 */
public class GraphView extends View {

    public static int HIGH_STRESS_SCORE = 50;

    public static boolean BAR = true;
    public static boolean LINE = false;
    private Paint paint;
    private int [] values;
    private String[] horlabels;
    private String[] verlabels;
    private String title;



    private boolean type;
    public GraphView(Context context, int [] values, String title, String[] horlabels, String[] verlabels , int threshold) {
        super(context);

        HIGH_STRESS_SCORE = threshold;

        if (values == null)
            values = new int[0];
        else
            this.values = values;
        if (title == null)
            title = "";
        else
            this.title = title;

        if (horlabels == null)
            this.horlabels = new String[0];
        else
            this.horlabels = horlabels;

        if (verlabels == null)
            this.verlabels = new String[0];
        else
            this.verlabels = verlabels;

        paint = new Paint();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        float border = 0;
        float horstart = border * 2;

        float height = getHeight();
        float width = getWidth();

        float max = getMax();
        float min = getMin();

        float diff = max - min;

        float graphheight = height;
        float graphwidth = width;

        /*
        int hors = horlabels.length - 1;
        for (int i = 0; i < horlabels.length; i++) {
            paint.setColor(Color.DKGRAY);
            float x = ((graphwidth / hors) * i) + horstart;
            canvas.drawLine(x, height - border, x, border, paint);
            paint.setTextAlign(Align.CENTER);
            if (i==horlabels.length-1)
                paint.setTextAlign(Align.RIGHT);
            if (i==0)
                paint.setTextAlign(Align.LEFT);
            paint.setColor(Color.WHITE);
            canvas.drawText(horlabels[i], x, height - 4, paint);
        }
        */

        paint.setTextAlign(Align.CENTER);
        canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);
        if (max != min) {
            paint.setColor(Color.BLUE);

                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;

                for (int i = 0; i < values.length; i++) {
                    float val = values[i] - min;
                    float rat = val / diff;
                    float h = graphheight * rat;

                    if(values[i] >= HIGH_STRESS_SCORE)
                        paint.setARGB(255 ,  255 ,  30 , 30);
                    else
                        paint.setARGB(255 ,  255 ,  200 , 200);

                    canvas.drawRect((i * colwidth) + horstart, (border - h) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), height - (border - 1), paint);
                }
        }
        int vers = verlabels.length - 1;

        for (int i = 0; i < verlabels.length; i++) {
            float y = ((graphheight / vers) * i) + border;
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            canvas.drawText(verlabels[i], 60, y, paint);
        }

        paint.setTextAlign(Align.LEFT);
        for (int i = 0; i < verlabels.length; i++) {
            paint.setColor(Color.argb(255,180,180,180));
            float y = ((graphheight / vers) * i) + border;
            canvas.drawLine(horstart, y, width, y, paint);
        }
    }

    private float getMax() {
        float largest = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] > largest)
                largest = values[i];

        //return largest;
        return 100;
    }
    private float getMin() {
        float smallest = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++)
            if (values[i] < smallest)
                smallest = values[i];
        //return smallest;
        return 0;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 측정된 폭과 높이를 출력해 보자
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        // 패딩값을 측정값의 10%를 주어 뺀다.
        int paddingWidth = 0;
        int paddingHeight = 0;

        setMeasuredDimension(width - paddingWidth, height - paddingHeight);
    }

}