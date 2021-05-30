package com.project;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import javax.media.j3d.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

public class Main extends JFrame {

    private            Box[][][]   piece; //an array of cube segments
    private     Appearance[][][]   appearancePiece; //tablica wyglądów poszczególnych segmentów (po jednym dla segmentu)
    private TransformGroup[][][]   transformPiece; //table of the appearance of individual segments (one for each segment)
    private    Transform3D[][][]   shiftPiece; //table of offsets of individual segments (one for a segment)
    private         String[][][]   rotations;
    private final float offset = 0.21f;
    /*a constant specifying the distance of each segment from the point (0, 0, 0) in the x, y and z axes
    (each segment has a character location (+/- offset, +/- offset, +/- offset))*/

    enum Plane{
        X,
        Y,
        Z
    }

    Main() {

        //Create a window and block the possibility of changing its size
        super("Rubics cube");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

        final SimpleUniverse universe; //the universe in which the scene is located
        final Canvas3D canvas; //the canvas on which the scene will be displayed
        final ViewingPlatform viewingPlatform; //the point from which the observer looks at the scene
        final BranchGroup scene; //main scene where all visible objects are located
        //The canvas object initialization
        canvas = new Canvas3D(config);
        canvas.setPreferredSize(new Dimension(800,600));

        add(canvas);
        pack();
        setVisible(true);

        //Creation of the universe and initialization of the viewingPlatform
        universe = new SimpleUniverse(canvas);
        viewingPlatform = universe.getViewingPlatform();
        viewingPlatform.setNominalViewingTransform();

        //Setting the position of the observer in relation to the center of the coordinate system
        Transform3D observerOffset = new Transform3D();
        observerOffset.set(new Vector3f(0.0f,0.0f,3.0f));

        universe.getViewingPlatform().getViewPlatformTransform().setTransform(observerOffset);

        //Creation and compilation of the scene
        scene = createScene(canvas, viewingPlatform);
        scene.compile();

        //Adding a scene to the universe
        universe.addBranchGraph(scene);

        //Add KeyListener
        universe.getCanvas().addKeyListener(new KeyListener() {

            /*Axes in the universe:
                       Y
                       ^
                       |
                       |
                       |_________> X
                      /
                    /
                  /
                 v
                Z
            */

            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    //Rotate the "right" face counterclockwise once about the X axis
                    case KeyEvent.VK_Q -> {
                        System.out.println("\nQ");
                        rotatePlane(1, 0, Plane.X, 1);
                    }
                    //Rotate the "left" face counterclockwise once about the X axis
                    case KeyEvent.VK_E -> {
                        System.out.println("\nE");
                        rotatePlane(-1, 0, Plane.X, 1);
                    }
                    //Rotate the "right" face clockwise once about the X axis
                    case KeyEvent.VK_W -> {
                        System.out.println("\nW");
                        rotatePlane(1, 0, Plane.X, -1);
                    }
                    //Rotate the "left" face clockwise once about the X axis
                    case KeyEvent.VK_R -> {
                        System.out.println("\nR");
                        rotatePlane(-1, 0, Plane.X, -1);
                    }
                    //Rotate the "top" face counterclockwise about the Y axis once
                    case KeyEvent.VK_A -> {
                        System.out.println("\nA");
                        rotatePlane(1, 1, Plane.Y, 1);
                    }
                    //Rotate the "bottom" face counterclockwise about the Y axis once
                    case KeyEvent.VK_D -> {
                        System.out.println("\nD");
                        rotatePlane(-1, 1, Plane.Y, 1);
                    }
                    //Rotate the "top" face clockwise around the X axis once
                    case KeyEvent.VK_S -> {
                        System.out.println("\nS");
                        rotatePlane(1, 1, Plane.Y, -1);
                    }
                    //Rotate the "bottom" face clockwise around the X axis once
                    case KeyEvent.VK_F -> {
                        System.out.println("\nF");
                        rotatePlane(-1, 1, Plane.Y, -1);
                    }
                    //Counterclockwise rotation of the "front" face once
                    case KeyEvent.VK_Z -> {
                        System.out.println("\nZ");
                        rotatePlane(1, 2, Plane.Z, 1);
                    }
                    //Counterclockwise rotation of the "back" face once
                    case KeyEvent.VK_C -> {
                        System.out.println("\nC");
                        rotatePlane(-1, 2, Plane.Z, 1);
                    }
                    //Rotate the "front" face clockwise around the X axis once
                    case KeyEvent.VK_X -> {
                        System.out.println("\nX");
                        rotatePlane(1, 2, Plane.Z, -1);
                    }
                    //Rotate the "back" face clockwise around the X axis once
                    case KeyEvent.VK_V -> {
                        System.out.println("\nV");
                        rotatePlane(-1, 2, Plane.Z, -1);
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
                //Not applicable yet
            }

            public void keyTyped(KeyEvent e) {
                //Not applicable yet
            }

        });

    }

