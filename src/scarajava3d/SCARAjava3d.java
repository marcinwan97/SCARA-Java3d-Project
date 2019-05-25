package scarajava3d;
/**
 *
 * @author Marcin Wankiewicz and Lukasz Wroblewski
 */

import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.BranchGroup;
import com.sun.j3d.utils.geometry.*;
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
    
    Appearance apFloor, apRobot;                          // appearances
    
    Color3f darkGray = new Color3f(0.1f, 0.1f, 0.1f);     // colors
    Color3f lightGray = new Color3f(0.2f, 0.2f, 0.2f); 
    
    Material mat1 = new Material(darkGray, darkGray, darkGray, darkGray, 6f);       // materials
    Material mat2 = new Material(lightGray, lightGray, lightGray, lightGray, 6f);
    
    TransformGroup tgFloor, tgArm1, tgArm2, tgArm3;
    Transform3D arm1Position, joint1Position, arm2Position, joint2Position, arm3Position;
    
    private float angle1 = 0f, angle2 = 0f, angle3 = 0f, height = -0.2f;
    private double move = (Math.PI) / 60;
    
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
     
        MouseRotate mouseRotate = new MouseRotate(tgFloor);                  // mouse functionality: LMB rotation
        mouseRotate.setSchedulingBounds(bounds);
        tgFloor.addChild(mouseRotate);
        MouseTranslate mouseTranslate = new MouseTranslate(tgFloor);         // RMB translation
        mouseTranslate.setSchedulingBounds(bounds);
        tgFloor.addChild(mouseTranslate);
        MouseZoom mouseZoom = new MouseZoom(tgFloor);                        // MMB zoom
        mouseZoom.setSchedulingBounds(bounds);
        tgFloor.addChild(mouseZoom);
        
        Color3f lightColor = new Color3f(1f, 1f, 1f);                       // directional light
        Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
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
        apFloor.setMaterial(mat2);
        Box floor = new Box(2f, 0.01f, 2f, apFloor);
        tgFloor = new TransformGroup();
        tgFloor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgFloor.addChild(floor);
        scene.addChild(tgFloor);
        
        apRobot = new Appearance();                                         // base
        apRobot.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        apRobot.setMaterial(mat1);
        Cylinder base = new Cylinder(0.12f, 0.8f, apRobot);
        Transform3D basePosition = new Transform3D();
        basePosition.set(new Vector3f(0f, 0.4f, 0f));
        TransformGroup tgBase = new TransformGroup(basePosition);
        tgBase.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgBase.addChild(base);
        tgFloor.addChild(tgBase);
        
        Box arm1 = new Box (0.3f, 0.05f, 0.12f, apRobot);                   // arm1
        arm1Position = new Transform3D();
        tgArm1 = new TransformGroup(arm1Position);
        tgArm1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm1.addChild(arm1);
        tgBase.addChild(tgArm1);
        
        Cylinder joint1 = new Cylinder (0.12f, 0.2f, apRobot);              // joint1
        joint1Position = new Transform3D();
        joint1Position.set(new Vector3f(0.3f, 0.05f, 0f));
        TransformGroup tgJoint1 = new TransformGroup(joint1Position);
        tgJoint1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgJoint1.addChild(joint1);
        tgArm1.addChild(tgJoint1);
        
        Box arm2 = new Box (0.3f, 0.05f, 0.12f, apRobot);                   // arm2
        arm2Position = new Transform3D();
        tgArm2 = new TransformGroup(arm2Position);
        tgArm2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm2.addChild(arm2);
        tgJoint1.addChild(tgArm2);
        
        Cylinder joint2 = new Cylinder (0.12f, 0.1f, apRobot);              // joint2
        joint2Position = new Transform3D();
        joint2Position.set(new Vector3f(0.3f, 0f, 0f));
        TransformGroup tgJoint2 = new TransformGroup(joint2Position);        
        tgJoint2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgJoint2.addChild(joint2);
        tgArm2.addChild(tgJoint2);
        
        Cylinder arm3 = new Cylinder (0.05f, 0.7f, apRobot);                  // arm3
        arm3Position = new Transform3D();
        arm3Position.set(new Vector3f(0f, -0.2f, 0f));
        tgArm3 = new TransformGroup(arm3Position);
        tgArm3.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArm3.addChild(arm3);
        tgJoint2.addChild(tgArm3);

        
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
            if(height < 0.25f) height += 0.01;
        }
        if (key.getKeyCode() == KeyEvent.VK_DOWN) {
            if(height > -0.25f) height -= 0.01;
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
        }
    }
}
