package scarajava3d;
/**
 *
 * @author Marcin Wankiewicz and Lukasz Wroblewski
 */

import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.BranchGroup;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;
import java.awt.Frame;
import java.awt.Label;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

public class SCARAjava3d extends Frame implements KeyListener {

    private Timer timer = new Timer();
    private BranchGroup scene;
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private BoundingSphere bounds;
    
    Appearance apFloor, apRobot, apCrate;                       // appearances
    
    Color3f darkGray = new Color3f(0.2f, 0.2f, 0.2f);     // colors
    Color3f lightGray = new Color3f(0.4f, 0.4f, 0.4f);    
    Color3f white = new Color3f(1f, 1f, 1f);  
    
    Texture floorTexture = new TextureLoader("images/rock.jpg", this).getTexture();     // textures
    Texture robotTexture = new TextureLoader("images/robot.jpg", this).getTexture();
    Texture crateTexture = new TextureLoader("images/crate.png", this).getTexture();
    
    Material mat1 = new Material(darkGray, darkGray, darkGray, darkGray, 6f);       // materials
    Material mat2 = new Material(lightGray, lightGray, lightGray, lightGray, 6f);
    
    TransformGroup tgFloor, tgArm1, tgArm2, tgArm3, tgCrate;
    Transform3D arm1Position, joint1Position, arm2Position, joint2Position, arm3Position, cratePosition;
    
    PositionInterpolator fallInterpolator;
    
    private float angle1 = 0f, angle2 = 0f, angle3 = 0f, height = -0.2f;
    private double move = (Math.PI) / 80;
    private boolean moveCrate = false;
    private int upSteps = 0, crateUpSteps = 0;
    
    public SCARAjava3d() {                              // constructor        
        setLayout(new BorderLayout());
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas.addKeyListener(this);
        add(BorderLayout.CENTER,canvas);        
        universe = new SimpleUniverse(canvas);
        Transform3D observatorMove = new Transform3D();
        observatorMove.set(new Vector3f(0f,1f,5.0f));
        universe.getViewingPlatform().getViewPlatformTransform().setTransform(observatorMove);   
        scene = new BranchGroup();
        bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        
        robotBuilder(); 

        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE);      // mouse functionality
        orbit.setSchedulingBounds(new BoundingSphere());
        universe.getViewingPlatform().setViewPlatformBehavior(orbit);
                               
        Vector3f lightDirection = new Vector3f(-10.0f, -10.0f, -10.0f);     // directional light
        DirectionalLight light = new DirectionalLight(white, lightDirection);
        light.setInfluencingBounds(bounds);
        scene.addChild(light);   

        
        universe.addBranchGraph(scene);                                     // add everything to universe
        timer.scheduleAtFixedRate(new Movement(), 10, 10);
        
