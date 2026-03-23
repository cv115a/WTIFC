package model;

public class Line {
    /*线条实体，记录起点和终点，和绘制元数据*/
    private final Point start;
    private final Point end;
    private final long timestamp;
    private boolean isReference;
    public Line(Point start, Point end) {
        if(start == null || end == null){
            throw new NullPointerException("起点和终点不能为空");
        }
        this.start = start;
        this.end = end;
        this.timestamp = System.currentTimeMillis();
        isReference = false;
    }
    /*计算像素长度*/
    public double pixelLength(){
        return start.distanceTo(end);
    }
    public  Point getStart(){
        return start;
    }
    public Point getEnd(){
        return end;
    }
    public long getTimestamp(){
        return timestamp;
    }
    public boolean isReference(){
        return isReference;
    }
    public void setReference(boolean reference){
        this.isReference = reference;
    }
    @Override
    public String toString(){
        return String.format("Line[%s -> %s, %.1fpx, ref=%b]", start, end, pixelLength(), isReference);
    }
}
