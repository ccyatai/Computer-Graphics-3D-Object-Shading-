package w4160;

import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class PA3 {

    String windowTitle = "3D Shapes";
    public boolean closeRequested = false;

    long lastFrameTime; // used to calculate delta
    
    float triangleAngle; // Angle of rotation for the triangles
    float quadAngle; // Angle of rotation for the quads

    ShaderProgram shader;
    
    public void run() {

        createWindow();
        getDelta(); // Initialise delta timer
        initGL();
        initShaders();
        
        while (!closeRequested) {
            pollInput();
            updateLogic(getDelta());
            renderGL();

            Display.update();
        }
        
        cleanup();
    }
    
    private void initGL() {

        /* OpenGL */
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        GL11.glViewport(0, 0, width, height); // Reset The Current Viewport
        GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
        GL11.glLoadIdentity(); // Reset The Projection Matrix
        GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f, 100.0f); // Calculate The Aspect Ratio Of The Window
        GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix
        GL11.glLoadIdentity(); // Reset The Modelview Matrix

        GL11.glShadeModel(GL11.GL_SMOOTH); // Enables Smooth Shading
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
        GL11.glClearDepth(1.0f); // Depth Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Test To Do
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); // Really Nice Perspective Calculations
        Camera.create();        
    }
    
    private void initShaders() {
        String vertex_shader =
     
//        		"uniform mat4 projection, modelview, normalMat;" +

		        "varying vec3 normalInterp;" +
		        "varying vec3 vertPos;" +
 
		        "void main(){" +
		        "    gl_Position = ftransform();" +
		        "    vertPos = gl_Vertex;" +
		        "    normalInterp = gl_Normal;" +
		        "}";
     
        String fragment_shader =
        
		        "varying vec3 normalInterp;" +
		        "varying vec3 vertPos;" +
		         
		        "uniform int mode;" +
		         
		        "uniform vec3 lightPos;" +
		        "vec3 ambientColor;" +
		        "const vec3 diffuseColor = vec3(0.5, 0.5, 0.5);" +
		        "const vec3 specColor = vec3(1.0, 1.0, 1.0);" +
		         
		        "void main() {" +
		        
		        "if (mod(floor(vertPos.x) + floor(vertPos.y) + floor(vertPos.z), 2) == 0) {" +
				"ambientColor = vec3(0.2, 0.2, 0.2);" +
		        "}" +
		        
				"else {" +
		        "ambientColor = vec3(0.0, 0.0, 0.0);" +
				"}" +
				
		        "  vec3 normal = normalize(normalInterp);" +
		        "  vec3 lightDir = normalize(lightPos - vertPos);" +
		         
		        "  float lambertian = max(dot(lightDir,normal), 0.0);" +
		        "  float specular = 0.0;" +
		         
		        "  if(lambertian > 0.0) {" +
		         
		        "    vec3 viewDir = normalize(-vertPos);" +
		         
		            // this is blinn phong
		        "    vec3 halfDir = normalize(lightDir + viewDir);" +
		        "    float specAngle = max(dot(halfDir, normal), 0.0);" +
		        "    specular = pow(specAngle, 16.0);" +
		         
   	            	// this is phong (for comparison)
		        "    if(mode == 2) {" +
		        "      vec3 reflectDir = reflect(-lightDir, normal);" +
		        "      specAngle = max(dot(reflectDir, viewDir), 0.0);" +
		            // note that the exponent is different here
		        "      specular = pow(specAngle, 4.0);" +
		        "    }" +
		        "  }" +
		         
		        "  gl_FragColor = vec4(ambientColor + lambertian * diffuseColor + specular * specColor, 1.0);" +
				"}";

        try {
            shader = new ShaderProgram(vertex_shader, fragment_shader);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void updateLogic(int delta) {
        triangleAngle += 0.1f * delta; // Increase The Rotation Variable For The Triangles
        quadAngle -= 0.05f * delta; // Decrease The Rotation Variable For The Quads
    }

    private Model LoadObject(String key) {
		Model m = new Model();
		OBJLoader OBJ = new OBJLoader();
	    	try{
	    		m = OBJ.loadModel(new File("D:/workspace/PA#3/" + key + ".obj"));
	    	} catch(FileNotFoundException e) {
	    		e.printStackTrace();
	    		Display.destroy();
	    		System.exit(1);
	    	} catch(IOException e) {
	    		e.printStackTrace();
	    		Display.destroy();
	    		System.exit(1);
	    	}
		return m;
	}
	
	private Texture LoadTexture(String key) {
		try {
	        return TextureLoader.getTexture("PNG", new FileInputStream(new File("D:/workspace/PA#3/" + key + ".png")));
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		return null;
	}
    
    public Vector3f rotate(Vector3f angle, Vector3f vec) {
		Vector3f v = new Vector3f(0, 0, 0);
		v.x = (float) (vec.x * Math.cos(Math.toRadians(angle.y)) * Math.cos(Math.toRadians(angle.z)) - vec.y * Math.cos(Math.toRadians(angle.y)) * Math.sin(Math.toRadians(angle.z)) + vec.z * Math.sin(Math.toRadians(angle.y)));
		v.y = (float) (vec.x * ( Math.sin(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.y)) * Math.cos(Math.toRadians(angle.z)) + Math.cos(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.z))) + vec.y * ( - Math.sin(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.y)) * Math.sin(Math.toRadians(angle.z)) + Math.cos(Math.toRadians(angle.x)) * Math.cos(Math.toRadians(angle.z))) - vec.z * Math.sin(Math.toRadians(angle.x)) * Math.cos(Math.toRadians(angle.y)));
		v.z = (float) (vec.x * ( - Math.cos(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.y)) * Math.cos(Math.toRadians(angle.z)) + Math.sin(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.z))) + vec.y * ( Math.cos(Math.toRadians(angle.x)) * Math.sin(Math.toRadians(angle.y)) * Math.sin(Math.toRadians(angle.z)) + Math.sin(Math.toRadians(angle.x)) * Math.cos(Math.toRadians(angle.z))) + vec.z * Math.cos(Math.toRadians(angle.x)) * Math.cos(Math.toRadians(angle.y)));
		return v;
	}
    
    public Vector3f translate(Vector3f vec, Vector3f point) {
		Vector3f v = new Vector3f(0, 0, 0);
		v.x = vec.x + point.x;
		v.y = vec.y + point.y;
		v.z = vec.z + point.z;
		return v;
	}
    
    private void renderGL() {

        // start to use shaders
        shader.begin();

        if (Keyboard.getEventKey() == Keyboard.KEY_1)
        	shader.setUniform1i("mode", 1);
        else if (Keyboard.getEventKey() == Keyboard.KEY_2)
            shader.setUniform1i("mode", 2);
 
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
        GL11.glLoadIdentity(); // Reset The View
        GL11.glTranslatef(0.0f, 0.0f, -19.0f); // Move Right And Into The Screen

        Camera.apply();
        
        Vector3f light = new Vector3f(10.0f, 10.0f, 10.0f);
        Vector3f angle = new Vector3f(-Camera.rotation.x, -Camera.rotation.y, -Camera.rotation.z);
        light = rotate(angle, light);
        shader.setUniform3f("lightPos", light.x, light.y, light.z);
        
        
        GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing
        
        Model table = LoadObject("Table");
        
    	for (Face face : table.faces) {
    		
    		GL11.glTexCoord2f(0, 0);
    		Vector3f n1 = table.normals.get((int) face.normal.x - 1);
    		GL11.glNormal3f(n1.x, n1.y, n1.z);
    		Vector3f v1 = table.vertices.get((int) face.vertex.x - 1);
    		GL11.glVertex3f(v1.x, v1.y, v1.z);
    		
    		GL11.glTexCoord2f(0, 1);
    		Vector3f n2 = table.normals.get((int) face.normal.y - 1);
    		GL11.glNormal3f(n2.x, n2.y, n2.z);
    		Vector3f v2 = table.vertices.get((int) face.vertex.y - 1);
    		GL11.glVertex3f(v2.x, v2.y, v2.z);
    		
    		GL11.glTexCoord2f(1, 1);
    		Vector3f n3 = table.normals.get((int) face.normal.z - 1);
    		GL11.glNormal3f(n3.x, n3.y, n3.z);
    		Vector3f v3 = table.vertices.get((int) face.vertex.z - 1);
    		GL11.glVertex3f(v3.x, v3.y, v3.z);
    	}
        
        GL11.glEnd(); // Done Drawing

        shader.end();
    }

    /**
     * Poll Input
     */
    public void pollInput() {
        Camera.acceptInput(getDelta());
        // scroll through key events
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    public void snapshot() {
        System.out.println("Taking a snapshot ... snapshot.png");

        GL11.glReadBuffer(GL11.GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
     
        return delta;
    }

    private void createWindow() {
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
            Display.setVSyncEnabled(true);
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Destroy and clean up resources
     */
    private void cleanup() {
        Display.destroy();
    }
    
    public static void main(String[] args) {
        new PA3().run();
    }
    
    public static class Camera {
        public static float moveSpeed = 0.5f;

        private static float maxLook = 85;

        private static float mouseSensitivity = 0.05f;

        private static Vector3f pos;
        private static Vector3f rotation;

        public static void create() {
            pos = new Vector3f(0, 0, 0);
            rotation = new Vector3f(0, 0, 0);
        }

        public static void apply() {
            if (rotation.y / 360 > 1) {
                rotation.y -= 360;
            } else if (rotation.y / 360 < -1) {
                rotation.y += 360;
            }

            //System.out.println(rotation);
            GL11.glRotatef(rotation.x, 1, 0, 0);
            GL11.glRotatef(rotation.y, 0, 1, 0);
            GL11.glRotatef(rotation.z, 0, 0, 1);
            GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
        }

        public static void acceptInput(float delta) {
            //System.out.println("delta="+delta);
            acceptInputRotate(delta);
            acceptInputMove(delta);
        }

        public static void acceptInputRotate(float delta) {
            if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
                float mouseDX = Mouse.getDX();
                float mouseDY = -Mouse.getDY();
                //System.out.println("DX/Y: " + mouseDX + "  " + mouseDY);
                rotation.y += mouseDX * mouseSensitivity * delta;
                rotation.x += mouseDY * mouseSensitivity * delta;
                rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
            }
        }

        public static void acceptInputMove(float delta) {
            boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
            boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
            boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A);
            boolean keyFast = Keyboard.isKeyDown(Keyboard.KEY_Q);
            boolean keySlow = Keyboard.isKeyDown(Keyboard.KEY_E);
            boolean keyFlyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            float speed;

            if (keyFast) {
                speed = moveSpeed * 5;
            } else if (keySlow) {
                speed = moveSpeed / 2;
            } else {
                speed = moveSpeed;
            }

            speed *= delta;

            if (keyFlyUp) {
                pos.y += speed;
            }
            if (keyFlyDown) {
                pos.y -= speed;
            }

            if (keyDown) {
                pos.x -= Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z += Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyUp) {
                pos.x += Math.sin(Math.toRadians(rotation.y)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y)) * speed;
            }
            if (keyLeft) {
                pos.x += Math.sin(Math.toRadians(rotation.y - 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y - 90)) * speed;
            }
            if (keyRight) {
                pos.x += Math.sin(Math.toRadians(rotation.y + 90)) * speed;
                pos.z -= Math.cos(Math.toRadians(rotation.y + 90)) * speed;
            }
        }

        public static void setSpeed(float speed) {
            moveSpeed = speed;
        }

        public static void setPos(Vector3f pos) {
            Camera.pos = pos;
        }

        public static Vector3f getPos() {
            return pos;
        }

        public static void setX(float x) {
            pos.x = x;
        }

        public static float getX() {
            return pos.x;
        }

        public static void addToX(float x) {
            pos.x += x;
        }

        public static void setY(float y) {
            pos.y = y;
        }

        public static float getY() {
            return pos.y;
        }

        public static void addToY(float y) {
            pos.y += y;
        }

        public static void setZ(float z) {
            pos.z = z;
        }

        public static float getZ() {
            return pos.z;
        }

        public static void addToZ(float z) {
            pos.z += z;
        }

        public static void setRotation(Vector3f rotation) {
            Camera.rotation = rotation;
        }

        public static Vector3f getRotation() {
            return rotation;
        }

        public static void setRotationX(float x) {
            rotation.x = x;
        }

        public static float getRotationX() {
            return rotation.x;
        }

        public static void addToRotationX(float x) {
            rotation.x += x;
        }

        public static void setRotationY(float y) {
            rotation.y = y;
        }

        public static float getRotationY() {
            return rotation.y;
        }

        public static void addToRotationY(float y) {
            rotation.y += y;
        }

        public static void setRotationZ(float z) {
            rotation.z = z;
        }

        public static float getRotationZ() {
            return rotation.z;
        }

        public static void addToRotationZ(float z) {
            rotation.z += z;
        }

        public static void setMaxLook(float maxLook) {
            Camera.maxLook = maxLook;
        }

        public static float getMaxLook() {
            return maxLook;
        }

        public static void setMouseSensitivity(float mouseSensitivity) {
            Camera.mouseSensitivity = mouseSensitivity;
        }

        public static float getMouseSensitivity() {
            return mouseSensitivity;
        }
    }
}
