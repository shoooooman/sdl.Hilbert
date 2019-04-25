package jp.ac.titech.itpro.sdl.hilbert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HilbertView extends View {

    private HashMap<Integer, Future<Bitmap>> caches = new HashMap<>();

    private int order = 1;

    public HilbertView(Context context) {
        this(context, null);
    }

    public HilbertView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HilbertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        makeCaches(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        try {
            canvas.drawBitmap(caches.get(order).get(), 0, 0, new Paint());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int preW, int preH) {
        makeCaches(w, h);
    }

    public void setOrder(int n) {
        order = n;
        invalidate();
    }

    // 最初に全てのorderのヒルベルト曲線を描画しキャッシュしておく
    private void makeCaches(final int w, final int h) {
        final ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 1; i <= MainActivity.MAX_ORDER; i++) {
            final int j = i; // for scope
            caches.put(j, pool.submit(new Callable<Bitmap>() {
                @Override
                public Bitmap call() {
                    return drawHilbert(w, h, j);
                }
            }));
        }
    }

    // ヒルベルト曲線をBitmapとして作成
    private Bitmap drawHilbert(final int w, final int h, final int order) {
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        final Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        int size = Math.min(w, h);
        double step = (double) size / (1 << order);

        HilbertTurtle turtle = new HilbertTurtle(new Turtle.Drawer() {
            @Override
            public void drawLine(double x0, double y0, double x1, double y1) {
                canvas.drawLine((float) x0, (float) y0, (float) x1, (float) y1, paint);
            }
        });
        turtle.setPos((w - size + step) / 2, (h + size - step) / 2);
        turtle.setDir(HilbertTurtle.E);
        turtle.draw(order, step, HilbertTurtle.R);

        return bitmap;
    }
}
