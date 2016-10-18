                                                 Computer Graphics Project Description
                                                           3D Object Shading
                                              Programming application: Java, OpenGL, GLSL
                                              
In this project, I implemented Gouraud shader, Blinn-Phong shading model and Checkerboard texture using GLSL shading language. 
To start with, I used Blender to draw a 3D table model and imported the mesh triangle vertices and normals information into java to color the fragments with shading effects in the fragment shader.

In Gouraud shader, I computed the vertex color using Phong reflection model lighting formula, which is basically made up by ambient, diffuse and specular colors. The color inside a triangle was interpolated. 

The Blinn-Phong shading model is a simple modification to the Phong reflection model. The lighting formula for the specular part is a little bit changed as to Phong reflection model. So it looks brighter than ordinary Phong reflection model.

I applied a checkerboard pattern as a simple procedural texture on my table surface. The algorithm is to divide the space into numerous small unit cubes arranged black and white one by one. Each vertex dropping in black region would be shaded in black and in white region, shaded in white.

The smooth table geometry showed the smoothly changing color effects produced by shaders. The Checkerboard texture was combined to Phong reflection model or Blinn-Phong shading model.

For the user interface, press KEY_1 to set the Phong reflection model shader and press KEY_2 to set the Blinn-Phong shading model.

Welcome to continue to see my showcase movie on https://youtu.be/PeST53HvTvE!








Interactions
Keyboard:
       KEY	                                 Function
        1	                     Set the Phong reflection model shader
        2	                     Set the Blinn-Phong shader
        W	                     Move the camera view point forward
        S	                     Move the camera view point backward
        D	                     Move the camera view point to the right
        A	                     Move the camera view point to the left
        Q	                     Move the camera view point faster
        E	                     Move the camera view point slower
      SPACE	                   Move the camera view point upward
      SHIFT	                   Move the camera view point downward
       Esc	                   Close the window
        P	                     Take a snapshot

Mouse:
LeftButton: Rotate the angle of view
