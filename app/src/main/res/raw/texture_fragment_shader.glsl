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

// Texture to apply uniformly for texture coordinates.
uniform sampler2D u_TextureUnit;

// Coordinates into texture to apply to the various fragments.
varying vec2 v_TextureCoordinates;

void main() {
    // Use the color of the texture at the specified coordinates to color the fragment.
    gl_FragColor = texture2D( u_TextureUnit, v_TextureCoordinates );
}