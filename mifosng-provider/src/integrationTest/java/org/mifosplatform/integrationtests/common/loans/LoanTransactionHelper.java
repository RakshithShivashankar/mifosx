/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.loans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.LocalDate;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoanTransactionHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_LOAN_PRODUCT_URL = "/mifosng-provider/api/v1/loanproducts?" + Utils.TENANT_IDENTIFIER;
    private static final String APPLY_LOAN_URL = "/mifosng-provider/api/v1/loans?" + Utils.TENANT_IDENTIFIER;
    private static final String APPROVE_LOAN_COMMAND = "approve";
    private static final String UNDO_APPROVAL_LOAN_COMMAND = "undoApproval";
    private static final String DISBURSE_LOAN_COMMAND = "disburse";
    private static final String DISBURSE_LOAN_TO_SAVINGS_COMMAND = "disburseToSavings";
    private static final String UNDO_DISBURSE_LOAN_COMMAND = "undoDisbursal";
    private static final String WRITE_OFF_LOAN_COMMAND = "writeoff";
    private static final String WAIVE_INTEREST_COMMAND = "waiveinterest";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";
    private static final String WITHDRAW_LOAN_APPLICATION_COMMAND = "withdrawnByApplicant";

    public LoanTransactionHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Integer getLoanProductId(final String loanProductJSON) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_LOAN_PRODUCT_URL, loanProductJSON, "resourceId");
    }

    public Integer getLoanId(final String loanApplicationJSON) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, APPLY_LOAN_URL, loanApplicationJSON, "loanId");
    }
    public Integer getLoanOfficerId(final String loanId) {
        final String GET_LOAN_URL = "/mifosng-provider/api/v1/loans/" + loanId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(this.requestSpec, this.responseSpec, GET_LOAN_URL, "loanOfficerId");
    }


    public Object createLoanAccount(final String loanApplicationJSON, final String responseAttribute) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, APPLY_LOAN_URL, loanApplicationJSON, responseAttribute);
    }

    public Integer updateLoan(final Integer id, final String loanApplicationJSON) {
        return Utils.performServerPut(this.requestSpec, this.responseSpec, "/mifosng-provider/api/v1/loans/" + id
                + "?" + Utils.TENANT_IDENTIFIER, loanApplicationJSON, "loanId");
    }

    public ArrayList getLoanRepaymentSchedule(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        final String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=repaymentSchedule&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "repaymentSchedule");
        return (ArrayList) response.get("periods");
    }
    
    public ArrayList getLoanFutureRepaymentSchedule(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        final String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=repaymentSchedule,futureSchedule&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "repaymentSchedule");
        return (ArrayList) response.get("futurePeriods");
    }

    public HashMap getLoanSummary(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer loanID) {
        final String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "summary");
        return response;
    }

    public Object getLoanDetail(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer loanID,
            final String param) {
        final String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, param);
    }

    public ArrayList getLoanCharges(final Integer loanId) {
        final String GET_LOAN_CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, GET_LOAN_CHARGES_URL, "");
    }

    public HashMap approveLoan(final String approvalDate, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID), getApproveLoanAsJSON(approvalDate));
    }
    
    public HashMap approveLoanWithApproveAmount(final String approvalDate, final String approvalAmount, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID), getApproveLoanAsJSON(approvalDate, approvalAmount));
    }
    
    public Object approveLoanWithApproveAmount(final String approvalDate, final String approvalAmount, final Integer loanID,
            final String responseAttribute) {
        
        final String approvalURL = createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID);
        final String approvalJSONData = getApproveLoanAsJSON(approvalDate, approvalAmount);
        
        return performLoanTransaction(approvalURL, approvalJSONData, responseAttribute);
    }

    public HashMap undoApproval(final Integer loanID) {
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performLoanTransaction(createLoanOperationURL(UNDO_APPROVAL_LOAN_COMMAND, loanID), undoBodyJson);
    }

    public HashMap disburseLoan(final String date, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(DISBURSE_LOAN_COMMAND, loanID), getDisburseLoanAsJSON(date, null));
    }

    public HashMap disburseLoan(final String date, final Integer loanID, final String disburseAmt) {
        return performLoanTransaction(createLoanOperationURL(DISBURSE_LOAN_COMMAND, loanID), getDisburseLoanAsJSON(date, disburseAmt));
    }

    public HashMap disburseLoanToSavings(final String date, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(DISBURSE_LOAN_TO_SAVINGS_COMMAND, loanID), getDisburseLoanAsJSON(date, null));
    }

    public HashMap undoDisbursal(final Integer loanID) {
        final String undoDisburseJson = "{'note' : 'UNDO DISBURSAL'}";
        System.out.println("IN DISBURSE LOAN");
        final String url = createLoanOperationURL(UNDO_DISBURSE_LOAN_COMMAND, loanID);
        System.out.println("IN DISBURSE LOAN URL " + url);
        return performLoanTransaction(createLoanOperationURL(UNDO_DISBURSE_LOAN_COMMAND, loanID), undoDisburseJson);
    }

    public HashMap writeOffLoan(final String date, final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(WRITE_OFF_LOAN_COMMAND, loanID), getWriteOffBodyAsJSON(date));
    }

    public HashMap waiveInterest(final String date, final String amountToBeWaived, final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(WAIVE_INTEREST_COMMAND, loanID), getWaiveBodyAsJSON(date, amountToBeWaived));
    }

    public Integer waiveInterestAndReturnTransactionId(final String date, final String amountToBeWaived, final Integer loanID) {
        Integer resourceId = Utils.performServerPost(this.requestSpec, this.responseSpec,
                createLoanTransactionURL(WAIVE_INTEREST_COMMAND, loanID), getWaiveBodyAsJSON(date, amountToBeWaived), "resourceId");
        return resourceId;
    }

    public HashMap makeRepayment(final String date, final Float amountToBePaid, final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(MAKE_REPAYMENT_COMMAND, loanID),
                getRepaymentBodyAsJSON(date, amountToBePaid));
    }

    public HashMap withdrawLoanApplicationByClient(final String date, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(WITHDRAW_LOAN_APPLICATION_COMMAND, loanID),
                getWithdrawLoanApplicationBodyAsJSON(date));
    }

    public Integer addChargesForLoan(final Integer loanId, final String request) {
        System.out.println("--------------------------------- ADD CHARGES FOR LOAN --------------------------------");
        final String ADD_CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerPost(requestSpec, responseSpec, ADD_CHARGES_URL, request, "");
        return (Integer) response.get("resourceId");
    }

    public Integer updateChargesForLoan(final Integer loanId, final Integer loanchargeId, final String request) {
        System.out.println("--------------------------------- ADD CHARGES FOR LOAN --------------------------------");
        final String UPDATE_CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges/" + loanchargeId
                + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerPut(requestSpec, responseSpec, UPDATE_CHARGES_URL, request, "");
        return (Integer) response.get("resourceId");
    }

    public Integer deleteChargesForLoan(final Integer loanId, final Integer loanchargeId) {
        System.out.println("--------------------------------- DELETE CHARGES FOR LOAN --------------------------------");
        final String DELETE_CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges/" + loanchargeId
                + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerDelete(requestSpec, responseSpec, DELETE_CHARGES_URL, "");
        return (Integer) response.get("resourceId");
    }

    public Integer waiveChargesForLoan(final Integer loanId, final Integer loanchargeId, final String json) {
        System.out.println("--------------------------------- WAIVE CHARGES FOR LOAN --------------------------------");
        final String CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges/" + loanchargeId
                + "?command=waive&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return (Integer) response.get("resourceId");
    }

    public Integer payChargesForLoan(final Integer loanId, final Integer loanchargeId, final String json) {
        System.out.println("--------------------------------- WAIVE CHARGES FOR LOAN --------------------------------");
        final String CHARGES_URL = "/mifosng-provider/api/v1/loans/" + loanId + "/charges/" + loanchargeId
                + "?command=pay&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerPost(requestSpec, responseSpec, CHARGES_URL, json, "");
        return (Integer) response.get("resourceId");
    }

    private String getDisburseLoanAsJSON(final String actualDisbursementDate, final String transactionAmount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("actualDisbursementDate", actualDisbursementDate);
        map.put("note", "DISBURSE NOTE");
        if (transactionAmount != null) {
            map.put("transactionAmount", transactionAmount);
        }
        return new Gson().toJson(map);
    }

    private String getApproveLoanAsJSON(final String approvalDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }
    
    private String getApproveLoanAsJSON(final String approvalDate, final String approvalAmount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedLoanAmount", approvalAmount);
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }

    private String getRepaymentBodyAsJSON(final String transactionDate, final Float transactionAmount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }

    private String getWriteOffBodyAsJSON(final String transactionDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate", transactionDate);
        return new Gson().toJson(map);
    }

    private String getWaiveBodyAsJSON(final String transactionDate, final String amountToBeWaived) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", amountToBeWaived);
        map.put("note", " Interest Waived!!!");
        return new Gson().toJson(map);
    }

    private String getWithdrawLoanApplicationBodyAsJSON(final String withdrawDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("withdrawnOnDate", withdrawDate);
        map.put("note", " Loan Withdrawn By Client!!!");
        return new Gson().toJson(map);

    }

    public static String getSpecifiedDueDateChargesForLoanAsJSON(final String chargeId) {
        return getSpecifiedDueDateChargesForLoanAsJSON(chargeId, "12 January 2013", "100");
    }

    public static String getSpecifiedDueDateChargesForLoanAsJSON(final String chargeId, final String dueDate, final String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("amount", amount);
        map.put("dueDate", dueDate);
        map.put("chargeId", chargeId);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getDisbursementChargesForLoanAsJSON(final String chargeId) {
        return getDisbursementChargesForLoanAsJSON(chargeId, "100");
    }

    public static String getDisbursementChargesForLoanAsJSON(final String chargeId, String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("amount", amount);
        map.put("chargeId", chargeId);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getInstallmentChargesForLoanAsJSON(final String chargeId, final String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("amount", amount);
        map.put("chargeId", chargeId);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getUpdateChargesForLoanAsJSON(String amount) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("amount", amount);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getPayChargeJSON(final String date, final String installmentNumber) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", date);
        if (installmentNumber != null) {
            map.put("installmentNumber", installmentNumber);
        }
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public static String getWaiveChargeJSON(final String installmentNumber) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("installmentNumber", installmentNumber);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public String getLoanCalculationBodyAsJSON(final String productID) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("productId", productID);
        map.put("principal", "4,500.00");
        map.put("loanTermFrequency", "4");
        map.put("loanTermFrequencyType", "2");
        map.put("numberOfRepayments", "4");
        map.put("repaymentEvery", "1");
        map.put("repaymentFrequencyType", "2");
        map.put("interestRateFrequencyType", "2");
        map.put("interestRatePerPeriod", "2");
        map.put("amortizationType", "1");
        map.put("interestType", "0");
        map.put("interestCalculationPeriodType", "1");
        map.put("expectedDisbursementDate", "20 September 2011");
        map.put("transactionProcessingStrategyId", "1");
        return new Gson().toJson(map);
    }

    private String createLoanOperationURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createLoanTransactionURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private HashMap performLoanTransaction(final String postURLForLoanTransaction, final String jsonToBeSent) {

        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForLoanTransaction, jsonToBeSent,
                "changes");
        return (HashMap) response.get("status");
    }
    
    private Object performLoanTransaction(final String postURLForLoanTransaction, final String jsonToBeSent, final String responseAttribute) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForLoanTransaction, jsonToBeSent, responseAttribute);
    }
    
    
    public HashMap getPrepayAmount(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer loanID) {
        final String URL = "/mifosng-provider/api/v1/loans/" + loanID + "/transactions/template?command=prepayLoan&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return response;
    }

    public void verifyRepaymentScheduleEntryFor(final int repaymentNumber, final float expectedPrincipalOutstanding, final Integer loanID) {
        System.out.println("---------------------------GETTING LOAN REPAYMENT SCHEDULE--------------------------------");
        final ArrayList<HashMap> repaymentPeriods = getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        assertEquals("Mismatch in Principal Loan Balance Outstanding ", expectedPrincipalOutstanding, repaymentPeriods.get(repaymentNumber)
                .get("principalLoanBalanceOutstanding"));
    }

    public void checkAccrualTransactionForRepayment(final LocalDate transactionDate, final Float interestPortion, final Float feePortion,
            final Float penaltyPortion, final Integer loanID) {

        ArrayList<HashMap> transactions = (ArrayList<HashMap>) getLoanDetail(this.requestSpec, this.responseSpec, loanID, "transactions");
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isAccrualTransaction = (Boolean) transactionType.get("accrual");

            if (isAccrualTransaction) {
                ArrayList<Integer> accrualEntryDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate accrualEntryDate = new LocalDate(accrualEntryDateAsArray.get(0), accrualEntryDateAsArray.get(1),
                        accrualEntryDateAsArray.get(2));

                if (transactionDate.equals(accrualEntryDate)) {
                    isTransactionFound = true;
                    assertEquals("Mismatch in transaction amounts", interestPortion,
                            Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))));
                    assertEquals("Mismatch in transaction amounts", feePortion,
                            Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))));
                    assertEquals("Mismatch in transaction amounts", penaltyPortion,
                            Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))));
                    break;
                }
            }
        }
        assertTrue("No Accrual entries are posted", isTransactionFound);

    }
}