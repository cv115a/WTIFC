package api;

public interface DrawingListener {
    /*参考线绘制完成，等待输入实际距离*/
    void onReferenceDrawn(double pixelDistance);

    /*测量线绘制完成，返回实际距离*/
    void onMeasurementDrawn(double realDistanceMeters);

    /*图像加载完成*/

    void onImageLoaded(int width, int height);

}
