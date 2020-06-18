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
// Coordinates into a texture for a particular vertex.
attribute vec2 a_TextureCoordinates;
// Coordinates into a texture for resulting fragment.
varying vec2 v_TextureCoordinates;

void main() {
    // Pass the texture coordinates as is.
    v_TextureCoordinates = a_TextureCoordinates;
    // Multiply the uniform matrix against the position to change it.
    gl_Position = u_Matrix * a_Position;
}