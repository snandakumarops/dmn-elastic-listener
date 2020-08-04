package com.redhat;

import com.google.gson.Gson;

import org.apache.camel.Exchange;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

public class TransformerBean {
    private  KieContainer kieContainer;



    public String validateTxn() {
        String resultJson=  "NO_DATA";
        try {
            KieServices kieServices = KieServices.Factory.get();
            kieContainer = kieServices.newKieClasspathContainer();

            DMNRuntime dmnRuntime = RuleSessionFactory.createDMNRuntime();
            dmnRuntime.addListener(new CustomDMNListener());
            String namespace = "https://kiegroup.org/dmn/_03A4B62B-BA02-43B4-B776-34B0D7DA117C";
            String modelName = "CustomerEligibilityDMN";
            DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);

            DMNContext dmnContext = dmnRuntime.newContext();

            //Customer Data Lookup, Mock data setup for test
            dmnContext.set("KYC Check",false);
            dmnContext.set("Member Since",2020);
            dmnContext.set("Last Transaction Date",LocalDate.now());
            dmnContext.set("Credit Rating", 700);
            dmnContext.set("Residency","NON-RESIDENT");
            dmnContext.set("Customer Status","SILVER");
            dmnContext.set("Customer Age",60);
            dmnContext.set("Delinquency History",2);
            dmnContext.set("Customer Id","CUST7865");
            DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
            DMNDecisionResult resultOffer = dmnResult.getDecisionResultByName("Customer Eligibility");
            boolean resultOfferPayload = (boolean)resultOffer.getResult();
            DMNDecisionResult dueDiligence = dmnResult.getDecisionResultByName("Due Diligence");
            boolean dueDiligencePayload = (boolean)dueDiligence.getResult();
            DMNDecisionResult creditRatingCheck = dmnResult.getDecisionResultByName("Credit Rating Check");
            boolean creditRatingCheckPayload = (boolean)creditRatingCheck.getResult();
            DMNDecisionResult riskCheck = dmnResult.getDecisionResultByName("Risk Checks");
            BigDecimal bigDecimal = (BigDecimal) riskCheck.getResult();
            boolean riskCheckPayload =bigDecimal.compareTo(BigDecimal.valueOf(3)) < 0;




            String resultString = "{\n" +
                    "  \"Due Diligence\":"+dueDiligencePayload+",\n" +
                    "  \"Credit Rating Check\" : "+creditRatingCheckPayload+",\n" +
                    "  \"Risk Check\" : "+riskCheckPayload+",\n" +
                    "  \"Product Eligibility\":"+resultOfferPayload+"\t\n" +
                    "}";

            return resultString;



        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    

}
