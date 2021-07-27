package com.akash.dto;

public class RLThreshold {

  // time unit will be in seconds
  private Integer timeWindow;
  private Integer threshold;
  private String key;
  private Long lastWindowTime;
  private String epoch;

  public RLThreshold(Integer timeWindow, Integer threshold) {
    this.timeWindow = timeWindow;
    this.threshold = threshold;
    lastWindowTime = System.currentTimeMillis() - timeWindow.longValue();
    epoch = String.valueOf(System.currentTimeMillis());
  }

  public void setKey(String clientId) {
    if (key == null) {
      key = clientId + "-" + timeWindow + "-" + threshold;
    }
  }

  public Integer getTimeWindow() {
    return timeWindow;
  }

  public Integer getThreshold() {
    return threshold;
  }

  public String getKey() {
    return key;
  }

  public Long getLastWindowTime() {
    return lastWindowTime;
  }

  public String getEpoch() {
    return epoch;
  }
}
