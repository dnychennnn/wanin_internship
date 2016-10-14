package appGame.Puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PuzzleActivity extends Activity implements SensorEventListener
{
	/** Called when the activity is first created. */
	private final static int RESULT_LOAD_IMAGE = 123;
 	private final static int START_CAMERA = 125;
	private final static String TAG = "PuzzleActivity";

	private int x_count=4;					//拼圖鈕最大列數
	private int y_count=4;					//拼圖鈕最大欄數
	private int sence;						//記錄所在的XML

	private int goal_image_id = R.drawable.a;    //初始圖片參數
	private int i1=1;
    private boolean isSnapAndLoad = false;

    //接近感應器
    private SensorManager mgr;
    private Sensor proximity;
    private Vibrator vibrator;
    private float lastVal = -1;


	Bitmap bmpBuffer;
	ImageView image1;

	//全屏滑動
	private int pre_x;
	private int pre_y;




    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sence = R.layout.main;//記錄目前在main
		init();
	}

	public void init()
	{//初始化
		image1= (ImageView) findViewById(R.id.ImageView1);
		Button btnEasy;
		Button btnMiddle;
		Button btnHard;
		Button btnLoad;
		Button btnShot;
        Button btnScreenShot;
		Button btnTurn;
		//取得按鈕元件
		btnEasy = (Button)findViewById(R.id.btn_3x3);
		btnMiddle = (Button)findViewById(R.id.btn_4x4);
		btnHard = (Button)findViewById(R.id.btn_5x5);
		btnLoad = (Button)findViewById(R.id.btn_load);
		btnShot = (Button)findViewById(R.id.btn_shot);
		btnTurn = (Button)findViewById(R.id.btn_turn);
        btnScreenShot = (Button)findViewById(R.id.btn_screenshot);
		// 設定Click事件
		btnEasy.setOnClickListener(onbtnChoose);
		btnMiddle.setOnClickListener(onbtnChoose);
		btnHard.setOnClickListener(onbtnChoose);
		btnLoad.setOnClickListener(onbtnChoose);
		btnShot.setOnClickListener(onbtnChoose);
        btnScreenShot.setOnClickListener(onbtnChoose);
		btnTurn.setOnClickListener(onbtnChoose);

		//選取圖片的Button
		Button btn_next = (Button)findViewById(R.id.NEXT_BUTTON);//設定觸碰更換圖片按鈕的反應
		btn_next.setOnClickListener(getchangeimageNEXT);

		Button btn_pre = (Button)findViewById(R.id.Previous_BUTTON);//設定觸碰更換圖片按鈕的反應
		btn_pre.setOnClickListener(getchangeimagePrevious);

		//View Pager
		final LayoutInflater mInflater = getLayoutInflater().from(this);

		final View v1 = mInflater.inflate(R.layout.viewpager_1, null);
		View v2 = mInflater.inflate(R.layout.viewpager_2, null);
		View v3 = mInflater.inflate(R.layout.viewpager_3, null);
		View v4 = mInflater.inflate(R.layout.viewpager_4, null);
		View v5 = mInflater.inflate(R.layout.viewpager_5, null);
		View v6 = mInflater.inflate(R.layout.viewpager_6, null);
		View v7 = mInflater.inflate(R.layout.viewpager_7, null);
		View v8 = mInflater.inflate(R.layout.viewpager_8, null);
		final View v9 = mInflater.inflate(R.layout.viewpager_9, null);

		List viewList = new ArrayList<View>();
		viewList.add(v1);
		viewList.add(v2);
		viewList.add(v3);
		viewList.add(v4);
		viewList.add(v5);
		viewList.add(v6);
		viewList.add(v7);
		viewList.add(v8);
		viewList.add(v9);

		ViewPager mviewpager = (ViewPager)findViewById(R.id.viewpager);
		mviewpager.setAdapter(new MyViewPagerAdapter(this, viewList));
		mviewpager.setCurrentItem(0);
		mviewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				//目前顯示的圖片編號，並印出圖片
				isSnapAndLoad = false;
				position = position % 9;
				Log.i(TAG, position+"");
				switch(position)
				{
					case 0:
						goal_image_id=R.drawable.a;//如果亂數結果為1，則選擇圖片a
						break;
					case 1:
						goal_image_id=R.drawable.b;
						break;
					case 2:
						goal_image_id=R.drawable.c;
						break;
					case 3:
						goal_image_id=R.drawable.d;
						break;
					case 4:
						goal_image_id=R.drawable.e;
						break;
					case 5:
						goal_image_id=R.drawable.f;
						break;
					case 6:
						goal_image_id=R.drawable.g;
						break;
					case 7:
						goal_image_id=R.drawable.h;
						break;
					case 8:
						isSnapAndLoad = true;
						ImageView LoadStoreImage = (ImageView) v9.findViewById(R.id.imageVie);
						LoadStoreImage.setImageBitmap(getloadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/"));
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


		//初始圖片參數
		goal_image_id = R.drawable.a;

        // 將id設為1避免留著之前的數
        i1 = 1;


		// Android 所有的感應器的統一介面
		this.mgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		// 取得距離感應器
		this.proximity = this.mgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		// 用振動來反應距離的變化
		this.vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);


		// Zoom

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		Log.i(TAG, "sensor name: "+sensor.getName() + " " +
				"sensor type: "+ sensor.getType());
		if(event.sensor.getType()== Sensor.TYPE_PROXIMITY)
		{
			Log.d(TAG, "onSensorChanged...");
			if (event.values[0] == 0) {
				//near
				Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
			} else {
				//far
				Toast.makeText(getApplicationContext(), "far", Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "registerListener...");
		// 一定要在這註冊
		this.mgr.registerListener(this, this.proximity,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "unregisterListener...");
		// 一定要在這解註冊
		this.mgr.unregisterListener(this, this.proximity);
	}




	private Button.OnClickListener onbtnChoose = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			switch(v.getId())
			{
				case R.id.btn_3x3:
					x_count=3;
					y_count=3;
					PuzzleGame game3x3 = new PuzzleGame(x_count,y_count,v.getContext(),returnTitle);
                    if(isSnapAndLoad){
                        bmpBuffer = getloadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/");
                        game3x3.game_start(bmpBuffer);
                        isSnapAndLoad = false; //避免一直進來snapload
                    }else{
                        bmpBuffer = BitmapFactory.decodeResource(getResources(), goal_image_id);
                        game3x3.game_start(bmpBuffer);
                    }
					sence = R.layout.middle;//記錄目前在middle
					break;
				case R.id.btn_4x4:
					x_count=4;
					y_count=4;
					PuzzleGame game4x4 = new PuzzleGame(x_count,y_count,v.getContext(),returnTitle);
					goal_image_id = R.drawable.chess;//限定為棋盤
					bmpBuffer = BitmapFactory.decodeResource(getResources(), goal_image_id);
					game4x4.game_start(bmpBuffer);
					sence = R.layout.middle;//記錄目前在middle
					break;
				case R.id.btn_5x5:
					x_count=5;
					y_count=5;
					PuzzleGame game5x5 = new PuzzleGame(x_count,y_count,v.getContext(),returnTitle);
                    if (isSnapAndLoad) {
                        bmpBuffer = getloadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/");
                        game5x5.game_start(bmpBuffer);
                        isSnapAndLoad = false; //避免一直進來snapload
                    }else {
                        bmpBuffer = BitmapFactory.decodeResource(getResources(), goal_image_id);
                        game5x5.game_start(bmpBuffer);
                    }
					sence = R.layout.middle;//記錄目前在middle
					break;
				case R.id.btn_load:
					Log.i("Button", "Load Image");
					Intent intent = new Intent();
					// Show only images, no videos or anything else
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					// Always show the chooser (if there are multiple options available)
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
					break;
				case R.id.btn_shot:
					//使用Intent調用其他服務幫忙拍照
					Intent intent_camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent_camera, START_CAMERA);
					Log.i(TAG, "start camera..");
					break;
                case R.id.btn_screenshot:
                    takeScreenshot();
                    break;
				case R.id.btn_turn:
					x_count=4;
					y_count=4;
					PuzzleGame gameTurn = new PuzzleGame(x_count,y_count,v.getContext(),returnTitle);
					goal_image_id = R.drawable.chess;//限定為棋盤
					bmpBuffer = BitmapFactory.decodeResource(getResources(), goal_image_id);
					gameTurn.turn_start(bmpBuffer);
					sence = R.layout.middle;//記錄目前在middle
					break;
			}
		}
	};



	private OnClickListener getchangeimageNEXT = new OnClickListener(){//設定當觸碰螢幕中那個更換圖片的按鈕後會出現的反應
		public void onClick(View v)
		{

			if(i1>8){
				i1=1;
			}else{
				i1++;
			}
            Log.i(TAG, "getCount: " + i1);

			//用來顯示的圖片編號TextView
			TextView commt = (TextView)findViewById(R.id.textView1);
			String i2= String.valueOf(i1);  //轉成字串格式
			commt.setText(i2);//印出目前選取的圖片編號


			//目前顯示的圖片編號，並印出圖片
			switch(i1)
			{
				case 1:
					image1.setImageResource(R.drawable.a);//如果亂數結果為1，則選擇圖片a
					goal_image_id=R.drawable.a;//如果亂數結果為1，則選擇圖片a
					break;
				case 2:
					image1.setImageResource(R.drawable.b);
					goal_image_id=R.drawable.b;
					break;
				case 3:
					image1.setImageResource(R.drawable.c);
					goal_image_id=R.drawable.c;
					break;
				case 4:
					image1.setImageResource(R.drawable.d);
					goal_image_id=R.drawable.d;
					break;
				case 5:
					image1.setImageResource(R.drawable.e);
					goal_image_id=R.drawable.e;
					break;
				case 6:
					image1.setImageResource(R.drawable.f);
					goal_image_id=R.drawable.f;
					break;
				case 7:
					image1.setImageResource(R.drawable.g);
					goal_image_id=R.drawable.g;
					break;
				case 8:
					image1.setImageResource(R.drawable.h);
					goal_image_id=R.drawable.h;
                    break;
                case 9:
                    isSnapAndLoad = true;
                    loadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/");
            }

		}
	};

	private OnClickListener getchangeimagePrevious = new OnClickListener(){//設定當觸碰螢幕中那個更換圖片的按鈕後會出現的反應
		public void onClick(View v)
		{

			if(i1<2){
				i1=9;
			}else{
				i1--;
			}
            Log.i(TAG, "getCount: " + i1);


			//用來顯示的圖片編號TextView
			TextView commt = (TextView)findViewById(R.id.textView1);
			String i2= String.valueOf(i1);  //轉成字串格式
			commt.setText(i2);//印出目前選取的圖片編號
			final ImageView image1 = (ImageView) findViewById(R.id.ImageView1);

			//目前顯示的圖片編號，並印出圖片
			switch(i1)
			{
				case 1:
					image1.setImageResource(R.drawable.a);//如果亂數結果為1，則選擇圖片a
					goal_image_id=R.drawable.a;//如果亂數結果為1，則選擇圖片a
					break;
				case 2:
					image1.setImageResource(R.drawable.b);
					goal_image_id=R.drawable.b;
					break;
				case 3:
					image1.setImageResource(R.drawable.c);
					goal_image_id=R.drawable.c;
					break;
				case 4:
					image1.setImageResource(R.drawable.d);
					goal_image_id=R.drawable.d;
					break;
				case 5:
					image1.setImageResource(R.drawable.e);
					goal_image_id=R.drawable.e;
					break;
				case 6:
					image1.setImageResource(R.drawable.f);
					goal_image_id=R.drawable.f;
					break;
				case 7:
					image1.setImageResource(R.drawable.g);
					goal_image_id=R.drawable.g;
					break;
				case 8:
					image1.setImageResource(R.drawable.h);
					goal_image_id=R.drawable.h;
                    break;
				case 9:
                    isSnapAndLoad = true;
					loadImageFromStorage("/data/data/appGame.Puzzle/app_imageDir/");
			}

		}
	};

	private DialogInterface.OnClickListener returnTitle = new DialogInterface.OnClickListener()
	{//回主選單
		public void onClick(DialogInterface arg0, int arg1)
		{
			setContentView(R.layout.main);
			sence = R.layout.main;//記錄目前在main
			init();
		}
	};

	/************* 將匯入或拍照的照片存在Internal Storage as profile.jpg ************/
 	// 存照片檔
	private String saveToInternalStorage(Bitmap bitmapImage) throws IOException {
		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
		// Create imageDir
		File mypath=new File(directory,"profile.jpg");
        Log.i(TAG, mypath.toString());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(mypath);
			// Use the compress method on the BitMap object to write image to the OutputStream
			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fos.close();
		}
		return directory.getAbsolutePath();
	}

    /*********** 外部匯入照片 ************/
    // 讀照片檔
	 private void loadImageFromStorage(String path) {

         try {
             File f = new File(path, "profile.jpg");
             Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
             // 將預覽圖設成profile.jpg
             image1.setImageBitmap(b);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
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

    /*********** ScreenShot function ************/
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            saveToInternalStorage(bitmap);
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CHOOSER);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

			Uri uri = data.getData();

			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				 Log.d(TAG, String.valueOf(bitmap));
				try{
					saveToInternalStorage(bitmap);
				}catch (Exception e){
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(requestCode == START_CAMERA && resultCode == RESULT_OK) {
			//取出拍照後回傳資料
			Bundle extras = data.getExtras();
			//將資料轉換為圖像格式
			Bitmap bmp = (Bitmap) extras.get("data");
			try{
				saveToInternalStorage(bmp);
			}catch (Exception e){
				e.printStackTrace();
			}
			//loadImageFromStorage(getFilesDir().toString());
		}
	}


		@Override
	public void onBackPressed()
	{//按下系統的回上頁鈕
		AlertDialog.Builder alertMessage = new AlertDialog.Builder(PuzzleActivity.this);
		alertMessage.setTitle("系統訊息");
		alertMessage.setMessage("您確定要離開?");
		alertMessage.setPositiveButton("確定", exitGame);
		alertMessage.setNegativeButton("取消", null);
		alertMessage.create().show();
	}

	private DialogInterface.OnClickListener exitGame = new DialogInterface.OnClickListener()
	{//關閉遊戲
		public void onClick(DialogInterface arg0, int arg1)
		{
			PuzzleActivity.this.finish();
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{//螢幕翻轉，覆寫讓他不會執行onCreate()
		super.onConfigurationChanged(newConfig);
		if(sence == R.layout.middle)
		{//如果是middle翻轉，則調整option_panel的設定
			LinearLayout option_panel = (LinearLayout)findViewById(R.id.option_panel);
			FrameLayout.LayoutParams p1 = (FrameLayout.LayoutParams)option_panel.getLayoutParams();
			//取得目前的螢幕翻轉方向
			int vOrientation = newConfig.orientation;
			//依翻轉方向進行調整
			if(vOrientation == Configuration.ORIENTATION_PORTRAIT)
			{//直立狀態
				option_panel.setOrientation(LinearLayout.HORIZONTAL);
				p1.height = p1.width;
				p1.width = LayoutParams.FILL_PARENT;
				p1.gravity = Gravity.BOTTOM;
			}
			else
			{//橫立
				option_panel.setOrientation(LinearLayout.VERTICAL);
				p1.width = p1.height;
				p1.height = LayoutParams.FILL_PARENT;
				p1.gravity = Gravity.RIGHT;
			}
		}
	}
}

