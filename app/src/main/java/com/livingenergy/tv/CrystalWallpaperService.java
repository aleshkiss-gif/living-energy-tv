package com.livingenergy.tv;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import java.util.Random;
public class CrystalWallpaperService extends WallpaperService {
 @Override public Engine onCreateEngine(){return new CrystalEngine();}
 class CrystalEngine extends Engine {
  final Handler handler=new Handler(Looper.getMainLooper()); final Paint paint=new Paint(3);
  final Random rng=new Random(717); final float[] seeds=new float[14000];
  boolean visible=false; long start=System.currentTimeMillis();
  CrystalEngine(){for(int i=0;i<seeds.length;i++)seeds[i]=rng.nextFloat();}
  final Runnable drawTask=new Runnable(){public void run(){drawFrame();}};
  @Override public void onVisibilityChanged(boolean v){visible=v;handler.removeCallbacks(drawTask);if(v)handler.post(drawTask);}
  @Override public void onSurfaceDestroyed(SurfaceHolder h){visible=false;handler.removeCallbacks(drawTask);super.onSurfaceDestroyed(h);}
  void drawFrame(){
   if(!visible)return;Canvas c=null;
   try{c=getSurfaceHolder().lockCanvas();if(c!=null)render(c);}
   catch(Exception ignored){}finally{if(c!=null)getSurfaceHolder().unlockCanvasAndPost(c);}
   handler.postDelayed(drawTask,33);
  }
  void render(Canvas c){
   int w=c.getWidth(),h=c.getHeight();c.drawColor(0xff000000);
   SharedPreferences p=getSharedPreferences("living",MODE_PRIVATE);
   float speed=p.getInt("speed",70)/100f,amp=p.getInt("height",100)/100f;
   float size=p.getInt("size",100)/100f,shine=p.getInt("shine",100)/100f;
   float t=(System.currentTimeMillis()-start)*.0003f*speed;
   int cols=175,rows=80;
   for(int j=rows-1;j>=0;j--){
    float depth=(float)j/(rows-1),persp=(float)Math.pow(depth,1.8);
    float yy=h*(.36f+.64f*persp);
    float radius=(.65f+4.7f*persp)*size*Math.max(.6f,w/1920f);
    for(int i=0;i<cols;i++){
     int id=j*cols+i;float rnd=seeds[id];
     float x=(i+.18f+.65f*rnd)/cols*w;
     float z=1f+depth*90f;
     float world=x/w*28f;
     float wave=(float)(Math.sin(world*.55+z*.15-t*.8)*.43+
       Math.sin(-world*.36+z*.23+t*.61)*.37+
       Math.sin(world*.19-z*.38-t*.48)*.3+
       Math.pow(Math.max(0,Math.sin(world*.24+z*.16-t*.28)),4)*.9);
     float y=yy-wave*amp*(5+36*persp);
     boolean purple=Math.sin(world*.65+z*.33-t*.3)+Math.sin(z*.23-world*.41)>0;
     float twinkle=(float)Math.pow(Math.max(0,Math.sin(t*2.8+rnd*80+z*.3)),12);
     int base=purple?0xff6c12bd:0xff083ac5;
     paint.setColor(base);paint.setStyle(Paint.Style.FILL);
     float angle=rnd*6.28f;int facets=5+(int)(rnd*3);
     Path path=new Path();
     for(int v=0;v<facets;v++){
       float a=angle+v*6.28318f/facets;
       float rr=radius*(.75f+.25f*seeds[(id+v*19)%seeds.length]);
       float vx=x+(float)Math.cos(a)*rr,vy=y+(float)Math.sin(a)*rr;
       if(v==0)path.moveTo(vx,vy);else path.lineTo(vx,vy);
     }path.close();c.drawPath(path,paint);
     paint.setColor(purple?0xffbd4aff:0xff2a9aff);
     paint.setAlpha((int)Math.min(255,45+170*twinkle*shine));
     c.drawCircle(x-radius*.22f,y-radius*.24f,Math.max(.4f,radius*.22f),paint);
    }
   }
  }
 }
}