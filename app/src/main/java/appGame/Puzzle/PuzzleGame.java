package appGame.Puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PuzzleGame
{
	public Activity activity;
	public int x_count;										//拼圖鈕最大列數
	public int y_count;										//拼圖鈕最大欄數
	private int CurrentButtonNumber = 5;					//拼圖鈕的id編號(可隨意列)
	private int pre_x;
	private int pre_y;
	private int now_x;
	private int now_y;
	private int block;										//目前空格的拼圖鈕座標編號
	private int[] move_btn;									//目前要移動至空格的拼圖鈕座標編號,移動方向
	private int[] move_direction = new int[]{0,0};			//判斷全螢幕時之上下左右滑動

	public 	int  goal_image_id;								//拼圖目標圖片的編號
	public	Bitmap puzzle_goal;								//拼圖目標圖片(拼圖面版背景+拼圖鈕圖片來源)
	public	Rect goal_split;								//拼圖來源切割矩形區塊
	public	PuzzleObject Puzzles[];							//拼圖鈕陣列
	public	int Q_array[];									//題目陣列，即random後的陣列
	public	int A_array[];									//答案陣列
	private int WASH_TIMES = 100;							//洗牌時的隨機次數
	private DialogInterface.OnClickListener returnTitle;	//從上層接過來的回主選單function
	private boolean isFullScreenSlide=false;						//判斷滑動狀態

	private int puzzle_xdp=70;								//拼圖鈕的dpi，這邊設定為
	private int puzzle_ydp=70;								//拼圖鈕的dpi，這邊設定為
	private float dpi;										//解析度，即1dpi為多少像素
	private int scene_flag=0;								//播放動畫的flag，0為目前沒動畫
	private Handler handler; 								//動畫tick的timer驅動者

	private ImageView imgAnswer;							//答案圖
	private Button btnAnswer;								//答案按鈕
	private Button btnRestart;								//重新玩按鈕
	private Button btnReturnTitle;							//回主選單按紐
	private Button btnFullScreen;                           //切換為全螢幕滑動
	private int[] RID = {R.id.btnbuffer1, R.id.btnbuffer2, R.id.btnbuffer3, R.id.btnbuffer4, R.id.btnbuffer5,
		R.id.btnbuffer6, R.id.btnbuffer7, R.id.btnbuffer8, R.id.btnbuffer9, R.id.btnbuffer10,
		R.id.btnbuffer11, R.id.btnbuffer12, R.id.btnbuffer13, R.id.btnbuffer14, R.id.btnbuffer15,
		R.id.btnbuffer16, R.id.btnbuffer17, R.id.btnbuffer18, R.id.btnbuffer19, R.id.btnbuffer20,
		R.id.btnbuffer21, R.id.btnbuffer22, R.id.btnbuffer23, R.id.btnbuffer24, R.id.btnbuffer25};

	public PuzzleGame(int _x_count,int _y_count,Context _activity,DialogInterface.OnClickListener _returnTitle)
	{//建構式
		//從外部讀入參數
		x_count = _x_count;
		y_count = _y_count;
		activity = (Activity)_activity;
		returnTitle = _returnTitle;
		//取得DPI
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		dpi=metrics.scaledDensity;
		puzzle_xdp = (int)(metrics.widthPixels*0.75/x_count);//取得目前x最大的dpi，均分給x_count的各拼圖鈕
		//設定timer
		handler = new Handler();
		handler.removeCallbacks(tick); 	//事件
		handler.postDelayed(tick, 33); 	//延遲時間
	}

	public void game_start(Bitmap _goalImage)
    {//遊戲初始化
        // 取得切換到middle.xml(遊戲主頁)
        activity.setContentView(R.layout.middle);
        FrameLayout.LayoutParams params1;
        //FrameLayout puzzle_bg = (FrameLayout)activity.findViewById(R.id.puzzle_bg);
        FrameLayout puzzle_panel = (FrameLayout)activity.findViewById(R.id.puzzle_panel);
        //初始化遊戲資料記錄陣列
        puzzle_goal = _goalImage;
        //puzzle_bg.setBackgroundDrawable(new BitmapDrawable(puzzle_goal));
        goal_split = new Rect(0,0,puzzle_goal.getWidth(),puzzle_goal.getHeight());//切割矩形區塊為整張圖
        Bitmap[] puzzles_image = BitmapSpliter(goal_split,x_count,y_count,puzzle_goal);
        set_QA_array(x_count,y_count);//取得A_array跟Q_array
        Puzzles = new PuzzleObject[x_count*y_count];
        //動態生成拼圖鈕
        puzzle_ydp = (int)(puzzle_xdp*(goal_split.bottom-goal_split.top)/(goal_split.right-goal_split.left));//依拼圖圖片比例(xdp*高/寬)調整拼圖鈕的dpi

        ImageView btnBuffer;
        for(int i = 0 ; i < x_count ; i++) {
            for (int j = 0; j < y_count; j++) {
                if (i == x_count - 1 && j == y_count - 1) {//設定目前空格的拼圖鈕座標編號為最右下角
                    block = i * x_count + j;
                } else {
                    btnBuffer = (ImageView) activity.findViewById(RID[i * x_count + j]); //將XML按鈕陣列帶進BUFFER操作
                    //產生新的button
//					btnBuffer = new ImageView(activity);
                    CurrentButtonNumber++;
                    //將值設定入遊戲資料記錄陣列
                    btnBuffer.setImageBitmap(puzzles_image[Q_array[i * x_count + j]]);
                    Puzzles[i * x_count + j] = new PuzzleObject();
                    Puzzles[i * x_count + j].no = Q_array[i * x_count + j];
                    Puzzles[i * x_count + j].values = A_array[Q_array[i * x_count + j]];
                    Puzzles[i * x_count + j].id = btnBuffer.getId();
                    Puzzles[i * x_count + j].image = puzzles_image[Q_array[i * x_count + j]];
                    Puzzles[i * x_count + j].display_object = btnBuffer;
                    //設定按鈕座標及重心於左上角
                    params1 = new FrameLayout.LayoutParams(puzzle_xdp, puzzle_ydp, Gravity.TOP | Gravity.LEFT);
                    params1.setMargins(j * puzzle_xdp, i * puzzle_ydp, j * 50 + 50, i * 50 + 50);
                    btnBuffer.setLayoutParams(params1);
                    //設定按鈕的觸控事件
					btnBuffer.setOnTouchListener(onTouchPuzzle);


                    //將按鈕加入拼圖面板中
//					puzzle_panel.addView(btnBuffer);
                    //刷新按鈕狀態
//					btnBuffer.invalidate();
                }
            }
        }
    }

    public void turn_start(Bitmap _goalImage)
    {//遊戲初始化
        // 取得切換到middle.xml(遊戲主頁)
        activity.setContentView(R.layout.middle);
        FrameLayout.LayoutParams params1;
        //FrameLayout puzzle_bg = (FrameLayout)activity.findViewById(R.id.puzzle_bg);
        FrameLayout puzzle_panel = (FrameLayout)activity.findViewById(R.id.puzzle_panel);
        //初始化遊戲資料記錄陣列
        puzzle_goal = _goalImage;
        //puzzle_bg.setBackgroundDrawable(new BitmapDrawable(puzzle_goal));
        goal_split = new Rect(0,0,puzzle_goal.getWidth(),puzzle_goal.getHeight());//切割矩形區塊為整張圖
        Bitmap[] puzzles_image = BitmapSpliter(goal_split,x_count,y_count,puzzle_goal);
        set_QA_array(x_count,y_count);//取得A_array跟Q_array
        Puzzles = new PuzzleObject[x_count*y_count];
        //動態生成拼圖鈕
        puzzle_ydp = (int)(puzzle_xdp*(goal_split.bottom-goal_split.top)/(goal_split.right-goal_split.left));//依拼圖圖片比例(xdp*高/寬)調整拼圖鈕的dpi

        ImageView btnBuffer;
        for(int i = 0 ; i < x_count ; i++) {
            for (int j = 0; j < y_count; j++) {
                btnBuffer = (ImageView) activity.findViewById(RID[i * x_count + j]); //將XML按鈕陣列帶進BUFFER操作
                //產生新的button
//					btnBuffer = new ImageView(activity);
                CurrentButtonNumber++;
                //將值設定入遊戲資料記錄陣列
                btnBuffer.setImageBitmap(puzzles_image[Q_array[i * x_count + j]]);
                Puzzles[i * x_count + j] = new PuzzleObject();
                Puzzles[i * x_count + j].no = Q_array[i * x_count + j];
                Puzzles[i * x_count + j].values = A_array[Q_array[i * x_count + j]];
                Puzzles[i * x_count + j].id = btnBuffer.getId();
                Puzzles[i * x_count + j].image = puzzles_image[Q_array[i * x_count + j]];
                Puzzles[i * x_count + j].display_object = btnBuffer;
                //設定按鈕座標及重心於左上角
                params1 = new FrameLayout.LayoutParams(puzzle_xdp, puzzle_ydp, Gravity.TOP | Gravity.LEFT);
                params1.setMargins(j * puzzle_xdp, i * puzzle_ydp, j * 50 + 50, i * 50 + 50);
                btnBuffer.setLayoutParams(params1);
                //設定按鈕的觸控事件
//					btnBuffer.setOnTouchListener(onTouchPuzzle);
                btnBuffer.setOnTouchListener(onTouchTurn);

                //將按鈕加入拼圖面板中
//					puzzle_panel.addView(btnBuffer);
                //刷新按鈕狀態
//					btnBuffer.invalidate();
            }
        }

        //答案面版安裝
		imgAnswer = (ImageView)activity.findViewById(R.id.img_answer);
		imgAnswer.setImageBitmap(puzzle_goal);

		//判定是否已經有正確的按鈕
		check_original_OK();


		//主功能選單事件安裝
		//--重玩鈕--
		btnRestart = (Button)activity.findViewById(R.id.btn_restart);
		btnRestart.setOnClickListener(onClickRestart);
		//--答案鈕--
		btnAnswer = (Button)activity.findViewById(R.id.btn_answer);
		btnAnswer.setOnTouchListener(onTouchAnswer);
		//回主選單
		btnReturnTitle= (Button)activity.findViewById(R.id.btn_returntitle);
		btnReturnTitle.setOnClickListener(onClickReturntitle);
		//切換全螢幕滑動
		btnFullScreen = (Button)activity.findViewById(R.id.btnfullscreen);
        btnFullScreen.setOnClickListener(OnClickFullScreen);
    }

	public void set_QA_array(int _x_count,int _y_count)
	{
		//暫存序列化陣列(即ser_array[0] = 0,ser_array[1]=1...)
		int[] ser_array = new int[_x_count*_y_count];
		for(int i = 0; i < _x_count *_y_count ; i++)
		{
			ser_array[i] = i;
		}
		//初始化A_array
		if(_x_count == 4 && _y_count == 4)
		{//棋盤版
			A_array = new int[]{0,1,1,2,2,3,3,4,4,5,5,6,6,6,6,6};
			FrameLayout  Puzzle_bg = (FrameLayout)activity.findViewById(R.id.puzzle_bg);
			Puzzle_bg.setBackgroundResource(R.drawable.wood4);
		}
		else
		{//一般圖片
			A_array = ser_array.clone();
			FrameLayout  Puzzle_bg = (FrameLayout)activity.findViewById(R.id.puzzle_bg);
			Puzzle_bg.setBackgroundResource(R.drawable.background);
		}
		//初始化Q_array
		Q_array = random_puzzle(ser_array);
	}

	public int[] random_puzzle(int[] _old)
	{//洗牌
		int[] result = _old.clone();//先複製答案陣列到結果陣列中
		int swap1;
		int swap2;
		int randBuffer;
		int rand_count = result.length-1;//-1因為最後一格不洗牌

		for(int i = 0; i < WASH_TIMES; i++)
		{
			//隨機出要交換的陣列元素編號
			swap1=(int)((Math.random()*rand_count));
			swap2=(int)((Math.random()*rand_count));
			if(swap1 != swap2)
			{//陣列元素不同，則交換
				randBuffer = result[swap1];
				result[swap1] = result[swap2];
				result[swap2] = randBuffer;
			}
			else
			{//陣列元素不同，重洗一次
				i--;
			}
		}
		return result;
	}

	/**
	 * 將大張BITMAP依參數切成等份小塊BITMAP，置入陣列並回傳
	 * private Bitmap[] BitmapSpliter(Rect 切割矩形區塊,int x的份數,int y的份數 ,Bitmap 被切割的圖片)
	 */
	private Bitmap[] BitmapSpliter(Rect split_rect,int x_split,int y_split,Bitmap split_source)
	{
		Bitmap[] result;
		Bitmap bitmapBuffer;
		int postion_width = split_rect.right - split_rect.left;		//計算切割矩型的寬
		int postion_height = split_rect.bottom - split_rect.top;	//計算切割矩型的高
		int split_width = postion_width / x_split;					//計算切割區塊每塊的寬
		int split_height = postion_height / y_split;				//計算切割區塊每塊的高

		result = new Bitmap[x_split*y_split];

		for(int i = 0; i < x_split; i++)
		{
			for(int j = 0; j < y_split; j++)
			{
				//取出指定區塊的bitmap
				bitmapBuffer = Bitmap.createBitmap(split_source, split_rect.left+(j*split_width), split_rect.top+(i*split_height), split_width,split_height);
				//存入結果陣列
				result[i*y_split+j] = bitmapBuffer;
			}
		}

		return result;
	}

	private ImageView.OnTouchListener onTouchPuzzle = new ImageView.OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent event)
		{
			//動畫中不接受事件
			if(scene_flag != 0)
				return true;

			switch(event.getAction())
			{//依touch事件不同作不同的事情
				case MotionEvent.ACTION_DOWN:
					//按下時，取得目前的x,y
					pre_x=(int)event.getX();
					pre_y=(int)event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					//移動時，判斷按鈕移動
					ImageView btnBuffer=(ImageView)activity.findViewById(view.getId());
					//判斷move的方向
					int now_x = (int)event.getX();
					int now_y = (int)event.getY();
					int[] move_direction = new int[]{0,0};
					if(pre_x > now_x)//往左
						move_direction[0]=3;
					else if(pre_x < now_x)//往右
						move_direction[0]=4;
					if(pre_y > now_y)//往上
						move_direction[1]=1;
					else if(pre_y < now_y)//往下
						move_direction[1]=2;

					//取得按下的拼圖鈕座標及可移動方向
					int[] canMove = get_canMove(btnBuffer.getId());
					//判斷是否可移動
					if((canMove[1] == move_direction[0] || canMove[1] == move_direction[1]) && canMove[1] != 0)
					{//如果目前的移動方向等於可移動方向，最後得判斷!=0，以確保不會出現都是0結果進入的情況
						//將此拼圖鈕設定為移動目標
						move_btn = canMove;
						scene_flag = 1;
					}
					break;
			}
			return true;
		}
	};

	private View.OnTouchListener onTouchFullScreen = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			// 紀錄判斷上下左右

			//動畫中不接受事件.
			if(scene_flag != 0)
				return true;

			switch(motionEvent.getAction())
			{//依touch事件不同作不同的事情
				case MotionEvent.ACTION_DOWN:
					//按下時，取得目前的x,y
					pre_x=(int)motionEvent.getX();
					pre_y=(int)motionEvent.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					//移動時，判斷按鈕移動
					//判斷move的方向
					int now_x = (int)motionEvent.getX();
					int now_y = (int)motionEvent.getY();
                    int[] canMove;
					int dx = Math.abs(now_x - pre_x);
					int dy = Math.abs(now_y - pre_y);
					if(dx>dy) { //判斷x or y large
						if(pre_x > now_x){
							//往左
							move_direction[0]=3;
							//canMove = get_canMove2(move_direction[0]);
							Log.i("Dir", "往左 ");
						}
						else if(pre_x < now_x){
							//往右
							move_direction[0]=4;
							//canMove = get_canMove2(move_direction[0]);
							Log.i("Dir", "往右 ");
						}
					} else{
						if(pre_y > now_y){
							//往上
							move_direction[0]=1;
							//canMove = get_canMove2(move_direction[0]);
							Log.i("Dir", "往上 ");
						}
						else if(pre_y < now_y){
							//往下
							move_direction[0]=2;
							//canMove = get_canMove2(move_direction[0]);
							Log.i("Dir", "往下 ");
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					Log.i("dir", move_direction[0]+"");
					canMove = get_canMove2(move_direction[0]);
					Log.i("ACTION: ", "UP");
					//Log.i("dir", canMove[1]+"");

					//判斷是否可移動
					if((canMove[1] == move_direction[0] || canMove[1] == move_direction[1]) && canMove[1] != 0)
					{//如果目前的移動方向等於可移動方向，最後得判斷!=0，以確保不會出現都是0結果進入的情況
						//將此拼圖鈕設定為移動目標
						move_btn = canMove;
						scene_flag = 1;
					}
			}
			return true;
		}
	};

	int down_x;
	int down_y;
    int orig_x;
    int orig_y;
	private View.OnTouchListener onTouchTurn = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			//動畫中不接受事件
			if(scene_flag != 0)
				return true;
			//取得資源
			ImageView btnBuffer=(ImageView)activity.findViewById(view.getId());
			FrameLayout.LayoutParams p_start =(FrameLayout.LayoutParams)btnBuffer.getLayoutParams();
			pre_x=p_start.leftMargin;
			pre_y=p_start.topMargin;
            Log.i("pre", pre_x +  " " +pre_y);

			switch(event.getAction())
			{//依touch事件不同作不同的事情
				case MotionEvent.ACTION_DOWN:
					//按下時，取得目前的x,y
                    //初始座標
					down_x = (int)event.getX();
					down_y = (int)event.getY();
                    orig_x = p_start.leftMargin;
                    orig_y = p_start.topMargin;
					btnBuffer.setAlpha(0.5f);
					btnBuffer.setPadding(10,0,0,20);
                    Log.i("corr_orig", "x: " + orig_x + "y: " + orig_y);
					break;
				case MotionEvent.ACTION_MOVE:
					//移動時，獲取手指位置
					now_x = ((int)event.getX()-down_x)+pre_x;
					now_y = ((int)event.getY()-down_y)+pre_y;
                    if(now_x>3*puzzle_xdp) {
                        now_x = 3*puzzle_xdp;
                    }
                    if(now_y>3*puzzle_ydp){
                        now_y = 3*puzzle_ydp;
                    }
                    //將更新的位置設入參數
					p_start.setMargins(now_x,now_y,0,0);
                    btnBuffer.setLayoutParams(p_start);
                    //交換事件
                    if((now_x-orig_x) > (puzzle_xdp/2) || (now_y - orig_y) > (puzzle_ydp/2) || Math.abs(now_x-orig_x) > (puzzle_xdp/2) || Math.abs(now_y-orig_y) > (puzzle_ydp/2)) {
                        int change_btn_x = Math.round((float)(now_x)/(float)(puzzle_xdp))*puzzle_xdp;  //計算目的按鈕的得先轉為浮點數再進行運算，方能再滑到一半就switch
                        int change_btn_y = Math.round((float)now_y/(float)puzzle_ydp)*puzzle_ydp;
                        Log.i("corr", "x " + change_btn_x + "y " + change_btn_y);

                        //將要交換的象棋設定位置
                        ImageView change_btn = Puzzles[ (change_btn_x/puzzle_xdp) + y_count*(change_btn_y/puzzle_ydp)].display_object;
                        FrameLayout.LayoutParams c_start = (FrameLayout.LayoutParams)change_btn.getLayoutParams();
                        c_start.setMargins(orig_x, orig_y,0,0);
                        change_btn.setLayoutParams(c_start);

                        //將象棋物件調換
                        Puzzles[(orig_x / puzzle_xdp) + y_count * (orig_y / puzzle_ydp)] = Puzzles[(change_btn_x / puzzle_xdp) + y_count * (change_btn_y / puzzle_ydp)];

                        orig_x = change_btn_x;
                        orig_y = change_btn_y;

                        Log.d("coor_orid", "x: " + orig_x + "y: " + orig_y);
                    }
					break;
				case MotionEvent.ACTION_UP:

					int new_x = Math.round((float)(now_x)/(float)(puzzle_xdp))*puzzle_xdp;;
					int new_y = Math.round((float)(now_y)/(float)(puzzle_ydp))*puzzle_ydp;

					p_start.setMargins(new_x, new_y, 0, 0);
					btnBuffer.setLayoutParams(p_start);

                    for(int i=0;i<x_count;i++){
                        for(int j=0;j<y_count;j++){
                            if(Puzzles[i*x_count+j].id == btnBuffer.getId()){
                                Puzzles[new_x/puzzle_xdp+y_count*(new_y/puzzle_ydp)] = Puzzles[i*x_count+j]; //將象棋物件給到提起的象棋
                            }
                            check_OK(i*x_count+j); //判斷是否已達正確位置並設flag
                        }
                    }

                    btnBuffer.setAlpha(1f);
                    btnBuffer.setPadding(0,0,0,0);
			}
			return true;
		}
	};

    private View.OnTouchListener onTouchCancel = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };


	private ImageView.OnClickListener onClickRestart = new ImageView.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			//動畫中不接受事件
			if(scene_flag != 0)
				return;

			//按下時，詢問重新啟動遊戲
			AlertDialog.Builder alertMessage = new AlertDialog.Builder(activity);
			alertMessage.setTitle("系統公告");
			alertMessage.setMessage("是否要重新洗牌，您將失去目前遊玩的進度");
			alertMessage.setPositiveButton("確定", playAgain);
			alertMessage.setNegativeButton("取消", null);
			alertMessage.create().show();
		}
	};

	private ImageView.OnTouchListener onTouchAnswer = new ImageView.OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent event)
		{
			//動畫中不接受事件
			if(scene_flag != 0)
				return true;

			FrameLayout puzzle_panel;
			FrameLayout answer_panel;

			switch(event.getAction())
			{//依touch事件不同作不同的事情
				case MotionEvent.ACTION_DOWN:
					//按下時，顯示答案畫面
					puzzle_panel = (FrameLayout)activity.findViewById(R.id.puzzle_panel);
					answer_panel = (FrameLayout)activity.findViewById(R.id.answer_panel);
					puzzle_panel.setVisibility(View.GONE);//隱藏
					answer_panel.setVisibility(View.VISIBLE);//顯示
					break;
				case MotionEvent.ACTION_UP:
					//放開時，隱藏答案畫面
					puzzle_panel = (FrameLayout)activity.findViewById(R.id.puzzle_panel);
					answer_panel = (FrameLayout)activity.findViewById(R.id.answer_panel);
					puzzle_panel.setVisibility(View.VISIBLE);//顯示
					answer_panel.setVisibility(View.GONE);//隱藏
					break;
			}
			return true;
		}
	};

	private ImageView.OnClickListener onClickReturntitle = new ImageView.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			//動畫中不接受事件
			if(scene_flag != 0)
				return;

			//彈跳詢問訊息
			AlertDialog.Builder alertMessage = new AlertDialog.Builder(activity);
			alertMessage.setTitle("系統公告");
			alertMessage.setMessage("是否返回主選單?");
			alertMessage.setPositiveButton("回主選單", returnTitle);
			alertMessage.setNegativeButton("取消", null);
			alertMessage.create().show();
		}
	};

	private Button.OnClickListener OnClickFullScreen = new Button.OnClickListener(){
		@Override
		public void onClick(View view) {
            View v = activity.findViewById(R.id.puzzle_panel);
            if(!isFullScreenSlide) {
                v.setOnTouchListener(onTouchFullScreen);
                btnFullScreen.setBackgroundColor(Color.BLUE);
            }
			else{
                v.setOnTouchListener(onTouchCancel);
                btnFullScreen.setBackgroundColor(Color.LTGRAY);
            }
			isFullScreenSlide = !isFullScreenSlide;
            Log.i("FULLSCREEN", "isFullScreenSlide: "+isFullScreenSlide);
		}
	};


    public void switch_button() {


    }



	public int[] get_canMove(int myButton)
	{//取得目前myButton拼圖鈕的座標編號和可以移動的方向
        int[] result = new int[]{0,0};//[0]為目前拼圖鈕的座標編號 [1]為可移動方向

        for(int i = 0; i < x_count; i++)
        {
            for(int j = 0; j < y_count; j++)
            {
                if(Puzzles[i*x_count+j].id == myButton)
                {
                    result[0] = i*x_count+j;

                    if(((i-1)*x_count+j) == block)//可上
                        result[1] =1;
                    else if(((i+1)*x_count+j) == block)//可下
                        result[1] =2;
                    else if((i*x_count+j-1) == block)//可左
                        result[1] =3;
                    else if((i*x_count+j+1) == block)//可右
                        result[1] =4;
                    //強制結束迴圈
                    i=4;
                    j=4;
                }
            }
        }
        return result;
	}

	public int[] get_canMove2(int movedirection)
	{//取得目前myButton拼圖鈕的座標編號和可以移動的方向
		int[] result = new int[]{0,0};//[0]為目前拼圖鈕的座標編號 [1]為可移動方向
        int block_x, block_y;

        block_x = block%x_count;
        block_y = block/y_count;

        if(movedirection==1 && block_y!=(y_count-1)){
            // 向上
            result[0] = (block_y+1)*y_count+block_x;
            result[1] = movedirection;
        } else if(movedirection==2 && block_y!=0){
            // 向下
            result[0] = (block_y-1)*x_count+block_x;
            result[1] = movedirection;
        } else if(movedirection==3 && block_x != (x_count-1) ){
            //向左
            result[0] = block_y * y_count + (block_x+1);
            result[1] = movedirection;
        } else if(movedirection==4 && block_x != 0){
            //向右
            result[0] = block_y * y_count + (block_x-1);
            result[1] = movedirection;
        }
        return result;
	}

	//動畫用timer
	private Runnable tick = new Runnable()
	{//TION
		public void run()
		{
			if(scene_flag !=0)
			{
				puzzle_move();
			}
			handler.postDelayed(this, 33);
		}
	};

	public void puzzle_move()
	{
		//設定每次位移的量
		int x_speed = (int)(10*dpi);
		int y_speed = (int)(10*dpi);
		ImageView btn_start = Puzzles[move_btn[0]].display_object;
		FrameLayout.LayoutParams p_start =(FrameLayout.LayoutParams)btn_start.getLayoutParams();
		//取得相關座標
		int new_x = p_start.leftMargin;		//移動後的X(先預設為目前起始的點)
		int new_y = p_start.topMargin;		//移動後的Y(先預設為目前起始的點)
		int goal_x = block%x_count;			//目的地的X
		int goal_y = block/y_count;			//目的地的Y
		//移動處理
		switch(move_btn[1])
		{
			case 1://向上
				if((p_start.topMargin - y_speed)<= (goal_y*puzzle_ydp))
					new_y = goal_y*puzzle_ydp;
				else
					new_y -= y_speed;
				break;
			case 2://向下
				if((p_start.topMargin + y_speed)>= goal_y*puzzle_ydp)
					new_y = goal_y*puzzle_ydp;
				else
					new_y += y_speed;
				break;
			case 3://向左
				if((p_start.leftMargin - x_speed)<= goal_x*puzzle_xdp)
					new_x = goal_x*puzzle_xdp;
				else
					new_x -= x_speed;
				break;
			case 4://向右
				if((p_start.leftMargin + x_speed)>= goal_x*puzzle_xdp)
					new_x = goal_x*puzzle_xdp;
				else
					new_x += x_speed;
				break;
		}
		//將新座標置入
		p_start.setMargins(new_x, new_y, 0, 0);
		btn_start.setLayoutParams(p_start);

		if(new_x == goal_x*puzzle_xdp && new_y == goal_y*puzzle_ydp)
		{//已移動到目標點，關閉動畫
			Puzzles[block] = Puzzles[move_btn[0]];
			Puzzles[move_btn[0]] = new PuzzleObject();

			//確認拼圖已經到答案處
			check_OK(block);

			block = move_btn[0];
            Log.i("block", "x: "+ block%x_count + ", y: " + block/y_count);

			//如果空白處為最後一格，則判斷勝負
			if(block == x_count*y_count-1)
			{
				check_win();
			}
			scene_flag = 0;
		}
		//String error_info = "move_btn="+move_btn[0]+","+move_btn[1]+" ,block="+block+" ,new_xy="+new_x+","+new_y+" ,goal_xy="+goal_x*puzzle_dp+","+goal_y*puzzle_dp;
		//debug_view.setText(error_info);
	}


	public void check_win()
	{//判斷是否已經過關
		for(int i = 0; i < x_count*y_count-1; i++)
		{
			if(Puzzles[i].values != A_array[i])
			{//出現不相同則結束判斷
				return;
			}
		}
		//除最後一格皆相同，則成功過關
		game_end();
	}

	public void check_OK(int block) {
			// 直接測試移動過去那一個是不是正確的位置!!!!
            Bitmap bitmap = Bitmap.createBitmap(Puzzles[block].image, 0, 0, Puzzles[block].image.getWidth(), Puzzles[block].image.getHeight());
            Canvas canvas = new Canvas(bitmap);
			View drawView = new DrawView(activity.getApplicationContext(), puzzle_xdp, puzzle_ydp);
			drawView.draw(canvas);

			if (Puzzles[block].values == A_array[block]) {
                Puzzles[block].display_object.setImageBitmap(bitmap);
            } else {
                Puzzles[block].display_object.setImageBitmap(Puzzles[block].image);
            }
		}

	public void check_original_OK() {
		for(int i = 0; i < x_count*y_count-1; i++) {
			if(Puzzles[i].values == A_array[i]) {
				Bitmap bitmap = Bitmap.createBitmap(Puzzles[i].image, 0, 0, Puzzles[i].image.getWidth(), Puzzles[i].image.getHeight());
				View drawView = new DrawView(activity.getApplicationContext(), puzzle_xdp, puzzle_ydp);
				Canvas canvas = new Canvas(bitmap);
				drawView.draw(canvas);
				Puzzles[i].display_object.setImageBitmap(bitmap);
			}
		}
	}


	public void game_end()
	{//過關處理
		//彈跳過關訊息
		AlertDialog.Builder alertMessage = new AlertDialog.Builder(activity);
		alertMessage.setTitle("系統公告");
		alertMessage.setMessage("恭喜您成功達成!!!");
		alertMessage.setPositiveButton("再玩一次", playAgain);
		alertMessage.setNeutralButton("回主選單", returnTitle);
		alertMessage.setNegativeButton("不玩了", exitGame);
		alertMessage.create().show();
	}

	private DialogInterface.OnClickListener playAgain = new DialogInterface.OnClickListener()
	{//再玩一次
		public void onClick(DialogInterface arg0, int arg1)
		{
			game_start(puzzle_goal);
		}
	};

	private DialogInterface.OnClickListener exitGame = new DialogInterface.OnClickListener()
	{//不玩了
		public void onClick(DialogInterface arg0, int arg1)
		{
			activity.finish();
		}
	};
}
