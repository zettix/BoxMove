/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* global THREE */
/* global WASD */
/* global tankette */

/* scene and stuff */
var scene = new THREE.Scene();
var aspect = window.innerWidth / window.innerHeight;
var camera = new THREE.PerspectiveCamera(75, aspect, 0.1, 1000);
var renderer = new THREE.WebGLRenderer();
renderer.setSize( window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);

var zerovec = new THREE.Vector3(0, 0, 0);
renderer.shadowMap.enabled = true;
renderer.shadowMapSoft = true;
console.log("Main init");
var playerManager = new tankette.PlayerManager("rocket1opt", scene);
console.log("playerManger is done");
var turdleManager = new tankette.TurdleManager("rocket1opt", scene);
var crumbManager = new tankette.CrumbManager("NoModel", scene);
var dotManager = new tankette.DotManager("rocket1opt", scene);
var terrainManager = new tankette.TerrainManager(scene);
console.log("turdlemanager is done too!=) happy");
console.log("Turdle manager:" + turdleManager);
console.log("Crumb manager:" + crumbManager);
console.log("Player Manager: " + playerManager);
console.log("Terrain Manager: " + terrainManager);

var ResetCamera = function() {
  camera.position.z = -26;
  camera.position.y = 14;
  camera.position.x = -26;
  camera.lookAt(zerovec);
};

var clock = new THREE.Clock();

//var helpie = new THREE.CameraHelper(dirlight.shadow);

var ambientLight = new THREE.AmbientLight(0x0c0c0c);
scene.add(ambientLight);

var spot1 = new THREE.SpotLight(0xffffff);
spot1.position.set(-200, 100, -10);
spot1.target.position.set(zerovec);
spot1.castShadow = true;
spot1.shadowMapWidth = 1024;
spot1.shadowMapHeight = 1024;
spot1.shadowCameraNear = 50;
spot1.shadowCameraFar = 4000;
spot1.shadowCameraFov = 30;
spot1.intensity = 2.0;

// Rockets
//var rocket1 = new tankette.Rocket("rocket1opt", 20, 0.3, 20);
//scene.add(rocket1.group);
//test
var boxgeo = new THREE.BoxGeometry(2, 2, 2);
var redmat = new THREE.MeshLambertMaterial( {color: 0xff0000});
var cube = new THREE.Mesh(boxgeo, redmat);
cube.position.set(0, 0, 0);
cube.castShadow = true;
cube.receiveShadow = true;
var planegeo = new THREE.PlaneGeometry(40, 40, 10, 10);
var greenmat = new THREE.MeshLambertMaterial( {color: 0x22ff22});
var plane = new THREE.Mesh(planegeo, greenmat);
plane.rotation.x = -Math.PI * 0.5;
plane.position.set(0, -3.5, 0);
plane.castShadow = false;
plane.receiveShadow = true;

scene.add(spot1);
scene.add(cube);
scene.add(plane);

var controls = new WASD.Controls(undefined);
controls.movementSpeed = 5;
controls.lookSpeed = 0.05;

var chasecam = new tankette.ChaseCam(undefined, camera);
ResetCamera();
var use_chase_cam = false;
var can_change_cam = true;
var update_timeout = 0;
var can_wireframe = false;
var use_wireframe = false;

var pushBox = function() {
    // I have no idea what this does.
};

var Update = function() {
  var update_delta = clock.getDelta();
  update_timeout += update_delta;
  if (update_timeout > 16) {
      update_timeout = 0;
      console.log("X: " + chasecam.target.group.position.x + " Y: " + chasecam.target.group.position.y +  " Z: " + chasecam.target.group.position.z);
      console.log("Players: " + playerManager.NumPlayers());
      console.log("Turdles: " + turdleManager.NumTurdles());
      console.log("Crumbs: " + crumbManager.NumCrumbs());
      console.log("Terrain: " + terrainManager.Summary());
      console.log("Wireframe: " + use_wireframe);
      pushBox();
  }
  //controls.update(update_delta);
  WireFrameUpdate();
  pushButtons(clock.elapsedTime);
  
};

// all this jazz is to make a T flip flop.
var WireFrameUpdate = function() {
    if (controls.toggleWireFrame === true) {
        if (can_wireframe === true) {
            can_wireframe = false;
            if (use_wireframe === true) {
                // disable wireframe;
                use_wireframe = false;
            } else {
                // enable wireframe
                use_wireframe = true;
            }
        }
    } else {
        can_wireframe = true;
    }
};

var CamUpdate = function() {
  if (controls.toggleCam === true) {
    if (can_change_cam === true) {
      if (use_chase_cam === true) {
        use_chase_cam = false;
      } else {
        use_chase_cam = true;
      }
      can_change_cam = false;
    }
  } else {
    can_change_cam = true;
  }
  if (use_chase_cam === true) {
    chasecam.Update();
  } else {
    ResetCamera();
  }
  //rocket1.group.rotation.y  += 0.01;
};



var render = function() {
  requestAnimationFrame(render);
  renderer.render(scene, camera);
  Update();
};

render();
