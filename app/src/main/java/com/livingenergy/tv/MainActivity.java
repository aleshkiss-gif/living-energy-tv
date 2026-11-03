package com.livingenergy.tv;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.webkit.WebView;
import android.webkit.WebSettings;
public class MainActivity extends Activity {
 WebView web;
 LinearLayout menu;
 @Override public void onCreate(Bundle state){super.onCreate(state);
  getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  FrameLayout root=new FrameLayout(this);web=new WebView(this);web.setBackgroundColor(0xff000000);
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
  root.addView(web);web.loadUrl("file:///android_asset/index.html");
  menu=new LinearLayout(this);menu.setOrientation(LinearLayout.VERTICAL);menu.setPadding(20,12,20,12);menu.setBackgroundColor(0xdd08091a);
  TextView title=new TextView(this);title.setText("LIVING ENERGY 4K  •  Настройки");title.setTextColor(-1);title.setTextSize(18);menu.addView(title);
  addSlider("Скорость", "speed",70,200);addSlider("Высота волн","height",100,250);addSlider("Размер частиц","size",100,250);addSlider("Блеск","shine",100,200);
  Button install=new Button(this);install.setText("Установить как живые обои");install.setOnClickListener(v->{
    try{Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
      intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new android.content.ComponentName(this,CrystalWallpaperService.class));
      startActivity(intent);
    }catch(Exception e){Toast.makeText(this,"Открой настройки обоев Android TV",Toast.LENGTH_LONG).show();}
  });menu.addView(install);
  Button toggle=new Button(this);toggle.setText("Скрыть меню / показать");toggle.setOnClickListener(v->menu.setVisibility(View.GONE));menu.addView(toggle);
  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);
  root.addView(menu,lp);
  Button show=new Button(this);show.setText("⚙");show.setOnClickListener(v->menu.setVisibility(View.VISIBLE));
  FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.TOP|android.view.Gravity.RIGHT);
  root.addView(show,sp);setContentView(root);
 }
 void addSlider(String name,String key,int def,int max){
  TextView label=new TextView(this);label.setText(name);label.setTextColor(-1);menu.addView(label);
  SeekBar bar=new SeekBar(this);bar.setMax(max);bar.setProgress(getSharedPreferences("living",MODE_PRIVATE).getInt(key,def));
  bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
   public void onProgressChanged(SeekBar s,int progress,boolean fromUser){if(fromUser)getSharedPreferences("living",MODE_PRIVATE).edit().putInt(key,Math.max(1,progress)).apply();}
   public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
  });menu.addView(bar);
 }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}