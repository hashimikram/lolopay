package com.mangopay.entities;

import com.google.gson.annotations.SerializedName;

/** Transfer entity. */
public class Settlement extends Transfer {
  /** Repudiation Id */
  @SerializedName("RepudiationId")
  private String repudiationId;

  /** @return the repudiationId */
  public String getRepudiationId() {

    return repudiationId;
  }

  /** @param repudiationId the repudiationId to set */
  public void setRepudiationId(String repudiationId) {

    this.repudiationId = repudiationId;
  }
}
