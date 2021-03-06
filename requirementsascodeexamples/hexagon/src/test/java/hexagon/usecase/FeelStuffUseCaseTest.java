package hexagon.usecase;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;

import hexagon.adapter.stub.RepositoryStub;
import hexagon.adapter.stub.WriterStub;
import hexagon.usecaserealization.FeelStuffUseCaseRealization;

public class FeelStuffUseCaseTest {

  private Model model;
  private ModelRunner testRunner;
  private FeelStuffUseCaseRealization feelStuffUseCaseRealization;

  @Before
  public void setUp() throws Exception {
    feelStuffUseCaseRealization =
        new FeelStuffUseCaseRealization(new WriterStub(), new RepositoryStub());
    model =
        new HexagonModel(feelStuffUseCaseRealization)
            .buildWith(Model.builder());
    testRunner = new ModelRunner().startRecording();
  }

  @Test
  public void testBasicFlow() {
    testRunner.run(model);
    testRunner.reactTo(new AsksForPoem(), new AsksForPoem(), new AsksForPoem());
    assertRecordedStepNames("1", "2", "3");
  }
  
  protected void assertRecordedStepNames(String... expectedStepNames) {
	String[] actualStepNames = testRunner.getRecordedStepNames();
	assertArrayEquals(expectedStepNames, actualStepNames);
  }
}
