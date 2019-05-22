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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.j3d.*;
import javax.vecmath.*;

public class SCARAjava3d extends Frame{

    public SCARAjava3d() {                              // constructor        
        setLayout(new BorderLayout());
        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add(BorderLayout.CENTER,canvas);        
        SimpleUniverse universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();        
        BranchGroup scene = new BranchGroup();       
        TransformGroup tgMain = new TransformGroup();
        tgMain.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scene.addChild(tgMain);        
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
     
        MouseRotate mouseRotate = new MouseRotate(tgMain);                  // mouse functionality: LMB rotation
        mouseRotate.setSchedulingBounds(bounds);
        tgMain.addChild(mouseRotate);
        MouseTranslate mouseTranslate = new MouseTranslate(tgMain);         // RMB translation
        mouseTranslate.setSchedulingBounds(bounds);
        tgMain.addChild(mouseTranslate);
        MouseZoom mouseZoom = new MouseZoom(tgMain);                        // MMB zoom
        mouseZoom.setSchedulingBounds(bounds);
        tgMain.addChild(mouseZoom);
        
        Color3f lightColor = new Color3f(1f, 1f, 1f);                       // directional light
        Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
        light.setInfluencingBounds(bounds);
        scene.addChild(light);
       
        Cylinder cylinder = new Cylinder(0.4f, 0.2f);
        tgMain.addChild(cylinder);
        
        universe.addBranchGraph(scene);                                     // add everything to universe
        
        setSize(1024,768);
        setTitle("SCARA - by Marcin Wankiewicz and Lukasz Wroblewski");
        setVisible(true);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        }
        );
    }

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        SCARAjava3d thisObject = new SCARAjava3d();
    }
    
}
