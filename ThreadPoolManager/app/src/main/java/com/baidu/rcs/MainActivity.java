package com.baidu.rcs;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.io.File;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";
    private PausableThreadPoolExecutor executorService;
    private ThreadManager threadManager;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);
        Log.e(TAG, "系统核心进程数：" + Runtime.getRuntime().availableProcessors());

    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyStoragePermissions(this);
    }

    public void start(View v) {
        threadManager = ThreadManager.getInstance(ThreadManager.TYPE_PAUSE, Thread.NORM_PRIORITY);
        executorService = (PausableThreadPoolExecutor) threadManager.getPool();
//        for (int i = 0; i < 7; i++) {
//            threadManager.addTask(new SimpleTask());
//
//        }
//        threadManager.executeTasks();
        final String imagePath = Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "P50926-185955.jpg";
        for (int i = 0; i < 20; i++) {
//            threadManager.excuteTask(new SimpleTask());
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    System.out.println(params[0]);
                    Bitmap bitmap = decodeBitmapFromFilePath(params[0], 1200, 1200);
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    image.setImageBitmap(bitmap);
                }
            }.executeOnExecutor(executorService, imagePath);
        }

//        executorService.pause();
        Log.e(TAG, "线程开始");
    }

    public void pause(View v) {
        executorService.pause();
        Log.e(TAG, "线程暂停");
    }

    public void goOn(View v) {
        executorService.resume();
        Log.e(TAG, "线程继续");
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap decodeBitmapFromFilePath(String imagePath,
                                                  int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
