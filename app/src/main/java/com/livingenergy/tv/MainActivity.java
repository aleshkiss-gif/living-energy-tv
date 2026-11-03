package com.livingenergy.tv;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
public class MainActivity extends Activity {
 WebView web;
 @Override public void onCreate(Bundle state){super.onCreate(state);
 getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
 web=new WebView(this);web.setBackgroundColor(0xff000000);
 WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);
 settings.setDomStorageEnabled(true);web.setWebChromeClient(new WebChromeClient());
 setContentView(web);web.loadUrl("file:///android_asset/index.html"); }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}