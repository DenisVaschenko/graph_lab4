package com.example;
import com.sun.j3d.utils.universe.*;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RubiksCube3D extends Frame {
    private class Cube{
        TransformGroup rotationGroup;
        RotationBehavior rotator;
        TransformGroup cubeGroup;
        public Cube(TransformGroup cubeGroup, TransformGroup rotationGroup){
            this.rotationGroup = rotationGroup;
            this.cubeGroup = cubeGroup;
            rotationGroup.addChild(cubeGroup);
            rotationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            rotationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            cubeGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            rotator = new RotationBehavior(rotationGroup);
            rotator.setSchedulingBounds(new BoundingSphere());
            rotationGroup.addChild(rotator);
            
        }
        public void rotate(int axis, float direction){
            rotator.rotate(axis, direction);
        }
        public boolean isInFace(int[] faceMult){
            // Перевірка, чи куб потрапляє в площину
            Transform3D t3d = new Transform3D();
            cubeGroup.getTransform(t3d);
            Transform3D t3d2 = new Transform3D();
            rotationGroup.getTransform(t3d2);
            t3d2.mul(t3d);
            Vector3d pos = new Vector3d();
            t3d2.get(pos);
            return pos.x* faceMult[0] + pos.y * faceMult[1] + pos.z * faceMult[2] - 0.1 > 0;
        }
    }
    private BranchGroup scene;
    private SimpleUniverse universe;
    private int[][] faceChecker = new int[][]
    {
        new int[] {1, 0, 0},
        new int[] {-1, 0, 0},
        new int[] {0, 1, 0},
        new int[] {0, -1, 0},
        new int[] {0, 0, 1},
        new int[] {0, 0, -1},
    };
    private ArrayList<Cube> cubies= new ArrayList<>();
    public RubiksCube3D() {
        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add("Center", canvas);
        universe = new SimpleUniverse(canvas);
        scene = createSceneGraph();
        scene.addChild(createLightGroup());

        universe.getViewingPlatform().setNominalViewingTransform();
        OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
        orbit.setSchedulingBounds(new BoundingSphere());
        universe.getViewingPlatform().setViewPlatformBehavior(orbit);
        scene.compile();
        universe.addBranchGraph(scene);
        setSize(800, 800);
        setVisible(true);
    }

    private BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();
        // Група об'єктів
        TransformGroup allCubies = new TransformGroup();
        allCubies.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root.addChild(allCubies);

        // Розміри одного маленького кубика
        float size = 0.1f;
        float gap = 0.02f;

        // Побудова 3x3x3 кубиків
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Box cube = new Box(size, size, size,Box.GENERATE_TEXTURE_COORDS | Box.GENERATE_NORMALS, null);
                    setColors(cube, x, y, z);
                    Transform3D transform = new Transform3D();
                    transform.setTranslation(new Vector3f(
                        x * (2 * size + gap),
                        y * (2 * size + gap),
                        z * (2 * size + gap)
                    ));
                    TransformGroup tg = new TransformGroup(transform);
                    tg.addChild(cube);
                    tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                    TransformGroup rotateGroup = new TransformGroup();
                    allCubies.addChild(rotateGroup);
                    cubies.add(new Cube(tg, rotateGroup));
                }
            }
        }
        
        animateRandomRotation();
        return root;
    }
    private TransformGroup createLightGroup(){
        TransformGroup lightTG = new TransformGroup();
    
        // Світло з шести сторін
        lightTG.addChild(createPointLight( 1, 0, 0));
        lightTG.addChild(createPointLight(-1, 0, 0));
        lightTG.addChild(createPointLight(0,  1, 0));
        lightTG.addChild(createPointLight(0, -1, 0));
        lightTG.addChild(createPointLight(0, 0,  1));
        lightTG.addChild(createPointLight(0, 0, -1));
    
        return lightTG;
    }
    private PointLight createPointLight(float x, float y, float z){
        PointLight light = new PointLight();
        light.setColor(new Color3f(1f, 1f, 1f)); // Біле світло
        light.setAttenuation(1.0f, 0.0f, 0.0f); // Без затухання (константна яскравість)
        light.setPosition(new Point3f(x, y, z));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        return light;
    }
    private void setColors(Box box, int x, int y, int z) {
        box.getShape(Box.LEFT).setAppearance(x == -1? createColoredAppearance(Color.RED) : createColoredAppearance(Color.BLACK));
        box.getShape(Box.RIGHT).setAppearance(x == 1? createColoredAppearance(new Color(255, 145,0)) : createColoredAppearance(Color.BLACK));
        box.getShape(Box.BOTTOM).setAppearance(y == -1? createColoredAppearance(Color.YELLOW) : createColoredAppearance(Color.BLACK));
        box.getShape(Box.TOP).setAppearance(y == 1? createColoredAppearance(Color.WHITE) : createColoredAppearance(Color.BLACK));
        box.getShape(Box.FRONT).setAppearance(z == 1? createColoredAppearance(Color.BLUE) : createColoredAppearance(Color.BLACK));
        box.getShape(Box.BACK).setAppearance(z == -1? createColoredAppearance(Color.GREEN) : createColoredAppearance(Color.BLACK));
    }
    private Appearance createColoredAppearance(Color color) {
        Appearance appearance = new Appearance();
    
        // Задаємо матеріал для відблиску
        Material material = new Material();
        material.setDiffuseColor(new Color3f(color));
        material.setSpecularColor(new Color3f(Color.WHITE));
        material.setShininess(64f);
        appearance.setMaterial(material);
    
        // (Опціонально) Вимикаємо полігональне освітлення
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE); // показати всі грані
        appearance.setPolygonAttributes(pa);
    
        return appearance;
    }
    private void animateRandomRotation() {
        // Тема випадкове обертання для кожної грані
        Random rand = new Random();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int randFace = rand.nextInt(6);
                float angle = (float)Math.PI/2 * (rand.nextInt(2) * 2 - 1);
                for (Cube cube : cubies){
                    if (cube.isInFace(faceChecker[randFace])) cube.rotate(randFace / 2, angle);
                }
            }
        }, 0, 4000);
    }
    public static void main(String[] args) {
        new RubiksCube3D();
    }
}