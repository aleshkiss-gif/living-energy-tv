package com.livingenergy.tv;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.webkit.WebView;
import android.webkit.WebSettings;
public class MainActivity extends Activity {
 WebView web;
 LinearLayout menu;
 @Override public void onCreate(Bundle state){super.onCreate(state);
  getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  FrameLayout root=new FrameLayout(this);web=new WebView(this);web.setBackgroundColor(0xff000000);
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
  root.addView(web);web.setWebViewClient(new android.webkit.WebViewClient(){@Override public void onPageFinished(WebView view,String url){syncPreview();}});web.loadUrl("file:///android_asset/index.html");
  menu=new LinearLayout(this);menu.setOrientation(LinearLayout.VERTICAL);menu.setPadding(20,12,20,12);menu.setBackgroundColor(0xdd08091a);
  TextView title=new TextView(this);title.setText("LIVING ENERGY 4K  •  Настройки");title.setTextColor(-1);title.setTextSize(18);menu.addView(title);
  addPaletteControls();
  addSlider("Скорость", "speed",70,200);addSlider("Высота волн","height",100,250);addSlider("Размер частиц","size",100,250);addSlider("Блеск","shine",100,200);
  addSlider("Расстояние между частицами","spacing",0,100);addSlider("Микроблики","microGlints",100,200);addSlider("Световые волны","lightWaves",100,200);addSlider("Глубина освещения","depthLight",100,200);addSlider("Яркость горизонта","horizonGlow",100,200);addSlider("Интенсивность лучей","rayIntensity",100,200);addSlider("Скорость перелива цветов","raySpeed",100,300);TextView compensationTitle=new TextView(this);compensationTitle.setText("КОМПЕНСАЦИЯ ЗАТЕМНЕНИЯ ANDROID TV (только установленные обои)");compensationTitle.setTextColor(0xff88eeff);compensationTitle.setTextSize(15);menu.addView(compensationTitle);addSlider("Яркость установленных обоев","wallpaperBrightness",100,250);addSlider("Насыщенность установленных обоев","wallpaperSaturation",100,150);addSlider("Гамма установленных обоев","wallpaperGamma",100,180);addQualitySelector();
  Button install=new Button(this);install.setText("Установить как живые обои");install.setOnClickListener(v->{
    try{Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
      intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new android.content.ComponentName(this,CrystalWallpaperService.class));
      startActivity(intent);
    }catch(Exception e){Toast.makeText(this,"Открой настройки обоев Android TV",Toast.LENGTH_LONG).show();}
  });menu.addView(install);
  Button toggle=new Button(this);toggle.setText("Скрыть меню / показать");toggle.setOnClickListener(v->menu.setVisibility(View.GONE));menu.addView(toggle);
  ScrollView scroll=new ScrollView(this);scroll.addView(menu);scroll.setFillViewport(false);
  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);lp.height=(int)(getResources().getDisplayMetrics().heightPixels*.86f);
  root.addView(scroll,lp);
  Button show=new Button(this);show.setText("⚙");show.setOnClickListener(v->menu.setVisibility(View.VISIBLE));
  FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.TOP|android.view.Gravity.RIGHT);
  root.addView(show,sp);setContentView(root);addUpdateControls();UpdateManager.schedule(this);UpdateManager.check(this,false);
 }
 void addUpdateControls(){
  TextView heading=new TextView(this);heading.setText("ОБНОВЛЕНИЯ • версия "+getVersionName());heading.setTextColor(0xff88eeff);menu.addView(heading);
  Switch automatic=new Switch(this);automatic.setText("Автоматически проверять и скачивать обновления");automatic.setTextColor(-1);automatic.setChecked(UpdateManager.prefs(this).getBoolean("automatic",true));automatic.setOnCheckedChangeListener((v,on)->{UpdateManager.prefs(this).edit().putBoolean("automatic",on).apply();UpdateManager.schedule(this);});menu.addView(automatic);
  TextView info=new TextView(this);info.setText("Проверка раз в сутки и при открытии приложения. Установку подтверждаете вы.");info.setTextColor(0xffbbbbcc);menu.addView(info);
  Button check=new Button(this);check.setText("Проверить обновления");check.setOnClickListener(v->UpdateManager.check(this,true));menu.addView(check);
  Button installUpdate=new Button(this);installUpdate.setText("Установить скачанное обновление");installUpdate.setOnClickListener(v->{if(UpdateManager.ready(this))UpdateManager.install(this);else Toast.makeText(this,"Сначала проверьте обновления",Toast.LENGTH_SHORT).show();});menu.addView(installUpdate);
 }
 String getVersionName(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "20";}}
 void syncPreview(){android.content.SharedPreferences p=getSharedPreferences("living",MODE_PRIVATE);String js="window.setWallpaperSettings({palette:"+p.getInt("palette",0)+",colors:"+ColorPalette.json(p)+",speed:"+p.getInt("speed",70)+",height:"+p.getInt("height",100)+",size:"+p.getInt("size",100)+",shine:"+p.getInt("shine",100)+",spacing:"+p.getInt("spacing",0)+",microGlints:"+p.getInt("microGlints",100)+",lightWaves:"+p.getInt("lightWaves",100)+",depthLight:"+p.getInt("depthLight",100)+",quality:"+p.getInt("quality",1)+",horizonGlow:"+p.getInt("horizonGlow",100)+",rayIntensity:"+p.getInt("rayIntensity",100)+",raySpeed:"+p.getInt("raySpeed",100)+"})";web.evaluateJavascript(js,null);}
 void addPaletteControls(){
  final android.content.SharedPreferences prefs=getSharedPreferences("living",MODE_PRIVATE);
  TextView label=new TextView(this);label.setText("Цветовая палитра");label.setTextColor(-1);menu.addView(label);
  Spinner palette=new Spinner(this);palette.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ColorPalette.NAMES));palette.setSelection(Math.max(0,Math.min(6,prefs.getInt("palette",0))));menu.addView(palette);
  LinearLayout custom=new LinearLayout(this);custom.setOrientation(1);menu.addView(custom);
  Switch third=new Switch(this);third.setText("Использовать третий цвет");third.setTextColor(-1);third.setChecked(prefs.getBoolean("thirdColor",true));custom.addView(third);
  final Button[] buttons=new Button[3];int[] defaults={0xff1455ff,0xff00daca,0xff923dff};
  for(int i=0;i<3;i++){final int slot=i;final int fallback=defaults[i];Button button=new Button(this);buttons[i]=button;updateColorButton(button,slot,prefs.getInt("customColor"+slot,fallback));button.setOnClickListener(v->{
   LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(24,12,24,12);final int[] rgb={android.graphics.Color.red(prefs.getInt("customColor"+slot,fallback)),android.graphics.Color.green(prefs.getInt("customColor"+slot,fallback)),android.graphics.Color.blue(prefs.getInt("customColor"+slot,fallback))};
   TextView sample=new TextView(this);sample.setText("Предпросмотр цвета");sample.setPadding(16,20,16,20);box.addView(sample);sample.setBackgroundColor(Color.rgb(rgb[0],rgb[1],rgb[2]));
   String[] names={"Красный","Зелёный","Синий"};for(int ch=0;ch<3;ch++){final int channel=ch;TextView value=new TextView(this);value.setText(names[ch]+": "+rgb[ch]);box.addView(value);SeekBar bar=new SeekBar(this);bar.setMax(255);bar.setProgress(rgb[ch]);box.addView(bar);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){rgb[channel]=n;value.setText(names[channel]+": "+n);sample.setBackgroundColor(Color.rgb(rgb[0],rgb[1],rgb[2]));}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});}
   new android.app.AlertDialog.Builder(this).setTitle("Цвет "+(slot+1)).setView(box).setPositiveButton("Сохранить",(d,w)->{int color=Color.rgb(rgb[0],rgb[1],rgb[2]);prefs.edit().putInt("customColor"+slot,color).apply();updateColorButton(button,slot,color);syncPreview();}).setNegativeButton("Отмена",null).show();
  });custom.addView(button);}
  buttons[2].setVisibility(third.isChecked()?View.VISIBLE:View.GONE);third.setOnCheckedChangeListener((b,on)->{prefs.edit().putBoolean("thirdColor",on).apply();buttons[2].setVisibility(on?View.VISIBLE:View.GONE);syncPreview();});
  custom.setVisibility(palette.getSelectedItemPosition()==6?View.VISIBLE:View.GONE);
  palette.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int position,long id){prefs.edit().putInt("palette",position).apply();custom.setVisibility(position==6?View.VISIBLE:View.GONE);syncPreview();}});
 }
 void updateColorButton(Button b,int slot,int color){b.setText(String.format(java.util.Locale.ROOT,"Цвет %d   #%06X",slot+1,color&0xffffff));b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));b.setTextColor((Color.red(color)*.299+Color.green(color)*.587+Color.blue(color)*.114)>150?Color.BLACK:Color.WHITE);}
 void addQualitySelector(){
  TextView label=new TextView(this);label.setText("Качество изображения");label.setTextColor(-1);menu.addView(label);
  Spinner spinner=new Spinner(this);String[] modes={"Производительность","Баланс","Максимальное качество"};
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,modes);
  spinner.setAdapter(adapter);spinner.setSelection(Math.max(0,Math.min(2,getSharedPreferences("living",MODE_PRIVATE).getInt("quality",1))));
  spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
   public void onNothingSelected(android.widget.AdapterView<?> parent){}
   public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
    getSharedPreferences("living",MODE_PRIVATE).edit().putInt("quality",position).apply();syncPreview();
   }
  });menu.addView(spinner);
 }
 void addSlider(String name,String key,int def,int max){
  LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);TextView label=new TextView(this);label.setText(name);label.setTextColor(-1);line.addView(label,new LinearLayout.LayoutParams(0,-2,1));TextView value=new TextView(this);value.setTextColor(0xff88eeff);value.setTextSize(16);line.addView(value);menu.addView(line);
  SeekBar bar=new SeekBar(this);bar.setMax(max);bar.setProgress(getSharedPreferences("living",MODE_PRIVATE).getInt(key,def));value.setText(bar.getProgress()+"%");
  bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
   public void onProgressChanged(SeekBar s,int progress,boolean fromUser){value.setText(progress+"%");if(fromUser){getSharedPreferences("living",MODE_PRIVATE).edit().putInt(key,Math.max(key.equals("spacing")||key.equals("raySpeed")?0:1,progress)).apply();syncPreview();}}
   public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
  });menu.addView(bar);
 }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}
