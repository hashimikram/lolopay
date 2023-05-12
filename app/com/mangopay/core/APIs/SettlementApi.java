package com.mangopay.core.APIs;

import com.mangopay.entities.Settlement;
import com.mangopay.entities.SettlementTransfer;

public interface SettlementApi {

  /**
   * Get settlement transfer entity
   *
   * @param id settlement identifier
   * @return settlement entity returned from API
   * @throws Exception
   */
  SettlementTransfer get(String id) throws Exception;

  /**
   * Get full object M3 settlement transfer entity
   *
   * @param id settlement identifier
   * @return settlement entity returned from API
   * @throws Exception
   */
  Settlement getSettlement(String id) throws Exception;
}
