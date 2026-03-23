package model;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;

/*
 * 地图图像实体，封装图像数据及所有变换状态
 */

public class MapImage {
    private final BufferedImage source;
    private final int originalWidth;
    private final int originalHeight;

    private double scale = 1.0;
    private float opacity = 1.0f;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    public  MapImage(BufferedImage source){
        if (source == null){
            throw new IllegalArgumentException("图像源不能为空");
        }
        this.source = source;
        this.originalWidth = source.getWidth();
        this.originalHeight = source.getHeight();
    }
    //变换设置
    public void setScale(double scale){
        if (scale <= 0){
            throw new IllegalArgumentException("缩放必须大于0");
        }
        this.scale = scale;
    }
    public void setOpacity(float opacity){
        if (opacity <= 0){
            throw new IllegalArgumentException("透明度必须在0.0~1.0之间");
        }
        this.opacity = opacity;
    }
    public void setOffset(double x, double y){
        this.offsetX = x;
        this.offsetY = y;
    }
    //========== 查询 ==========
    public  BufferedImage getSource(){
        return source;
    }
    public  int getOriginalWidth(){
        return originalWidth;
    }
    public  int getOriginalHeight(){
        return originalHeight;
    }
    /*
     * 显示宽度 = 原始宽 × 缩放
     */
    public int getDisplayWidth(){
        return (int) Math.round(originalWidth * scale);
    }
    /*
     * 显示高度 = 原始高 × 缩放
     */
    public int getDisplayHeight(){
        return (int) Math.round(originalHeight * scale);
    }
    public double getScale(){
        return scale;
    }
    public float getOpacity(){
        return opacity;
    }
    public double getOffsetX(){
        return offsetX;
    }
    public double getOffsetY(){
        return offsetY;
    }
    /*
     * 获取当前完整变换矩阵（用于渲染）
     * 顺序：平移 → 缩放
     */

    public AffineTransform getAffineTransform(){
        AffineTransform transform = new AffineTransform();
        transform.translate(offsetX, offsetY);
        transform.scale(scale, scale);
        return transform;
    }
    /*
     * 将屏幕坐标转换为图像原始坐标（用于精确测量）
     */

    public Point screenToImage(Point screenPoint){
        double imgX = (screenPoint.x - offsetX) / scale;
        double imgY = (screenPoint.y - offsetY) / scale;
        return new Point(imgX, imgY);
    }
    /*
     * 将图像原始坐标转换为屏幕坐标
     */
    public Point imageToScreen(Point imagePoint){
        double imgX = imagePoint.x / scale + offsetX;
        double imgY = imagePoint.y / scale + offsetY;
        return new Point(imgX, imgY);
    }
    @Override
    public String toString(){
        return String.format("MapImage[%dx%d, scale=%.2f, opacity=%.2f, offset=(%.1f,%.1f)]", originalWidth, originalHeight, scale, opacity, offsetX, offsetY);
    }

}
