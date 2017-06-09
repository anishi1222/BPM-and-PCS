package pcsoauth2;


public class FundsTransferRequest {

    private String incidentId;
    private String sourceAcctNo;
    private String destAcctNo;
    private Integer amount;
    private String transferType;

    public FundsTransferRequest(){
        incidentId="1";
        sourceAcctNo="2";
        destAcctNo="3";
        amount=400;
        transferType="tparty";
    }

    public void setDestAcctNo(String destAcctNo) {
        this.destAcctNo = destAcctNo;
    }

    public String getDestAcctNo() {
        return destAcctNo;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setSourceAcctNo(String sourceAcctNo) {
        this.sourceAcctNo = sourceAcctNo;
    }

    public String getSourceAcctNo() {
        return sourceAcctNo;
    }
}
