package FileEngine.translate.Plugin.fileTranslate;

import FileEngine.translate.Plugin.PluginMain;
import FileEngine.translate.Plugin.settings.Settings;
import FileEngine.translate.Plugin.threadPool.CachedThreadPool;
import FileEngine.translate.Plugin.translate.TranslateUtil;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class FileTranslate {
    private JTextField textFieldSourse;
    private JLabel labelSourse;
    private JLabel labelDest;
    private JTextField textFieldDest;
    private JButton buttonSourseFileChooser;
    private JButton buttonDestFileChooser;
    private JButton buttonStop;
    private JButton buttonStart;
    private JSeparator seperator;
    private JPanel panel;
    private JLabel labelStatus;
    private JLabel labelPlaceHolder;
    private JLabel labelPlaceHolder2;
    private JLabel labelTranslateMode;
    private JLabel labelChangeModeTip;
    private JLabel labelShowCurrentMode;
    private JLabel labelTranslatedColumn;
    private final JFrame frame = new JFrame("File Translate");
    private volatile boolean isStop;
    private volatile int status = TRANSLATE_DONE;

    private static final int TRANSLATING = 0;
    private static final int TRANSLATE_DONE = 1;

    private static class FileTranslateBuilder {
        private static final FileTranslate INSTANCE = new FileTranslate();
    }

    public static FileTranslate getInstance() {
        return FileTranslateBuilder.INSTANCE;
    }

    private FileTranslate() {
        buttonSourseFileChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showSaveDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File chosen = fileChooser.getSelectedFile();
                textFieldSourse.setText(chosen.getAbsolutePath());
            }
        });
        buttonDestFileChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = fileChooser.showSaveDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File chosen = fileChooser.getSelectedFile();
                textFieldDest.setText(chosen.getAbsolutePath());
            }
        });
        buttonStart.addActionListener(e -> {
            String source = textFieldSourse.getText();
            String dest = textFieldDest.getText();
            isStop = false;
            if (status == TRANSLATE_DONE) {
                CachedThreadPool.getInstance().execute(() -> translateFile(source, dest));
            }
        });
        buttonStop.addActionListener(e -> isStop = true);

        CachedThreadPool.getInstance().execute(() -> {
            try {
                while (PluginMain.isNotExit) {
                    if (frame.isVisible()) {
                        frame.repaint();
                    }
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            }catch (InterruptedException ignored) {
            }
        });
    }

    public void showWindow() {
        String t = Settings.getInstance().getFromLang() + "--->" + Settings.getInstance().getToLang();
        labelShowCurrentMode.setText(t);
        frame.setContentPane(FileTranslateBuilder.INSTANCE.panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void translateFile(String source, String dest) {
        status = TRANSLATING;
        labelStatus.setText("Translating...");
        File destDir = new File(dest);
        String withoutSource = destDir.getAbsolutePath() + File.separator + "withoutSource.txt";
        String withSource = destDir.getAbsolutePath() + File.separator + "withSource.txt";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(source)));
             BufferedWriter withoutSourceW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(withoutSource)));
             BufferedWriter withSourceW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(withSource)))) {
            String eachLine;
            int count = 0;
            while ((eachLine = br.readLine()) != null) {
                if (eachLine.endsWith("=")) {
                    eachLine = eachLine.substring(0, eachLine.length() - 1);
                }
                //生成两个文件，一个是只含有翻译结果的文件，一个是包含源字符串的翻译文件
                String result;
                try {
                    result = TranslateUtil.getInstance().getTranslation(eachLine, Settings.getInstance().getFromLang(), Settings.getInstance().getToLang());
                }catch (IOException | IllegalAccessException | InvocationTargetException e) {
                    labelStatus.setText("Request translation too frequently, please try later.");
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(1500);

                withoutSourceW.write(result);
                withoutSourceW.newLine();

                withSourceW.write(eachLine + "=" + result);
                withSourceW.newLine();
                count++;
                labelTranslatedColumn.setText("Number of translated rows:" + count);
                if (isStop) {
                    break;
                }
            }
        }catch (IOException | InterruptedException ignored) {
        }finally {
            status = TRANSLATE_DONE;
            labelStatus.setText("Translated.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            labelStatus.setText("");
            labelTranslatedColumn.setText("");
        }
    }
}
