package service;
import model.CanvasState;
import model.MapImage;
import model.Point;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.Optional;

/*图像管理服务，处理加载、变换、查询*/

public class ImageService {
    private final CanvasState state;
    //支持的图片格式
    private static final String[] SUPPORTED_FORMATS = new String[]{"jpg", "png","jpeg","bmp"};
    public ImageService(CanvasState state) {
        if (state == null) {
            throw new IllegalArgumentException("CanvasState不能为null");
        }
        this.state = state;
    }

    //加载
    /*从文件加载图像*/
    public void loadImage(File file) throws IOException {
        if(!file.exists()){
            throw new IOException("文件不存在："+file.getPath());
        }
        if(!isSupportedFormat(file)){
            throw new IllegalArgumentException("不支持的格式，仅支持" + String.join(",", SUPPORTED_FORMATS));
        }
        BufferedImage source = ImageIO.read(file);
        if(source == null){
            throw new IOException("无法读取图像数据");
        }
        MapImage mapImage = new MapImage(source);
        state.setImage(mapImage);
    }
    private boolean isSupportedFormat(File file) {
        String name = file.getName().toLowerCase();
        for (String ext : SUPPORTED_FORMATS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }



    //变换
    public void setOpacity(float opacity) {
        getImageOrThrow().setOpacity(opacity);
    }
    public void setScale(double scale) {
        getImageOrThrow().setScale(scale);
    }

    /*自适应画布*/
    public void fitToCanvas(int canvasWidth, int canvasHeight) {
        MapImage img = getImageOrThrow();
        double scaleX = (double) canvasWidth / img.getOriginalWidth();
        double scaleY = (double) canvasHeight / img.getOriginalHeight();
        double scale = Math.min(scaleX, scaleY) * 0.95;
        img.setScale(scale);

        int displayWidth = (int)(img.getOriginalWidth() * scale);
        int displayHeight = (int)(img.getOriginalHeight() * scale);

        //居中
        double offsetX = (canvasWidth - displayWidth) / 2.0;
        double offsetY = (canvasHeight - displayHeight) / 2.0;
        img.setOffset(offsetX, offsetY);
    }
    public void setOffset(double x, double y) {
        getImageOrThrow().setOffset(x,y);
    }
    public void moveOffset(double dx, double dy) {
        MapImage img = getImageOrThrow();
        img.setOffset(img.getOffsetX() + dx, img.getOffsetY() + dy);
    }
    //查询
    public boolean hasImage() {
        return state.hasImage();
    }
    public Optional<MapImage> getImage() {
        return state.getImage();
    }


    private MapImage getImageOrThrow() {
        return state.getImage()
                .orElseThrow(() -> new IllegalStateException("未加载图像"));
    }
    // ========== 坐标转换 ==========
    /*     * 屏幕坐标 → 图像原始坐标（用于精确测量）*/
    public Point screenToImage(Point screenPoint) {
        return getImageOrThrow().screenToImage(screenPoint);
    }

    /*
     * 图像原始坐标 → 屏幕坐标
     */
    public Point imageToScreen(Point imagePoint) {
        return getImageOrThrow().imageToScreen(imagePoint);
    }
    public void clearImage() {
        state.clearImage();
    }
}
