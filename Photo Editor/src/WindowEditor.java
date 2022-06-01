//Maxim
/*
Invert Filter - inverts colors on image
Greyscale Filter - converts colors on image to greyscale
Pixelate Filter - pixelates the image
Scribble tool - lets you scribble on the screen, following your mouse
Fill tool - fills in all pixels of the same color to the designated color
Color button - lets you chose a color for the tools to use
Undo button - lets you undo
Redo button - lets you redo
Brush size slider - lets you change the size of the scribble tool lines
Pixel size slider - lets you change how pixelated the pixelate filter changes the image
Open button - allows user to open an image file
Save button - allows user to save an image file
New button - opens a blank image
Revert button - reverts the image to its unedited version


 */

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/* This program creates a custom class that extends JButton and combines
    the button appearance and behavior into one class.
*/

class WindowEditor {


    public static void main(String[] args) {
        new WindowEditor();
    }
    BufferedImage bufferedImage;
    BufferedImage originalImage;
    PhotoCanvas canvas;
    JFrame myJFrame = new JFrame("My Window");
    Color color = Color.BLACK;
    int brushSize = 10;
    int pixelSize = 10;
    ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();
    int imageIndex =0;
    int imageY=0;
    int imageX=0;

    WindowEditor() {
        System.setProperty("apple.laf.useScreenMenuBar","true");


        JMenuBar menuBar = new JMenuBar();
        JMenu localFilters = new JMenu("Local Filters");
        JMenu globalFilters = new JMenu("Global Filters");

        localFilters.add(new LocalFilterButton("Invert Filter",1));
        localFilters.add(new LocalFilterButton("GreyScale Filter",2));

        globalFilters.add(new GlobalFilterButton("Pixelate",1));

        menuBar.add(localFilters);
        menuBar.add(globalFilters);






        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BorderLayout());


        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        NewButton newButton = new NewButton();
        sidePanel.add(newButton);
        sidePanel.add(new OpenFileButton());
        sidePanel.add(new SaveFileButton());

        ToolButton scribbleButton = new ToolButton("Scribble Tool");
        ToolButton fillButton = new ToolButton("Fill Tool");
        scribbleButton.otherButton=fillButton;
        fillButton.otherButton=scribbleButton;
        sidePanel.add(scribbleButton);
        sidePanel.add(fillButton);

        sidePanel.add(new ColorChooserButton());
        sidePanel.add(new UndoButton());
        sidePanel.add(new RedoButton());
        myPanel.add(sidePanel, BorderLayout.WEST);