        setSize(1024,768);
        setTitle("SCARA - by Marcin Wankiewicz and Lukasz Wroblewski");
        setVisible(true);
        setResizable(false);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        }
        );
    }

    private void robotBuilder()
    {
        apFloor = new Appearance();                                         // floor
        apFloor.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        apFloor.setTextureAttributes(texAttr);
        apFloor.setTexture(floorTexture);
        apFloor.setMaterial(mat2);
        Box floor = new Box(2f, 0.01f, 2f, Box.GENERATE_TEXTURE_COORDS+Box.GENERATE_NORMALS, apFloor);
        tgFloor = new TransformGroup();
        tgFloor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgFloor.addChild(floor);
        scene.addChild(tgFloor);
        
        apRobot = new Appearance();                                         // base
        apRobot.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        apRobot.setTextureAttributes(texAttr);
        apRobot.setTexture(robotTexture);
        apRobot.setMaterial(mat1);
        Cylinder base = new Cylinder(0.12f, 0.8f, Cylinder.GENERATE_TEXTURE_COORDS+Cylinder.GENERATE_NORMALS, apRobot);
        Transform3D basePosition = new Transform3D();
        basePosition.set(new Vector3f(0f, 0.4f, 0f));
        TransformGroup tgBase = new TransformGroup(basePosition);
        tgBase.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgBase.addChild(base);
        tgFloor.addChild(tgBase);
        
        Box arm1 = new Box (0.3f, 0.05f, 0.12f, Box.GENERATE_TEXTURE_COORDS+Box.GENERATE_NORMALS, apRobot);                   // arm1
        arm1Position = new Transform3D();
        tgArm1 = new TransformGroup(arm1Position);
        tgArm1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm1.addChild(arm1);
        tgBase.addChild(tgArm1);
        
        Cylinder joint1 = new Cylinder (0.12f, 0.2f, Cylinder.GENERATE_TEXTURE_COORDS+Cylinder.GENERATE_NORMALS, apRobot);              // joint1
        joint1Position = new Transform3D();
        joint1Position.set(new Vector3f(0.3f, 0.05f, 0f));
        TransformGroup tgJoint1 = new TransformGroup(joint1Position);
        tgJoint1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgJoint1.addChild(joint1);
        tgArm1.addChild(tgJoint1);
        
        Box arm2 = new Box (0.3f, 0.05f, 0.12f, Box.GENERATE_TEXTURE_COORDS+Box.GENERATE_NORMALS, apRobot);                   // arm2
        arm2Position = new Transform3D();
        tgArm2 = new TransformGroup(arm2Position);
        tgArm2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm2.addChild(arm2);
        tgJoint1.addChild(tgArm2);
        
        Cylinder joint2 = new Cylinder (0.12f, 0.1f, Cylinder.GENERATE_TEXTURE_COORDS+Cylinder.GENERATE_NORMALS, apRobot);              // joint2
        joint2Position = new Transform3D();
        joint2Position.set(new Vector3f(0.3f, 0f, 0f));
        TransformGroup tgJoint2 = new TransformGroup(joint2Position);        
        tgJoint2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgJoint2.addChild(joint2);
        tgArm2.addChild(tgJoint2);
        
        Cylinder arm3 = new Cylinder (0.05f, 0.7f, Cylinder.GENERATE_TEXTURE_COORDS+Cylinder.GENERATE_NORMALS, apRobot);                  // arm3
        arm3Position = new Transform3D();
        arm3Position.set(new Vector3f(0f, -0.2f, 0f));
        tgArm3 = new TransformGroup(arm3Position);
        tgArm3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm3.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tgArm3.addChild(arm3);
        tgJoint2.addChild(tgArm3);

        apCrate = new Appearance();                                         // crate
        apCrate.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        apCrate.setTextureAttributes(texAttr);
        apCrate.setTexture(crateTexture);
        apCrate.setMaterial(mat2);
        Box crate = new Box(0.15f, 0.15f, 0.15f, Box.GENERATE_TEXTURE_COORDS+Box.GENERATE_NORMALS, apCrate);
        cratePosition = new Transform3D();
        cratePosition.set(new Vector3f(0.8f, 0.165f, 0.5f));
        tgCrate = new TransformGroup(cratePosition);
        tgCrate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgCrate.addChild(crate);
        tgFloor.addChild(tgCrate);
    }

    @Override
    public void keyPressed(KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_LEFT) {
            angle1 += move;
        }
        if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
            angle1 -= move;
        }
        if (key.getKeyCode() == KeyEvent.VK_A) {
            if(angle2 < 2.6f)angle2 += move;
        }
        if (key.getKeyCode() == KeyEvent.VK_D) {
            if(angle2 > -2.6f) angle2 -= move;
        }
        if (key.getKeyCode() == KeyEvent.VK_UP) {
            if(height < 0.2f) {
                height += 0.01;
                upSteps++;
                if(moveCrate) crateUpSteps++;
            }
        }
        if (key.getKeyCode() == KeyEvent.VK_DOWN) {
            if(height > -0.2f) {
                height -= 0.01;
                upSteps--;
                if(moveCrate) crateUpSteps--;
            }
        }
        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
            moveCrate = !moveCrate;
            if (moveCrate) crateUpSteps = upSteps;
        }
        if (key.getKeyCode() == KeyEvent.VK_C)
        {
        //    System.out.print(crateHeight);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent key) {
    }

    @Override
    public void keyTyped(KeyEvent key) {
    }
    
    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        SCARAjava3d thisObject = new SCARAjava3d();
    }
    
    private class Movement extends TimerTask{
        @Override
        public void run() {
            Transform3D tempRotation = new Transform3D();
            Transform3D tempPosition = new Transform3D();
            
            tempPosition.set(new Vector3f(0.3f, 0.35f, 0f));
            tempRotation.rotY(angle1);
            tempRotation.mul(tempPosition);
            tgArm1.setTransform(tempRotation);
            
            tempPosition.set(new Vector3f(0.3f, 0.05f, 0f));
            tempRotation.rotY(angle2);
            tempRotation.mul(tempPosition);
            tgArm2.setTransform(tempRotation);  
            
            tempPosition.set(new Vector3f(0f, height, 0f));
            tempRotation.rotY(angle3);
            tempRotation.mul(tempPosition);
            tgArm3.setTransform(tempRotation);
            
            tgArm3.getLocalToVworld(arm3Position);
            Transform3D positionFix = new Transform3D();
            positionFix.set(new Vector3f(0f, height-0.5f, 0f));
            arm3Position.mul(positionFix);
            if(moveCrate)
            {
                tgCrate.setTransform(arm3Position);        
            }
            else if(crateUpSteps > 0)
            {
                positionFix.setTranslation(new Vector3f(0f, crateUpSteps*0.01f-height-0.2f, 0f));
                arm3Position.mul(positionFix);
                tgCrate.setTransform(arm3Position);
                crateUpSteps--;
            }
        }
    }
}
