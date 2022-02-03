package me.homeHelper;

import com.formdev.flatlaf.FlatLightLaf;
import me.aiot.SocketServer;

import javax.swing.*;

import java.awt.*;

import static me.homeHelper.assets.TextAssets.TEXTS;

public final class App {
    public static App APP;

    public final JFrame mainFrame;
    public final JTabbedPane tabPane;

    public final SocketServer socketServer;

    public static void main(String[] args) {
        FlatLightLaf.setup();
        APP = new App();
    }

    App() {
        mainFrame = new JFrame(TEXTS.get("title"));
        mainFrame.setSize(1080, 720);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tabPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainFrame.setContentPane(tabPane);
        initTabs();
        // 启动AIOT4J服务器
        socketServer = new SocketServer();
        final var socketThread = new Thread(socketServer::init);
        //socketThread.start();
    }

    private void initTabs() {
        tabPane.addTab(TEXTS.get("welcome_tab"), createWelcomeTab());
    }

    private Component createWelcomeTab() {
        var box1 = Box.createHorizontalBox();
        var label1 = new JLabel(TEXTS.get("welcome_title"), JLabel.CENTER);
        label1.setFont(label1.getFont().deriveFont(48f));
        box1.add(Box.createHorizontalGlue());
        box1.add(label1);
        box1.add(Box.createHorizontalGlue());
        var box2 = Box.createHorizontalBox();
        var label2 = new JLabel(TEXTS.get("author_title"), JLabel.CENTER);
        label2.setFont(label2.getFont().deriveFont(24f));
        label2.setForeground(Color.GRAY);
        box2.add(Box.createHorizontalGlue());
        box2.add(label2);
        box2.add(Box.createHorizontalGlue());
        var outBox = Box.createVerticalBox();
        outBox.add(Box.createVerticalGlue());
        outBox.add(box1);
        outBox.add(Box.createVerticalStrut(48));
        outBox.add(box2);
        outBox.add(Box.createVerticalGlue());
        return outBox;
    }
}
