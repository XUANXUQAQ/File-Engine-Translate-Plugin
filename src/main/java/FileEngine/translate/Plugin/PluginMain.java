package FileEngine.translate.Plugin;

import FileEngine.translate.Plugin.config.ConfigurationUtil;
import FileEngine.translate.Plugin.fileTranslate.FileTranslate;
import FileEngine.translate.Plugin.settings.Settings;
import FileEngine.translate.Plugin.threadPool.CachedThreadPool;
import FileEngine.translate.Plugin.translate.TranslateUtil;
import FileEngine.translate.Plugin.versionCheck.VersionCheckUtil;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class PluginMain extends Plugin {
    private volatile String translateText;
    private volatile boolean startFlag = false;
    private volatile long startTime;
    public static boolean isNotExit = true;

    /**
     * Do Not Remove, this is used for File-Engine to get message from the plugin.
     * You can show message using "displayMessage(String caption, String message)"
     * @return String[2], the first string is caption, the second string is message.
     * @see #displayMessage(String, String)
     */
    public String[] getMessage() {
        return _getMessage();
    }

    /**
     * Do Not Remove, this is used for File-Engine to get results from the plugin
     * You can add result using "addToResultQueue(String result)".
     * @see #addToResultQueue(String)
     * @return result
     */
    public String pollFromResultQueue() {
        return _pollFromResultQueue();
    }

    /**
     * Do Not Remove, this is used for File-Engine to check the API version.
     * @return Api version
     */
    public int getApiVersion() {
        return _getApiVersion();
    }

    /**
     * Do Not Remove, this is used for File-Engine to clear results to prepare for the next time.
     * @see #addToResultQueue(String)
     * @see #pollFromResultQueue()
     */
    public void clearResultQueue() {
        _clearResultQueue();
    }

    /**
     * When the search bar textChanged, this function will be called.
     * @param text
     * Example : When you input "&gt;examplePlugin TEST" to the search bar, the param will be "TEST"
     */
    @Override
    public void textChanged(String text) {
        if (!(text == null || text.isEmpty())) {
            if (">file".equals(text)) {
                FileTranslate.getInstance().showWindow();
            } else if (">settings".equals(text)) {
                Settings.getInstance().showWindow();
            } else {
                translateText = text;
                startTime = System.currentTimeMillis();
                startFlag = true;
            }
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
            try {
                long endTime;
                while (isNotExit) {
                    endTime = System.currentTimeMillis();
                    if ((endTime - startTime > 500) && startFlag) {
                        startFlag = false;
                        String fromLang = instance.getFromLang();
                        String toLang = instance.getToLang();
                        String result = TranslateUtil.getTranslation(translateText, fromLang, toLang);
                        addToResultQueue("翻译结果：");
                        addToResultQueue(result);
                        Thread.sleep(50);
                    }
                }
            }catch (InterruptedException ignored) {
            }
        });
        JSONObject json = ConfigurationUtil.readSettings();
        String fromLang = json.getString("fromLang");
        String toLang = json.getString("toLang");
        instance.setFromLang(fromLang);
        instance.setToLang(toLang);
        instance.setFromLangName(instance.getKeyByValue(fromLang));
        instance.setToLangName(instance.getKeyByValue(toLang));
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
     * @param e KeyEvent, Which key on the keyboard is released.
     * @param result Currently selected content.
     */
    @Override
    public void keyReleased(KeyEvent e, String result) {
    }

    /**
     * Invoked when a key has been pressed. See the class description for the swing KeyEvent for a definition of a key pressed event.
     * Notice : Up and down keys will not be included (key code 38 and 40 will not be included).
     * @param e KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyPressed(KeyEvent e, String result) {
        //todo 检测enter键以复制翻译结果
    }

    /**
     * Invoked when a key has been typed.See the class description for the swing KeyEvent for a definition of a key typed event.
     * Notice : Up and down keys will not be included (key code 38 and 40 will not be included).
     * @param e KeyEvent, Which key on the keyboard is pressed.
     * @param result Currently selected content.
     */
    @Override
    public void keyTyped(KeyEvent e, String result) {

    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e Mouse event
     * @param result Currently selected content.
     */
    @Override
    public void mousePressed(MouseEvent e, String result) {
        //todo 双击以复制翻译结果
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e Mouse event
     * @param result Currently selected content
     */
    @Override
    public void mouseReleased(MouseEvent e, String result) {
    }

    /**
     * Get the plugin Icon. It can be the png, jpg.
     * Make the icon small, or it will occupy too much memory.
     * @return icon
     */
    @Override
    public ImageIcon getPluginIcon() {
        return null;
    }

    /**
     * Get the official site of the plugin.
     * @return official site
     */
    @Override
    public String getOfficialSite() {
        return null;
    }

    /**
     * Get the plugin version.
     * @return version
     */
    @Override
    public String getVersion() {
        return VersionCheckUtil._getPluginVersion();
    }

    /**
     * Get the description of the plugin.
     * Just write the description outside, and paste it to the return value.
     * @return description
     */
    @Override
    public String getDescription() {
        return "A plugin to make File-Engine translate strings quickly.\n" +
                "Usage: >trans 测试  --> return \"test\"\n" +
                "一个使File-Engine快速翻译字符串的插件\n" +
                "使用方法：>trans 测试 --> 返回 \"test\"";
    }

    /**
     * Check if the current version is the latest.
     * @return true or false
     * @see #getUpdateURL()
     */
    @Override
    public boolean isLatest() {
        return VersionCheckUtil._isLatest();
    }

    /**
     * Get the plugin download url.
     * Invoke when the isLatest() returns false;
     * @see #isLatest()
     * @return download url
     */
    @Override
    public String getUpdateURL() {
        return VersionCheckUtil._getUpdateURL();
    }

    /**
     * Show the content to the GUI.
     * @param result current selected content.
     * @param label The label to be displayed.
     * @param isChosen If the label is being selected.
     *                 If so, you are supposed to set the label at a different background.
     */
    @Override
    public void showResultOnLabel(String result, JLabel label, boolean isChosen) {

    }

    @Override
    public String getAuthor() {
        return "XUANXU";
    }
}
