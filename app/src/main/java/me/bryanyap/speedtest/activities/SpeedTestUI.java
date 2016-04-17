package me.bryanyap.speedtest.activities;

public interface SpeedTestUI {
    void setStatusText(String input);
    void setSpeedText(String input);
    void setTestButtonText(String input);
    void notifyDone();
    void notifyCancelled();
}
