/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zettix.terrain;

// TerrainManager
//   Serve terrain tiles based on location.
//
// Resources: textures of form image_x_y.jpg for tile x, y
//            float data of form data_x_y.dat for tile x, y
// Output: for given geometric point P, return some data with geometry and texture from resources.
//          The texture is for the client.
// Load(): load resources.
// List<names> getTilesForPoint(float x, float y);
//  be lazy.  Do not load unless asked.

import com.zettix.rocketsocket.RocketConstants;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import org.apache.log4j.*;

public final class TerrainManager {

   private static final String RESOURCE_PATH = "/terrain";
   private static final String MANIFEST = "files";
   private static final Pattern DATA_RE = Pattern.compile(
       "data_(\\d+)_(\\d+).dat");
   private final Logger LOG = Logger.getLogger(TerrainManager.class);
   private final Map<String, Tile> tiles = new HashMap<>();
   private final Map<String, String> datapaths = new HashMap<>();
   private final Queue<String> tileQueue = new LinkedList<>();
   private final float x = 0.0f;
   private final float y = 0.0f;
   private final float z = 0.0f;
   private final float xDim;
   private final float yDim;
   private int xTileCount = 0;
   private int yTileCount = 0;
   private float xTileWidth = 0.0f;
   private float yTileWidth = 0.0f;
   private float xTilesPerDim = 0.0f;
   private float yTilesPerDim = 0.0f;
   private DataBaseHandler dataBaseHandler;
   private final int maxTiles = 256;  // tune this.
   
   public TerrainManager() {
       this(100.0f, 100.0f);
   }
   
   public <E extends Number> TerrainManager(E x, E y) {
       this.xDim = Float.parseFloat(x.toString());
       this.yDim = Float.parseFloat(y.toString());
       SoftLoad();
   }

   public String GetShortName(int ax, int ay) {
     //System.out.println("Getting shortie for " + ax + " and " + ay);
     if (ax < 0 || ay < 0 || ax > xTileCount || ay > yTileCount) {
         // System.out.println("I don't think so! " + ax + " limit " + xTileCount + " , " + ay + " limit " + yTileCount);
         return null;
     }
     return String.format("%d_%d", ax, ay);
   }

   /** Get information about tiles without loading any.
    *
    *  Given a directory, the RESOURCE_PATH, files matching *.dat
    *  are parsed with DATA_RE extracting x and y values from
    *  files like data_33_2.dat
    *  Maximum x and y define the tile space, which must be rectangular.
    *  individual data files can vary their rectangular resolution
    *  independently, and are not loaded until needed.
   */
   private void SoftLoad() {
    // TODO(sean): add cache expiry.  The loaded tiles are a cache.
    /* 
    Path rpath =  Paths.get(RESOURCE_PATH);
    DirectoryStream<Path> stream = null;
    // System.out.println("Looking in " + rpath);
    try {
      stream = Files.newDirectoryStream(rpath, "*.dat");
    } catch (IOException | DirectoryIteratorException e) {
      System.out.println("Directory Exception! " + e.getCause());
      return;
    }
    */
    
    dataBaseHandler = new DataBaseHandler(
            RocketConstants.getTerrainDemPath(
                    RocketConstants.getDefaultTerrainKey()));
    dataBaseHandler.Connect();
    dataBaseHandler.getManifest();
    int dx = dataBaseHandler.getX();
    int dy = dataBaseHandler.getY();
    System.out.println("Database Loaded with X: " + dx + " and Y: " + dy );
    System.out.println("Loading the terrain manager");
    /* 
    String manifest = RESOURCE_PATH + "/" + MANIFEST;
    InputStream stream = TerrainManager.class.getClassLoader().getResourceAsStream(manifest);
    if (stream == null) {
        LOG.error("No resource found: " + manifest);
    }
    System.out.println("Yay, Terrain Manifiest found! " + manifest);
    BufferedInputStream bb = new BufferedInputStream(stream);
    Scanner ss = new Scanner(bb);
    while (ss.hasNext()) {    
       String filename = ss.next();
       //System.out.println("hmmm " + filename);
       Matcher m = DATA_RE.matcher(filename);
       if (m.matches() == true)  {
         //  System.out.println("Matched! " + filename);
         int px = Integer.parseInt(m.group(1));
         int py = Integer.parseInt(m.group(2));
         String shortname = String.format("%d_%d", px, py); // *** Cannot use GetShortName, limits not set.
         //System.out.println("Oh, yeah: " + px + " at " + py + " was:" + shortname);
         if (shortname == null) continue;
         tiles.put(shortname, null);  // lazy.
         // System.out.println("D:" + filename +  " x:" + px + " y:" + py);
         //System.out.println("D: " + shortname);
         if (px > xTileCount) {
           xTileCount = px;
         }
         if (py > yTileCount) {
           yTileCount = py;
         }
         datapaths.put(shortname, filename);
        } else {
           System.out.println("Didn't match yo' " + filename);
       }
    }
    // big fat assumption that the data started at 0.
    xTileCount++;
    yTileCount++;
    */
    xTileCount = dx;
    yTileCount = dy;
    xTileWidth = (float) xDim / (float) (xTileCount); // start w/ 0
    yTileWidth = (float) yDim / (float) (yTileCount); // start w/ 0
    xTilesPerDim =  (xTileCount) / (xDim + x);
    yTilesPerDim =  (yTileCount) / (yDim + y);
    System.out.println(GetStatus());
  }
   
