/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* global THREE */

var tankette = tankette = tankette || {};

tankette.Crumb = function(model, x, y, z, xr, yr, zr) {
  this.x = x;
  this.y = y;
  this.z = z;

  this.group = new THREE.Object3D();

  this.group.matrixAutoUpdate = true;
  this.group.position.x = x;
  this.group.position.y = y;
  this.group.position.z = z;
  // The crumb, is a box:
  var crumbgeo = new THREE.BoxGeometry(0.7, 0.7, 0.7); 
  //var yellow = new THREE.MeshLambertMaterial( {color: 0xffff00});
  //var yellow = new THREE.MeshNormalMaterial();
  var yellow = new THREE.MeshBasicMaterial({color: 0xffff00})
  var cubeingo = new THREE.Mesh(crumbgeo, yellow);
  this.group.add(cubeingo);
  this.group.rotation.x = xr;
  this.group.rotation.y = yr;
  this.group.rotation.z = zr;
};
