package api;
import model.CanvasState;
import controller.DrawController;
import model.Line;
import model.Point;
import service.MeasureService;
import service.ImageService;
import service.DrawingService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * 标图版主面板（Facade模式）
 * 对外暴露标图版的所有功能，隐藏内部复杂度
 */

public class MapBoardPanel extends JPanel {
    //内部组件
    private final CanvasState canvasState;
    private final DrawingService drawingService;
    private final MeasureService measureService;
    private final ImageService imageService;
    private final DrawController drawController;
    private DrawingListener drawingListener;

    //渲染
    private static final Color GRID_COLOR = new Color(200, 200, 200);
    private static final Color LINE_COLOR = Color.RED;
    private static final Color LINE_DRAWING_COLOR = Color.BLUE;

    /*初始化内部组件*/
    public MapBoardPanel() {
        //数据层
        this.canvasState = new CanvasState();
        //服务层
        this.drawingService = new DrawingService(canvasState);
        this.measureService = new MeasureService(canvasState);
        this.imageService = new ImageService(canvasState);

        //控制层
        this.drawController = new DrawController(canvasState, drawingService, measureService);
        this.drawController.setDrawingListener(createInternalListener());
        this.drawController.attachTo(this);
        this.drawController.setRepaintCallback(() -> repaint());
    }


    /*创建内部监听器，转发给外部*/
    private DrawingListener createInternalListener() {
        return new DrawingListener() {
            @Override
            public void onReferenceDrawn(double pixelDistance) {
                if (drawingListener != null) {
                    drawingListener.onReferenceDrawn(pixelDistance);
                }
            }

            @Override
            public void onMeasurementDrawn(double realDistanceMeters) {
                if (drawingListener != null) {
                    drawingListener.onMeasurementDrawn(realDistanceMeters);
                }
            }

            @Override
            public void onImageLoaded(int width, int height) {
                if (drawingListener != null) {
                    drawingListener.onImageLoaded(width, height);
                }
            }
        };
    }
    //  图像操作
    public void loadImage(File file) throws IOException {
        imageService.loadImage(file);
        repaint();
        //通知外侧
        imageService.getImage().ifPresent(img -> {
            if (drawingListener != null) {
                drawingListener.onImageLoaded(img.getOriginalWidth(), img.getOriginalHeight());
            }
        });
    }
    public void clearImage() {
        imageService.clearImage();
        repaint();
    }
    public void setImageScale(double scale) {


        imageService.setScale(scale);
        repaint();
    }


    public void setImageOpacity(float opacity) {
        imageService.setOpacity(opacity);
    }
    public void fitImageToWindow() {
        imageService.fitToCanvas(getWidth(), getHeight());
        repaint();
    }
    public void applyImageConfig(ImageConfig imageConfig) {
        if(imageConfig.isFitToWidth()){
            fitImageToWindow();
        }else {
            setImageScale(imageConfig.getScale());
        }
        setImageOpacity(imageConfig.getOpacity());
    }
    //绘图模式
    public void setModes(Mode mode) {
        if(mode == Mode.REFERENCE && hasReferenceLine()){
            throw new IllegalStateException("已有参考线，请先校准或清除后重试");
        }
        switch (mode) {
            case REFERENCE:
                drawController.startReferenceMode();
                break;
            case MEASURE:
                drawController.startMeasureMode();
                break;
            case IDLE:
                drawController.resetToIdle();
                break;
        }
    }
    public boolean hasReferenceLine() {
        return canvasState.getReferencePixelLength() != null;
    }
    public CanvasState.Mode getMode(){
        return canvasState.getMode();
    }
    public boolean isCalibrated(){
        return  measureService.isCalibrated();
    }
    public void calibrate(double realDistanceMeters) {
        measureService.calibrate(realDistanceMeters);
        setModes(Mode.MEASURE);
    }
    //查询
    public double getLastPixelDistance(){
        return canvasState.getLastLine().map(Line::pixelLength).orElse(0.0);
    }
    public double getLastRealDistance(){
        if(!isCalibrated()){
            return 0.0f;
        }
        return canvasState.getLastLine().map(Line::pixelLength).orElse(0.0);
    }
    //监听注册
    public void setDrawingListener(DrawingListener drawingListener) {
        this.drawingListener = drawingListener;
    }
    //控制
    public void clearAllLines(){
        drawingService.clearAll();
        repaint();
    }
    public void undoLastLine(){
        drawingService.undoLast();
        repaint();
    }
    //渲染
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawImageLayer(g2d);
        drawGrid(g2d);
        drawFinishedLines(g2d);
        drawCurrentLine(g2d);

        // 1. 先画图像（如果有）
        drawImageLayer(g2d);

        // 2. 再画网格
        drawGrid(g2d);

        // 3. 画已完成的线
        drawFinishedLines(g2d);

        // 4. 画进行中的线
        drawCurrentLine(g2d);
    }
    public void drawImageLayer(Graphics2D g2d){
        imageService.getImage().ifPresent(img -> {
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, img.getOpacity()));
            g2d.drawImage(img.getSource(), img.getAffineTransform(), null);
            g2d.setComposite(oldComposite);
        });
    }
    private void drawGrid(Graphics2D g2d){
        g2d.setColor(GRID_COLOR);
        int width = getWidth();
        int height = getHeight();
        for(int x = 0; x < width; x += 50){
            g2d.drawLine(x, 0, x, height);
        }
        for(int y = 0; y < height; y += 50){
            g2d.drawLine(0, y, width, y);
        }
    }
    private void drawFinishedLines(Graphics2D g2d){
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(2));
        for(Line line : drawingService.getAllLines()){
            Point start = line.getStart();
            Point end = line.getEnd();
            g2d.drawLine((int)start.x, (int)start.y, (int)end.x, (int)end.y);
        }
    }
    private void drawCurrentLine(Graphics2D g2d){
        drawingService.getCurrentDrawingLine().ifPresent(line -> {g2d.setColor(LINE_DRAWING_COLOR);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    10, new float[]{5, 5}, 0));
            Point s = line.getStart();
            Point e = line.getEnd();
            g2d.drawLine((int)s.x, (int)s.y, (int)e.x, (int)e.y);});
    }
    //枚举
    public enum Mode{
        REFERENCE,
        MEASURE,
        IDLE
    }

}
