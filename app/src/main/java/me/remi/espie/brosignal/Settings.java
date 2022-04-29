package me.remi.espie.brosignal;

/**
 * Singleton stockant les paramètres utilisateurs en RAM
 */
public class Settings {
    private boolean spam;
    private boolean showNumbers;
    private String broName;
    private String customMessage;
    private static Settings single_instance = null;

    public boolean isSpam() {
        return spam;
    }

    public boolean isShowNumbers() {
        return showNumbers;
    }

    public void setShowNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
    }

    public String getBroName() {
        return broName;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    public void setBroName(String broName) {
        this.broName = broName;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    private Settings(String broName, String customMessage, boolean spam, boolean showNumbers) {
        this.broName = broName;
        this.customMessage = customMessage;
        this.spam = spam;
        this.showNumbers=showNumbers;
    }

    public static Settings getInstance(String broName, String customMessage, boolean spam, boolean showNumbers) {
        if (single_instance==null) single_instance = new Settings(broName, customMessage, spam, showNumbers);
        return single_instance;
    }

    public static Settings getInstance() {
        return single_instance;
    }

    public Settings setInstance() {
        single_instance = this;
        return single_instance;
    }
}
