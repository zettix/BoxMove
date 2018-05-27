/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/* global playerlist */
/* global THREE */
/* global controls */
/* global chasecam */


var tankette = tankette = tankette || {};

tankette.CrumbManager = function(model, scene) {
    var crumbs = {};
    var model_ = model;
        
    this.GetCrumb = function(crumbid) {
      if (crumbs.hasOwnProperty(crumbid)) {
        return crumbs[crumbid];
      } else {
          console.log("Error! crumb not found:" + crumbid);
      }
      return null;
    };
    
    this.NumCrumbs = function() {
        return Object.keys(crumbs);
    };
    
    
    this.GetCrumbIds = function() {
        var keylist = Object.keys(crumbs);
        return keylist;
    };
    
    this.AddCrumb = function(crumbid, x, y, z, xr, yr, zr) {
        var rockie = new tankette.Crumb(model_, x, y, z, xr, yr, zr);
               
        console.log("Crumb! " + rockie);
        console.log("Crumb x! " + rockie.x);
        console.log("Crumb y! " + rockie.y);
        console.log("Crumb z! " + rockie.z);
        console.log("Crumid ! " + crumbid);
        console.log("Crumb group! " + rockie.group);
        crumbs[crumbid] = rockie;
        scene.add(rockie.group);
       console.log("Checking registered crumb " + Object.keys(crumbs));
    };

    this.RemoveCrumb = function(crumbid) {
      if (crumbs.hasOwnProperty(crumbid)) {
          scene.remove(crumbs[crumbid].group);
          delete crumbs[crumbid];
      } else {
          console.log("Error! crumb not found:" + crumbid);
      }
    };
      
    this.UpdateCrumb = function(crumbid, x, y, z, xr, yr, zr) {
      if (crumbs.hasOwnProperty(crumbid)) {
          var p = this.GetCrumb(crumbid);
          p.group.position.x = x;
          p.group.position.y = y;
          p.group.position.z = z;
          p.group.rotation.x = xr;
          p.group.rotation.y = yr;
          p.group.rotation.z = zr;
      } else {
          console.log("Error! Update turdle not found:" + crumbid);
      }
    };
};
