package com.mangopay.core.APIs.implementation;

import com.mangopay.MangoPayApi;
import com.mangopay.core.APIs.ApiBase;
import com.mangopay.core.APIs.SettlementApi;
import com.mangopay.entities.Settlement;
import com.mangopay.entities.SettlementTransfer;

/** API for settlements */
public class SettlementApiImpl extends ApiBase implements SettlementApi {

  public SettlementApiImpl(MangoPayApi root) {

    super(root);
  }

  @Override
  public SettlementTransfer get(String id) throws Exception {

    return this.getObject(SettlementTransfer.class, "settlements_get", id);
  }

  @Override
  public Settlement getSettlement(String id) throws Exception {

    return this.getObject(Settlement.class, "settlements_get", id);
  }
}
