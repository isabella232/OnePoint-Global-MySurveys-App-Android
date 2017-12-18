package com.opg.my.surveys.lite;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opg.my.surveys.lite.common.MySurveysPreference;
import com.opg.my.surveys.lite.common.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignatureActivity extends RootActivity {

    private ImageView mClear, mGetSign, mCancel;


    // Creating Separate Directory for saving Generated Images
    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/MySurveys/UserSignature/";
    String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String StoredPath = DIRECTORY + pic_name + ".png";
    private TextView hintTV;
    private RelativeLayout mContent;
    private OPGSignatureView mSignature;
    private File file;
    private View view;
    private Bitmap bitmap;
    private Context mContext;
    private boolean isTablet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mContext = this;

        isTablet = Util.isTablet(mContext);
        if(!isTablet){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mContent = (RelativeLayout) findViewById(R.id.canvasLayout);


        addSignatureView(mContent);
        addhintTV(mContent);
        mClear = (ImageView) findViewById(R.id.clear);
        mGetSign = (ImageView) findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (ImageView) findViewById(R.id.cancel);
        view = mContent;
        mGetSign.setOnClickListener(onButtonClick);
        mClear.setOnClickListener(onButtonClick);
        mCancel.setOnClickListener(onButtonClick);

        // Method to create Directory, if the Directory doesn't exists
        file = new File(DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }

        setToolbar();
    }

    // Dynamically adding the signature view to the Layout
    private void addSignatureView(RelativeLayout mContent) {
        mSignature = new OPGSignatureView(getApplicationContext());
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    // Dynamically adding the textview to the Layout
    private void addhintTV(RelativeLayout mContent) {
        hintTV  = new TextView(getApplicationContext());
        hintTV.setText(getResources().getString(R.string.sign_here_title));
        hintTV.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
        setTypeface(hintTV,"font/roboto_regular.ttf");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        mContent.addView(hintTV,layoutParams);
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //To hide the default title from ToolBar
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setBackgroundColor(Color.parseColor(MySurveysPreference.getThemeActionBtnColor(this)));
    }

    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == mClear) {
                mSignature.clear();
                enableCanvas(false);
            } else if (v == mGetSign) {
                if (Build.VERSION.SDK_INT >= 23) {
                    boolean status = isStoragePermissionGranted();
                    if(!status){
                        return;
                    }
                }
                view.setDrawingCacheEnabled(true);
                mSignature.save(view, StoredPath);
                recreate();

            } else if(v == mCancel){
                // Calling the BillDetailsActivity
                Intent intent = new Intent();
                intent.putExtra("imagePath", StoredPath);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        }
    };


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            view.setDrawingCacheEnabled(true);
            mSignature.save(view, StoredPath);
            Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
            // Calling the same class
            recreate();
        }
        else
        {
            Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
        }
    }

    /*public class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v, String StoredPath) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);

                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, mFileOutStream);
                Intent intent = new Intent();
                intent.putExtra("imagePath", StoredPath);
                setResult(Activity.RESULT_OK, intent);
                finish();
                mFileOutStream.flush();
                mFileOutStream.close();

            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }

        }

        public void clear() {
            path.reset();
            invalidate();
            enableCanvas(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            enableCanvas(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }




        private void debug(String string) {

            Log.v("log_tag", string);

        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }*/

    public void enableCanvas(boolean status){
        mGetSign.setEnabled(status);
        if(status){
            hintTV.setVisibility(View.GONE);
        }else{
            hintTV.setVisibility(View.VISIBLE);
        }

    }

    public class OPGSignatureView extends View

    {

        private Paint mPaint = new Paint();
        private Path mPath = new Path();
        private Bitmap mBitmap;
        private Canvas mCanvas;

        private float curX, curY;
        private boolean isDragged = false;

        private static final int TOUCH_TOLERANCE = 4;
        private static final int TABLET_STROKE_WIDTH = 2;
        private static final int MOBILE_STROKE_WIDTH = 6;

        public OPGSignatureView(Context context) {
            super(context);
            init();
        }

        public OPGSignatureView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public OPGSignatureView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            setFocusable(true);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            if(isTablet){
                mPaint.setStrokeWidth(TABLET_STROKE_WIDTH);
            }else{
                mPaint.setStrokeWidth(MOBILE_STROKE_WIDTH);
            }

        }

        public void setSignatureColor(int color) {
            mPaint.setColor(color);
        }

        public void setSignatureColor(int a, int r, int g, int b) {
            mPaint.setARGB(a, r, g, b);
        }

        public void clear() {
            if (mCanvas != null) {
                mCanvas.drawPaint(mPaint);
                mCanvas.drawColor(Color.WHITE);
                mPath.reset();
                invalidate();
                enableCanvas(false);
            }

        }

        public void save(View v, String StoredPath) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);

                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, mFileOutStream);
                Intent intent = new Intent();
                intent.putExtra("imagePath", StoredPath);
                setResult(Activity.RESULT_OK, intent);
                finish();
                mFileOutStream.flush();
                mFileOutStream.close();

            } catch (Exception e) {
                Log.v(mContext.getClass().getName(), e.toString());
            }

        }

        public Bitmap getImage() {
            return this.mBitmap;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int bitW = mBitmap != null ? mBitmap.getWidth() : 0;
            int bitH = mBitmap != null ? mBitmap.getWidth() : 0;

            if (bitW >= w && bitH >= h) {
                return;
            }

            if (bitW < w)
                bitW = w;
            if (bitH < h)
                bitH = h;

            Bitmap newBitmap = Bitmap.createBitmap(bitW, bitH,
                    Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);

            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }

            mBitmap = newBitmap;
            mCanvas = newCanvas;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            canvas.drawPath(mPath, mPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            enableCanvas(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDown(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    break;
            }
            invalidate();
            return true;
        }

        private void touchDown(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            curX = x;
            curY = y;
            isDragged = false;
        }

        private void touchMove(float x, float y) {
            float dx = Math.abs(x - curX);
            float dy = Math.abs(y - curY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2);
                curX = x;
                curY = y;
                isDragged = true;
            }
        }

        private void touchUp() {
            if (isDragged) {
                mPath.lineTo(curX, curY);
            } else {
                mPath.lineTo(curX+2, curY+2);
            }
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
