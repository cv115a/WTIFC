package service;
import model.CanvasState;
import model.Line;
import model.Point;

public class MeasureService {
    /*测量计算服务，处理距离换算*/
    private final CanvasState state;
    public MeasureService(CanvasState state) {
        if (state == null) {
            throw new IllegalArgumentException("state may not be null");
        }
        this.state = state;
    }
    //像素距离
    /*计算两点像素距离*/
    public double pixelDistance(Point p1, Point p2) {
        return p1.distanceTo(p2);
    }
    /*获取线的像素长度*/
    public double pixelLength(Line line){
        return line.pixelLength();
    }
    //实际距离（需校准）
    /*将像素距离转换为实际距离（米）*/
    public double toRealDistance(double pixelDistance){
        return pixelDistance / state.getScaleFactor();
    }
    /*将实际距离转换为像素距离*/
    public double toPixelDistance(double realDistanceMeters){
        return state.pixelToReal(realDistanceMeters);
    }
    /*获取线的实际距离*/
    public double realLength(Line line){
        return toRealDistance(line.pixelLength());
    }
    //校准
    /*使用参考线完成校准*/
    public void calibrate(double realDistanceMeters){
        state.calibrate(realDistanceMeters);
    }
    public boolean isCalibrated(){
        return state.isCalibrated();
    }
    /*获取当前比例尺信息*/
    public String getScaleInfo(){
        if (!isCalibrated()){
            return "未校准";
        }
        return String.format("%.2f 像素/米",state.getScaleFactor());
    }

    //快捷计算
    /*计算最后一条线的实际距离*/
    public double measureLastLine(){
        return state.getLastLine().map(this::realLength).orElseThrow(() -> new IllegalStateException("没有测量线"));
    }
}
