package model;
import java.util.*;

public class CanvasState {
    /*
     * 画布全局状态，集中管理所有运行时数据
     */
        public enum Mode{
            IDLE,REFERENCE,MEASURE
        }
        private  Mode mode = Mode.IDLE;
        private MapImage currentImage;
        private final List<Line> lines = new ArrayList<>();
        private Line drawingLine;                           //正在拖拽的临时线
        private Double referencePixelLength;                // 参考线像素长度
        private Double scaleFactor;                          // 比例尺：像素/米（越小表示比例尺越大）
        // ========== 模式管理 ==========
    public void setMode(Mode mode){
        if(mode == null){
            throw new IllegalArgumentException("模式不能为");
        }
        this.mode = mode;
    }
    public Mode getMode(){
        return mode;
    }
    // ========== 图像管理 ==========
    public void setImage(MapImage image) {
        this.currentImage = image;
    }
    public Optional<MapImage> getImage(){
        return Optional.ofNullable(currentImage);
    }
    private void CurrentImage(){
        this.currentImage = null;
    }
    public void clearImage() {
        this.currentImage = null;
    }
    public boolean hasImage(){
        return currentImage != null;
    }
    // ========== 线条管理 ==========

    /*
     * 开始绘制新线（鼠标按下）
     */
    public void startDrawing(Point start){
        this.drawingLine = new Line(start,start);
    }
    /*
     * 更新绘制中的线（鼠标拖拽）
     */
    public void updateDrawing(Point current){
        if(drawingLine == null){
            return;
        }
        Point start = drawingLine.getStart();
        this.drawingLine = new Line(start,current);
    }
    /*
     * 完成绘制（鼠标释放），返回完成的线
     */
    public Line finishDrawing(){
        if(drawingLine == null){
            return null;
        }
        Line completed = drawingLine;
        lines.add(completed);
        drawingLine = null;

        if(mode == Mode.REFERENCE){
            completed.setReference(true);
            this.referencePixelLength = completed.pixelLength();
            this.mode = Mode.IDLE;
        }

        return completed;
    }
    /*
     * 取消当前绘制
     */
    public void cancelDrawing(){
        drawingLine = null;
    }
    public Optional<Line> getDrawingLine(){
        return Optional.ofNullable(drawingLine);
    }
    public List<Line> getLines(){
        return Collections.unmodifiableList(lines);
    }
    public void removeLastLine(){
        if (!lines.isEmpty()){
            lines.remove(lines.size() - 1);
        }
    }
    public void clearAllLines(){
        lines.clear();
        referencePixelLength = null;
        scaleFactor = null;
        setMode(Mode.IDLE);
    }
    // ========== 校准与换算 ==========
    /*
     * 设置参考线并进入待校准状态
     */
    public void setReferenceLine(Line line){
        if(line == null){
            throw new IllegalArgumentException("参考线不能为null");
        }
        line.setReference(true);
        this.referencePixelLength = line.pixelLength();
    }

    /*
     * 使用实际距离完成校准
     * @param realDistanceMeters 参考线对应的实际距离（米）
     */

    public void calibrate(double realDistanceMeters){
        if(realDistanceMeters <= 0){
            throw new IllegalArgumentException("实际距离必须大于0");
        }
        if(referencePixelLength == null || referencePixelLength == 0.0){
            throw new IllegalArgumentException("未设置有效的参考线");
        }
        this.scaleFactor = referencePixelLength/realDistanceMeters;
    }
    public boolean isCalibrated(){
        return referencePixelLength != null && scaleFactor != null && scaleFactor > 0;
    }
    /*
     * 像素距离转换为实际距离（米）
     */
    public double pixelToReal(double pixels){
        if (!isCalibrated()) {
            throw new IllegalStateException("未完成校准");
        }
        return pixels / scaleFactor;
    }


    /*
     * 实际距离（米）转换为像素距离
     */
    public double realToPixel(double meters){
        if (!isCalibrated()) {
            throw new IllegalArgumentException("未完成校准");
        }
        return meters * scaleFactor;
    }
    public Double getScaleFactor(){
        return scaleFactor;
    }
    public Double getReferencePixelLength(){
        return referencePixelLength;
    }
    // ========== 工具方法 ==========

    /*
     * 获取最后一条线
     */
    public Optional<Line> getLastLine(){
        if (lines.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(lines.get(lines.size() - 1));
    }
    @Override
    public String toString(){
        return String.format("CanvasState[mode=%s, image=%s, lines=%d, calibrated=%b]",
                mode, hasImage() ? "有" : "无", lines.size(), isCalibrated());
    }
}



