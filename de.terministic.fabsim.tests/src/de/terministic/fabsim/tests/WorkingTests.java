package de.terministic.fabsim.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.terministic.fabsim.tests.batchtests.BatchNegativeFillTest;
import de.terministic.fabsim.tests.batchtests.BatchOverflowSplitTest;
import de.terministic.fabsim.tests.batchtests.BatchOverflowTest;
import de.terministic.fabsim.tests.batchtests.BatchSplitRevertTest;
import de.terministic.fabsim.tests.batchtests.BatchWaitingTest;
import de.terministic.fabsim.tests.batchtests.EmptyBatchTest;
import de.terministic.fabsim.tests.batchtests.EmptyBatchTimerTest;
import de.terministic.fabsim.tests.batchtests.LotSplitRevertTest;
import de.terministic.fabsim.tests.batchtests.MinBatchBiggerMaxBatch;
import de.terministic.fabsim.tests.batchtests.MinBatchEqualsMaxBatchTest;
import de.terministic.fabsim.tests.batchtests.SameBatchIDTest;
import de.terministic.fabsim.tests.batchtests.TimerBatchStartTest;
import de.terministic.fabsim.tests.batchtests.fullBatchStartTest;
import de.terministic.fabsim.tests.breakdown.BreakdownStateChangeTest;
import de.terministic.fabsim.tests.breakdown.SimTimeBasedBreakdownTest;
import de.terministic.fabsim.tests.coretests.EventComparatorTest;
import de.terministic.fabsim.tests.coretests.EventListManagerTest;
import de.terministic.fabsim.tests.dispatchingtests.FifoTest;
import de.terministic.fabsim.tests.dispatchingtests.LowLevelFifoTest;
import de.terministic.fabsim.tests.durationobjecttests.ExponentialDurationObjectTest;
import de.terministic.fabsim.tests.featuretests.LoadUnloadTest;
import de.terministic.fabsim.tests.maintenancetests.CountedMaintenanceTest;
import de.terministic.fabsim.tests.maintenancetests.InterruptedCountedMaintenanceTest;
import de.terministic.fabsim.tests.maintenancetests.InterruptedTimedMaintenanceTest;
import de.terministic.fabsim.tests.maintenancetests.SimTimeBasedMaintenanceTests;
import de.terministic.fabsim.tests.maintenancetests.TimedMaintenanceTest;
import de.terministic.fabsim.tests.maintenancetests.WorkingWhileMaintenanceTest;
import de.terministic.fabsim.tests.modeltests.DelayTest;
import de.terministic.fabsim.tests.modeltests.GG1ModelTest;
import de.terministic.fabsim.tests.modeltests.MM1ModelTest;
import de.terministic.fabsim.tests.modeltests.SourceSinkModelTest;
import de.terministic.fabsim.tests.operator.OperatorBasicTest;
import de.terministic.fabsim.tests.setuptests.CountedSetupFunctionTest;
import de.terministic.fabsim.tests.setuptests.MinItemsInQueueBasicTest;
import de.terministic.fabsim.tests.setuptests.NegativeSetupTimeTest;
import de.terministic.fabsim.tests.setuptests.SetupFunctionTest;
import de.terministic.fabsim.tests.setuptests.SetupOverwriteTest;
import de.terministic.fabsim.tests.setuptests.SetupWhileWorkingTest;
import de.terministic.fabsim.tests.setuptests.TimedSetupFunctionTest;
import de.terministic.fabsim.tests.setuptests.UsageWhileSetupTest;
import de.terministic.fabsim.tests.setuptests.ZeroSetupTimeTest;
import de.terministic.fabsim.tests.statistics.AverageCycleTimeTest;
import de.terministic.fabsim.tests.statistics.ItemLogTest;

@RunWith(Suite.class)
@SuiteClasses({
	//duration object tests
	ExponentialDurationObjectTest.class,
	
	//batchtests
	BatchNegativeFillTest.class,
	BatchOverflowSplitTest.class,
	BatchOverflowTest.class,
	BatchSplitRevertTest.class,
	BatchWaitingTest.class,
	EmptyBatchTest.class,
	EmptyBatchTimerTest.class,
	fullBatchStartTest.class,
	LotSplitRevertTest.class,
	MinBatchBiggerMaxBatch.class,
	MinBatchEqualsMaxBatchTest.class,
	SameBatchIDTest.class,
	TimerBatchStartTest.class,
	
	//breakdown
	BreakdownStateChangeTest.class,
	SimTimeBasedBreakdownTest.class,
	
	//coretest
	EventComparatorTest.class,
	EventListManagerTest.class,

	//DispatchingTest
	FifoTest.class,
	LowLevelFifoTest.class,
	
	//featuretests
	LoadUnloadTest.class,
	
	//maintenance
	CountedMaintenanceTest.class,
	InterruptedCountedMaintenanceTest.class,
	InterruptedTimedMaintenanceTest.class,
	SimTimeBasedMaintenanceTests.class,
	TimedMaintenanceTest.class,
	WorkingWhileMaintenanceTest.class,
	
	//modeltests
	DelayTest.class,
	GG1ModelTest.class,
	MM1ModelTest.class,
	SourceSinkModelTest.class,
	
	//operator
	OperatorBasicTest.class,
	
	//Setup
	CountedSetupFunctionTest.class,
	MinItemsInQueueBasicTest.class,
	NegativeSetupTimeTest.class,
	SetupFunctionTest.class,
	SetupOverwriteTest.class,
	SetupWhileWorkingTest.class,	
	TimedSetupFunctionTest.class,
	UsageWhileSetupTest.class,
	ZeroSetupTimeTest.class,
	
	//Statistics
	AverageCycleTimeTest.class,
	ItemLogTest.class
	
})
public class WorkingTests {

}
