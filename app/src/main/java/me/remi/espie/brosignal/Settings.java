package me.remi.espie.brosignal;

public class Settings {
    private boolean spam;
    private String broName;
    private String customMessage;

    public boolean isSpam() {
        return spam;
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

    public Settings(String broName, String customMessage, boolean spam) {
        this.broName = broName;
        this.customMessage = customMessage;
        this.spam = spam;
    }
}
