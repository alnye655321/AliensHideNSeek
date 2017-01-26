# Aliens Hide N Seek
A multiplayer horror hide and seek game. For Android ![alt text](https://developer.android.com/favicon.ico "Logo Title Text 1") 2+ players
* [YouTube](https://www.youtube.com/playlist?list=PLZ3nvNonAbdBqWw4GL2vDvr4wACjmlTHm)

## Description
Inspired by the colonial marines motion tracker from the movie [Aliens](http://avp.wikia.com/wiki/M314_Motion_Tracker) There are two separate game views, one for the hider (human) and another for the seekers (aliens). The hider hosts the game and handles most of the game environment tracking. The seekers communicate with the host via a Node.js API. The game is started by the hider who also sets a time limit. The game continuously tracks the GPS location of players and calculates the distance between them. If the seekers capture the hider within the time limit they win. A capture is determined by occupying roughly the same GPS space.  The game screen is kept intentionally sparse. What made the device effectively scary in the movie was the lack of information it provided.

Sound is another important feature that increases the tension of the game. I incorporated a beeping sound that is sufficiently creepy. As the distance closes between the hider and seekers the game programmatically changes the frequency of the sound. The goal was to have a game that can be played with a well trained ear using audio alone.

## Features
* Animation
* Lobby and game create areas
* Audio
* Google location API
* Game engine

## Technology
Client: Android Java Application

Server: Node.js API
