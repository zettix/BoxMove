/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* global playerManager */
/* global turdleManager */
/* global terrainManager */
/* global controls */
/* global clock */
/* global dotManager */

console.log("Websocket init");

//var socket = new WebSocket("ws://zettix.com:8889/BoxMove/actions");
var socket = new WebSocket("ws://172.16.7.125:8080/BoxMove/actions");
socket.onmessage = onMessage;

function onMessage(event) {
    var mm = JSON.parse(event.data);
    var msg_type = mm.msg_type;
    if (msg_type === "pos") {
      playerManager.UpdatePlayer(
       mm.id,
       parseFloat(mm.x),
       parseFloat(mm.y),
       parseFloat(mm.z),
       parseFloat(mm.xr),
       parseFloat(mm.yr),
       parseFloat(mm.zr),
       parseInt(mm.col));
       console.log("Col: " + mm.col);
    } else if (msg_type === "new") {
      playerManager.AddPlayer(       
       mm.id,
       parseFloat(mm.x),
       parseFloat(mm.y),
       parseFloat(mm.z),
       parseFloat(mm.xr),
       parseFloat(mm.yr),
       parseFloat(mm.zr));
   } else if (msg_type === "tile") {
       terrainManager.AddTile(mm.n);
   } else if (msg_type === "T") {
      // console.log("T packet!!");
       var in_tiles = mm.n;
       //console.log("Turdle# " + in_turdles.length);
       if (in_tiles !== undefined) {
         //console.log(in_tiles);
         terrainManager.Update(in_tiles, clock.getElapsed);
       } else {
           console.log("Nothing to terrian.");
       }
    } else if (msg_type === "register") {
        playerManager.RegisterWithServer(mm.id);
    } else if (msg_type === "del") {
        playerManager.RemovePlayer(mm.id);
    } else if (msg_type === "log") {
        console.log("Log:" + mm.log);
    } else if (msg_type === "V1") {
        var tmp_players = playerManager.GetPlayerIds();
        var tmp_players_hash = {};
        if (tmp_players === undefined) {
            console.log("No players yet");
        } else {
          for (var i = 0; i < tmp_players.length; i += 1) {
              tmp_players_hash[tmp_players[i]] = true;
          }
        }
        var in_players = mm.playerlist;
        // console.log("Parsing V1 message..." + in_players.length);
        for (var i = 0; i < in_players.length; i += 1) {
            // console.log("Player " + i);
            var in_p = in_players[i];
            if (tmp_players_hash.hasOwnProperty(in_p.id)) {
                playerManager.UpdatePlayer(
                        in_p.id,
                        parseFloat(in_p.x),
                        parseFloat(in_p.y),
                        parseFloat(in_p.z),
                        parseFloat(in_p.xr),
                        parseFloat(in_p.yr),
                        parseFloat(in_p.zr),
                        parseInt(in_p.col));
                        
                delete tmp_players_hash[in_p.id];
            } else {  // new player
              playerManager.AddPlayer(
                        in_p.id,
                        parseFloat(in_p.x),
                        parseFloat(in_p.y),
                        parseFloat(in_p.z),
                        parseFloat(in_p.xr),
                        parseFloat(in_p.yr),
                        parseFloat(in_p.zr),
                        parseInt(in_p.col));
            }
        }
        var players_to_delete = Object.keys(tmp_players_hash);
        if (players_to_delete !== undefined) {
            for (var i = 0; i < players_to_delete.length; i += 1) {
                playerManager.RemovePlayer(players_to_delete[i]); 
            }
        }
    
       
        // turdles.
        //if (turdleManager !== undefined) {
           var tmp_turdles = turdleManager.GetTurdleIds();
           var tmp_turdles_hash = {};
           if (tmp_turdles === undefined) {
              console.log("No turdles yet");
            } else { 
              for (var i = 0; i < tmp_turdles.length; i += 1) {
                  tmp_turdles_hash[tmp_turdles[i]] = true;
              }
            }
            var in_turdles = mm.turdlelist;
            //console.log("Turdle# " + in_turdles.length);
            if (in_turdles !== undefined) {
                for (var i = 0; i < in_turdles.length; i += 1) {
                    var in_p = in_turdles[i];
                    if (tmp_turdles_hash.hasOwnProperty(in_p.id)) {
                        turdleManager.UpdateTurdle(
                                in_p.id,
                                parseFloat(in_p.x),
                                parseFloat(in_p.y),
                                parseFloat(in_p.z),
                                parseFloat(in_p.xr),
                                parseFloat(in_p.yr),
                                parseFloat(in_p.zr),
                                parseFloat(in_p.s));

                        delete tmp_turdles_hash[in_p.id];
                    } else {  // new turdle
                      turdleManager.AddTurdle(
                                in_p.id,
                                parseFloat(in_p.x),
                                parseFloat(in_p.y),
                                parseFloat(in_p.z),
                                parseFloat(in_p.xr),
                                parseFloat(in_p.yr),
                                parseFloat(in_p.zr),
                                parseFloat(in_p.s));
                    }
                }
            }   
            var turdles_to_delete = Object.keys(tmp_turdles_hash);
            if (turdles_to_delete !== undefined) {
                for (var i = 0; i < turdles_to_delete.length; i += 1) {
                       turdleManager.RemoveTurdle(turdles_to_delete[i]); 
                }
            }
            var in_dots = mm.dotlist;
            if (in_dots !== undefined) {
                dotManager.RemoveDots();
                for (var i = 0; i < in_dots.length; i++) {
                    var in_p = in_dots[i];
                    dotManager.AddDot(
                            parseFloat(in_p.x),
                            parseFloat(in_p.y),
                            parseFloat(in_p.z));
                }
            }
        //}
    } // if v1
};

var doggy = 0;
function pushButtons() {
    if (doggy % 1000 === 3) {
          console.log("needed: " + terrainManager.Needed());
    }
    doggy += 1;
    if (doggy > 10000) {
        doggy -= 10000;
    }
    var msg = {
        msg_type: "but",
        F: controls.moveForward,
        B: controls.moveBackward,
        L: controls.moveLeft,
        R: controls.moveRight,
        T: controls.toggleTurdle,
        t: terrainManager.Needed()
    };
    if (socket.readyState === socket.OPEN) {
      socket.send(JSON.stringify(msg));
    }
}