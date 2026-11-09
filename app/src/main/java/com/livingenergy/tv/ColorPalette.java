package com.livingenergy.tv;
import android.content.SharedPreferences;
public final class ColorPalette {
 public static final String[] NAMES={"Классический синий / фиолетовый","Океан","Северное сияние","Закат","Золото","Радуга","Своя палитра"};
 private static final int[][] COLORS={{0xff0a21f2,0xff8509d1},{0xff1455ff,0xff00daca,0xff923dff},{0xff21e67a,0xff16cddd,0xffed50b1},{0xffff861e,0xffff477d,0xff962edc},{0xffff9a18,0xffffd453,0xffffefc7},{0xffff4141,0xffffab32,0xffffe34c,0xff34db83,0xff27c8ff,0xff6555ff,0xffd94dff}};
 public static int[] colors(SharedPreferences p){int index=Math.max(0,Math.min(6,p.getInt("palette",0)));if(index<6)return COLORS[index];int n=p.getBoolean("thirdColor",true)?3:2;int[] c=new int[n];for(int i=0;i<n;i++)c[i]=p.getInt("customColor"+i,COLORS[1][i]);return c;}
 public static String json(SharedPreferences p){int[] c=colors(p);StringBuilder b=new StringBuilder("[");for(int i=0;i<c.length;i++){if(i>0)b.append(',');b.append(c[i]&0xffffff);}return b.append(']').toString();}
}
