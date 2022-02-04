package me.homeHelper;

import com.formdev.flatlaf.FlatLightLaf;
import me.aiot.SizeOnlyComponentListener;
import me.aiot.SocketServer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.aiot.js.JSPool.POOL;
import static me.homeHelper.assets.TextAssets.TEXTS;

public final class App {
    public static int connectTimes = 0;
    public static App APP;

    public final JFrame mainFrame;
    public final JTabbedPane tabPane;
    public final JMenuBar menuBar;

    public JList<String> moduleList;

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

        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);
        initMenuBar();

        // 创建AIOT4J服务器
        socketServer = new SocketServer();

        tabPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainFrame.setContentPane(tabPane);
        initTabs();

        // 启动AIOT4J服务器
        final var socketThread = new Thread(() -> socketServer.init((socket) -> {
            // 刷新调试终端页面
            tabPane.removeTabAt(tabPane.indexOfTab(TEXTS.get("debug_tab")));
            tabPane.addTab(TEXTS.get("debug_tab"), createDebugTab());
        }, (socket, message) -> connectTimes++));
        socketThread.start();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                socketServer.close();
                socketServer.setRunning(false);
                socketThread.interrupt();
                super.windowClosing(e);
            }
        });
    }

    private void initTabs() {
        tabPane.addTab(TEXTS.get("welcome_tab"), createWelcomeTab());
        tabPane.addTab(TEXTS.get("debug_tab"), createDebugTab());
        tabPane.addTab(TEXTS.get("module_tab"), createModuleTab());
    }

    private void initMenuBar() {
        final var moduleMenu = new JMenu(TEXTS.get("module_menu"));
        final var addModuleItem = new JMenuItem(TEXTS.get("add_module"));
        addModuleItem.addActionListener(e -> {
            final var name = JOptionPane.showInputDialog(mainFrame, "新模块名称");
            POOL.addJSModule(socketServer, name, "// 在此输入您的居家帮手模块代码\n");
            refreshModuleList();
        });
        moduleMenu.add(addModuleItem);
        final var runModuleItem = new JMenuItem(TEXTS.get("run_module"));
        runModuleItem.addActionListener(e -> {
            final var name = moduleList.getSelectedValue();
            if (POOL.codeMap.containsKey(name)) {
                POOL.runJSModule(name);
            }
        });
        moduleMenu.add(runModuleItem);
        menuBar.add(moduleMenu);
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

    static final Map<Integer, String> wordMap = new LinkedHashMap<>();

    static {
        wordMap.put(0, "垃圾回收");
        wordMap.put(1, "获取数字输入值");
        wordMap.put(2, "设置数字输入值");
        wordMap.put(3, "模拟输出");
        wordMap.put(4, "模拟输入");
        wordMap.put(5, "设置蜂鸣器状态");
        wordMap.put(6, "获取温湿度");
        wordMap.put(7, "自定义命令");
    }

    private Component createDebugTab() {
        var panel = new JPanel(new BorderLayout(8, 8));
        var connectLabel = new JLabel();
        var box = Box.createVerticalBox();
        var list = new JList<>(wordMap.values().toArray(new String[0]));
        var innerBox = Box.createHorizontalBox();
        var textField = new JTextField();
        var button = new JButton("↗");
        var textArea = new JTextArea();

        final var client = this.socketServer.getClient();
        if (client != null) {
            connectLabel.setText(TEXTS.get("connect_success", client.getSocket().getRemoteSocketAddress().toString()));
            connectLabel.setForeground(new Color(80, 152, 60));
        } else {
            connectLabel.setText(TEXTS.get("connect_failed"));
            connectLabel.setForeground(new Color(168, 93, 72));
        }
        connectLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 0, 0));
        panel.add(connectLabel, BorderLayout.NORTH);

        textField.setToolTipText("输入命令参数");
        button.setToolTipText("发送");
        innerBox.add(textField);
        innerBox.add(button);

        list.setBorder(BorderFactory.createEmptyBorder(0, 0, panel.getHeight() - 208, 0));
        panel.addComponentListener((SizeOnlyComponentListener) e ->
                list.setBorder(BorderFactory.createEmptyBorder(0, 0, panel.getHeight() - 208, 0)));

        button.addActionListener(e -> {
            if (list.getSelectedIndex() != -1) {
                final var index = list.getSelectedIndex();
                if (client != null) {
                    if (index == 7) {
                        client.send(textField.getText())
                                .then(message -> textArea.append(message + "\n"));
                    } else {
                        client.send("$" + index + textField.getText().replace(" ", ""))
                                .then(message -> textArea.append(message + "\n"));
                    }
                }
                System.out.println("$" + index + textField.getText().replace(" ", ""));
            }
        });

        box.add(list);
        box.add(Box.createVerticalStrut(4));
        box.add(innerBox);
        box.add(Box.createVerticalStrut(4));
        panel.add(box, BorderLayout.WEST);

        panel.add(textArea, BorderLayout.CENTER);
        return panel;
    }

    public Component createModuleTab() {
        var splitPanel = new JSplitPane();
        moduleList = new JList<>();
        var textarea = new JTextArea();

        splitPanel.setLeftComponent(moduleList);
        splitPanel.setRightComponent(textarea);
        splitPanel.setDividerLocation(160);

        refreshModuleList();
        moduleList.addListSelectionListener(e -> {
            final var name = moduleList.getModel().getElementAt(e.getFirstIndex());
            textarea.setText(POOL.codeMap.get(name));
        });

        textarea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    POOL.saveJSModule(moduleList.getSelectedValue(), e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    POOL.saveJSModule(moduleList.getSelectedValue(), e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    POOL.saveJSModule(moduleList.getSelectedValue(), e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return splitPanel;
    }

    private void refreshModuleList() {
        if (moduleList == null) {
            return;
        }
        moduleList.setListData(POOL.runtimeMap.keySet().toArray(new String[0]));
    }
}
