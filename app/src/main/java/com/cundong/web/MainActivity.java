package com.cundong.web;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton mOpenLinkBtn;

    private WebView mWebview;

    /**
     * 文件保存路径
     */
    private static final String DODO_FOLDER_PATH = (Environment.getExternalStorageDirectory() + "/dodo-clip");

    /**
     * 保存webview的可见区域尺寸
     */
    private Rect mWebViewRect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenLinkBtn = (FloatingActionButton) findViewById(R.id.open_link_btn);
        mOpenLinkBtn.setOnClickListener(this);
        mWebview = (WebView) findViewById(R.id.h5_web);

        WebSettings webSettings = mWebview.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient());
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            clip();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_link_btn: {
                OpenInputUrlDialogBox();
            }
            break;
        }
    }

    /**
     * 打开输入连接对话框
     */
    private void OpenInputUrlDialogBox() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_input, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Open url");
        alert.setView(promptView);

        final EditText input = (EditText) promptView
                .findViewById(R.id.dialog_input);

        input.requestFocus();
        input.setHint("Enter web url");
        input.setTextColor(Color.BLACK);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String webUrl = input.getText().toString();
                mWebview.loadUrl(webUrl);
            }
        });


        alert.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }
        );
        AlertDialog alert1 = alert.create();
        alert1.show();
    }

    /**
     * 保存bitmap到文件
     *
     * @param bitmap
     * @param path
     * @return
     */
    private final String saveToFile(Bitmap bitmap, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        String str = file.getAbsolutePath() + "/" + formatDate(System.currentTimeMillis(), "yyMMddHHmmssSSSS") + ".jpg";
        try {
            OutputStream fileOutputStream = new FileOutputStream(str);
            if (fileOutputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                return str;
            }
            return null;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 格式化时间格式
     *
     * @param date
     * @param str
     * @return
     */
    private String formatDate(long date, String str) {
        return new SimpleDateFormat(str).format(new Date(date));
    }

    /**
     * 保存图片到文件
     */
    private void clip() {

        //重新计算webView
        calcLayout(true);

        int measuredWidth = mWebview.getMeasuredWidth();
        int measuredHeight = mWebview.getMeasuredHeight();

        if (measuredWidth > 0 && measuredHeight > 0) {

            //将WebView影像绘制在Canvas上
            Paint paint = new Paint();
            Bitmap createBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(createBitmap);
            canvas.drawBitmap(createBitmap, 0.0f, (float) createBitmap.getHeight(), paint);
            mWebview.draw(canvas);

            AsyncTask<Bitmap, Void, String> saveTask = new AsyncTask<Bitmap, Void, String>() {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                @Override
                protected void onPostExecute(String resultFile) {
                    super.onPostExecute(resultFile);
                    calcLayout(false);
                    if (!TextUtils.isEmpty(resultFile)) {
                        showImage(resultFile);
                    }
                    progressDialog.setMessage("处理完成。。。");
                    progressDialog.dismiss();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("处理中。。。");
                    progressDialog.show();
                }

                @Override
                protected String doInBackground(Bitmap... params) {

                    Bitmap bitmap = params[0];
                    String resultFile = null;
                    if (bitmap != null)
                        resultFile = saveToFile(bitmap, DODO_FOLDER_PATH);
                    bitmap.recycle();

                    return resultFile;
                }
            };

            saveTask.execute(createBitmap);
        }
    }

    /**
     * 计算页面尺寸
     *
     * @param enableDrawingCache
     */
    private void calcLayout(boolean enableDrawingCache) {
        if (mWebViewRect == null || mWebViewRect.isEmpty()) {
            mWebViewRect = new Rect(mWebview.getLeft(), mWebview.getTop(), mWebview.getRight(), mWebview.getBottom());
        }

        if (enableDrawingCache) {
            mWebview.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            //开启cache
            mWebview.setDrawingCacheEnabled(true);
//            mWebview.buildDrawingCache();
        } else {
            mWebview.setDrawingCacheEnabled(false);
        }

        mWebview.layout(mWebViewRect.left, mWebViewRect.top, mWebViewRect.right, enableDrawingCache ? mWebview.getMeasuredHeight() : mWebViewRect.bottom);
    }

    private void showImage(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(str)), "image/*");
        startActivity(intent);
    }
}