  private void FreeTiles(int numToFree) {
      int counter = numToFree;
      do {
          String key = tileQueue.poll();
          if (null == key) {
              break;
          }
          datapaths.remove(key);
          tiles.remove(key);
          counter--;
      } while (counter > 0);
      System.out.println("Freed " + numToFree + " Tiles from TileServer");
  }
   
  protected Tile FullLoad(String tilename) {
    boolean dbLoad = true;

    int numTiles = datapaths.size();
    if (numTiles > maxTiles) {
        FreeTiles(25);
    }
    System.out.println("Full load of " + tilename + " For a toal of tiles: " + numTiles);
    String path;
    if (datapaths.containsKey(tilename)) {
      path = datapaths.get(tilename);
    } else {
      path = "data_" + tilename + ".dat";
      datapaths.put(tilename, path);
    }
    String fullpath = RESOURCE_PATH + "/" + path;
    Matcher m = DATA_RE.matcher(path);
    if (m.matches() == true)  {
      int px = Integer.parseInt(m.group(1));
      int py = Integer.parseInt(m.group(2));
      float xx = (float) (px) * xTileWidth + x;
      float yy = (float) (py) * yTileWidth + y;
      //  public Tile(float x, float y, float z, float rx, float ry, int c,
      //        String url, String name, float[] indata)
      //String url = String.format("/BoxMove/images/image_%d_%d.jpg", px, py);
      // With the database endpoint:
      //     localhost:8080/BoxMove/ImageServelet?image=image_0_9.jpg
      String url = String.format("/BoxMove/ImageServelet?image=image_%d_%d.jpg", px, py);
      try {
        InputStream stream;
        if (dbLoad) {
            stream = new ByteArrayInputStream(dataBaseHandler.getBlob("terrain", path));
        } else {
            stream = TerrainManager.class.getClassLoader().getResourceAsStream(fullpath);
        }
        DataInputStream f = new DataInputStream(stream);
        int count = f.readInt();
        int unusedYcount = f.readInt();
        // System.out.println("Loading tile: " + tilename + " c: " + count);
        float[] d = new float[count * count];
        for (int j = 0; j < count * count; j++) {
            // Important change.  Database is now int16s.
            d[j] = ((float) f.readShort()) * 0.0005f;
            // d[j] = (float) f.readFloat() * 0.9f;  // scaled down?
        }
        /* System.out.println("  xx:" + xx +
                           "\n  yy:" + yy +
                           "\n  z:" + z +
                           "\n  xTileWidth:" + xTileWidth +
                           "\n  yTileWidth:" + yTileWidth +
                           "\n  count:" + count +
                           "\n  url:" + url +
                           "\n  tilename:" + tilename +
                           "\n  d:" + Arrays.toString(d)); */
        Tile t = new Tile(xx, yy, z, xTileWidth, yTileWidth, count, url,
                          tilename, d);
        tiles.put(tilename, t);
        tileQueue.add(tilename);
        return t;
      } catch (IOException e) {
        System.out.println("Could not get tile! " + e);
      }
    }
    System.out.println("Tile load failed, no match? " + tilename);
    return null;
  }

