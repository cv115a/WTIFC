package service;

import model.Line;
import model.Point;
import model.CanvasState;

import java.util.List;
import java.util.Optional;

/*
 * 绘图业务服务，管理线条生命周期
 */

public class DrawingService {
    private final CanvasState state;

    public DrawingService(CanvasState state) {
        if (state == null) {
            throw new IllegalArgumentException("state may not be null");
        }
        this.state = state;
    }
    // ========== 绘制流程 ==========

    /*开始绘制*/
    public void startDrawing(Point startPoint){
        state.startDrawing(startPoint);
    }
    /*更新绘制*/
    public void updateDraw(Point currentPoint){
        state.updateDrawing(currentPoint);
    }
    /*完成绘制，返回完成的线*/
    public Optional<Line> finishDraw() {
        Line completed = state.finishDrawing();
        if (completed == null) {
            return Optional.empty();
        }

        // 根据当前模式处理
        switch (state.getMode()) {
            case REFERENCE:
                // 设为参考线，进入待校准状态
                state.setReferenceLine(completed);
                break;
            case MEASURE:
                // 普通测量线，无需特殊处理
                break;
            default:
                // IDLE模式不应该画线
                break;
        }

        return Optional.of(completed);
    }
    /*取消当前绘制*/
    public void cancelDrawing(){
        state.cancelDrawing();
    }
    //查询
    public Optional<Line> getCurrentDrawingLine(){
        return state.getDrawingLine();
    }
    public List<Line> getAllLines(){
        return state.getLines();
    }
    public Optional<Line> getLastLine(){
        return state.getLastLine();
    }
    //修改
    public void undoLast(){
        state.removeLastLine();
    }
    public void clearAll(){
        state.clearAllLines();
    }
    //模式切换
    public void setReferenceMOde(){
        state.setMode(CanvasState.Mode.REFERENCE);
    }
    public void setMeasureMOde(){
        if (!state.isCalibrated()){
            throw new IllegalStateException("未校准，无法进入测量模式");
        }
        state.setMode(CanvasState.Mode.MEASURE);
    }
    public CanvasState.Mode getCurrentMode(){
        return state.getMode();
    }
}
