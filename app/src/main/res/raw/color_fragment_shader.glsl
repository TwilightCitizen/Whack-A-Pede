/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

/*
Use medium precision in floating point calculations.  High precision results in the most precise
placement of screen elements, but carries a high risk of being unsupported on many devices.  It aslo
incurs a heavy performance hit.  Low precision performs fastest, but results in imprecise screen
placement.  Medium precision strikes a nice balance between the two without compatibility risks.
*/
precision mediump float;

// Color to apply uniformly for all fragments.
uniform vec4 u_Color;

/*
ColorFragmentShader is an OpenGL shader program written in GLSL for execution on the graphics card.
It accepts a color to apply uniformly against all fragments it generates for the vertices it
receives from ColorVertexShader with which it is linked.  Here is where OpenGL takes the precisely
placed vertices for whichever shape it must draw and approximates it as the composition of discrete
individual fragments.  These can be though of as pixels and may even direclty translate to pixels
exactly.  However, fragments can be sized larger than a single pixel depending on hardware and
operating system settings.  This borrows from OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
void main() {
    // Use the uniform color for drawing the fragment to screen.
    gl_FragColor = u_Color;
}