  public String GetTileName(float xi, float yi) {
    // validate
    if (x + xi < 0.0 || y + yi < 0.0 || x + xi > xDim || y + yi > yDim ) {
      return null;
    }
    float xx = (float) (xi + x) * xTilesPerDim;
    float yy = (float) (yi + y) * yTilesPerDim;
    int tx = (int) xx;
    int ty = (int) yy;
    return GetShortName(tx, ty);
  }
    
  public Tile GetTile(float xi, float yi) {
    // System.out.println("Getting tile at: " + xi +  ", " + yi);
    String name =  GetTileName(xi, yi);
    // System.out.println("name" + name);
    return GetTile(name);
  }

  public Tile GetTile(String tilename) {
    //System.out.println("Getting tile name: " + tilename);
    //System.out.println("All keys: " + tiles.keySet());
    // System.out.println("And is this key: " + tilename + " in the keyset?" + tiles.containsKey(tilename) + " is your ans");
    if (tilename == null) return null;
    
    Tile t = null;
    if (tiles.containsKey(tilename)) {
      t = tiles.get(tilename);
    } else {
      tiles.put(tilename, t);
    }
    // System.out.println("here is teh tiule: " + t);
    if (t == null) {
        return FullLoad(tilename);
    } else {
        return t;
    }
  }

  public float GetHeight(float xi, float yi) {
    Tile t = GetTile(xi, yi);
    if (null == t) {
        return 0.0f;
    }
    return t.GetHeight(xi, yi);
  }

  public List<String> GetTileNamesFor(float xi, float yi, int radius) {
    // validate
    List<String> names = new ArrayList<>();
    if (x + xi < 0.0 || y + yi < 0.0 || x + xi > xDim || y + yi > yDim
        || radius < 1) {
      return names;
    }
    float xx = (float) (xi + x) * xTilesPerDim;
    float yy = (float) (yi + y) * yTilesPerDim;
    int tx = (int) xx;
    int ty = (int) yy;
    for (int j = -radius + 1 ; j < radius ; j++) {
      for (int i = -radius + 1 ; i < radius ; i++) {
        String tilename = GetShortName(tx + i, ty + j);
        if (tilename != null) {
            names.add(tilename);
        }
      }
    }
    return names;
  }

  public String GetStatus() {
    StringBuilder sb = new StringBuilder();
    String nn = "\n";
    sb.append("Terrain Server, at your service!\n")
        .append("\n")
        .append("X pos: ")
        .append(x)
        .append(nn)
        .append("Y pos: " + y + nn)
        .append("Z pos: " + z + nn)
        .append("xTileCount: " + xTileCount + nn)
        .append("yTileCount: " + yTileCount + nn)
        .append("xDim: " + xDim + nn)
        .append("yDim: " + yDim + nn)
        .append("Tiles: " + tiles.size() + nn);
    
    int count = 0;
    for (Map.Entry<String, Tile> entry : tiles.entrySet()) {
      sb.append(entry.getKey() + " -> " + entry.getValue());
      if  (count == 5) {
        sb.append("\n");
        count = 0;
      } else {
        sb.append(", ");
        count++;
      }
    }
    if (count != 6) sb.append("\n");
    return sb.toString();
  }

  // testing.
  public static void main(String... argv) {
    TerrainManager ts = new TerrainManager();
    System.out.println(ts.GetStatus());
    System.out.println(ts.GetTile(5.0f, 5.0f));
    List<String> names = ts.GetTileNamesFor(5.0f, 5.0f, 1);
    System.out.println("Better get " + names.size());
    for (String s : names) {
      System.out.println("  get: " + s);
      Tile t = ts.GetTile(s);
      System.out.print("Here he is: " + t);
    }
    System.out.println(ts.GetStatus());
      for (float f = 0.0f; f < 9.0f; f += .10f) {
      float ix = f * 1.0f;
      float iy = f * .4f;
      System.out.println("Traviling to " + ix + ", " + iy);
      System.out.println("Height: " + ts.GetHeight(ix, iy));
    }
  }
}
