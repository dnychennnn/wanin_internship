package appGame.Puzzle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FullScreenViewActivity extends Activity {

    Button btnclose;
    SubsamplingScaleImageView FullScreenImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_view);
        Context context = getApplicationContext();

        FullScreenImage = (SubsamplingScaleImageView) findViewById(R.id.imgDisplay);

        //FullScreenImage.setScaleType(ImageView.ScaleType.CENTER);
        int goal_image_id;
        switch (getIntent().getExtras().getInt("position")%9){
            case 0:
                goal_image_id=R.drawable.a;//如果亂數結果為1，則選擇圖片a
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 1:
                goal_image_id=R.drawable.b;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 2:
                goal_image_id=R.drawable.c;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 3:
                goal_image_id=R.drawable.d;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 4:
                goal_image_id=R.drawable.e;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 5:
                goal_image_id=R.drawable.f;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 6:
                goal_image_id=R.drawable.g;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 7:
                goal_image_id=R.drawable.h;
                FullScreenImage.setImage(ImageSource.resource(goal_image_id));
                break;
            case 8:
                FullScreenImage.setImage(ImageSource.bitmap(getloadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/")));
        }

        btnclose = (Button)findViewById(R.id.btnClose);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private Bitmap getloadImageFromStorage(String path)
    {
        Bitmap b = null;
        try {
            File f=new File(path, "profile.jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            //載入ImageView

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }
}
