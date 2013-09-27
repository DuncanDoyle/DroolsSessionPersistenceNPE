package org.jboss.ddoyle.brms.cep.ha.management;

import java.io.IOException;

public class MainAllRuns {
	
	public static void main(String[] args) throws IOException {
		TestScenarioRunner.firstRun();
		TestScenarioRunner.secondRun();
	}

}
