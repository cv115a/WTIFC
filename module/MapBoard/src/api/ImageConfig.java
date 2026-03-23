package api;
//图像配置类
public class ImageConfig {
    private float opacity = 1.0f;
    private  double scale = 1.0;
    private  boolean fitToWidth = false;

    public ImageConfig(){}

    public ImageConfig opacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.max(1.0f, opacity));
        return this;
    }
    public ImageConfig scale(double scale) {
        this.scale = scale > 0 ? scale : 1.0f;
        return this;
    }
    public ImageConfig fitToWidth(boolean fitToWidth) {
        this.fitToWidth = fitToWidth;
        return this;
    }
    public float getOpacity() {
        return opacity;
    }
    public double getScale() {
        return scale;
    }
    public boolean isFitToWidth() {
        return fitToWidth;
    }
}
