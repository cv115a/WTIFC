package model;

public class Point {
    public  final double x;
    public  final double y;

    public  Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double distanceTo(Point other){
        /*计算像素点间的距离*/
        double dx = this.x - other.x;
        double dy = this.y - other.y;

        return Math.sqrt(dx * dx + dy * dy);
    }
    public Point translate(Point other){
        /*平移后，返回新点*/
        return new Point(x + other.x, y + other.y);
    }
    public Point scale(Point other){
        /*缩放后，返回新点*/
        return new Point(x * other.x, y * other.y);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }
    @Override
    public int hashCode(){
        return java.util.Objects.hash(x, y);
    }
    @Override
    public String toString(){
        return "(" + x + ", " + y + ")";
    }

}
