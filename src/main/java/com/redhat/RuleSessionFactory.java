package com.redhat;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNRuntime;

public class RuleSessionFactory {


    protected static DMNRuntime createDMNRuntime() {
        KieServices kieServices = KieServices.Factory.get();

//        ReleaseId releaseId = kieServices.newReleaseId( "com.myspace", "RiskRatingDMN", "1.0.0-SNAPSHOT" );
        KieContainer kieContainer = kieServices.newKieClasspathContainer(  );
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieContainer.getKieBase()).get(DMNRuntime.class);

        return dmnRuntime;
    }




}