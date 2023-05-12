package ro.iss.lolopay.models.classes;

public enum BankCardStatus {
  ISSUED, // 0 – Issued not Active - Card has been issued, PIN not received yet
  OPEN, // 1 - Open - PIN received, card is ready to be used;
  // - a card can get to this status if previously it was in status “C - PNV” and now we realise
  // that the user is not a fraudulent person;
  // - a card can get to this status if previously it was in status “Q - TB”;
  // - a card can get to this status if previously it was in status “4 - Deposit Only”
  LOST, // 2 – Lost - The user has declared it at lost; it is not possible to move back from this
        // status to status 1 - Open.
  STOLEN, // 3 - Stolen - Same as status 2 - Lost.
  BLOCKED_PAYOUT, // 4 - Deposit Only - Can be manually set by an admin user who wants to block the
                  // debits from the card
  BLOCKED_FINAL, // 9 - Closed - This is the status in which a “C - PNV” status can get into if the
                 // user actually committed fraud. An admin user who wants to block the card can set
                 // this status.
  EXPIRED, // E - Expired - This is a status automatically set when the expiry date written on the
           // card has been met.
  BLOCKED_PIN, // Q – Temporary Block (TB) - A card will get to this status if the user types the
               // PIN wrong for 3 times (does not need to be consecutive times). In this case, once
               // the user has contacted us, the status can be put back to “1 - Open”, which will
               // reset wrong PIN counts as well.
  BLOCKED_FRAUD // C - Phone Number Verification (PNV) - This is a status that an admin user can set
                // when he/she wants to mark as user as possibly fraudulent.
}
