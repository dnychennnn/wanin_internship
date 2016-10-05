package appGame.Puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by yungyu.chen on 2016/9/30.
 */

public class DrawView extends View {

    int width = 0;
    int height = 0;
    public DrawView(Context context, float width, float height) {
        super(context);
        this.width = (int)width;
        this.height = (int)height;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Paint pOK = new Paint();
//        pText.setColor(Color.BLACK);
//        pText.setTextSize(50);
//        canvas.drawText("OK!!!", 100, 100, pText);
        Bitmap icon = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.anwser_ok);
        Rect rect = new Rect(0,0,width,height);
        canvas.drawBitmap(icon,null,rect,pOK);
    }
}
