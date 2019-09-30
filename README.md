# Processing Sketches
Collection of some of my latest Processing sketches published often to [krabcode.eu](http://www.krabcode.eu), my [instagram](https://www.instagram.com/krabcode/) and reddit at [/r/processing](https://www.reddit.com/r/processing) under the username [/u/simplyfire](https://www.reddit.com/user/Simplyfire).

## Project structure
The sketches are found as standalone classes in the [src](https://github.com/KrabCode/ProcessingSketches/tree/master/src) folder, each with their own main() method based on the approach in this [Processing in Eclipse tutorial](https://processing.org/tutorials/eclipse/).

Each sketch extends an abstract class from the [applet](https://github.com/KrabCode/ProcessingSketches/tree/master/src/applet) folder that adds some functionality to the default PApplet, mostly for faster and more comfortable iteration.
- [GuiSketch](https://github.com/KrabCode/ProcessingSketches/blob/master/src/applet/GuiSketch.java) extends PApplet and adds easy to use sliders, toggles and buttons in an tray that can be hidden as well as some recording and other utilities.
- [HotswapGuiSketch](https://github.com/KrabCode/ProcessingSketches/blob/master/src/applet/HotswapGuiSketch.java) extends GuiSketch and can also recompile shaders at runtime as the shader file is edited.
- [ShadowGuiSketch](https://github.com/KrabCode/ProcessingSketches/blob/master/src/applet/ShadowGuiSketch.java) extends GuiSketch and lets you hide most of the implementation of [shadow mapping](https://forum.processing.org/two/discussion/12775/simple-shadow-mapping) as long as you call super.draw() at the right time and implement animate() as seen in the [shadow test](https://github.com/KrabCode/ProcessingSketches/blob/master/src/ShadowTest.java) sketch.

## Libraries
- [Processing 3.5.3](https://processing.org/) as the main framework
- [PostFX](https://github.com/cansik/processing-postfx) for beautiful shader effects
- [PeasyCam](https://github.com/jdf/peasycam) for easy camera control in 3D
- [Tablet](https://github.com/codeanticode/tablet) for drawing tablet support
- [Jamepad](https://github.com/williamahartman/Jamepad) for gamepad support

All of these libraries are packaged together in [libs.rar](https://github.com/KrabCode/ProcessingSketches/blob/26de9225ad5e330157e9853e5c5b1070096fe308/libs.rar) so you don't have to look for the .jars all over the internet.


## Discord
You can usually find me on the Creative Coding discord server under the username Krab. Use this [invite](https://discord.gg/KatY9nm) to get in.