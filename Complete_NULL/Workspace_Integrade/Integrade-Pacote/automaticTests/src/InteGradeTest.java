import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import asct.ui.ASCTControllerTest;
import asct.ui.ExecutionRequestDescriptorCompositeTest;

@RunWith(Suite.class)
@SuiteClasses({
	// TODO Test when queue is ready
	// ComparableTaskTest.class,
	// TaskQueueTest.class,
	ASCTControllerTest.class,
	ExecutionRequestDescriptorCompositeTest.class
})

public class InteGradeTest {
}