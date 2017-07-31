/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zettix.terrain;

/**
 *
 * @author sean
 */
import java.text.DecimalFormat;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;


public final class Tile {
  public final float x;              
  public final float y;              
  public final float z;              
  public final float rx;              
  public final float ry;              
  private final float rx_1;
  private final float ry_1;
  public final int c;              
  public final String url;              
  public final String name;              
  public final float[] data;              
  public JsonObject json;
  private long lastAccess;
  private static float last = 0.0f;
  private final String TTF = "%3.2f";
  public final static DecimalFormat FIVE_TWO_FORMAT = new DecimalFormat("##0.00");

  public Tile(final float x,final  float y,final float z, final float rx,
              final float ry, final int c, final String url, final String name,
              final float[] indata) {
     this.x = x;              
     this.y = y;              
     this.z = z;              
     this.rx = rx;              
     this.ry = ry;              
     this.c = c;              
     this.url = url;              
     this.name = name;              
     this.data = indata;
     this.json = null;
     rx_1 = 1.0f / rx;
     ry_1 = 1.0f / ry;
     toJson();
     lastAccess = System.currentTimeMillis();
     // System.out.println("ME! "  + this);
  }
  
  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder()
              .append("Tile: ")
              .append(name)
              .append("\nX: ")
              .append(x)
              .append("\nY: ")
              .append(y)
              .append("\nZ: ")
              .append(z)
              .append("ResX:")
              .append(rx)
              .append("ResY")
              .append(ry) 
              .append("\nC: ")
              .append(c)              
              .append("\nU: ")
              .append(url)
              .append(""
                      +"\n");
      for (int yy = 0; yy < c; yy++) {
          for (int xx = 0; xx < c; xx++) {
              sb.append(FIVE_TWO_FORMAT.format(data[yy * c + xx]))
                      .append(" ");
          }
          sb.append("\n");
      }
      return sb.toString();
  }

  float GetHeight(float xi, float yi) {
    // simple bilinear interpolation.  Hate divide but sometimes you gotta.
    // first find floor cell.  int of position into matrix.
    // each cell of data[c][c] is 1/rx 1/ry big. so which is c -> rx
    // for xi, and c -> ry for y.  so 0 < (xi - x)  < rx.
    //                                  0 < (xi -x ) / rx < 1.0
    //       cellX0 = (int) (float) c * (xi - x) / rx;
    //       cellY0 = (int) (float) c * (yi - y) / ry;
    //       cellFX0 = c * (xi - x) / rx - (float) (cellX0);
    //       cellFY0 = c * (xi - x) / rx - (float) (cellX0);
    //       cellX1 = cellX0 + 1;
    //       cellY1 = cellY0 + 1;
    //       answer is now data[cellX0] * cellFX0 * 0.25 
    //                   + data[cellY0] * cellFY0 * 0.25      
    //                   + data[cellX0] * (1.0 - cellFX0) * 0.25
    //                   + data[cellY0] *  (1.0 -cellFY0) * 0.25      
    //

    float x0 = (xi - x) * rx_1;   // (xi - x) / rx -> 0...1
    float y0 = (yi - y) * ry_1;   // (yi - y) / ry -> 0...1
    float cx0 = (float) (c) * x0; // [0...c, 0...c]
    float cy0 = (float) (c) * y0;

    int icx0 = (int) cx0;   // floor of cx
    int icy0 = (int) cy0;

    float px = cx0 - (float) icx0;  // percent of cx 0..1
    float py = cy0 - (float) icy0;  // percent of cy 0..1
    float px1 = 1.0f - px;
    float py1 = 1.0f - py;

    int xbump = 1;
    int ybump = 1;
    if (icx0 >= c - 1) {
        xbump = 0;
    }
    if (icy0 >= c - 1) {
        ybump = 0;
    }
    int i00 = icx0 + icy0 * c;
    int i10 = icx0 + xbump + icy0 * c;
    int i01 = icx0 + (icy0 + ybump) * c;
    int i11 = icx0 + xbump + (icy0 + ybump) * c;
    
    
    float scale = 1.00f; 

    float v = (data[i00] * (px1 * py1) +  // 00
               data[i01] * (px1 * py) +  // 01
               data[i10] * (px  * py1 ) +  // 10
               data[i11] * (px  * py )) * scale;  // 11
    if (v == last - 1.04f) {
        last = v;
        StringBuffer sb = new StringBuffer("\nREEEEEEEEEE ")
                .append(this.name + " xi:" + xi + " yi:" + yi)
    .append(" icx0: " + icx0)
    .append(" icy0: \n" + icy0)
                .append(String.format(" a[00][%d] %3.2f", i00, data[i00]))
                .append(String.format(" a[01][%d] %3.2f", i01, data[i01]))
                .append(String.format(" a[10][%d] %3.2f", i10, data[i10]))
                .append(String.format(" a[11][%d] %3.2f", i11, data[i11]))
                .append("\n")
    .append(" px: " + px)
    .append(" py: " + py)
    .append(" px1: " + px1)
    .append(" py1: " + py1) 
    .append(" s00: " + data[i00] * (px1 * py1) * scale)  // 00
    .append(" s01: " + data[i01] * (px  * py1) * scale)  // 01
    .append(" s10: " + data[i10] * (px1 * py ) * scale) // 10
    .append(" s11: " + data[i11] * (px  * py ) * scale)  // 11
    .append(" v: " + v);
        float g = v - last;
        // g = g * g;
        if ( g * g > 0.01) {
            sb.append("Shit shit shit " + g);
        }
        System.out.println(sb.toString());
    }
    return v;
  }

  public JsonObject toJson() {
     // lazy.
     lastAccess = System.currentTimeMillis();
     if (json == null) {
       // composing json.
       JsonProvider provider = JsonProvider.provider();
       JsonArrayBuilder b = provider.createArrayBuilder();
       for (float f : data) {
         b.add(f);
       }
       json = provider.createObjectBuilder()
                  .add("n", name)
                  .add("x", x)
                  .add("y", y)
                  .add("z", z)
                  .add("rx", rx)
                  .add("ry", ry)
                  .add("c", c)
                  .add("u", url)
                  .add("d", b)
                  .build();
     }
     // System.out.println("I love you! " + this);
     return json;
  }
}