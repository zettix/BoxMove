/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zettix.rocketsocket;
import com.zettix.players.Dot;
import com.zettix.players.HitboxHandler;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import com.zettix.players.Player;
import com.zettix.players.Crumb;
import com.zettix.players.Turdle;
import com.zettix.terrain.TerrainManager;
import com.zettix.terrain.Tile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
//import org.apache.log4j.*;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

/**
 *
 * @author sean
 */
@ApplicationScoped
public class RocketHandler {
    private final Map<Player, Session> playerMap;
    private final Map<String, Player> idToPlayerMap;
    private final Set<Turdle> turdles = new HashSet<>();
    private final Set<Crumb> crumbs = new HashSet<>();
    private final HitboxHandler hitboxHandler;
    private final Timer timer = new Timer();
    private final Random rnd = new Random(); // Because random.
    private final long delayseconds = 2l;
    //private final TerrainManager terrainManager = new TerrainManager(20000, 10000);
    private final TerrainManager terrainManager = new TerrainManager(3000, 3000);
    boolean doneloop = true;
    int loopcount = 0;
    int doneloop_count = 0;
    int turdle_serial = 0;
    int crumb_serial = 0;
    // private static final Logger LOG = Logger.getLogger(
    //            RocketHandler.class.getName());

    //private static final Logger LOG = LogManager.getLogger(RocketHandler.class.getName());
    
    public RocketHandler() {
        playerMap = new ConcurrentHashMap<>();
        idToPlayerMap = new ConcurrentHashMap<>();
        hitboxHandler = new HitboxHandler();
        StringBuffer sb = new StringBuffer("Hello I am a rocket handler! ")
                    .append("I am: ");
                    StackTraceElement[] stack = new Throwable().getStackTrace();
                    for (StackTraceElement elem : stack) {
                        sb.append("\n")
                                .append(elem.getFileName())
                                .append(" ")
                                .append(elem.getLineNumber());
                    }
        System.out.println(sb.toString());
        this.ScheduleRunMe();
    }
    
    private void RunMe() {
        // every frame game loop
        // update collions/physics
        // distribute to clients:
        // positions.
        // terrain.
        // explosions.
        //score, etc.
        loopcount++;
        if (loopcount > 1000) {
            StringBuffer sb = new StringBuffer("RocketHandler Summary!\n")
                    .append("I am: ");
                    StackTraceElement[] stack = new Throwable().getStackTrace();
                    for (StackTraceElement elem : stack) {
                        sb.append("\n")
                                .append(elem.getFileName())
                                .append(" ")
                                .append(elem.getLineNumber());
                    }                    
                    sb.append("\nPlayer count:")
                    .append(playerMap.size())
                    .append("\n and Terrain:")
                    .append(terrainManager.toString());
            
            System.out.println(sb.toString());
            loopcount = 0;
        }
        if (doneloop) {
           doneloop = false;
           updatePlayers();
           // InfoLog("Horey shett, it works.");
           doneloop = true;
        } else {
            doneloop_count++;
            if (doneloop_count > 1000) {
                doneloop = true;
                doneloop_count = 0;
            }
        }
    }
    
    class MainLoopTimer extends TimerTask {
        @Override
        public void run() {
            RunMe();
        }
    }
    
    private void ScheduleRunMe() {
        
        /* @param task   task to be scheduled.
           @param delay  delay in milliseconds before task is to be executed.
           @param period time in milliseconds between successive task executions
        */
        timer.schedule(new MainLoopTimer(), delayseconds * 1000, 10);
    }
    
    public void updateCollisions() {
        hitboxHandler.DetectCollisions();
    }
    
    public void addSession(Session session) {
        addPlayer(session);
        sendToSession(session, createGamePacket());
        Player p = getPlayerById(session.getId());
        JsonObject regMessage = createRegisterMessage(p);
        sendToSession(session, regMessage);
    }
    
