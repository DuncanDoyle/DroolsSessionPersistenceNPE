package org.jboss.ddoyle.brms.cep.ha.management;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.drools.core.ClockType;
import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.time.SessionPseudoClock;
import org.jboss.ddoyle.brms.cep.ha.fact.SimpleFact;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.mockito.Mockito;


/**
 * Provides some simple tests that create a session, add a rule, persist the session, load the session, and add another rule.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class TestScenarioRunner {
	
	private static final String kieSessionFileName = "/tmp/brms-cep.sks";

	private static final String DEFAULT_STREAM = "LinkyStream";

	private static final SimpleFact testFactOne = new SimpleFact(UUID.randomUUID().toString(), 1000, "TestStatus");

	private static final SimpleFact testFactTwo = new SimpleFact(UUID.randomUUID().toString(), 6000, "TestStatus");

	public static void firstRun() throws IOException {
		File kieSessionFile = new File(kieSessionFileName);
		// First clean up the old file.
		if (kieSessionFile.exists()) {
			kieSessionFile.delete();
			kieSessionFile.createNewFile();
		}

		/**
		 * Tests a {@link KieSession} which uses CEP time windows. In order to properly control time we use an {@link SessionPseudoClock}.
		 * This test saves and reloads the session.
		 * 
		 * @throws IOException
		 */
		KieSession kieSession = getKieSessionWithPseudoClock("kiesessionloadertest/simpleFactWithTimeWindow.drl");

		SessionPseudoClock sessionClock = (SessionPseudoClock) kieSession.getSessionClock();
		// PseudoClock starts at 0 and counts in ms ....
		System.out.println("Current time of the PseudoClock: " + sessionClock.getCurrentTime());

		// Use Mockito to Mock a WorkingMemoryEventListener and AgendaEventListener ....
		AgendaEventListener agendaEventListener = Mockito.mock(AgendaEventListener.class);
		WorkingMemoryEventListener workingMemoryEventListener = Mockito.mock(WorkingMemoryEventListener.class);
		kieSession.addEventListener(agendaEventListener);
		kieSession.addEventListener(workingMemoryEventListener);

		EntryPoint entryPoint = kieSession.getEntryPoint(DEFAULT_STREAM);

		// Advance the clock to the time of our fact ...
		sessionClock.advanceTime(testFactOne.getTimestamp(), TimeUnit.MILLISECONDS);

		// Insert the fact.
		entryPoint.insert(testFactOne);

		verify(workingMemoryEventListener, times(1)).objectInserted(any(ObjectInsertedEvent.class));

		verify(agendaEventListener, times(0)).matchCreated(any(MatchCreatedEvent.class));
		verify(agendaEventListener, times(0)).afterMatchFired(any(AfterMatchFiredEvent.class));

		kieSession.fireAllRules();
		// Nothing should have fired.
		verify(agendaEventListener, times(0)).matchCreated(any(MatchCreatedEvent.class));
		verify(agendaEventListener, times(0)).afterMatchFired(any(AfterMatchFiredEvent.class));

		// persist the session and reload

		System.out.println("File location: " + kieSessionFile.getCanonicalPath());
		KieSessionLoader loader = new FileKieSessionLoader(kieSessionFile);

		loader.save(kieSession);
		// Cleanup.
		kieSession.dispose();
	}

	public static void secondRun() {

		File kieSessionFile = new File(kieSessionFileName);
		// First clean up the old file.
		if (!kieSessionFile.exists()) {
			throw new IllegalStateException("Can't find a previously stored KieSession. Cannot find file: " + kieSessionFileName);
		}

		KieSessionLoader loader = new FileKieSessionLoader(kieSessionFile);

		KieSession loadedKieSession = loader.load();

		// Use Mockito to Mock a WorkingMemoryEventListener and AgendaEventListener ....
		AgendaEventListener agendaEventListener = Mockito.mock(AgendaEventListener.class);
		WorkingMemoryEventListener workingMemoryEventListener = Mockito.mock(WorkingMemoryEventListener.class);
		// Aattach our listeners:
		loadedKieSession.addEventListener(agendaEventListener);
		loadedKieSession.addEventListener(workingMemoryEventListener);

		// Advance the clock to the time of the event.
		SessionPseudoClock loadedSessionClock = loadedKieSession.getSessionClock();
		long advanceClock = testFactTwo.getTimestamp() - loadedSessionClock.getCurrentTime();
		loadedSessionClock.advanceTime(advanceClock, TimeUnit.MILLISECONDS);
		System.out.println("Current time of the PseudoClock: " + loadedSessionClock.getCurrentTime());

		// Insert the fact.
		EntryPoint loadedEntryPoint = loadedKieSession.getEntryPoint(DEFAULT_STREAM);
		loadedEntryPoint.insert(testFactTwo);

		loadedKieSession.fireAllRules();

		// Rule should have fired.
		verify(agendaEventListener, times(1)).matchCreated(any(MatchCreatedEvent.class));
		verify(agendaEventListener, times(1)).afterMatchFired(any(AfterMatchFiredEvent.class));

		// Cleanup
		loadedKieSession.dispose();
	}

	private static KieSession getKieSessionWithPseudoClock(final String drlFileClasspath) {
		KieServices kieServices = KieServices.Factory.get();
		KieSessionConfiguration config = kieServices.newKieSessionConfiguration();
		config.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		return getKieSession(drlFileClasspath, config);
	}

	private static KieSession getKieSession(final String drlFileClasspath, KieSessionConfiguration kieSessionConfiguration) {
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		Resource simpleDrlResource = new ClassPathResource(drlFileClasspath);
		kieFileSystem.write(simpleDrlResource);
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

		KieModule kieModule = kieBuilder.getKieModule();
		KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

		if (kieSessionConfiguration != null) {
			return kieContainer.newKieSession(kieSessionConfiguration);
		} else {
			return kieContainer.newKieSession();
		}
	}
}
