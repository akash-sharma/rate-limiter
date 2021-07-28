package com.akash.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RLConfig {

  private String clientId;
  private List<RLThreshold> rlThresholds = new ArrayList<>();

  public RLConfig(String clientId, List<RLThreshold> rlThresholds) {
    this.clientId = clientId;
    this.rlThresholds = rlThresholds;
    for (RLThreshold rlThreshold : rlThresholds) {
      rlThreshold.setKey(clientId);
    }
  }

  public String getClientId() {
    return clientId;
  }

  public List<RLThreshold> getRlThresholds() {
    return Collections.unmodifiableList(rlThresholds);
  }

  @Override
  public String toString() {
    return "RLConfig{" + "clientId='" + clientId + '\'' + ", rlThresholds=" + rlThresholds + '}';
  }
}
