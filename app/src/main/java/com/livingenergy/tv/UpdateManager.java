package com.livingenergy.tv;

import android.app.*;
import android.app.job.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateManager {
 private static final String BASE="https://github.com/aleshkiss-gif/living-energy-tv/releases/";
 private static final AtomicBoolean BUSY=new AtomicBoolean();
 static final int JOB=2020;
 public static SharedPreferences prefs(Context c){return c.getSharedPreferences("updates",0);}
 public static File apk(Context c){return new File(c.getFilesDir(),"update.apk");}
 public static void schedule(Context c){
  JobScheduler scheduler=c.getSystemService(JobScheduler.class);
  if(!prefs(c).getBoolean("automatic",true)){scheduler.cancel(JOB);return;}
  scheduler.schedule(new JobInfo.Builder(JOB,new ComponentName(c,UpdateJobService.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(24*60*60*1000L).setPersisted(true).build());
 }
 public static void check(Activity a,boolean manual){
  if(!manual&&!prefs(a).getBoolean("automatic",true))return;
  if(!manual&&System.currentTimeMillis()-prefs(a).getLong("lastCheck",0)<12*60*60*1000L){if(ready(a))offer(a);return;}
  if(manual)Toast.makeText(a,"Проверка обновлений…",Toast.LENGTH_SHORT).show();
  new Thread(()->{String result=fetch(a.getApplicationContext());a.runOnUiThread(()->{if(a.isFinishing()||a.isDestroyed())return;if(ready(a))offer(a);else if(manual)new AlertDialog.Builder(a).setTitle("Обновления").setMessage(result).setPositiveButton("OK",null).show();});},"UpdateCheck").start();
 }
 public static boolean ready(Context c){try{return apk(c).isFile()&&prefs(c).getLong("version",0)>c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode;}catch(Exception e){return false;}}
 private static HttpURLConnection connection(String address)throws IOException{
  URL url=new URL(address);
  for(int i=0;i<6;i++){
   String host=url.getHost();if(!"https".equals(url.getProtocol())||!(host.equals("github.com")||host.equals("release-assets.githubusercontent.com")||host.equals("objects.githubusercontent.com")))throw new IOException("Недопустимый адрес обновления");
   HttpURLConnection con=(HttpURLConnection)url.openConnection();con.setInstanceFollowRedirects(false);con.setConnectTimeout(15000);con.setReadTimeout(30000);con.setRequestProperty("User-Agent","LivingEnergyTV-Updater");int status=con.getResponseCode();
   if(status>=300&&status<400){String next=con.getHeaderField("Location");con.disconnect();if(next==null)throw new IOException("Ошибка перенаправления");url=new URL(url,next);continue;}
   if(status==404){con.disconnect();throw new FileNotFoundException();}if(status!=200){con.disconnect();throw new IOException("Сервер обновлений: HTTP "+status);}return con;
  }throw new IOException("Слишком много перенаправлений");
 }
 private static byte[] readManifest()throws Exception{
  HttpURLConnection con=connection(BASE+"latest/download/update.json");try(InputStream in=con.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>65536)throw new IOException("Некорректный манифест");}return out.toByteArray();
  }finally{con.disconnect();}
 }
 static synchronized void verify(Context c,File file,long expected)throws Exception{
  PackageManager pm=c.getPackageManager();PackageInfo next=pm.getPackageArchiveInfo(file.getAbsolutePath(),PackageManager.GET_SIGNATURES);PackageInfo current=pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNATURES);
  if(next==null||!c.getPackageName().equals(next.packageName)||next.versionCode!=expected||next.versionCode<=current.versionCode)throw new IOException("Неверная версия APK");
  if(next.signatures==null||current.signatures==null||!Arrays.equals(next.signatures,current.signatures))throw new IOException("Подпись APK отличается. Установите первоначальную версию с постоянной подписью.");
 }
 static String fetch(Context c){
  if(!BUSY.compareAndSet(false,true))return "Проверка уже выполняется";
  File part=new File(c.getFilesDir(),"update.part");
  try{
   JSONObject info=new JSONObject(new String(readManifest(),"UTF-8"));long version=info.getLong("versionCode");int installed=c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode;
   if(version<=installed){prefs(c).edit().putLong("lastCheck",System.currentTimeMillis()).apply();if(apk(c).exists())apk(c).delete();return "Установлена последняя версия";}
   if(ready(c)&&prefs(c).getLong("version",0)==version){verify(c,apk(c),version);return "Обновление готово";}
   String address=info.getString("apkUrl"),hash=info.getString("sha256");long expectedSize=info.getLong("size");
   if(!address.startsWith(BASE+"download/")||!hash.matches("[a-fA-F0-9]{64}")||expectedSize<1||expectedSize>100*1024*1024)throw new IOException("Некорректные данные обновления");
   if(info.optInt("minSdk",26)>Build.VERSION.SDK_INT)return "Для обновления нужна более новая версия Android";
   MessageDigest digest=MessageDigest.getInstance("SHA-256");HttpURLConnection con=connection(address);long total=0;
   try(InputStream in=con.getInputStream();OutputStream out=new FileOutputStream(part)){
    byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted())throw new IOException("Загрузка отменена");total+=n;if(total>expectedSize)throw new IOException("Неверный размер APK");out.write(b,0,n);digest.update(b,0,n);}
   }finally{con.disconnect();}
   StringBuilder actual=new StringBuilder();for(byte b:digest.digest())actual.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
   if(total!=expectedSize||!hash.equalsIgnoreCase(actual.toString()))throw new IOException("Файл обновления повреждён");verify(c,part,version);
   if(!part.renameTo(apk(c)))throw new IOException("Не удалось сохранить APK");
   prefs(c).edit().putLong("version",version).putString("versionName",info.optString("versionName",String.valueOf(version))).putLong("lastCheck",System.currentTimeMillis()).apply();notifyReady(c);return "Обновление загружено";
  }catch(FileNotFoundException e){return "Новых опубликованных обновлений пока нет";}catch(Exception e){return "Не удалось обновить: "+(e.getMessage()==null?"проверьте интернет":e.getMessage());}finally{part.delete();BUSY.set(false);}
 }
 private static void notifyReady(Context c){
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  NotificationManager n=c.getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel("updates","Обновления Living Energy",NotificationManager.IMPORTANCE_DEFAULT));
  PendingIntent open=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  try{n.notify(JOB,new Notification.Builder(c,"updates").setSmallIcon(android.R.drawable.stat_sys_download_done).setContentTitle("Living Energy: обновление готово").setContentText("Откройте приложение, чтобы установить").setContentIntent(open).setAutoCancel(true).build());}catch(SecurityException ignored){}
 }
 private static void offer(Activity a){new AlertDialog.Builder(a).setTitle("Доступна версия "+prefs(a).getString("versionName","")).setMessage("Обновление скачано и проверено. Установить сейчас?").setPositiveButton("Установить",(d,w)->install(a)).setNegativeButton("Позже",null).show();}
 public static void install(Activity a){
  if(!ready(a))return;
  if(!a.getPackageManager().canRequestPackageInstalls()){
   new AlertDialog.Builder(a).setTitle("Разрешение на обновление").setMessage("Разрешите Living Energy устанавливать обновления. После возврата нажмите «Установить обновление».").setPositiveButton("Открыть настройки",(d,w)->{try{a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+a.getPackageName())));}catch(Exception e){Toast.makeText(a,"Откройте разрешение установки из неизвестных источников в настройках Android",Toast.LENGTH_LONG).show();}}).setNegativeButton("Позже",null).show();return;
  }
  try{verify(a,apk(a),prefs(a).getLong("version",0));Uri uri=Uri.parse("content://"+a.getPackageName()+".updates/update.apk");Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);a.startActivity(i);}catch(Exception e){new AlertDialog.Builder(a).setMessage("Не удалось открыть установку: "+e.getMessage()).setPositiveButton("OK",null).show();}
 }
}
