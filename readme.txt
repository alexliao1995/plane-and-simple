Plane n' Simple is composed of 3 main classes that provide the functionality of the game. 
The first is GameSpace.java. The GameSpace can be put into several modes, menu, instructions, game and game over. 
Each of these modes allow for the program to draw and perform different functionalities. 
The planes in the game are stored in a TreeSet as Planes class implements the Comparable interface.
Key presses are stored in a boolean hashmap in order to reduce system input lag issues. 
Much of the responsibility of the game is passed off to the two seperate classes. 

The Planes class provides the functionality of the plane, include physics-based movement calculations and animations. 
Planes class also provides collision detection functionality and will draw differently depending on different states. 
The Runway class provides the landing functionality based on a landing zone implementation.
The Position class provides utility functions that are used throughout the game for distance and orientation calculations. 
The Game class provides the shell for running the Game. 
The GameObject is largely an general interface that could provide functionality for extending the game to different objects.

Much of the implement of the functionality in Planes, Runway and GameSpace are dynamic, allowing for extensive subtyping (especially of the Planes class). Very less of the numerical calculations are "hard coded". 
Also, Planes has a small block of commented code that provides for more realistic mechanics; however, the ability for the speed to drop below zero and a lack of time to implement stall failures meant that this was left out. 
Also, the current movement calculation are more easily "playable". 

To-do: Clean up rushed code, add more planes/maps!