    public void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (java.lang.IllegalStateException | IOException ex) {
            removePlayer(session.getId());
            // Logger.getLogger(RocketHandler.class.getName()).log(Level.INFO, ex);
        } catch (NullPointerException e) {
            System.out.println("Null pointer exception in sendToSession, probably disconnected.");
        }
    }
    
    public void removeSession(Session session) {
        /* 	at org.jboss.weld.bean.proxy.ProxyMethodHandler.getInstance(ProxyMethodHandler.java:125)
	at com.zettix.rocketsocket.RocketHandler$Proxy$_$$_WeldClientProxy.removeSession(Unknown Source)
	at com.zettix.rocketsocket.SocketServer.close(SocketServer.java:45)
        */ 
        String playerid = session.getId();
        removePlayer(playerid);
        JsonObject delMe = createDelMessage(playerid);
        sendToAllConnectedSessions(delMe);
    }
    
    private List<Player> getPlayers() {
        List<Player> result; 
        synchronized(playerMap) {
            result = new ArrayList<>(playerMap.keySet());
        }
      return result;
    }

    private void addPlayer(Session s) {
        Player p = new Player();
        p.setX(rnd.nextDouble() * 10.0f);
        p.setY(0.0f);
        p.setZ(rnd.nextDouble() * 10.0f);
        p.setXr(0.0f);
        p.setYr(rnd.nextDouble() * 3.14f);
        p.setZr(0.0f);
        p.setId(s.getId());
        synchronized (playerMap) {
            playerMap.put(p, s);
            idToPlayerMap.put(p.getId(), p);
            hitboxHandler.AddPlayer(p);
        }
    }
    
    private void addTurdle(String id) {
        Player p = getPlayerById(id);
        if (p.moved) {
          p.movecount++;
          if (p.movecount > 5) {
            Turdle t = new Turdle();
            t.setId("A" + turdle_serial++);
            t.setX(p.getX());
            t.setY(p.getY());
            t.setZ(p.getZ());
            t.setXr(p.getXr());
            t.setYr(p.getYr());
            t.setZr(p.getZr());
            turdles.add(t);
            p.movecount = 0;
          }
        }
    }

    private void addCrumb(String id) {
        Player p = getPlayerById(id);
        if (p.moved) {
          p.movecount++;
          if (p.movecount > 5) {
            Crumb t = new Crumb();
            t.setId("C" + crumb_serial++);
            t.setX(p.getX());
            t.setY(p.getY());
            t.setZ(p.getZ());
            t.setXr(p.getXr());
            t.setYr(p.getYr());
            t.setZr(p.getZr());
            crumbs.add(t);
            p.movecount = 0;
          }
        }
    }


    private void removeCrumb(Crumb t) {
        synchronized(crumbs) {
          crumbs.remove(t);
        }
    }
    
    private void removeTurdle(Turdle t) {
        synchronized(turdles) {
          turdles.remove(t);
        }
    }
    
    private void removePlayer(String id) {
        synchronized (playerMap) {
          Player p = getPlayerById(id);
          try {
            playerMap.remove(p);
          } catch (NullPointerException e) {
            System.out.println("Concurrent Exception Probably!!!");
          }
          try {
            idToPlayerMap.remove(p.getId());
          } catch (NullPointerException e) {
            System.out.println("Concurrent Exception Probably!!!");
          }
          hitboxHandler.DelPlayer(id);
        }
    }

    public Player getPlayerById(String id) {
        synchronized (playerMap) {
          try  {
            if (idToPlayerMap.containsKey(id)) {
              return idToPlayerMap.get(id);
            }
          } catch (NullPointerException e) {
            System.out.println("getPlayerById null pointer exception!!" + id);
          }
        }
        return null;
    }

    private Turdle getTurdleById(String id) {
        for (Iterator it = turdles.iterator(); it.hasNext();) {
            Turdle p = (Turdle) it.next();
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private Crumb getCrumbById(String id) {
        for (Iterator it = crumbs.iterator(); it.hasNext();) {
            Crumb p = (Crumb) it.next();
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    private JsonObject createGamePacket() {
        JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder playerlist = provider.createArrayBuilder();
        Set<Player> players;
        synchronized (playerMap) {
            players = playerMap.keySet();
        }
        for (Player p : players) {
            int collision = 0;
            if (hitboxHandler.IsHit(p)) {
                collision = 1;
            }
            JsonObject pj = provider.createObjectBuilder()
                .add("id", p.getId())
                .add("x", p.getX())
                .add("y", p.getY())
                .add("z", p.getZ())
                .add("xr", p.getXr())
                .add("yr", p.getYr())
                .add("zr", p.getZr())
                .add("col", collision)                    
                .build();        
            playerlist.add(pj);
        }
        
        JsonArrayBuilder turdlelist = provider.createArrayBuilder();
        for (Iterator<Turdle> it = turdles.iterator(); it.hasNext();) {
            Turdle p = it.next();
            JsonObject pj = provider.createObjectBuilder()
              .add("id", p.getId())
              .add("x", p.getX())
              .add("y", p.getY())
              .add("z", p.getZ())
              .add("xr", p.getXr())
              .add("yr", p.getYr())
              .add("zr", p.getZr())
              .add("s", p.getScale())      
              .build();        
            turdlelist.add(pj);
        }

        JsonArrayBuilder crumblist = provider.createArrayBuilder();
        for (Iterator<Crumb> it = crumbs.iterator(); it.hasNext();) {
            Crumb p = it.next();
            JsonObject pj = provider.createObjectBuilder()
              .add("id", p.getId())
              .add("x", p.getX())
              .add("y", p.getY())
              .add("z", p.getZ())
              .add("xr", p.getXr())
              .add("yr", p.getYr())
              .add("zr", p.getZr())
              .build();        
            crumblist.add(pj);
        }

        JsonArrayBuilder dotlist = provider.createArrayBuilder();
        for (Dot p : hitboxHandler.dots) {
            JsonObject pj = provider.createObjectBuilder()
                    .add("x", p.getX())
                    .add("y", p.getY())
                    .add("z", p.getZ())
                    .build(); 
            dotlist.add(pj);
        }
        
        JsonObject packet = provider.createObjectBuilder()
                .add("msg_type", "V1")
                .add("playerlist", playerlist)
                .add("turdlelist", turdlelist)
                .add("crumblist", crumblist)
                .add("dotlist", dotlist)
                .build();
        return packet;
    }
    
    private JsonObject createMoveMessage(Player p) {
        int collision = 0;
        if (hitboxHandler.IsHit(p)) {
            collision = 1;
        }
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "pos")
                .add("id", p.getId())
                .add("x",p.getX())
                .add("y",p.getY())
                .add("z",p.getZ())
                .add("xr",p.getXr())
                .add("yr",p.getYr())
                .add("zr",p.getZr())
                .add("col", collision)
                .build();
        return addMessage;
    }
    
    private JsonObject createRegisterMessage(Player p) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "register")
                .add("id", p.getId())
                .build();
        return addMessage; 
    }           
    
    private JsonObject createAddMessage(Player p) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "new")
                .add("id", p.getId())
                .add("x",p.getX())
                .add("y",p.getY())
                .add("z",p.getZ())
                .add("xr",p.getXr())
                .add("yr",p.getYr())
                .add("zr",p.getZr())
                .build();
        return addMessage; 
    }
    
    private JsonObject createDelMessage(String id) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "del")
                .add("id", id)
                .build();
        return addMessage; 
    }
    
    public JsonObject createTerrainTileMessage(String tilename) {
        Tile t = terrainManager.GetTile(tilename);
        if (t == null) {
            System.out.println("Ask for null tilename, get null json.");
            return null;
        }
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "tile")
                .add("n", t.toJson())
                .build();
        return addMessage; 
    }
    
    
    private JsonObject createTerrainMessage(Player p) {
        double x = (double) p.getX();
        double y = (double) p.getZ();
        int radius = 5;
        JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder jpatches = provider.createArrayBuilder();
        List<String> tpatches = terrainManager.GetTileNamesFor(x, y, radius);
        if (tpatches.isEmpty())
        {
            return null;
        }
        
        tpatches.forEach((patch) -> {
            jpatches.add(patch);
        });
 
        JsonObject addMessage = provider.createObjectBuilder()
                .add("msg_type", "T")
                .add("n", jpatches)
                .build();
        return addMessage;
    }
        
        
    private void updateTurdles() {
        Set<Turdle> removals = new HashSet<>();
        for (Iterator it = turdles.iterator(); it.hasNext();) {
            Turdle t = (Turdle) it.next();
            t.age++;
            double f = (double) (t.age) * 0.005;
            t.setXr(f);
            t.setYr(f);
            t.setZr(f);
            t.setScale(Math.sin(f) * Math.sin(f) * 10.00 + 1.0);
            if (t.age > 10000) {
                removals.add(t);
            }
        }
        for (Iterator<Turdle> it = removals.iterator(); it.hasNext();) {
            turdles.remove(it.next());
        }
    }

    private void updateCrumbs() {
        Set<Crumb> removals = new HashSet<>();
        for (Iterator it = crumbs.iterator(); it.hasNext();) {
            Crumb t = (Crumb) it.next();
            t.age++;
            double f = (double) (t.age) * 0.005;
            t.setXr(f);
            t.setYr(f);
            t.setZr(f);
            if (t.age > 10000) {
                removals.add(t);
            }
        }
        for (Iterator<Crumb> it = removals.iterator(); it.hasNext();) {
            crumbs.remove(it.next());
        }
    }
    
    
    private void updatePlayers() {
        
        Set<Player> playerSet;
        synchronized(playerMap) {
            playerSet = new HashSet<>(playerMap.keySet());
        }
        for (Player p : playerSet) {
            updatePlayerLocation(p);
            if (p.toggleturdle) {
                addTurdle(p.getId());
            }
            if (p.togglecrumb) {
                addCrumb(p.getId());
            }
        }
        DetectCollisions();
        for (Player p : playerSet) {
            if (hitboxHandler.IsHit(p)) {
                MoveUndo(p);
            }
        }
        updateTurdles();
        updateCrumbs();
        sendToAllConnectedSessions(createGamePacket());
        for (Player p : playerSet) {
            if (p.moved || true){
                JsonObject m = createTerrainMessage(p);
                if (m != null) {
                    sendToSession(playerMap.get(p), m);
                }
            }
        }
    }
    
    private void updatePlayerLocation(Player p) {
        double delta = 1.0;
        p.moved = false;
        if (p.forward) {
            p.MoveForward(delta);
            p.moved = true;
        }
        if (p.back) {
            p.MoveBackward(delta);
            p.moved = true;
        }
        if (p.left) {
            p.MoveLeft(delta);
        }
        if (p.right) {
            p.MoveRight(delta);
        }        
        // Terrain.
        double elevation = terrainManager.GetHeight((double) p.getX(), (double) p.getZ());
        p.setY((double) elevation + 1.0);
        
    }
    
    private void DetectCollisions() {
        hitboxHandler.DetectCollisions();
    }
    
    private void MoveUndo(Player p) {
        double delta = 2.0;
        //InfoLog(hitboxHandler.DetectCollisions());  // optimize by player?
        // simple: if collision on player, undo move.
          if (p.back) {
            p.MoveForward(delta);
          }
          if (p.forward) {
            p.MoveBackward(delta);
          }
          if (p.right) {
            p.MoveLeft(delta);
          }
          if (p.left) {
            p.MoveRight(delta);
          }
    }
    
    private void sendToAllConnectedSessions(JsonObject message) {
        if (message == null) return;
        Set<Session> sessionSet;
        synchronized (playerMap) {
          sessionSet = new HashSet<>(playerMap.values());
        }
        for (Session session : sessionSet) {
                /*Severe:   Exception in thread "Timer-2"
Severe:   java.util.ConcurrentModificationException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1429)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at com.zettix.rocketsocket.RocketHandler.sendToAllConnectedSessions(RocketHandler.java:466)
	at com.zettix.rocketsocket.RocketHandler.updatePlayers(RocketHandler.java:403)
	at com.zettix.rocketsocket.RocketHandler.RunMe(RocketHandler.java:73)
	at com.zettix.rocketsocket.RocketHandler.access$000(RocketHandler.java:36)
	at com.zettix.rocketsocket.RocketHandler$MainLoopTimer.run(RocketHandler.java:87)
	at java.util.TimerThread.mainLoop(Timer.java:555)
	at java.util.TimerThread.run(Timer.java:505)*/
                
                sendToSession(session, message);
        }
    }
    
    public void LogHits() {
        InfoLog(hitboxHandler.GetHits());
    }
    public void InfoLog(String msg) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject logMessage = provider.createObjectBuilder()
                .add("msg_type", "log")
                .add("log", msg)
                .build();
        sendToAllConnectedSessions(logMessage);
        //LOG.log(Level.INFO, msg);
    }
}
