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
    
    private TransformGroup tgFloor, tgBase, tgArm1, tgArm2, tgArm3;                     // transform groups    
    private Transform3D trBase, trArm1, trArm2, trArm3;                           // transforms
    private Transform3D trArm1rot, trArm2rot, trArm3rot;
    
    private RotationInterpolator rotJoint1, rotJoint2, rotJoint3;             // rotation interpolators    
    private float joint1, joint2, joint3 = 0.0f;                              // rotation angles    
    static final double move = (Math.PI) / 40;
    Alpha alpha1 = new Alpha(-1, 5000);
    Alpha alpha2 = new Alpha(-1, 5000);
    Alpha alpha3 = new Alpha(-1, 5000);
    
    Box floor, arm1, arm2;                              // robot parts and floor
    Cylinder base, arm3;
    
    Appearance apFloor, apRobot;                          // appearances
    
    Color3f darkGray = new Color3f(0.1f, 0.1f, 0.1f);     // colors
    Color3f lightGray = new Color3f(0.2f, 0.2f, 0.2f); 
    
    Material mat1 = new Material(darkGray, darkGray, darkGray, darkGray, 6f);       // materials
    Material mat2 = new Material(lightGray, lightGray, lightGray, lightGray, 6f);
    
    public SCARAjava3d() {                              // constructor        
        setLayout(new BorderLayout());
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas.addKeyListener(this);
        add(BorderLayout.CENTER,canvas);        
        universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();        
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
        tgFloor = new TransformGroup();
        tgFloor.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scene.addChild(tgFloor);        
        apFloor = new Appearance();
        apFloor.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        apFloor.setMaterial(mat2);
        floor = new Box(1.2f, 0.01f, 1.2f, apFloor);
        tgFloor.addChild(floor);
        
        trBase = new Transform3D();
        trBase.set(new Vector3f(0f, 0.25f, 0f));
        tgBase = new TransformGroup(trBase);
        tgBase.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgFloor.addChild(tgBase);
        apRobot = new Appearance();
        apRobot.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        apRobot.setMaterial(mat1);
        base = new Cylinder(0.06f, 0.5f, apRobot);        
        tgBase.addChild(base);
        
        tgArm1 = new TransformGroup();
        tgArm1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D axis1 = new Transform3D();
        axis1.set(new Vector3f(0f, 1f, 0f));
        rotJoint1 = new RotationInterpolator(alpha1, tgArm1, axis1, 0, 0);
        rotJoint1.setSchedulingBounds(bounds);
        tgFloor.addChild(tgArm1);
        trArm1 = new Transform3D();
        trArm1.set(new Vector3f(0.22f, 0.5f, 0f));
        arm1 = new Box (0.3f, 0.02f, 0.06f, apRobot);
        TransformGroup tgArm1Translation = new TransformGroup(trArm1);
        tgArm1Translation.addChild(arm1);
        tgArm1.addChild(rotJoint1);
        tgArm1.addChild(tgArm1Translation);      

        
    }

    @Override
    public void keyPressed(KeyEvent key) {
            if (key.getKeyCode() == KeyEvent.VK_LEFT) {
            joint1 += move;            
            }
            if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
            joint1 -= move;            
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
            rotJoint1.setMinimumAngle(joint1);
            rotJoint1.setMaximumAngle(joint1);
        }
    }
}
