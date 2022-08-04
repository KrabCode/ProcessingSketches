# Processing Sketches
Collection of some of my older Processing sketches published to my [instagram](https://www.instagram.com/krabcode/) and reddit at [/r/processing](https://www.reddit.com/r/processing) under the username [/u/Simplyfire](https://www.reddit.com/user/Simplyfire).

## Project structure
The sketches are found as standalone classes in the [src](https://github.com/KrabCode/ProcessingSketches/tree/master/src) folder, each with their own main() method based on the approach in this [Processing in Eclipse](https://processing.org/tutorials/eclipse/) tutorial, but IntelliJ IDEA is way better than Eclipse so I recommend using that. Just clone the project into your IDE of choice, include all of the libraries from the zip file in the project and then you can run the standalone sketches.

Each sketch extends an abstract class from the [applet](https://github.com/KrabCode/ProcessingSketches/tree/master/src/applet) folder that adds some extra features to the standard Processing PApplet, mostly for faster and more comfortable iteration.
- [GuiSketch](https://github.com/KrabCode/ProcessingSketches/blob/master/src/applet/GuiSketch.java) offers sliders, toggles and buttons in a collapsible tray and a few other utilities.
- [HotswapGuiSketch](https://github.com/KrabCode/ProcessingSketches/blob/master/src/applet/HotswapGuiSketch.java) reloads shaders at runtime as the shader file is edited.
- [KrabApplet](https://github.com/KrabCode/ProcessingSketches/blob/274778c9d39c08ce3565d400ab9c674733d10fb5/src/applet/KrabApplet.java) is the newest iteration of the GuiSketch which includes all of the realtime value control and shader reloading while offering new features like collapsible element groups, a color picker, infinite sliders and undo/redo functionality.

## Libraries
- [Processing 3.5.3](https://processing.org/) as the main framework
- [PostFX](https://github.com/cansik/processing-postfx) for beautiful shader effects
- [PeasyCam](https://github.com/jdf/peasycam) for easy camera control in 3D
- [Tablet](https://github.com/codeanticode/tablet) for drawing tablet support
- [Jamepad](https://github.com/williamahartman/Jamepad) for gamepad support

All of these libraries are packaged together in [libs.rar](https://github.com/KrabCode/ProcessingSketches/blob/26de9225ad5e330157e9853e5c5b1070096fe308/libs.rar) so you don't have to look for the .jars all over the internet.


## Discord
You can usually find me on the Creative Coding discord server under the username Krab. Use this [invite](https://discord.gg/KatY9nm) to get in.
