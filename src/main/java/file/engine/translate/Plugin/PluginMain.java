package file.engine.translate.Plugin;

import file.engine.translate.Plugin.fileTranslate.FileTranslate;
import file.engine.translate.Plugin.settings.Settings;
import file.engine.translate.Plugin.threadPool.CachedThreadPool;
import file.engine.translate.Plugin.translate.TranslateUtil;
import file.engine.translate.Plugin.versionCheck.VersionCheckUtil;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PluginMain extends Plugin {
    private volatile String translateText;
    private volatile boolean startFlag = false;
    private volatile long startTime;
    public static boolean isNotExit = true;
    private static ImageIcon icon;
    private Color pluginLabelColor = new Color(0xcccccc);
    private Color pluginBackgroundColor = new Color(0x333333);

    /**
     * Do Not Remove, this is used for File-Engine to get message from the plugin.
     * You can show message using "displayMessage(String caption, String message)"
     *
     * @return String[2], the first string is caption, the second string is message.
     * @see #displayMessage(String, String)
     */
    public String[] getMessage() {
        return _getMessage();
    }

    /**
     * Do Not Remove, this is used for File-Engine to get results from the plugin
     * You can add result using "addToResultQueue(String result)".
     *
     * @return result
     * @see #addToResultQueue(String)
     */
    public String pollFromResultQueue() {
        return _pollFromResultQueue();
    }

    /**
     * Do Not Remove, this is used for File-Engine to check the API version.
     *
     * @return Api version
     */
    public int getApiVersion() {
        return _getApiVersion();
    }

    /**
     * Do Not Remove, this is used for File-Engine to clear results to prepare for the next time.
     *
     * @see #addToResultQueue(String)
     * @see #pollFromResultQueue()
     */
    public void clearResultQueue() {
        _clearResultQueue();
    }

    @Override
    public void setCurrentTheme(int defaultColor, int choseLabelColor, int borderColor) {
        pluginBackgroundColor = new Color(defaultColor);
        pluginLabelColor = new Color(choseLabelColor);
    }

    @Override
    public void openSettings() {
        Settings.getInstance().showWindow();
    }

    /**
     * When the search bar textChanged, this function will be called.
     *
     * @param text Example : When you input "&gt;examplePlugin TEST" to the search bar, the param will be "TEST"
     */
    @Override
    public void textChanged(String text) {
        if (!(text == null || text.isEmpty())) {
            if (">file".equalsIgnoreCase(text)) {
                FileTranslate.getInstance().showWindow();
            } else if (">set".equalsIgnoreCase(text)) {
                Settings.getInstance().showWindow();
            } else {
                translateText = text;
                startTime = System.currentTimeMillis();
                startFlag = true;
            }
        } else {
            translateText = "";
            startTime = System.currentTimeMillis();
            startFlag = false;
        }
    }

    /**
     * When File-Engine is starting, the function will be called.
     * You can initialize your plugin here
     */
    @Override
    public void loadPlugin() {
        Settings instance = Settings.getInstance();
        CachedThreadPool.getInstance().execute(() -> {
            long endTime;
            try {
                while (isNotExit) {
                    endTime = System.currentTimeMillis();
                    if ((endTime - startTime > 500) && startFlag) {
                        startFlag = false;
                        String fromLang = instance.getFromLang();
                        String toLang = instance.getToLang();
                        String result = "";
                        try {
                            result = TranslateUtil.getInstance().getTranslation(translateText, fromLang, toLang);
                        } catch (IOException | IllegalAccessException | InvocationTargetException ignored) {
                        }
                        addToResultQueue("翻译结果：");
                        addToResultQueue(result);
                    }
                    TimeUnit.MILLISECONDS.sleep(1500);
                }
            } catch (InterruptedException ignored) {
            }
        });
        JSONObject json = Settings.readSettings();
        String fromLang = json.getString("fromLang");
        String toLang = json.getString("toLang");
        instance.setFromLang(fromLang);
        instance.setToLang(toLang);
        instance.setFromLangName(instance.getAbbreviationByLangName(fromLang));
        instance.setToLangName(instance.getAbbreviationByLangName(toLang));
        icon = new ImageIcon(Objects.requireNonNull(PluginMain.class.getResource("/icon.png")));
    }

    /**
     * When File-Engine is closing, the function will be called.
     */
    @Override
    public void unloadPlugin() {
        isNotExit = false;
        CachedThreadPool.getInstance().shutdown();
    }

    /**
     * Invoked when a key has been released.See the class description for the swing KeyEvent for a definition of a key released event.
     * Notice : Up and down keys will not be included (key code 38 and 40 will not be included).
     *
     * @param e      KeyEvent, Which key on the keyboard is released.
     * @param result Currently selected content.
     */
    @Override
    public void keyReleased(KeyEvent e, String result) {
    }

    /**
     * Invoked when a key has been pressed. See the class description for the swing KeyEvent for a definition of a key pressed event.
     * Notice : Up and down keys will not be included (key code 38 and 40 will not be included).
     *
     * @param e      KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyPressed(KeyEvent e, String result) {
        if (e.getKeyCode() == 10) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable trans = new StringSelection(result);
            clipboard.setContents(trans, null);
        }
    }

    /**
     * Invoked when a key has been typed.See the class description for the swing KeyEvent for a definition of a key typed event.
     * Notice : Up and down keys will not be included (key code 38 and 40 will not be included).
     *
     * @param e      KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyTyped(KeyEvent e, String result) {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e      Mouse event
     * @param result Currently selected content.
     */
    @Override
    public void mousePressed(MouseEvent e, String result) {
        if (e.getClickCount() == 2) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable trans = new StringSelection(result);
            clipboard.setContents(trans, null);
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e      Mouse event
     * @param result Currently selected content
     */
    @Override
    public void mouseReleased(MouseEvent e, String result) {
    }

    /**
     * Get the plugin Icon. It can be the png, jpg.
     * Make the icon small, or it will occupy too much memory.
     *
     * @return icon
     */
    @Override
    public ImageIcon getPluginIcon() {
        return icon;
    }

    /**
     * Get the official site of the plugin.
     *
     * @return official site
     */
    @Override
    public String getOfficialSite() {
        return "https://github.com/XUANXUQAQ/File-Engine-Translate-Plugin";
    }

    /**
     * Get the plugin version.
     *
     * @return version
     */
    @Override
    public String getVersion() {
        return VersionCheckUtil._getPluginVersion();
    }

    /**
     * Get the description of the plugin.
     * Just write the description outside, and paste it to the return value.
     *
     * @return description
     */
    @Override
    public String getDescription() {
        return  "English instruction:\n" +
                "A plugin that enables File-Engine to quickly translate strings\n" +
                "How to use: >tr test --> return \"test\"\n" +
                "Type \">tr >set\" in the search box ---> open the setting window to select the translation language\n" +
                "Enter \">tr >file\" in the search box --->Open the file translation window:\n" +
                "File translation can automatically translate each line of text in the file to generate 2 files: withSource.txt withoutSource.txt\n" +
                "withSource.txt: displayed as \"original string = translation string\"\n" +
                "withoutSource.txt: displayed as \"translation string\"\n" +
                "\n" +
                "中文说明：\n" +
                "一个使File-Engine快速翻译字符串的插件\n" +
                "使用方法：>tr 测试 --> 返回 \"test\"\n" +
                "在搜索框中输入\" >tr >set \" ---> 打开设置窗口以选择翻译语言\n" +
                "在搜索框中输入 \" >tr >file \" --->打开文件翻译功能窗口：\n" +
                "文件翻译可以自动翻译文件中的每一行文本，生成2个文件：withSource.txt withoutSource.txt\n" +
                "withSource.txt ：显示为  \"原字符串=翻译字符串\"\n" +
                "withoutSource.txt ：显示为  \"翻译字符串\"" +
                "\n图标来自： https://icons8.com/icon/8GBVhNNqHiGC/翻译 icon by https://icons8.com ";
    }

    /**
     * Check if the current version is the latest.
     *
     * @return true or false
     * @see #getUpdateURL()
     */
    @Override
    public boolean isLatest() throws Exception {
        return VersionCheckUtil._isLatest();
    }

    /**
     * Get the plugin download url.
     * Invoke when the isLatest() returns false;
     *
     * @return download url
     * @see #isLatest()
     */
    @Override
    public String getUpdateURL() {
        return VersionCheckUtil._getUpdateURL();
    }

    /**
     * Show the content to the GUI.
     *
     * @param result   current selected content.
     * @param label    The label to be displayed.
     * @param isChosen If the label is being selected.
     *                 If so, you are supposed to set the label at a different background.
     */
    @Override
    public void showResultOnLabel(String result, JLabel label, boolean isChosen) {
        label.setIcon(icon);
        label.setText(result);
        if (isChosen) {
            label.setBackground(pluginLabelColor);
        } else {
            label.setBackground(pluginBackgroundColor);
        }
    }

    @Override
    public String getAuthor() {
        return "XUANXU";
    }
}
