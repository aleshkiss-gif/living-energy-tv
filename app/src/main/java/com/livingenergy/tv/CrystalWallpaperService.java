package com.livingenergy.tv;
import android.service.wallpaper.WallpaperService;
import android.os.Handler;
import android.os.HandlerThread;
import android.opengl.GLES20;
import android.view.SurfaceHolder;
import android.util.Log;
import java.nio.*;
import javax.microedition.khronos.egl.*;
public class CrystalWallpaperService extends WallpaperService {
 public Engine onCreateEngine(){return new CrystalEngine();}
 class CrystalEngine extends Engine {
  HandlerThread thread=new HandlerThread("CrystalGL");
  Handler handler;boolean visible,ready;EGL10 egl;EGLDisplay display;EGLSurface surface;EGLContext context;
  int program,position,time,aspect,scale,heightFactor,shine,spacing,microGlints,lightWaves,fade,depthLight,quality;FloatBuffer vertices;long started;
  CrystalEngine(){thread.start();handler=new Handler(thread.getLooper());}
  final Runnable tick=new Runnable(){public void run(){if(!visible)return;try{if(!ready)init();render();}catch(Exception e){Log.e("CrystalGL","render",e);cleanup();}handler.postDelayed(this,33);}};
  public void onVisibilityChanged(boolean v){visible=v;handler.removeCallbacks(tick);if(v)handler.post(tick);}
  public void onSurfaceDestroyed(SurfaceHolder h){visible=false;handler.removeCallbacks(tick);handler.post(this::cleanup);super.onSurfaceDestroyed(h);}
  public void onDestroy(){visible=false;handler.removeCallbacks(tick);handler.post(()->{cleanup();thread.quitSafely();});super.onDestroy();}
  String read()throws Exception{java.io.InputStream in=getAssets().open("index.html");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))>0)out.write(b,0,n);in.close();return out.toString("UTF-8");}
  String shaderSource(String html,String name){String tag="const "+name+"=`";int i=html.indexOf(tag)+tag.length();if(i<tag.length())throw new RuntimeException("Missing shader");return html.substring(i,html.indexOf('`',i));}
  int compile(int kind,String src){int s=GLES20.glCreateShader(kind);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);int[] ok=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);if(ok[0]==0)throw new RuntimeException(GLES20.glGetShaderInfoLog(s));return s;}
  void init()throws Exception{
   egl=(EGL10)EGLContext.getEGL();display=egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);egl.eglInitialize(display,new int[2]);
   int[] attrs={EGL10.EGL_RED_SIZE,8,EGL10.EGL_GREEN_SIZE,8,EGL10.EGL_BLUE_SIZE,8,EGL10.EGL_DEPTH_SIZE,16,0x3040,4,EGL10.EGL_NONE};
   EGLConfig[] configs=new EGLConfig[1];int[] count=new int[1];egl.eglChooseConfig(display,attrs,configs,1,count);
   context=egl.eglCreateContext(display,configs[0],EGL10.EGL_NO_CONTEXT,new int[]{0x3098,2,EGL10.EGL_NONE});
   surface=egl.eglCreateWindowSurface(display,configs[0],getSurfaceHolder(),null);
   if(!egl.eglMakeCurrent(display,surface,surface,context))throw new RuntimeException("EGL");
   String html=read();int v=compile(GLES20.GL_VERTEX_SHADER,shaderSource(html,"vs"));int f=compile(GLES20.GL_FRAGMENT_SHADER,shaderSource(html,"fs"));
   program=GLES20.glCreateProgram();GLES20.glAttachShader(program,v);GLES20.glAttachShader(program,f);GLES20.glLinkProgram(program);
   int[] linked=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linked,0);if(linked[0]==0)throw new RuntimeException(GLES20.glGetProgramInfoLog(program));
   position=GLES20.glGetAttribLocation(program,"a");time=GLES20.glGetUniformLocation(program,"t");aspect=GLES20.glGetUniformLocation(program,"aspect");scale=GLES20.glGetUniformLocation(program,"scale");heightFactor=GLES20.glGetUniformLocation(program,"heightFactor");shine=GLES20.glGetUniformLocation(program,"shine");spacing=GLES20.glGetUniformLocation(program,"spacing");microGlints=GLES20.glGetUniformLocation(program,"microGlints");lightWaves=GLES20.glGetUniformLocation(program,"lightWaves");fade=GLES20.glGetUniformLocation(program,"fade");depthLight=GLES20.glGetUniformLocation(program,"depthLight");quality=GLES20.glGetUniformLocation(program,"quality");
   float[] data=new float[235*145*3];int k=0;long seed=193847;
   for(int y=0;y<145;y++)for(int x=0;x<235;x++){
    seed=(seed*1664525+1013904223)&0xffffffffL;float a=seed/4294967296f;
    seed=(seed*1664525+1013904223)&0xffffffffL;float b=seed/4294967296f;
    seed=(seed*1664525+1013904223)&0xffffffffL;float c=seed/4294967296f;
    data[k++]=(x+.08f+.84f*a)/235f;data[k++]=(y+.08f+.84f*b)/145f;data[k++]=c;
   }
   vertices=ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();vertices.put(data).position(0);
   GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glDepthFunc(GLES20.GL_LEQUAL);GLES20.glClearColor(0,0,0,1);started=System.nanoTime();ready=true;
  }
  void render(){
   int w=getSurfaceHolder().getSurfaceFrame().width(),h=getSurfaceHolder().getSurfaceFrame().height();if(w<1||h<1)return;
   GLES20.glViewport(0,0,w,h);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);GLES20.glUseProgram(program);
   android.content.SharedPreferences prefs=getSharedPreferences("living",MODE_PRIVATE);
   float speed=prefs.getInt("speed",70)/70f,size=prefs.getInt("size",100)/100f;
   GLES20.glUniform1f(time,(System.nanoTime()-started)*.00000000055f*speed);
   GLES20.glUniform1f(aspect,(float)w/h);GLES20.glUniform1f(scale,Math.min(w/1250f,1.5f)*size);GLES20.glUniform1f(heightFactor,prefs.getInt("height",100)/100f);GLES20.glUniform1f(shine,prefs.getInt("shine",100)/100f);GLES20.glUniform1f(spacing,prefs.getInt("spacing",0)/100f);GLES20.glUniform1f(microGlints,prefs.getInt("microGlints",100)/100f);GLES20.glUniform1f(lightWaves,prefs.getInt("lightWaves",100)/100f);GLES20.glUniform1f(fade,Math.min(1f,(System.nanoTime()-started)/2400000000f));GLES20.glUniform1f(depthLight,prefs.getInt("depthLight",100)/100f);int q=prefs.getInt("quality",1);GLES20.glUniform1f(quality,q==0?.65f:q==2?1f:.88f);
   GLES20.glEnableVertexAttribArray(position);vertices.position(0);GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,0,vertices);
   GLES20.glDrawArrays(GLES20.GL_POINTS,0,235*145);egl.eglSwapBuffers(display,surface);
  }
  void cleanup(){ready=false;if(egl==null)return;try{egl.eglMakeCurrent(display,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_CONTEXT);if(surface!=null)egl.eglDestroySurface(display,surface);if(context!=null)egl.eglDestroyContext(display,context);egl.eglTerminate(display);}catch(Exception e){}egl=null;}
 }
}