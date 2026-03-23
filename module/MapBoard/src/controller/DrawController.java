package controller;
import api.DrawingListener;
import model.CanvasState;
import model.Point;
import model.Line;
import service.DrawingService;
import service.MeasureService;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Optional;

/*绘图控制器*/

public class DrawController {
    private final CanvasState canvasState;
    private final DrawingService drawingService;
    private final MeasureService measureService;
    private DrawingListener drawingListener;
    private Runnable repaintCallback;

    //鼠标状态
    private  boolean isDrawing = false;
    private Point dragStart;
    public DrawController(CanvasState canvasState, DrawingService drawingService, MeasureService measureService) {
        this.canvasState = canvasState;
        this.drawingService = drawingService;
        this.measureService = measureService;
    }
    /*设置重绘回调*/
    public void setRepaintCallback(Runnable drawingListener) {
        this.repaintCallback = drawingListener;
    }

    /*注册绘图监听器*/
    public void setDrawingListener(DrawingListener drawingListener) {
        this.drawingListener = drawingListener;
    }

    public void attachTo(JPanel canvas) {
        MouseAdapter handler = createMouseHandler();
        canvas.addMouseListener(handler);
        canvas.addMouseMotionListener(handler);
    }

    /*绑定到画布组件*/
    private MouseAdapter createMouseHandler() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePress(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleRelease(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                handleIeDrag(e);
            }
        };
    }
    //事件处理
    private void handlePress(MouseEvent e) {
        CanvasState.Mode mode = canvasState.getMode();
        if(mode == CanvasState.Mode.IDLE){
            return;
        }
        isDrawing = true;
        dragStart = new Point(e.getX(), e.getY());
        drawingService.startDrawing(dragStart);
    }
    private void handleIeDrag(MouseEvent e) {
        if(!isDrawing){
           return;
        }
        Point current = new Point(e.getX(), e.getY());
        drawingService.updateDraw(current);
        triggerRepaint();
    }
    private void handleRelease(MouseEvent e) {
        if(!isDrawing){
            return;
        }
        isDrawing = false;
        Optional<Line>result = drawingService.finishDraw();
        result.ifPresent(line -> {
            switch (canvasState.getMode()) {
                case IDLE:
                    handleReferenceComplete(line);
                    break;

                case MEASURE:
                    handleMeasureComplete(line);
                    break;
                default:
                    break;
            }
        });
        triggerRepaint();
    }
/*重绘*/
    private void triggerRepaint() {
        if(repaintCallback != null){
            repaintCallback.run();
        }
    }

    private void handleMeasureComplete(Line line) {
        if(!measureService.isCalibrated()){
            return;
        }
        double realDistance = measureService.realLength(line);
        if(drawingListener != null){
            drawingListener.onMeasurementDrawn(realDistance);
        }
    }
    private void handleReferenceComplete(Line line) {
        if(drawingListener != null){
            drawingListener.onReferenceDrawn(line.pixelLength());
        }
    }

    //模式控制
    public void startReferenceMode(){
        canvasState.setMode(CanvasState.Mode.REFERENCE);
    }
    public void startMeasureMode(){
        if(!measureService.isCalibrated()){
            throw new IllegalStateException("未校准，无法进入测量模式");
        }
        drawingService.setMeasureMOde();
    }
    public void resetToIdle(){
        canvasState.setMode(CanvasState.Mode.IDLE);
    }


    //工具方法

    public void undo(){
        drawingService.undoLast();
        triggerRepaint();
    }
    public void clearAll(){
        drawingService.clearAll();
        triggerRepaint();
    }


}