package com.redhat;

import java.util.Date;

public class AuditObject {

    private String ruleName;
    private Date ruleFiredAt;
    private Object inputs;
    private String status;
    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Date getRuleFiredAt() {
        return ruleFiredAt;
    }

    public void setRuleFiredAt(Date ruleFiredAt) {
        this.ruleFiredAt = ruleFiredAt;
    }

    public Object getInputs() {
        return inputs;
    }

    public void setInputs(Object inputs) {
        this.inputs = inputs;
    }
}
