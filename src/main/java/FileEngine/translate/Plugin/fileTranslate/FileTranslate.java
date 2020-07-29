package FileEngine.translate.Plugin.fileTranslate;

import FileEngine.translate.Plugin.settings.Settings;
import FileEngine.translate.Plugin.threadPool.CachedThreadPool;
import FileEngine.translate.Plugin.translate.TranslateUtil;

import javax.swing.*;
import java.io.*;

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
    }

    public void showWindow() {
        frame.setContentPane(new FileTranslate().panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(600, 400);
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
            while ((eachLine = br.readLine()) != null) {
                if (eachLine.endsWith("=")) {
                    eachLine = eachLine.substring(0, eachLine.length() - 1);
                }
                //生成两个文件，一个是只含有翻译结果的文件，一个是包含源字符串的翻译文件
                String result = TranslateUtil.getTranslation(eachLine, Settings.getInstance().getFromLang(), Settings.getInstance().getToLang());

                withoutSourceW.write(result);
                withoutSourceW.newLine();

                withSourceW.write(eachLine + "=" + result);
                withSourceW.newLine();

                if (isStop) {
                    break;
                }
            }
        }catch (IOException ignored) {
        }finally {
            status = TRANSLATE_DONE;
            labelStatus.setText("Translated.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            labelStatus.setText("");
        }
    }
}
