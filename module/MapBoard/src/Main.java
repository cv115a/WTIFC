import api.DrawingListener;
import api.ImageConfig;
import api.MapBoardPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {createAndShowGUI();});
    }
    private static void createAndShowGUI() {
        // 创建主窗口
        JFrame frame = new JFrame("战雷标图版——间瞄计算器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        //创建标图版
        MapBoardPanel boardPanel = new MapBoardPanel();
        //文件选择器
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField filePathFileId = new JTextField(25);
        filePathFileId.setEditable(false);
        filePathFileId.setText("为选择地图文件");

        JButton browseBtn = new JButton("选择地图");
        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "图像文件", "png", "jpg", "jpeg", "bmp"));

            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathFileId.setText(selectedFile.getName());
                try {
                    boardPanel.loadImage(selectedFile);
                    boardPanel.fitImageToWindow();  // 确保 MapBoardPanel 有这个方法
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "加载失败: " + ex.getMessage());
                }
            }
        });
        topPanel.add(new JLabel("地图："));
        topPanel.add(browseBtn);
        topPanel.add(filePathFileId);
        //比例尺输入和距离输出
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(100,0));
        //比例尺输入
        JPanel scalePanel = new JPanel(new GridLayout(4,1,5,5));
        scalePanel.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
        scalePanel.add(new JLabel("参考线实际距离(mi)"));

        JTextField scaleFileId = new JTextField("1000");
        scaleFileId.setHorizontalAlignment(JTextField.CENTER);
        scalePanel.add(scaleFileId);

        JButton calibrateBtn = new JButton("执行校准");
        calibrateBtn.setBackground(Color.GREEN);
        calibrateBtn.add(new JLabel(""));


        //校准按钮事件
        calibrateBtn.addActionListener(e -> {
            try {
                double meters = Double.parseDouble(scaleFileId.getText().trim());
                if (meters > 0) {
                    boardPanel.calibrate(meters);
                    JOptionPane.showMessageDialog(frame, "校准成功！");
                } else {
                    JOptionPane.showMessageDialog(frame, "距离必须大于0");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "请输入有效数字");
            }
        });
        scalePanel.add(calibrateBtn);

        //距离输出部分
        JPanel resultPanel = new JPanel(new GridLayout(2,1,5,5));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20,5,10,5));
        resultPanel.add(new JLabel("测量结果"));

        JTextField distanceOutputFieId = new JTextField("等待测量");
        distanceOutputFieId.setEditable(false);
        distanceOutputFieId.setHorizontalAlignment(JTextField.CENTER);
        distanceOutputFieId.setFont(new Font("Monospaced", Font.BOLD, 16));
        distanceOutputFieId.setBackground(Color.BLACK);
        distanceOutputFieId.setBackground(Color.GREEN);
        resultPanel.add(distanceOutputFieId);

        infoPanel.add(scalePanel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(resultPanel);
        infoPanel.add(Box.createVerticalGlue());


        //设置回调
        boardPanel.setDrawingListener(new DrawingListener() {@Override
        public void onReferenceDrawn(double pixelDist) {
            JOptionPane.showMessageDialog(null,
                    "参考线已绘制！\n" +
                            "像素长度: " + String.format("%.1f", pixelDist) + " 像素\n\n" +
                            "请在右侧面板输入实际距离，然后点击【执行校准】按钮",
                    "参考线完成",
                    JOptionPane.INFORMATION_MESSAGE);

        }

            @Override
            public void onMeasurementDrawn(double realDist) {
            distanceOutputFieId.setText(String.format("%.1f", realDist) + "m");
            }

            @Override
            public void onImageLoaded(int w, int h) {
                System.out.println("地图尺寸: " + w + "x" + h);
            }});
        //加载图像
        try{
            boardPanel.loadImage(new File("map.png"));
            boardPanel.applyImageConfig(new ImageConfig().opacity(0.6f).fitToWidth(true));
        }catch (IOException e){
            System.out.println("未找到地图，使用空白背景");
        }
        //初始化
        boardPanel.setModes(MapBoardPanel.Mode.REFERENCE);
        //控制按钮
        JPanel controlPanel = new JPanel();
        JButton refBtn = new JButton("参考模式");

        refBtn.addActionListener(e -> {
            try {
                // 尝试进入参考模式
                boardPanel.setModes(MapBoardPanel.Mode.REFERENCE);
            } catch (IllegalStateException ex) {
                // 如果已有参考线，捕获异常并提示用户
                JOptionPane.showMessageDialog(frame,
                        ex.getMessage(),  // 显示 "已有参考线，请先校准或清除后重试"
                        "无法进入参考模式",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton measBtn = new  JButton("测量模式");
        JButton clearBtn = new JButton("清除");

        refBtn.addActionListener(e -> {boardPanel.setModes(MapBoardPanel.Mode.REFERENCE);});
        measBtn.addActionListener(e ->{
            if (boardPanel.isCalibrated()) {
                boardPanel.setModes(MapBoardPanel.Mode.MEASURE);
            } else {
                JOptionPane.showMessageDialog(null, "请先校准！");
            }
        });
        clearBtn.addActionListener(e -> boardPanel.clearAllLines());
        controlPanel.add(refBtn);
        controlPanel.add(measBtn);
        controlPanel.add(clearBtn);

        //组装

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.EAST);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(1000,700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }



}
