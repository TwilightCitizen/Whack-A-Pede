/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

// Matrix to multiply against every position uniformly.
uniform mat4 u_Matrix;

// Position of a vertex.
attribute vec4 a_Position;

/*
ColorVertexShader is an OpenGL shader program written in GLSL for execution on the graphics card.
It accepts a matrix to apply uniformly against the positions of all vertices to update their final
position for perspective, rotation, translation, and other adjustments.  This will be linked to
ColorFragmentShader into total program pipeline by OpenGL which will receive this program's results.
This borrows from OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
void main() {
    // Multiply the uniform matrix against the position to change it.
    gl_Position = u_Matrix * a_Position;
}