    /**
    * Coordinates the rotation of the plane specified by plane according to the orientation specified by orientation.
    *
    * @param wall index identifying one of the 6 sides of the cube (top, bottom, front, back, left, right)
    * @param indR takes the value 1 when rotating the face on the positive side of the axis, and -1 when rotating the face on the negative side of the axis
    * @param plane plane (X, Y or Z)
    * @param orientation rotation orientation (-1 - clockwise, 1 - counterclockwise)
    * */
    public void rotatePlane(int wall, int indR, Plane plane, int orientation) {
        int[][] side = new int[4][3];
        int[][] dir  = new int[4][3];
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                for(int k = 0; k < 2; k++) {
                    Vector3f bufVect = new Vector3f();
                    shiftPiece[i][j][k].get(bufVect);
                    float[] coords = new float[3];
                    if(plane == Plane.X){
                        coords[0] = bufVect.getX();
                        coords[1] = bufVect.getY();
                        coords[2] = bufVect.getZ();
                    }
                    else if(plane == Plane.Y){
                        coords[0] = bufVect.getY();
                        coords[1] = bufVect.getX();
                        coords[2] = bufVect.getZ();
                    }
                    else if(plane == Plane.Z){
                        coords[0] = bufVect.getZ();
                        coords[1] = bufVect.getX();
                        coords[2] = bufVect.getY();
                    }
                    if(coords[0] == wall * offset) {
                        if(plane == Plane.X) {
                            if(coords[1] == offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, wall, 1, 1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, wall, -1, -1);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, wall, -1, 1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, wall, 1, -1);
                                    }
                                }
                            }
                            else if(coords[1] == -offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, wall, 1, -1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, wall, -1, 1);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, wall, -1, -1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, wall, 1, 1);
                                    }
                                }
                            }
                        }
                        else if(plane == Plane.Y) {
                            if(coords[1] == offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, -1, wall, -1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, 1, wall, 1);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, 1, wall, -1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, -1, wall, 1);
                                    }
                                }
                            }
                            else if(coords[1] == -offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, -1, wall, 1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, 1, wall, -1);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, 1, wall, 1);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, -1, wall, -1);
                                    }
                                }
                            }
                        }
                        else if(plane == Plane.Z) {
                            if(coords[1] == offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, 1, 1, wall);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[3], dir[3], i, j, k, -1, -1, wall);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, -1, 1, wall);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[1], dir[1], i, j, k, 1, -1, wall);
                                    }
                                }
                            }
                            else if(coords[1] == -offset) {
                                if(coords[2] == -offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, 1, -1, wall);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[2], dir[2], i, j, k, -1, 1, wall);
                                    }
                                }
                                else if(coords[2] == offset) {
                                    if(orientation == 1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, -1, -1, wall);
                                    }
                                    else if(orientation == -1) {
                                        setSideAndDir(side[0], dir[0], i, j, k, 1, 1, wall);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        addRotations(side, dir, indR, orientation);
    }

    /**
     * Calls the setSide and setDir functions.
     *
     * @param side an array storing indexes identifying the four segments of the wall being rotated
     * @param dir an array that stores the coordinate directions into which the segments are to be moved
     * @param i array value side[0]
     * @param j array value side[1]
     * @param k array value side[2]
     * @param l array value dir[0]
     * @param m array valuedir[1]
     * @param n array value dir[2]
     */
    void setSideAndDir(int[] side, int[] dir, int i, int j, int k, int l, int m, int n) {
        setSide(side, i, j, k);
        setDir(dir, l, m, n);
    }

    /**
     * Assigns values ​​to the side table rows.
     *
     * @param side an array storing indexes identifying the four segments of the wall being rotated
     * @param i array value side[0]
     * @param j array value side[1]
     * @param k array value side[2]
     */
    void setSide(int[] side, int i, int j, int k) {
        side[0] = i;
        side[1] = j;
        side[2] = k;
    }

    /**
     * Assigns values ​​to the rows of the dir table
     *
     * @param dir an array that stores the coordinate directions into which the segments are to be moved
     * @param l array value dir[0]
     * @param m array value dir[1]
     * @param n array value dir[2]
     */
    void setDir(int[] dir, int l, int m, int n) {
        dir[0] = l;
        dir[1] = m;
        dir[2] = n;
    }

    /**
     * Adds a rotation to the variable rotations and calls the rotatePiece function.
     *
     * @param side an array storing indexes identifying the four segments of the wall being rotated
     * @param dir an array that stores the coordinate directions into which the segments are to be moved
     * @param indR number identifying the plane (0 - X, 1 - Y, 2 - Z)
     * @param orientation rotation orientation (-1 - clockwise, 1 - counterclockwise)
     */
    void addRotations(int[][] side, int[][] dir, int indR, int orientation) {
        for(int i = 0; i < 4; i++) {
            if(orientation == 1) {
                if(indR == 0) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "X";
                }
                else if(indR == 1) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "Y";
                }
                else if(indR == 2) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "Z";
                }
            }
            else if(orientation == -1) {
                if(indR == 0) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "x";
                }
                else if(indR == 1) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "y";
                }
                else if(indR == 2) {
                    rotations[side[i][0]][side[i][1]][side[i][2]] += "z";
                }
            }
            rotatePiece(side[i][0], side[i][1], side[i][2], dir[i][0], dir[i][1], dir[i][2]);
        }
    }

    /**
     * Rotates the single segment specified by ind1, ind2, and ind3 in the direction specified by dirX, dirY, and dirZ.
     *
     * @param ind1 the first index identifying the segment
     * @param ind2 a second index identifying the segment
     * @param ind3 a third index identifying the segment
     * @param dirX the direction of the shift relative to the x axis; takes the value 1 (for x> 0) or -1 (for x <0)
     * @param dirY the direction of the shift in relation to the y axis; takes the value 1 (for y> 0) or -1 (for y <0)
     * @param dirZ the direction of the shift relative to the z axis; takes the value 1 (for z> 0) or -1 (for z <0)
     */
    public void rotatePiece(int ind1, int ind2, int ind3, int dirX, int dirY, int dirZ) {
        Transform3D rotXA = new Transform3D();
        Transform3D rotXC = new Transform3D();
        Transform3D rotYA = new Transform3D();
        Transform3D rotYC = new Transform3D();
        Transform3D rotZA = new Transform3D();
        Transform3D rotZC = new Transform3D();
        rotXA.rotX( Math.PI/2);
        rotXC.rotX(-Math.PI/2);
        rotYA.rotY( Math.PI/2);
        rotYC.rotY(-Math.PI/2);
        rotZA.rotZ( Math.PI/2);
        rotZC.rotZ(-Math.PI/2);
        shiftPiece[ind1][ind2][ind3].set(new Vector3f(dirX * offset, dirY * offset, dirZ * offset));

        System.out.print("\n" + ind1 + "" + ind2 + "" + ind3 + ". ");
        //Kolejność przekształceń!
        for(int j = rotations[ind1][ind2][ind3].length() - 1; j >= 0; j--) {
            if(rotations[ind1][ind2][ind3].charAt(j) == 'X') {
                shiftPiece[ind1][ind2][ind3].mul(rotXA);
                System.out.print("X");
            }
            else if(rotations[ind1][ind2][ind3].charAt(j) == 'x') {
                shiftPiece[ind1][ind2][ind3].mul(rotXC);
                System.out.print("x");
            }
            else if(rotations[ind1][ind2][ind3].charAt(j) == 'Y') {
                shiftPiece[ind1][ind2][ind3].mul(rotYA);
                System.out.print("Y");
            }
            else if(rotations[ind1][ind2][ind3].charAt(j) == 'y') {
                shiftPiece[ind1][ind2][ind3].mul(rotYC);
                System.out.print("y");
            }
            else if(rotations[ind1][ind2][ind3].charAt(j) == 'Z') {
                shiftPiece[ind1][ind2][ind3].mul(rotZA);
                System.out.print("Z");
            }
            else if(rotations[ind1][ind2][ind3].charAt(j) == 'z') {
                shiftPiece[ind1][ind2][ind3].mul(rotZC);
                System.out.print("z");
            }
        }

        transformPiece[ind1][ind2][ind3].setTransform(shiftPiece[ind1][ind2][ind3]);
    }

    /**
     * Loads textures from files specified by the address variable.
     *
     * @param address the path under which the texture is located
     */
    Appearance loadTexture(String address) {
        TextureLoader loader; //pozwala na ładowanie tekstur z plików jpg
        ImageComponent2D image; //do tego obiektu są przesyłane dane załadowanej tekstury
        Texture2D texture;
        loader = new TextureLoader(address, this);
        image = loader.getImage();
        texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0 ,image);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);
        Appearance appearanceTexture = new Appearance();
        appearanceTexture.setTexture(texture);
        return appearanceTexture;
    }

    /** Creates a single cube segment.
     * Indexing of individual segments takes place according to the principles described in the report.
     * There are segments with the following indexes: 000, 001, 010, 011, 100, 101, 110, 111.
     *
     * @param ind1 the first index identifying the segment
     * @param ind2 a second index identifying the segment
     * @param ind3 a third index identifying the segment
     * @param side1 the first of the colored walls of the segment
     * @param appearanceSide1 the appearance of the first colored wall
     * @param side2 the second of the colored walls of the segment
     * @param appearanceSide2 the appearance of the second colored wall
     * @param side3 the third of the colored walls of the segment
     * @param appearanceSide3 the appearance of the third of the colored walls
     * @param group the group to which the segment is added at the end
     * @param dirX the direction of the shift in relation to the x axis; takes the value 1 (for x> 0) or -1 (for x <0)
     * @param dirY the direction of the shift in relation to the y axis; takes the value 1 (for y> 0) or -1 (for y <0)
     * @param dirZ the direction of the shift relative to the z axis; takes the value 1 (for z> 0) or -1 (for z <0)
     */
    void createPiece(int ind1, int ind2, int ind3, int side1, Appearance appearanceSide1,
                     int side2, Appearance appearanceSide2, int side3, Appearance appearanceSide3,
                     BranchGroup group, int dirX, int dirY, int dirZ) {
        appearancePiece[ind1][ind2][ind3] = new Appearance();
        appearancePiece[ind1][ind2][ind3].setColoringAttributes(new ColoringAttributes
                (0.0f,0.0f,0.0f, ColoringAttributes.NICEST));
        piece[ind1][ind2][ind3] = new Box(0.2f, 0.2f, 0.2f,
                Box.GENERATE_TEXTURE_COORDS | Box.GENERATE_NORMALS, appearancePiece[ind1][ind2][ind3]);

        Shape3D pieceSide1 = piece[ind1][ind2][ind3].getShape(side1);
        pieceSide1.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        pieceSide1.setAppearance(appearanceSide1);
        Shape3D pieceSide2 = piece[ind1][ind2][ind3].getShape(side2);
        pieceSide2.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        pieceSide2.setAppearance(appearanceSide2);
        Shape3D pieceSide3 = piece[ind1][ind2][ind3].getShape(side3);
        pieceSide3.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        pieceSide3.setAppearance(appearanceSide3);

        shiftPiece[ind1][ind2][ind3] = new Transform3D();
        shiftPiece[ind1][ind2][ind3].set(new Vector3f(dirX * offset, dirY * offset, dirZ * offset));
        transformPiece[ind1][ind2][ind3] = new TransformGroup(shiftPiece[ind1][ind2][ind3]);
        transformPiece[ind1][ind2][ind3].addChild(piece[ind1][ind2][ind3]);
        transformPiece[ind1][ind2][ind3].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.addChild(transformPiece[ind1][ind2][ind3]);
    }

    /** Creates a scene.
     *
     * @param canvas Canvas, the scene is displayed on
     * @param viewingPlatform point from which the observer sees the scene
     */
    BranchGroup createScene(Canvas3D canvas, ViewingPlatform viewingPlatform) {

        BranchGroup group = new BranchGroup();
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0f, 0.0f, 0.0f), 10.0f);

        //Orbit
        OrbitBehavior orbit; //allows you to turn on the option to rotate the cube
        orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL | OrbitBehavior.STOP_ZOOM);
        orbit.setSchedulingBounds(bounds);
        viewingPlatform.setViewPlatformBehavior(orbit);

        //Background
        Color3f bgColor = new Color3f(Color.LIGHT_GRAY);
        Background bgNode = new Background(bgColor);
        bgNode.setApplicationBounds(bounds);
        group.addChild(bgNode);

        //Lights
        AmbientLight lightA = new AmbientLight();
        lightA.setInfluencingBounds(bounds);
        group.addChild(lightA);

        DirectionalLight lightD = new DirectionalLight();
        lightD.setInfluencingBounds(bounds);
        lightD.setDirection(new Vector3f(0.0f, 0.0f, -1.0f));
        lightD.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        group.addChild(lightD);

        //Tekstures
        Appearance appearanceBlueTexture = loadTexture("colors/blue.jpg");
        Appearance appearanceGreenTexture = loadTexture("colors/green.jpg");
        Appearance appearanceOrangeTexture = loadTexture("colors/orange.jpg");
        Appearance appearanceRedTexture = loadTexture("colors/red.jpg");
        Appearance appearanceWhiteTexture = loadTexture("colors/white.jpg");
        Appearance appearanceYellowTexture = loadTexture("colors/yellow.jpg");
        Appearance appearanceLogoTexture = loadTexture("colors/logo.jpg");

        //A black, immobile cube inside the cube to give it a more aesthetic appearance
        Appearance appearanceCentre = new Appearance();
        appearanceCentre.setColoringAttributes(new ColoringAttributes(0.0f,0.0f,0.0f, ColoringAttributes.NICEST));
        Box centre = new Box(0.25f, 0.25f, 0.25f, Box.GENERATE_TEXTURE_COORDS | Box.GENERATE_NORMALS, appearanceCentre);
        Transform3D shiftCentre = new Transform3D();
        shiftCentre.set(new Vector3f(0.0f, 0.0f, 0.0f));
        TransformGroup transformCentre = new TransformGroup(shiftCentre);
        transformCentre.addChild(centre);
        group.addChild(transformCentre);

        //Initialization of arrays related to segments
        piece           = new Box[2][2][2];
        appearancePiece = new Appearance[2][2][2];
        transformPiece  = new TransformGroup[2][2][2];
        shiftPiece      = new Transform3D[2][2][2];
        rotations       = new String[2][2][2];

        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                for(int k = 0; k < 2; k++) {
                    rotations[i][j][k] = "";
                }
            }
        }

        //Create segments
        createPiece(0, 0, 0, Box.TOP, appearanceLogoTexture,
                Box.LEFT, appearanceBlueTexture, Box.BACK, appearanceRedTexture, group, -1, 1, -1);
        createPiece(0, 0, 1, Box.TOP, appearanceWhiteTexture,
                Box.RIGHT, appearanceGreenTexture, Box.BACK, appearanceRedTexture, group, 1, 1, -1);
        createPiece(0, 1, 0, Box.TOP, appearanceWhiteTexture,
                Box.LEFT, appearanceBlueTexture, Box.FRONT, appearanceOrangeTexture, group, -1, 1, 1);
        createPiece(0, 1, 1, Box.TOP, appearanceWhiteTexture,
                Box.RIGHT, appearanceGreenTexture, Box.FRONT, appearanceOrangeTexture, group, 1, 1, 1);
        createPiece(1, 0, 0, Box.BOTTOM, appearanceYellowTexture,
                Box.LEFT, appearanceBlueTexture, Box.BACK, appearanceRedTexture, group, -1, -1, -1);
        createPiece(1, 0, 1, Box.BOTTOM, appearanceYellowTexture,
                Box.RIGHT, appearanceGreenTexture, Box.BACK, appearanceRedTexture, group, 1, -1, -1);
        createPiece(1, 1, 0, Box.BOTTOM, appearanceYellowTexture,
                Box.LEFT, appearanceBlueTexture, Box.FRONT, appearanceOrangeTexture, group, -1, -1, 1);
        createPiece(1, 1, 1, Box.BOTTOM, appearanceYellowTexture,
                Box.RIGHT, appearanceGreenTexture, Box.FRONT, appearanceOrangeTexture, group, 1, -1, 1);

        return group;
    }

    public static void main(String[] args) {
        new Main();
    }

}
