# BoxMove
Precursor to Tankette, Java / Javascript / ThreeJS / WebGL / Mars / Terrain Server / GJK Intersection / multiplayer experience.

So you like graphics.  You want to write a game.  3D is a passion.  You gravitate toward java, if it is a fable, this
has the nicest backstory.  The giants, pixies, irascible curmudgeons, and dangerously boring people made java IDEs
just glorious. It's candy.  There's way too much of it.

I wanted to write a terrain server since the concept of running around virtually gripped me.  The limit of files in a
conventional directory really should not exceed say one million. A few thousand is OK but the typical unix shell has
its own limitations.  Anyone worth their salt runs against this.  Techniques include a light directory structure based
on the key of the data.  I went for SQLite.  The simple file structure appeals to games, for resource packs too, as well
as static or even dynamic data like players.  I'm not there yet but terrain is soon coming with 1 million tiles.

The stress test is the planet Mars.  By pressing X you can get a wireframe view.
![Screenshot](https://github.com/zettix/BoxMove/blob/master/screenshots/tankette-mars-lowres.png)
