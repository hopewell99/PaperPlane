package com.marktony.zhihudaily.detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.marktony.zhihudaily.R;
import com.marktony.zhihudaily.app.App;
import com.marktony.zhihudaily.innerbrowser.InnerBrowserActivity;
import com.marktony.zhihudaily.util.Api;
import com.marktony.zhihudaily.util.Theme;

/**
 * 2016.6.15 黎赵太郎
 * 果壳文章阅读
 */
public class GuokrDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ImageView ivHeadline;
    private WebView wbMain;
    private CollapsingToolbarLayout toolbarLayout;

    private AlertDialog dialog;

    private int id;
    private String headlineUrl;
    private String title;

    private SharedPreferences sp;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(App.getThemeResources());
        setContentView(R.layout.universal_read_layout);

        initViews();

        sp = getSharedPreferences("user_settings",MODE_PRIVATE);

        queue = Volley.newRequestQueue(getApplicationContext());

        dialog = new AlertDialog.Builder(GuokrDetailActivity.this).create();
        dialog.setView(getLayoutInflater().inflate(R.layout.loading_layout,null));

        id = getIntent().getIntExtra("id", 0);
        headlineUrl = getIntent().getStringExtra("headlineImageUrl");
        title = getIntent().getStringExtra("title");

        setCollapsingToolbarLayoutTitle(title);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
                    String shareText = title + " " +  Api.GUOKR_ARTICLE_LINK_V1 + id + getString(R.string.share_extra);
                    shareIntent.putExtra(Intent.EXTRA_TEXT,shareText);
                    startActivity(Intent.createChooser(shareIntent,getString(R.string.share_to)));
                } catch (android.content.ActivityNotFoundException ex){
                    Snackbar.make(fab,R.string.loaded_failed,Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        if (headlineUrl != null){
            Glide.with(GuokrDetailActivity.this)
                    .load(headlineUrl)
                    .asBitmap()
                    .centerCrop()
                    .into(ivHeadline);
        } else {
            ivHeadline.setImageResource(R.drawable.no_img);
        }

        // 设置是否加载图片，true不加载，false加载图片sp.getBoolean("no_picture_mode",false)
        wbMain.getSettings().setBlockNetworkImage(sp.getBoolean("no_picture_mode",false));

        //能够和js交互
        wbMain.getSettings().setJavaScriptEnabled(true);
        //缩放,设置为不能缩放可以防止页面上出现放大和缩小的图标
        wbMain.getSettings().setBuiltInZoomControls(false);
        //缓存
        wbMain.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //开启DOM storage API功能
        wbMain.getSettings().setDomStorageEnabled(true);
        //开启application Cache功能
        wbMain.getSettings().setAppCacheEnabled(false);

        if (sp.getBoolean("in_app_browser",true)){
            //不调用第三方浏览器即可进行页面反应
            wbMain.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    startActivity(new Intent(GuokrDetailActivity.this, InnerBrowserActivity.class).putExtra("url", url));
                    return true;
                }

            });

            // 设置在本WebView内可以通过按下返回上一个html页面
            wbMain.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN){
                        if (keyCode == KeyEvent.KEYCODE_BACK && wbMain.canGoBack()){
                            wbMain.goBack();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        StringRequest request = new StringRequest(Request.Method.GET, Api.GUOKR_ARTICLE_LINK_V2 + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (App.getThemeValue() == Theme.NIGHT_THEME){
                    s = s.replace("<div class=\"article \" id=\"contentMain\">", "<div class=\"article \" id=\"contentMain\" style=\"background-color:#212b30; color:#878787\">");
                    s = s.replace(" <div class=\"content clearfix\" id=\"articleContent\">", " <div class=\"content clearfix\" id=\"articleContent\"> style=\"background-color:#212b30\"");
                }
                wbMain.loadDataWithBaseURL("x-data://base",s,"text/html","utf-8",null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Snackbar.make(fab,R.string.loaded_failed,Snackbar.LENGTH_SHORT).show();
                if (dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        });

        /*JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.GUOKR_ARTICLE_BASE_URL + "?pick_id=" + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("ok").equals("true")){

                        {

                            content = jsonObject.getJSONArray("result").getJSONObject(0).getString("content");

                            String parseByTheme = null;
                            if (App.getThemeValue() == Theme.DAY_THEME){
                                parseByTheme = "<div class=\"container \">\n <div class=\"content clearfix\" id=\"articleContent\">";
                            } else {
                                parseByTheme = "<div class=\"container \" style=\"background-color:#212b30\">\n <div class=\"content clearfix\" id=\"articleContent\" style=\"background-color:#212b30\">";
                            }

                            String css = "\n<link rel=\"stylesheet\" href=\"http://static.guokr.com/apps/handpick/styles/5a4658ba.articleInline.css\" />\n";

                            String html = "<!DOCTYPE html>\n"
                                    + "<html class=\"no-js screen-scroll\">\n"
                                    + "<head>\n"
                                    + "\t<meta charset=\"utf-8\" />"
                                    + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1,user-scalable=no\" />"
                                    + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge,chrome=1\" />\n" +
                                    "        <meta name=\"format-detection\" content=\"telephone=no\" />"
                                    + css
                                    + "\n</head>"
                                    + "<body>"
                                    + parseByTheme
                                    + content
                                    + "</div>"
                                    + "</div>"
                                    + " <script>\n" +
                                    "            var ukey = null;\n" +
                                    "        </script>\n" +
                                    "        <script src=\"http://static.guokr.com/apps/handpick/scripts/9c661fc7.base.js\"></script>\n" +
                                    "<script src=\"http://static.guokr.com/apps/handpick/scripts/96c3e257.articleInline.js\"></script>"
                                    + "</body></html>";

                            // wbMain.loadDataWithBaseURL("x-data://base",html,"text/html","utf-8",null);

                            wbMain.loadUrl("http://jingxuan.guokr.com/pick/v2/20467/");
                        }
                    }

                    if (dialog.isShowing()){
                        dialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Snackbar.make(fab,R.string.loaded_failed,Snackbar.LENGTH_SHORT).show();

                if (dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        });*/

        queue.add(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_read,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        if (item.getItemId() == R.id.action_open_in_browser){
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Api.GUOKR_ARTICLE_LINK_V1 + id)));
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivHeadline = (ImageView) findViewById(R.id.image_view);
        wbMain = (WebView) findViewById(R.id.web_view);

    }

    // to change the title's font size of toolbar layout
    private void setCollapsingToolbarLayoutTitle(String title) {
        toolbarLayout.setTitle(title);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBarPlus1);
    }

}