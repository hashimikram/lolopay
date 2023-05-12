package ro.iss.lolopay.programs.classes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.BankAccountType;
import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.models.classes.CountryISO;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.DocumentRejectReason;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.DocumentType;
import ro.iss.lolopay.models.classes.ExecutionType;
import ro.iss.lolopay.models.classes.IncomeRange;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.PaymentType;
import ro.iss.lolopay.models.classes.RefundReasonType;
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.classes.WalletType;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.BankAccountCA;
import ro.iss.lolopay.models.database.BankAccountGB;
import ro.iss.lolopay.models.database.BankAccountIBAN;
import ro.iss.lolopay.models.database.BankAccountOTHER;
import ro.iss.lolopay.models.database.BankAccountUS;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.definition.DatabaseService;

public class LoLoClient {
  public static void mapCSVRecordToLoloPayInRefund(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    Wallet creditWallet =
        getUserWallet(dbService, account, csvRecord.get(CSVTransCol.CreditedWalletId));

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) {
      feeId = new ObjectId().toString();
    }

    PayIn payInRefund = new PayIn();
    payInRefund.setId(csvRecord.get(CSVTransCol.Id));
    payInRefund.setProviderId(csvRecord.get(CSVTransCol.Id));
    payInRefund.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    payInRefund.setCreditedUserId(creditWallet.getUserId());
    payInRefund.setCreditedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));

    // DebitedFundsCurrency - we put the entire transaction which appear on the record
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.DebitedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.DebitedFundsAmount))));
    payInRefund.setAmount(amount);

    payInRefund.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    payInRefund.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    payInRefund.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      payInRefund.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    payInRefund.setType(TransactionType.PAYIN);
    payInRefund.setNature(TransactionNature.REFUND);

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      payInRefund.setRelatedTransactionId(feeId);
    }

    // execution type
    if (!csvRecord.get(CSVTransCol.ExecutionType).equals("")) {
      payInRefund.setExecutionType(ExecutionType.valueOf(csvRecord.get(CSVTransCol.ExecutionType)));
    }

    // creation date
    payInRefund.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    // save pay in
    dbService.getConnection(account.getId()).save(payInRefund);

    // process fee
    if (!feeId.equals("")) {
      // get account wallet with teh same currency as the transfer fee
      Wallet accountWallet =
          getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.FeesCurrency));

      // process fee as well
      Transfer payInRefundFee = new Transfer();
      payInRefundFee.setId(feeId);
      payInRefundFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      payInRefundFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      payInRefundFee.setDebitedUserId(accountWallet.getUserId());
      payInRefundFee.setDebitedWalletId(accountWallet.getId());
      payInRefundFee.setCreditedUserId(creditWallet.getUserId());
      payInRefundFee.setCreditedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));

      // CreditedFundsAmount
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      payInRefundFee.setAmount(amountFee);

      payInRefundFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      payInRefundFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      payInRefundFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        payInRefundFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      payInRefundFee.setType(TransactionType.PAYIN_FEE);
      payInRefundFee.setNature(TransactionNature.REFUND);

      payInRefundFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      payInRefundFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(payInRefundFee);
    }
  }

  public static void mapCSVRecordToLoloPayOutRegular(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    Wallet debitWallet =
        getUserWallet(dbService, account, csvRecord.get(CSVTransCol.DebitedWalletId));

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) {
      feeId = new ObjectId().toString();
    }

    PayOut payOutRegular = new PayOut();
    payOutRegular.setId(csvRecord.get(CSVTransCol.Id));
    payOutRegular.setProviderId(csvRecord.get(CSVTransCol.Id));
    payOutRegular.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    payOutRegular.setDebitedUserId(debitWallet.getUserId());
    payOutRegular.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));

    // CreditedFundsAmount - we put the entire transaction which appear on the record
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.CreditedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.CreditedFundsAmount))));
    payOutRegular.setAmount(amount);

    payOutRegular.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    payOutRegular.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    payOutRegular.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      payOutRegular.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    payOutRegular.setType(TransactionType.PAYOUT);
    payOutRegular.setNature(TransactionNature.REGULAR);

    // payout details
    if (csvRecord.get(CSVTransCol.PaymentType).equals("BANKWIRE")) {
      payOutRegular.setPaymentType(PaymentType.BANK_WIRE);
    } else {
      payOutRegular.setPaymentType(PaymentType.CARD);
    }
    payOutRegular.setBankAccountId(csvRecord.get(CSVTransCol.BankAccountId));
    payOutRegular.setBankWireRef(csvRecord.get(CSVTransCol.BankWireRef));

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      payOutRegular.setRelatedTransactionId(feeId);
    }

    // creation date
    payOutRegular.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    // save pay in
    dbService.getConnection(account.getId()).save(payOutRegular);

    // process fee
    if (!feeId.equals("")) {
      // get account wallet with teh same currency as the transfer fee
      Wallet accountWallet =
          getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.FeesCurrency));

      // process fee as well
      Transfer payOutRegularFee = new Transfer();
      payOutRegularFee.setId(feeId);
      payOutRegularFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      payOutRegularFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      payOutRegularFee.setDebitedUserId(debitWallet.getUserId());
      payOutRegularFee.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));
      payOutRegularFee.setCreditedUserId(accountWallet.getUserId());
      payOutRegularFee.setCreditedWalletId(accountWallet.getId());

      // CreditedFundsAmount
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      payOutRegularFee.setAmount(amountFee);

      payOutRegularFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      payOutRegularFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      payOutRegularFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        payOutRegularFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      payOutRegularFee.setType(TransactionType.PAYOUT_FEE);
      payOutRegularFee.setNature(TransactionNature.REGULAR);

      payOutRegularFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      payOutRegularFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(payOutRegularFee);
    }
  }

  public static void mapCSVRecordToLoloPayOutRefund(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    Wallet debitWallet =
        getUserWallet(dbService, account, csvRecord.get(CSVTransCol.DebitedWalletId));

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) {
      feeId = new ObjectId().toString();
    }

    PayOut payOutRefund = new PayOut();
    payOutRefund.setId(csvRecord.get(CSVTransCol.Id));
    payOutRefund.setProviderId(csvRecord.get(CSVTransCol.Id));
    payOutRefund.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    payOutRefund.setDebitedUserId(debitWallet.getUserId());
    payOutRefund.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));

    // CreditedFundsAmount - we put the entire transaction which appear on the record
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.CreditedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.CreditedFundsAmount))));
    payOutRefund.setAmount(amount);

    payOutRefund.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    payOutRefund.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    payOutRefund.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      payOutRefund.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    payOutRefund.setType(TransactionType.PAYOUT);
    payOutRefund.setNature(TransactionNature.REFUND);

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      payOutRefund.setRelatedTransactionId(feeId);
    }

    // creation date
    payOutRefund.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    // save pay out
    dbService.getConnection(account.getId()).save(payOutRefund);

    // process fee
    if (!feeId.equals("")) {
      // get account wallet with the same currency as the transfer fee
      Wallet accountWallet =
          getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.FeesCurrency));

      // process fee as well
      Transfer payOutRefundFee = new Transfer();
      payOutRefundFee.setId(feeId);
      payOutRefundFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      payOutRefundFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));

      payOutRefundFee.setDebitedUserId(debitWallet.getUserId());
      payOutRefundFee.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));

      payOutRefundFee.setCreditedUserId(accountWallet.getUserId());
      payOutRefundFee.setCreditedWalletId(accountWallet.getId());

      // CreditedFundsAmount
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      payOutRefundFee.setAmount(amountFee);

      payOutRefundFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      payOutRefundFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      payOutRefundFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        payOutRefundFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      payOutRefundFee.setType(TransactionType.PAYOUT_FEE);
      payOutRefundFee.setNature(TransactionNature.REFUND);

      payOutRefundFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      payOutRefundFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(payOutRefundFee);
    }
  }

  public static void mapCSVRecordToLoloPayInRegular(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) {
      feeId = new ObjectId().toString();
    }

    PayIn payInRegular = new PayIn();
    payInRegular.setId(csvRecord.get(CSVTransCol.Id));
    payInRegular.setProviderId(csvRecord.get(CSVTransCol.Id));
    payInRegular.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    payInRegular.setCreditedUserId(csvRecord.get(CSVTransCol.CreditedUserId));
    payInRegular.setCreditedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));

    // DebitedFundsCurrency - we put the entire transaction which appear on the record
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.DebitedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.DebitedFundsAmount))));
    payInRegular.setAmount(amount);

    payInRegular.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    payInRegular.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    payInRegular.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      payInRegular.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    payInRegular.setType(TransactionType.PAYIN);
    payInRegular.setNature(TransactionNature.REGULAR);

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      payInRegular.setRelatedTransactionId(feeId);
    }

    // payment type
    if (csvRecord.get(CSVTransCol.PaymentType).equals("BANKWIRE")) {
      payInRegular.setPaymentType(PaymentType.BANK_WIRE);
    } else {
      payInRegular.setPaymentType(PaymentType.CARD);
    }

    // card type
    if (csvRecord.get(CSVTransCol.CardType).equals("CB")) {
      payInRegular.setCardType(CardType.CB_VISA_MASTERCARD);
    }

    // execution type
    payInRegular.setExecutionType(ExecutionType.valueOf(csvRecord.get(CSVTransCol.ExecutionType)));

    // creation date
    payInRegular.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    // save pay in
    dbService.getConnection(account.getId()).save(payInRegular);

    // process fee
    if (!feeId.equals("")) {
      // get account wallet with teh same currency as the transfer fee
      Wallet accountWallet =
          getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.FeesCurrency));

      // process fee as well
      Transfer transferRegularFee = new Transfer();
      transferRegularFee.setId(feeId);
      transferRegularFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      transferRegularFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      transferRegularFee.setDebitedUserId(csvRecord.get(CSVTransCol.CreditedUserId));
      transferRegularFee.setDebitedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));
      transferRegularFee.setCreditedUserId(accountWallet.getUserId());
      transferRegularFee.setCreditedWalletId(accountWallet.getId());

      // CreditedFundsAmount
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      transferRegularFee.setAmount(amountFee);

      transferRegularFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      transferRegularFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      transferRegularFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        transferRegularFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      transferRegularFee.setType(TransactionType.PAYIN_FEE);
      transferRegularFee.setNature(TransactionNature.REGULAR);

      transferRegularFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      transferRegularFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(transferRegularFee);
    }
  }

  public static void mapCSVRecordToLoloTransferRefund(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    // init var fee id to be used later
    String feeId = "";

    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) // FeesAmount
    {
      feeId = new ObjectId().toString();
    }

    // get account wallet - by transaction amount currency
    Wallet accountWallet =
        getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.CreditedFundsCurrency));
    Wallet debitWallet =
        getUserWallet(dbService, account, csvRecord.get(CSVTransCol.DebitedWalletId));
    //

    Refund transferRefund = new Refund();
    transferRefund.setId(csvRecord.get(CSVTransCol.Id));
    transferRefund.setProviderId(csvRecord.get(CSVTransCol.Id));
    transferRefund.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    transferRefund.setDebitedUserId(debitWallet.getUserId());
    transferRefund.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));
    transferRefund.setCreditedUserId(csvRecord.get(CSVTransCol.CreditedUserId));
    transferRefund.setCreditedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));

    // CreditedFundsAmount - money taken from user to be put in account wallet (all amount including
    // fee)
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.CreditedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.CreditedFundsAmount))));
    transferRefund.setAmount(amount);

    transferRefund.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    transferRefund.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    transferRefund.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      transferRefund.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    transferRefund.setType(TransactionType.TRANSFER);
    transferRefund.setNature(TransactionNature.REFUND);

    transferRefund.setInitialTransactionId("");
    transferRefund.setInitialTransactionType(TransactionType.TRANSFER);
    transferRefund.setRefundReasonType(RefundReasonType.OTHER);
    transferRefund.setRefusedReasonMessage("");

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      transferRefund.setRelatedTransactionId(feeId);
    }

    // creation date
    transferRefund.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    // save refund
    dbService.getConnection(account.getId()).save(transferRefund);

    // process fee
    if (!feeId.equals("")) {
      // process fee as well
      Transfer transferRefundFee = new Transfer();
      transferRefundFee.setId(feeId);
      transferRefundFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      transferRefundFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      transferRefundFee.setDebitedUserId(accountWallet.getUserId());
      transferRefundFee.setDebitedWalletId(accountWallet.getId());
      transferRefundFee.setCreditedUserId(debitWallet.getUserId());
      transferRefundFee.setCreditedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));

      // FeesAmount given back to the user
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      transferRefundFee.setAmount(amountFee);

      transferRefundFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      transferRefundFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      transferRefundFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        transferRefundFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      transferRefundFee.setType(TransactionType.TRANSFER_FEE);
      transferRefundFee.setNature(TransactionNature.REFUND);

      transferRefundFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      transferRefundFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(transferRefundFee);
    }
  }

  public static void mapCSVRecordToLoloTransferSettlement(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) // FeesAmount
    {
      feeId = new ObjectId().toString();
    }

    // get account wallet - by transaction amout currency
    Wallet accountWallet =
        getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.CreditedFundsCurrency));

    Transfer transferSettlement = new Transfer();
    transferSettlement.setId(csvRecord.get(CSVTransCol.Id));
    transferSettlement.setProviderId(csvRecord.get(CSVTransCol.Id));
    transferSettlement.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    transferSettlement.setDebitedUserId(csvRecord.get(CSVTransCol.AuthorId));
    transferSettlement.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));
    transferSettlement.setCreditedUserId(accountWallet.getUserId());
    transferSettlement.setCreditedWalletId(accountWallet.getId());

    // CreditedFundsAmount - money taken from user to be put in account wallet
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.CreditedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.CreditedFundsAmount))));
    transferSettlement.setAmount(amount);

    transferSettlement.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    transferSettlement.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    transferSettlement.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      transferSettlement.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    transferSettlement.setType(TransactionType.TRANSFER);
    transferSettlement.setNature(TransactionNature.SETTLEMENT);

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      transferSettlement.setRelatedTransactionId(feeId);
    }

    // creation date
    transferSettlement.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    dbService.getConnection(account.getId()).save(transferSettlement);

    // process fee
    if (!feeId.equals("")) {
      // process fee as well
      Transfer transferSettlementFee = new Transfer();
      transferSettlementFee.setId(feeId);
      transferSettlementFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      transferSettlementFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      transferSettlementFee.setDebitedUserId(accountWallet.getUserId());
      transferSettlementFee.setDebitedWalletId(accountWallet.getId());
      transferSettlementFee.setCreditedUserId(csvRecord.get(CSVTransCol.AuthorId));
      transferSettlementFee.setCreditedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));

      // FeesAmount given back to the user
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      transferSettlementFee.setAmount(amountFee);

      transferSettlementFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      transferSettlementFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      transferSettlementFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        transferSettlementFee.setExecutionDate(
            Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      transferSettlementFee.setType(TransactionType.TRANSFER_FEE);
      transferSettlementFee.setNature(TransactionNature.SETTLEMENT);

      transferSettlementFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      transferSettlementFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(transferSettlementFee);
    }
  }

  public static void mapCSVRecordToLoloTransferRegular(
      DatabaseService dbService, Account account, CSVRecord csvRecord) {

    String feeId = "";
    // check for fee
    if (!csvRecord.get(CSVTransCol.FeesAmount).equals("")
        && !csvRecord.get(CSVTransCol.FeesAmount).equals("0")) {
      feeId = new ObjectId().toString();
    }

    Transfer transferRegular = new Transfer();
    transferRegular.setId(csvRecord.get(CSVTransCol.Id));
    transferRegular.setProviderId(csvRecord.get(CSVTransCol.Id));
    transferRegular.setCustomTag(csvRecord.get(CSVTransCol.Tag));
    transferRegular.setDebitedUserId(csvRecord.get(CSVTransCol.AuthorId));
    transferRegular.setDebitedWalletId(csvRecord.get(CSVTransCol.DebitedWalletId));
    transferRegular.setCreditedUserId(csvRecord.get(CSVTransCol.CreditedUserId));
    transferRegular.setCreditedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));

    // DebitedFundsCurrency - we put the entire transaction which appear on the record
    Amount amount = new Amount();
    amount.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.DebitedFundsCurrency)));
    amount.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.DebitedFundsAmount))));
    transferRegular.setAmount(amount);

    transferRegular.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
    transferRegular.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
    transferRegular.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

    if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
      transferRegular.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
    }

    transferRegular.setType(TransactionType.TRANSFER);
    transferRegular.setNature(TransactionNature.REGULAR);

    // check for fee again
    if (!feeId.equals("")) {
      // we have fee
      transferRegular.setRelatedTransactionId(feeId);
    }

    // creation date
    transferRegular.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

    dbService.getConnection(account.getId()).save(transferRegular);

    // process fee
    if (!feeId.equals("")) {
      // get account wallet with teh same currency as the transfer fee
      Wallet accountWallet =
          getAccountWallet(dbService, account, csvRecord.get(CSVTransCol.FeesCurrency));

      // process fee as well
      Transfer transferRegularFee = new Transfer();
      transferRegularFee.setId(feeId);
      transferRegularFee.setProviderId(csvRecord.get(CSVTransCol.Id));
      transferRegularFee.setCustomTag(csvRecord.get(CSVTransCol.Tag));
      transferRegularFee.setDebitedUserId(csvRecord.get(CSVTransCol.CreditedUserId));
      transferRegularFee.setDebitedWalletId(csvRecord.get(CSVTransCol.CreditedWalletId));
      transferRegularFee.setCreditedUserId(accountWallet.getUserId());
      transferRegularFee.setCreditedWalletId(accountWallet.getId());

      // CreditedFundsAmount
      Amount amountFee = new Amount();
      amountFee.setCurrency(CurrencyISO.valueOf(csvRecord.get(CSVTransCol.FeesCurrency)));
      amountFee.setValue(Math.abs(Integer.valueOf(csvRecord.get(CSVTransCol.FeesAmount))));
      transferRegularFee.setAmount(amountFee);

      transferRegularFee.setStatus(TransactionStatus.valueOf(csvRecord.get(CSVTransCol.Status)));
      transferRegularFee.setResultCode(csvRecord.get(CSVTransCol.ResultCode));
      transferRegularFee.setResultMessage(csvRecord.get(CSVTransCol.ResultMessage));

      if (!csvRecord.get(CSVTransCol.ExecutionDate).equals("")) {
        transferRegularFee.setExecutionDate(Long.valueOf(csvRecord.get(CSVTransCol.ExecutionDate)));
      }

      transferRegularFee.setType(TransactionType.TRANSFER_FEE);
      transferRegularFee.setNature(TransactionNature.REGULAR);

      transferRegularFee.setRelatedTransactionId(csvRecord.get(CSVTransCol.Id));
      transferRegularFee.setCreatedAt(Long.valueOf(csvRecord.get(CSVTransCol.CreationDate)));

      dbService.getConnection(account.getId()).save(transferRegularFee);
    }
  }

  public static Document mapJsonToLoloDocument(JsonNode jsonDocument) {

    // create document object
    Document newDocument = new Document();

    // id
    if (jsonDocument.has("id")) {
      newDocument.setId(jsonDocument.findPath("id").asText());
      newDocument.setProviderId(jsonDocument.findPath("id").asText());
    }

    // tag
    if (jsonDocument.has("tag")) {
      newDocument.setCustomTag(getString(jsonDocument.findPath("tag").asText()));
    }

    // userId
    if (jsonDocument.has("userId")) {
      newDocument.setUserId(jsonDocument.findPath("userId").asText());
    }

    // type
    if (jsonDocument.has("type")) {
      String type = jsonDocument.findPath("type").asText();
      newDocument.setType(DocumentType.valueOf(type));
    }

    // status
    if (jsonDocument.has("status")) {
      String status = jsonDocument.findPath("status").asText();
      newDocument.setStatus(DocumentStatus.valueOf(status));
    }

    // status
    if (jsonDocument.has("refusedReasonType")) {
      String refusedReasonType = getString(jsonDocument.findPath("refusedReasonType").asText());
      if (refusedReasonType.equals("")) {
        newDocument.setRejectionReasonType(null);
      } else {
        newDocument.setRejectionReasonType(DocumentRejectReason.valueOf(refusedReasonType));
      }
    }

    // rejectionReasonMessage
    if (jsonDocument.has("rejectionReasonMessage")) {
      newDocument.setRejectionReasonMessage(
          getString(jsonDocument.findPath("rejectionReasonMessage").asText()));
    }

    // creationDate
    if (jsonDocument.has("creationDate")) {
      newDocument.setCreatedAt(jsonDocument.findPath("creationDate").asLong());
    }

    // pages
    List<String> pages = new ArrayList<>();
    newDocument.setPages(pages);

    return newDocument;
  }

  private static void mapBankAccountCommon(BankAccount bankAccount, JsonNode jsonBankAccount) {

    // id
    if (jsonBankAccount.has("id")) {
      bankAccount.setId(jsonBankAccount.findPath("id").asText());
      bankAccount.setProviderId(jsonBankAccount.findPath("id").asText());
    }

    // tag
    if (jsonBankAccount.has("tag")) {
      bankAccount.setCustomTag(getString(jsonBankAccount.findPath("tag").asText()));
    }

    // userId
    if (jsonBankAccount.has("userId")) {
      bankAccount.setUserId(jsonBankAccount.findPath("userId").asText());
    }

    // type
    bankAccount.setType(BankAccountType.IBAN);

    // ownerName
    if (jsonBankAccount.has("ownerName")) {
      bankAccount.setOwnerName(getString(jsonBankAccount.findPath("ownerName").asText()));
    }

    // ownerAddress
    if (jsonBankAccount.has("ownerAddress")) {
      // get response body
      JsonNode addressJson = jsonBankAccount.findPath("address");

      // create address
      Address address = new Address();

      // addressLine1
      if (addressJson.has("addressLine1")) {
        address.setAddressLine1(getString(addressJson.findPath("addressLine1").asText()));
      }

      // addressLine2
      if (addressJson.has("addressLine2")) {
        address.setAddressLine2(getString(addressJson.findPath("addressLine2").asText()));
      }

      // city
      if (addressJson.has("city")) {
        address.setCity(getString(addressJson.findPath("city").asText()));
      }

      // region
      if (addressJson.has("region")) {
        address.setCounty(getString(addressJson.findPath("region").asText()));
      }

      // country
      if (addressJson.has("country")) {
        String country = getString(addressJson.findPath("country").asText());
        address.setCountry(CountryISO.valueOf(country));
      }

      // postalCode
      if (addressJson.has("postalCode")) {
        address.setPostalCode(getString(addressJson.findPath("postalCode").asText()));
      }

      bankAccount.setOwnerAddress(address);
    }

    // active
    if (jsonBankAccount.has("active")) {
      bankAccount.setActive(jsonBankAccount.findPath("active").asBoolean(true));
    }

    // creationDate
    if (jsonBankAccount.has("creationDate")) {
      bankAccount.setCreatedAt(jsonBankAccount.findPath("creationDate").asLong());
    }
  }

  public static BankAccountIBAN mapJsonToBankAccountIBAN(JsonNode jsonBankAccountIBAN) {

    BankAccountIBAN baIBAN = new BankAccountIBAN();

    // map bank account common fields
    mapBankAccountCommon(baIBAN, jsonBankAccountIBAN);

    // details
    if (jsonBankAccountIBAN.has("details")) {

      // get details
      JsonNode details = jsonBankAccountIBAN.findPath("details");

      // iban
      if (details.has("iban")) {
        baIBAN.setIban(getString(details.findPath("iban").asText()));
      }

      // bic
      if (details.has("bic")) {
        baIBAN.setBic(getString(details.findPath("bic").asText()));
      }
    }

    return baIBAN;
  }

  public static BankAccountGB mapJsonToBankAccountGB(JsonNode jsonBankAccountGB) {

    BankAccountGB baGB = new BankAccountGB();

    // map bank account common fields
    mapBankAccountCommon(baGB, jsonBankAccountGB);

    // details
    if (jsonBankAccountGB.has("details")) {

      // get details
      JsonNode details = jsonBankAccountGB.findPath("details");

      // accountNumber
      if (details.has("accountNumber")) {
        baGB.setAccountNumber(getString(details.findPath("accountNumber").asText()));
      }

      // sortCode
      if (details.has("sortCode")) {
        baGB.setSortCode(getString(details.findPath("sortCode").asText()));
      }
    }

    return baGB;
  }

  public static BankAccountUS mapJsonToBankAccountUS(JsonNode jsonBankAccountUS) {

    BankAccountUS baUS = new BankAccountUS();

    // map bank account common fields
    mapBankAccountCommon(baUS, jsonBankAccountUS);

    // details
    if (jsonBankAccountUS.has("details")) {

      // get details
      JsonNode details = jsonBankAccountUS.findPath("details");

      // accountNumber
      if (details.has("accountNumber")) {
        baUS.setAccountNumber(getString(details.findPath("accountNumber").asText()));
      }

      // aba
      if (details.has("aba")) {
        baUS.setAba(getString(details.findPath("aba").asText()));
      }

      // depositAccountType
      if (details.has("depositAccountType")) {
        baUS.setDepositAccountType(
            ro.iss.lolopay.models.classes.DepositAccountType.valueOf(
                getString(details.findPath("depositAccountType").asText())));
      }
    }

    return baUS;
  }

  public static BankAccountCA mapJsonToBankAccountCA(JsonNode jsonBankAccountCA) {

    BankAccountCA baCA = new BankAccountCA();

    // map bank account common fields
    mapBankAccountCommon(baCA, jsonBankAccountCA);

    // details
    if (jsonBankAccountCA.has("details")) {

      // get details
      JsonNode details = jsonBankAccountCA.findPath("details");

      // accountNumber
      if (details.has("accountNumber")) {
        baCA.setAccountNumber(getString(details.findPath("accountNumber").asText()));
      }

      // branchCode
      if (details.has("branchCode")) {
        baCA.setBranchCode(getString(details.findPath("branchCode").asText()));
      }

      // institutionNumber
      if (details.has("institutionNumber")) {
        baCA.setInstitutionNumber(getString(details.findPath("institutionNumber").asText()));
      }

      // bankName
      if (details.has("bankName")) {
        baCA.setInstitutionNumber(getString(details.findPath("bankName").asText()));
      }
    }

    return baCA;
  }

  public static BankAccountOTHER mapJsonToBankAccountOTHER(JsonNode jsonBankAccountOTHER) {

    BankAccountOTHER baOTHER = new BankAccountOTHER();

    // map bank account common fields
    mapBankAccountCommon(baOTHER, jsonBankAccountOTHER);

    // details
    if (jsonBankAccountOTHER.has("details")) {

      // get details
      JsonNode details = jsonBankAccountOTHER.findPath("details");

      // accountNumber
      if (details.has("accountNumber")) {
        baOTHER.setAccountNumber(getString(details.findPath("accountNumber").asText()));
      }

      // bic
      if (details.has("bic")) {
        baOTHER.setBic(getString(details.findPath("bic").asText()));
      }

      // country
      if (details.has("country")) {
        baOTHER.setCountry(
            ro.iss.lolopay.models.classes.CountryISO.valueOf(
                getString(details.findPath("country").asText())));
      }
    }

    return baOTHER;
  }

  public static Wallet mapJsonToLoloWallet(JsonNode jsonWallet) {

    // create wallet object
    Wallet newWallet = new Wallet();

    // id
    if (jsonWallet.has("id")) {
      newWallet.setId(jsonWallet.findPath("id").asText());
      newWallet.setProviderId(jsonWallet.findPath("id").asText());
    }

    // tag
    if (jsonWallet.has("tag")) {
      newWallet.setCustomTag(getString(jsonWallet.findPath("tag").asText()));
    }

    // owners
    if (jsonWallet.has("owners")) {
      ArrayNode jsonOwners = (ArrayNode) jsonWallet.findPath("owners");
      newWallet.setUserId(jsonOwners.get(0).asText());
    }

    // description
    if (jsonWallet.has("description")) {
      newWallet.setDescription(getString(jsonWallet.findPath("description").asText()));
    }

    // fundsType
    newWallet.setType(WalletType.USER);

    // balance
    if (jsonWallet.has("balance")) {
      JsonNode walletAmountJson = jsonWallet.findPath("balance");

      // create amount
      Amount walletAmount = new Amount();

      // currency
      if (walletAmountJson.has("currency")) {
        walletAmount.setCurrency(
            CurrencyISO.valueOf(getString(walletAmountJson.findPath("currency").asText())));
      }

      // amount
      if (walletAmountJson.has("amount")) {
        walletAmount.setValue(walletAmountJson.findPath("amount").asInt());
      }

      newWallet.setBalance(walletAmount);
    }

    // currency
    if (jsonWallet.has("currency")) {
      newWallet.setCurrency(
          CurrencyISO.valueOf(getString(jsonWallet.findPath("currency").asText())));
    }

    // blocked balance
    Amount walletBlockedAmount = new Amount();
    walletBlockedAmount.setCurrency(newWallet.getCurrency());
    walletBlockedAmount.setValue(0);
    newWallet.setBlockedBalance(walletBlockedAmount);

    // creationDate
    if (jsonWallet.has("creationDate")) {
      newWallet.setCreatedAt(jsonWallet.findPath("creationDate").asLong());
    }

    // return
    return newWallet;
  }

  public static User mapJsonToLoloLegalUser(JsonNode jsonUser) {

    // create natural user object
    User newLegalUser = new User();

    // id
    if (jsonUser.has("id")) {
      newLegalUser.setId(jsonUser.findPath("id").asText());
      newLegalUser.setProviderId(jsonUser.findPath("id").asText());
    }

    // tag
    if (jsonUser.has("tag")) {
      newLegalUser.setCustomTag(getString(jsonUser.findPath("tag").asText()));
    }

    // personType
    newLegalUser.setType(UserType.LEGAL);

    // kycLevel
    if (jsonUser.has("kycLevel")) {
      if (jsonUser.findPath("kycLevel").asText().equals("REGULAR")) {
        newLegalUser.setKycLevel(KYCLevel.VERIFIED);
      } else {
        newLegalUser.setKycLevel(KYCLevel.STANDARD);
      }
    } else {
      newLegalUser.setKycLevel(KYCLevel.STANDARD);
    }

    // email
    if (jsonUser.has("email")) {
      newLegalUser.setCompanyEmail(getString(jsonUser.findPath("email").asText()));
    }

    // name
    if (jsonUser.has("name")) {
      newLegalUser.setCompanyName(getString(jsonUser.findPath("name").asText()));
    }

    // legalPersonType
    if (jsonUser.has("legalPersonType")) {
      String legalPersonType = getString(jsonUser.findPath("legalPersonType").asText());
      switch (legalPersonType) {
        case "BUSINESS":
          newLegalUser.setCompanyType(CompanyType.BUSINESS);
          break;
        case "ORGANIZATION":
          newLegalUser.setCompanyType(CompanyType.ORGANIZATION);
          break;
        case "SOLETRADER":
          newLegalUser.setCompanyType(CompanyType.SOLETRADER);
          break;
        default:
          break;
      }
    }

    // headquartersAddress
    if (jsonUser.has("headquartersAddress")) {
      // get response body
      JsonNode addressJson = jsonUser.findPath("headquartersAddress");

      // create address
      Address address = new Address();

      // addressLine1
      if (addressJson.has("addressLine1")) {
        address.setAddressLine1(getString(addressJson.findPath("addressLine1").asText()));
      }

      // addressLine2
      if (addressJson.has("addressLine2")) {
        address.setAddressLine2(getString(addressJson.findPath("addressLine2").asText()));
      }

      // city
      if (addressJson.has("city")) {
        address.setCity(getString(addressJson.findPath("city").asText()));
      }

      // region
      if (addressJson.has("region")) {
        address.setCounty(getString(addressJson.findPath("region").asText()));
      }

      // country
      if (addressJson.has("country")) {
        String country = getString(addressJson.findPath("country").asText());
        address.setCountry(CountryISO.valueOf(country));
      }

      // postalCode
      if (addressJson.has("postalCode")) {
        address.setPostalCode(getString(addressJson.findPath("postalCode").asText()));
      }

      newLegalUser.setCompanyAddress(address);
    }

    // legalRepresentativeFirstName
    if (jsonUser.has("legalRepresentativeFirstName")) {
      newLegalUser.setFirstName(
          getString(jsonUser.findPath("legalRepresentativeFirstName").asText()));
    }

    // legalRepresentativeLastName
    if (jsonUser.has("legalRepresentativeLastName")) {
      newLegalUser.setLastName(
          getString(jsonUser.findPath("legalRepresentativeLastName").asText()));
    }

    // legalRepresentativeAddress
    if (jsonUser.has("legalRepresentativeAddress")) {
      // get response body
      JsonNode addressJson = jsonUser.findPath("legalRepresentativeAddress");

      // create address
      Address address = new Address();

      // addressLine1
      if (addressJson.has("addressLine1")) {
        address.setAddressLine1(getString(addressJson.findPath("addressLine1").asText()));
      }

      // addressLine2
      if (addressJson.has("addressLine2")) {
        address.setAddressLine2(getString(addressJson.findPath("addressLine2").asText()));
      }

      // city
      if (addressJson.has("city")) {
        address.setCity(getString(addressJson.findPath("city").asText()));
      }

      // region
      if (addressJson.has("region")) {
        address.setCounty(getString(addressJson.findPath("region").asText()));
      }

      // country
      if (addressJson.has("country")) {
        String country = getString(addressJson.findPath("country").asText());
        address.setCountry(CountryISO.valueOf(country));
      }

      // postalCode
      if (addressJson.has("postalCode")) {
        address.setPostalCode(getString(addressJson.findPath("postalCode").asText()));
      }

      newLegalUser.setAddress(address);
    }

    // legalRepresentativeEmail
    if (jsonUser.has("legalRepresentativeEmail")) {
      newLegalUser.setEmail(getString(jsonUser.findPath("legalRepresentativeEmail").asText()));
    }

    // legalRepresentativeBirthday
    if (jsonUser.has("legalRepresentativeBirthday")) {
      newLegalUser.setBirthDate(jsonUser.findPath("legalRepresentativeBirthday").asLong());
    }

    // legalRepresentativeNationality
    if (jsonUser.has("legalRepresentativeNationality")) {
      String country = getString(jsonUser.findPath("legalRepresentativeNationality").asText());
      newLegalUser.setNationality(CountryISO.valueOf(country));
    }

    // legalRepresentativeCountryOfResidence
    if (jsonUser.has("legalRepresentativeCountryOfResidence")) {
      String country =
          getString(jsonUser.findPath("legalRepresentativeCountryOfResidence").asText());
      newLegalUser.setCountryOfResidence(CountryISO.valueOf(country));
    }

    // tag
    if (jsonUser.has("tag")) {
      newLegalUser.setMobilePhone(getString(jsonUser.findPath("tag").asText()));
    }

    // creationDate
    if (jsonUser.has("creationDate")) {
      newLegalUser.setCreatedAt(jsonUser.findPath("creationDate").asLong());
    }

    return newLegalUser;
  }

  public static User mapJsonToLoloNaturalUser(JsonNode jsonUser) {

    // create natural user object
    User newUser = new User();

    // id
    if (jsonUser.has("id")) {
      newUser.setId(jsonUser.findPath("id").asText());
      newUser.setProviderId(jsonUser.findPath("id").asText());
    }

    // tag
    if (jsonUser.has("tag")) {
      newUser.setCustomTag(getString(jsonUser.findPath("tag").asText()));
    }

    // personType
    newUser.setType(UserType.NATURAL);

    // kycLevel
    if (jsonUser.has("kycLevel")) {
      if (jsonUser.findPath("kycLevel").asText().equals("REGULAR")) {
        newUser.setKycLevel(KYCLevel.VERIFIED);
      } else {
        newUser.setKycLevel(KYCLevel.STANDARD);
      }
    } else {
      newUser.setKycLevel(KYCLevel.STANDARD);
    }

    // email
    if (jsonUser.has("email")) {
      newUser.setEmail(getString(jsonUser.findPath("email").asText()));
    }

    // firstName
    if (jsonUser.has("firstName")) {
      newUser.setFirstName(getString(jsonUser.findPath("firstName").asText()));
    }

    // lastName
    if (jsonUser.has("lastName")) {
      newUser.setLastName(getString(jsonUser.findPath("lastName").asText()));
    }

    // address
    if (jsonUser.has("address")) {
      // get response body
      JsonNode addressJson = jsonUser.findPath("address");

      // create address
      Address address = new Address();

      // addressLine1
      if (addressJson.has("addressLine1")) {
        address.setAddressLine1(getString(addressJson.findPath("addressLine1").asText()));
      }

      // addressLine2
      if (addressJson.has("addressLine2")) {
        address.setAddressLine2(getString(addressJson.findPath("addressLine2").asText()));
      }

      // city
      if (addressJson.has("city")) {
        address.setCity(getString(addressJson.findPath("city").asText()));
      }

      // region
      if (addressJson.has("region")) {
        address.setCounty(getString(addressJson.findPath("region").asText()));
      }

      // country
      if (addressJson.has("country")) {
        String country = getString(addressJson.findPath("country").asText());
        address.setCountry(CountryISO.valueOf(country));
      }

      // postalCode
      if (addressJson.has("postalCode")) {
        address.setPostalCode(getString(addressJson.findPath("postalCode").asText()));
      }

      newUser.setAddress(address);
    }

    // birthday
    if (jsonUser.has("birthday")) {
      newUser.setBirthDate(jsonUser.findPath("birthday").asLong());
    }

    // nationality
    if (jsonUser.has("nationality")) {
      String nationality = getString(jsonUser.findPath("nationality").asText());
      newUser.setNationality(CountryISO.valueOf(nationality));
    }

    // countryOfResidence
    if (jsonUser.has("countryOfResidence")) {
      String countryOfResidence = getString(jsonUser.findPath("countryOfResidence").asText());
      newUser.setCountryOfResidence(CountryISO.valueOf(countryOfResidence));
    }

    // occupation
    if (jsonUser.has("occupation")) {
      newUser.setOccupation(getString(jsonUser.findPath("occupation").asText()));
    }

    // incomeRange
    if (jsonUser.has("incomeRange")) {
      int incomeRange = jsonUser.findPath("incomeRange").asInt(1);
      switch (incomeRange) {
        case 1:
          newUser.setIncomeRange(IncomeRange.BELOW_18K);
          break;
        case 2:
          newUser.setIncomeRange(IncomeRange.BELOW_30K);
          break;
        case 3:
          newUser.setIncomeRange(IncomeRange.BELOW_50K);
          break;
        case 4:
          newUser.setIncomeRange(IncomeRange.BELOW_80K);
          break;
        case 5:
          newUser.setIncomeRange(IncomeRange.BELOW_120K);
          break;
        case 6:
          newUser.setIncomeRange(IncomeRange.ABOVE_120K);
          break;

        default:
          break;
      }
      newUser.setBirthDate(jsonUser.findPath("birthday").asLong());
    }

    // tag
    if (jsonUser.has("tag")) {
      newUser.setMobilePhone(getString(jsonUser.findPath("tag").asText()));
    }

    // creationDate
    if (jsonUser.has("creationDate")) {
      newUser.setCreatedAt(jsonUser.findPath("creationDate").asLong());
    }

    return newUser;
  }

  private static String getString(String object) {

    if (object == null) {
      return "";
    } else {
      if (object.equals("null")) {
        return "";
      } else {
        return object;
      }
    }
  }

  private static Wallet getAccountWallet(
      DatabaseService dbService, Account account, String currency) {

    return (Wallet) dbService.getRecord("", account, "CREDIT_" + currency, Wallet.class);
  }

  private static Wallet getUserWallet(DatabaseService dbService, Account account, String walletId) {

    return (Wallet) dbService.getRecord("", account, walletId, Wallet.class);
  }
}