        canvas = new PhotoCanvas(300, 200);
        myPanel.add(canvas, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom,BoxLayout.X_AXIS));

        JPanel brushPanel = new JPanel();
        brushPanel.setLayout(new BoxLayout(brushPanel,BoxLayout.Y_AXIS));
        brushPanel.add(new JLabel("Brush Size"));
        brushPanel.add(new BrushSizeSlider());

        JPanel pixelPanel = new JPanel();
        pixelPanel.setLayout(new BoxLayout(pixelPanel,BoxLayout.Y_AXIS));
        pixelPanel.add(new JLabel("Pixel Size"));
        pixelPanel.add(new PixelSlider());


        bottom.add(brushPanel);
        bottom.add(pixelPanel);
        myPanel.add(bottom, BorderLayout.SOUTH);


        myJFrame.add(myPanel);
        myJFrame.setJMenuBar(menuBar);


        myJFrame.setSize(1000, 1000);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
        myJFrame.pack();
        newButton.actionPerformed(null);
        //i wasn't sure what would happen if actionEvent was null, but this happened to work
    }



    public void trackChanges(){
        int count=imageIndex-1;
        while (count>=0){
            imageList.remove(count);
            count--;
        }
        imageList.add(0,copyBufferedImage(bufferedImage));
        imageIndex=0;
    }

    public int canvasToImage(int value, int type){
        //type 0 = width
        //type 1 = height
        //type 2 = brushStroke

        int imageWidth=bufferedImage.getWidth();
        int imageHeight=bufferedImage.getHeight();
        double imageRatio = (double)imageWidth/(double)imageHeight;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        double canvasRatio = (double)canvasWidth/(double)canvasHeight;

        if (imageRatio>canvasRatio){
            canvasHeight=(int)((double)canvasWidth*(1/imageRatio));
        }
        else if (imageRatio<=canvasRatio){
            canvasWidth=(int)((double)canvasHeight*imageRatio);
        }

        if (type==0){ //width
            return (value-imageX)*imageWidth/canvasWidth;
        }
        else if (type==1){ //height
            return (value-imageY)*imageHeight/canvasHeight;
        }
        //stroke
        return value*imageWidth/canvasWidth;  //so that stroke size doesn't change with canvas size and is constant


    }
    public BufferedImage copyBufferedImage(BufferedImage image){
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics pen = newImage.getGraphics();
        pen.drawImage(image,0,0,null);
        return newImage;
    }



    class BrushSizeSlider extends JSlider implements ChangeListener {
        BrushSizeSlider(){
            super(1,50,10);
            this.addChangeListener(this);
        }
        public void stateChanged(ChangeEvent e){
            brushSize = this.getValue();
        }
    }

    class UndoButton extends JButton implements ActionListener{
        UndoButton(){
            super("Undo");
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            if (imageList.size()<=1 || imageIndex>=imageList.size()-1) {
                return;
            }
            imageIndex++;
            bufferedImage = copyBufferedImage(imageList.get(imageIndex));
            canvas.clear();
            canvas.draw();
        }
    }
    class RedoButton extends JButton implements ActionListener{
        RedoButton(){
            super("Redo");
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            if (imageList.size()<=1 || imageIndex<=0) {
                return;
            }
            imageIndex--;
            bufferedImage = copyBufferedImage(imageList.get(imageIndex));
            canvas.clear();
            canvas.draw();

        }
    }


    class PhotoCanvas extends ImageCanvas {

        PhotoCanvas(int width, int height) {
            super(width, height);
        }

        public void draw() {
            if (bufferedImage==null){
                return;
            }
            int imageWidth=bufferedImage.getWidth();
            int imageHeight=bufferedImage.getHeight();
            double imageRatio = (double)imageWidth/(double)imageHeight;

            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            double canvasRatio = (double)canvasWidth/(double)canvasHeight;

            Graphics pen = this.getPen();
            //System.out.println("Image: "+imageRatio+"   Panel: "+panelRatio);

            if (imageRatio>canvasRatio){
                int newHeight = (int)(canvasWidth*(1/imageRatio));
                imageX=0;
                imageY=(canvasHeight-newHeight)/2;
                pen.drawImage(bufferedImage,imageX,imageY,canvasWidth,newHeight, null);
                pen.drawRect(imageX,imageY,canvasWidth,(int)(canvasWidth*(1/imageRatio)));
                }
            else if(imageRatio<=canvasRatio){
                int newWidth = (int)(canvasHeight*imageRatio);
                imageX=(canvasWidth-newWidth)/2;
                imageY=0;
                pen.drawImage(bufferedImage,imageX,imageY,newWidth,canvasHeight,null);
                pen.drawRect(imageX,imageY,(int)(canvasHeight*imageRatio),canvasHeight);
            }
            canvas.display();

        }

        public void resized() {
            draw();

        }
        public void toggleMouseListener(ToolButton button) {

            //System.out.println(this.getMouseListeners());

            for (MouseMotionListener motionListener:this.getMouseMotionListeners()){
                this.removeMouseMotionListener(motionListener);
                //System.out.println("Motion happened");
            }
            for (MouseListener listener:this.getMouseListeners()){
                this.removeMouseListener(listener);
                //System.out.println("Mouse happened");
            }
//            System.out.println("Mouse: "+this.getMouseListeners());
//            System.out.println("Motion: "+ this.getMouseMotionListeners());


            if (button.isActivated){
                button.isActivated = false;
                button.setForeground(Color.BLACK);
            }
            else { //if not activated
                button.isActivated = true;
                button.setForeground(Color.BLUE);

                button.otherButton.isActivated = false;
                button.otherButton.setForeground(Color.BLACK);

                this.addMouseListener(button.tool);
                this.addMouseMotionListener(button.tool);
            }
        }
    }





    /* This new inner class combines the the code for the button's appearance and behavior in one place. */
    class OpenFileButton extends JButton implements ActionListener {
        OpenFileButton() {
            super("Open File");             // calls the super class (JButton) constructor
            addActionListener(this);       // adds this object (itself) as its own action listener
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(myJFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    canvas.clear();
                    bufferedImage = ImageIO.read(chooser.getSelectedFile());
                    originalImage = ImageIO.read(chooser.getSelectedFile());
                    trackChanges();
                    canvas.draw();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }


        }
    }

    class SaveFileButton extends JButton implements ActionListener {
        SaveFileButton() {
            super("Save File");             // calls the super class (JButton) constructor
            addActionListener(this);       // adds this object (itself) as its own action listener
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(myJFrame)==JFileChooser.APPROVE_OPTION){
                try {
                    String fileLocation = chooser.getSelectedFile().getAbsolutePath();
                    ImageIO.write(bufferedImage,"png",new File(fileLocation));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public int invertColor(Color color){
        int red = 255-color.getRed();
        int green=255-color.getGreen();
        int blue=255-color.getBlue();
        return (new Color(red,green,blue)).getRGB();

    }

    public int greyScale(Color color){
        int average = (color.getRed()+color.getGreen()+color.getBlue())/3;
        return (new Color(average,average,average).getRGB());
    }


    class LocalFilterButton extends JMenuItem implements ActionListener{
        int operation;
        //when operation is:
        //1=invert
        //2=grey

        LocalFilterButton(String name,int operation){
            super(name);
            this.operation = operation;
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            int rgb=0;
            for(int x=0;x<bufferedImage.getWidth();x++){
                for (int y=0;y<bufferedImage.getHeight();y++){
                    Color color = new Color(bufferedImage.getRGB(x,y));
                    if (operation==1){
                        rgb = invertColor(color);
                    }
                    else if (operation==2){
                        rgb = greyScale(color);
                    }
                    bufferedImage.setRGB(x,y,rgb);
                }
            }
            trackChanges();
            canvas.draw();
        }
    }


    //I was going add multiple global buttons, but ran out of time
    class GlobalFilterButton extends JMenuItem implements ActionListener{
        int operation;
        //when operation is:
        //1=pixelate

        GlobalFilterButton(String name,int operation){
            super(name);
            this.operation = operation;
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            int rgb;
            for(int x=0;x<bufferedImage.getWidth();x+=pixelSize){
                for (int y=0;y<bufferedImage.getHeight();y+=pixelSize){
                    rgb = (new Color(bufferedImage.getRGB(x,y))).getRGB();
                    for(int x2=x;x2<x+pixelSize && x2<bufferedImage.getWidth();x2++){
                        for(int y2=y;y2<y+pixelSize && y2<bufferedImage.getHeight();y2++){
                            if (operation == 1){
                                bufferedImage.setRGB(x2,y2,rgb);
                            }
                        }
                    }
                }
            }
            trackChanges();
            canvas.draw();
        }
    }



    //I decided that new button should make a white canvas
    //When it's resized, instead of making the white buffered image bigger,
    //I also just decided to have the image stretch like the normal images.
    class NewButton extends JButton implements ActionListener{
        NewButton(){
            super("New");
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            bufferedImage = new BufferedImage(canvas.getWidth(),canvas.getHeight(),BufferedImage.TYPE_INT_RGB);
            Graphics pen = bufferedImage.getGraphics();
            pen.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
            originalImage = copyBufferedImage(bufferedImage);
            canvas.clear();
            canvas.draw();
            trackChanges();
        }
    }

    class ColorChooserButton extends JButton implements ActionListener{
        ColorChooserButton(){
            super("Color Chooser");
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e){
            Color newColor = JColorChooser.showDialog(myJFrame,"Color Chooser",Color.BLACK);
            if (newColor!=null){
                color=newColor;
                this.setForeground(color);
            }
        }
    }

    class ToolButton extends JButton implements ActionListener{
        boolean isActivated=false;
        String type;
        ToolButton otherButton;
        Tool tool;

        ToolButton(String type){
            super(type);
            this.type = type;
            tool = new Tool(type);
            this.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e) {
            canvas.toggleMouseListener(this);
        }
    }



    //unfortunately, if this draws outside the canvas, the undo/redo list still gets updated
    //I tried only drawing and tracking if inside, but that messed up the lines for the drag scribble
    //and the undo/redo stuff
    //pretty much, i ran out of time to figure this out :(


    class Tool implements MouseListener, MouseMotionListener{
        int prevX;
        int prevY;
        String type;

        public Tool(String type){
            this.type=type;
        }

        private boolean isOutside(int x, int y){
            if (x >= bufferedImage.getMinX() && x < bufferedImage.getMinX() + bufferedImage.getWidth() && y >= bufferedImage.getMinY() && y < bufferedImage.getMinY() + bufferedImage.getHeight()) {
                return false;
            }else {
                return true;
            }
        }


        @Override
        public void mousePressed(MouseEvent e){
            int x=canvasToImage(e.getX(),0);
            int y=canvasToImage(e.getY(),1);

            int newBrushSize = canvasToImage(brushSize,2);
            Graphics pen = bufferedImage.getGraphics();
            pen.setColor(color);
            if (type.equals("Scribble Tool")) {
                pen.fillOval(x - newBrushSize / 2, y - newBrushSize / 2, newBrushSize, newBrushSize);
            }
            else if (type.equals("Fill Tool")) {
                if (!isOutside(x,y)){
                    spreadColor(x,y);
                }
            }
            prevX=x;
            prevY=y;
            canvas.draw();


        }
        private void spreadColor(int x, int y){
            //System.out.println("x: "+x+"     y: "+y);
            int pixelRGB = bufferedImage.getRGB(x,y);
            bufferedImage.setRGB(x,y,color.getRGB());
            for (int x1=x-1;x1<=x+1;x1++){
                for (int y1=y-1;y1<=y+1;y1++){
                    if (x1>=0 && x1<bufferedImage.getWidth() && y1>=0 && y1<bufferedImage.getHeight()){
                        if (bufferedImage.getRGB(x1,y1)==pixelRGB && bufferedImage.getRGB(x1,y1) != color.getRGB()){
                            spreadColor(x1,y1);
                        }
                    }
                }
            }

        }
        public void mouseReleased(MouseEvent e){
            trackChanges();
        }



        public void mouseEntered(MouseEvent e){

        }

        public void mouseExited(MouseEvent e){

        }

        public void mouseClicked(MouseEvent e){

        }


        public void mouseDragged(MouseEvent e) {
            int x=canvasToImage(e.getX(),0);
            int y=canvasToImage(e.getY(),1);

            int newBrushSize = canvasToImage(brushSize,2);
            Graphics pen = bufferedImage.getGraphics();
            pen.setColor(color);
            Graphics2D pen2 = (Graphics2D)pen;
            pen2.setStroke(new BasicStroke(newBrushSize));
            if (type.equals("Scribble Tool")){ //was going to add other stuff, but ran out of time
                pen2.drawLine(prevX,prevY,x,y);
            }
            canvas.draw();
            prevX=x;
            prevY=y;
        }

        public void mouseMoved(MouseEvent e) {

        }
    }
    class PixelSlider extends JSlider implements ChangeListener{
        PixelSlider(){
            super(1,50,10);
            this.addChangeListener(this);
        }
        public void stateChanged(ChangeEvent e){
            pixelSize = this.getValue();
        }
    }
}


