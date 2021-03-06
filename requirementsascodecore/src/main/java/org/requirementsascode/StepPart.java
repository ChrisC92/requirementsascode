package org.requirementsascode;

import java.util.Objects;
import java.util.function.Consumer;

import org.requirementsascode.exception.NoSuchElementInModel;
import org.requirementsascode.systemreaction.IncludesUseCase;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see Step
 * @author b_muth
 */
public class StepPart {
    private Step step;
    private FlowPart flowPart;
    private ModelBuilder modelBuilder;
    private Actor userActor;
    private Actor systemActor;

    StepPart(Step step, UseCasePart useCasePart, FlowPart useCaseFlowPart) {
	this.step = step;
	this.flowPart = useCaseFlowPart;
	this.modelBuilder = useCasePart.getModelBuilder();
	this.userActor = modelBuilder.build().getUserActor();
	this.systemActor = modelBuilder.build().getSystemActor();
    }

    /**
     * Defines which actors (i.e. user groups) can cause the system to react to the
     * event of this step.
     *
     * @param actors
     *            the actors that define the user groups
     * @return the created as part of this step
     */
    public StepAsPart as(Actor... actors) {
	Objects.requireNonNull(actors);
	return new StepAsPart(this, actors);
    }

    /**
     * Defines the type of user command objects that this step accepts. Commands of
     * this type can cause a system reaction.
     *
     * <p>
     * Given that the step's condition is true, and the actor is right, the system
     * reacts to objects that are instances of the specified class or instances of
     * any direct or indirect subclass of the specified class.
     *
     * @param eventClass
     *            the class of commands the system reacts to in this step
     * @param <T>
     *            the type of the class
     * @return the created user part of this step
     */
    public <T> StepUserPart<T> user(Class<T> eventClass) {
	Objects.requireNonNull(eventClass);
	StepUserPart<T> userPart = as(userActor).user(eventClass);
	return userPart;
    }

    /**
     * Defines the type of system event objects or exceptions that this step
     * handles. Events of this type can cause a system reaction.
     *
     * <p>
     * Given that the step's condition is true, and the actor is right, the system
     * reacts to objects that are instances of the specified class or instances of
     * any direct or indirect subclass of the specified class.
     *
     * @param eventOrExceptionClass
     *            the class of events the system reacts to in this step
     * @param <T>
     *            the type of the class
     * @return the created user part of this step
     */
    public <T> StepUserPart<T> on(Class<T> eventOrExceptionClass) {
	Objects.requireNonNull(eventOrExceptionClass);
	StepUserPart<T> userPart = as(systemActor).user(eventOrExceptionClass);
	return userPart;
    }

    /**
     * Defines an "autonomous system reaction", meaning the system will react
     * without needing an event provided via {@link ModelRunner#reactTo(Object)}.
     *
     * @param systemReaction
     *            the autonomous system reaction
     * @return the created system part of this step
     */
    public StepSystemPart<ModelRunner> system(Runnable systemReaction) {
	Objects.requireNonNull(systemReaction);
	StepSystemPart<ModelRunner> systemPart = as(systemActor).system(systemReaction);
	return systemPart;
    }
    
    /**
     * Defines an "autonomous system reaction", meaning the system will react
     * without needing an event provided via {@link ModelRunner#reactTo(Object)}.
     * Instead, the model runner provides itself as an event to the system reaction.
     *
     * @param modelRunnerConsumer
     *            the autonomous system reaction (that needs information from a model runner to work)
     * @return the created system part of this step
     */
    public StepSystemPart<ModelRunner> system(Consumer<ModelRunner> modelRunnerConsumer) {
	Objects.requireNonNull(modelRunnerConsumer);
	StepSystemPart<ModelRunner> systemPart = as(systemActor).system(modelRunnerConsumer);
	return systemPart;
    }

    /**
     * Makes the model runner continue after the specified step.
     *
     * @param stepName
     *            name of the step to continue after, in this use case.
     * @return the use case part this step belongs to, to ease creation of further
     *         flows
     * @throws NoSuchElementInModel
     *             if no step with the specified stepName is found in the current
     *             use case
     */
    public UseCasePart continuesAfter(String stepName) {
	Objects.requireNonNull(stepName);
	UseCasePart useCasePart = as(systemActor).continuesAfter(stepName);
	return useCasePart;
    }

    /**
     * Makes the model runner continue at the specified step. If there are
     * alternative flows starting at the specified step, one may be entered if its
     * condition is enabled.
     *
     * @param stepName
     *            name of the step to continue at, in this use case.
     * @return the use case part this step belongs to, to ease creation of further
     *         flows
     * @throws NoSuchElementInModel
     *             if no step with the specified stepName is found in the current
     *             use case
     */
    public UseCasePart continuesAt(String stepName) {
	Objects.requireNonNull(stepName);
	UseCasePart useCasePart = as(systemActor).continuesAt(stepName);
	return useCasePart;
    }

    /**
     * Makes the model runner continue at the specified step. No alternative flow
     * starting at the specified step is entered, even if its condition is enabled.
     *
     * @param stepName
     *            name of the step to continue at, in this use case.
     * @return the use case part this step belongs to, to ease creation of further
     *         flows
     * @throws NoSuchElementInModel
     *             if no step with the specified stepName is found in the current
     *             use case
     */
    public UseCasePart continuesWithoutAlternativeAt(String stepName) {
	Objects.requireNonNull(stepName);
	UseCasePart useCasePart = as(systemActor).continuesWithoutAlternativeAt(stepName);
	return useCasePart;
    }

    /**
     * Includes the use case with the specified name.
     * 
     * The runner starts the included use case right after the current step. The
     * runner returns to the current flow when it reaches the end of an included
     * flow. The runner then continues after the current step of the current flow.
     * 
     * @param useCaseName
     *            the name of the use case to include
     * @return the step system part, to ease creation of further steps and flows
     * @throws NoSuchElementInModel
     *             if the included use case has not been specified before
     */
    public StepSystemPart<ModelRunner> includesUseCase(String useCaseName) {
	FlowStep flowStep = (FlowStep) step;
	UseCase includedUseCase = flowStep.getModel().findUseCase(useCaseName);
	StepSystemPart<ModelRunner> stepSystemPart = as(systemActor)
		.system(new IncludesUseCase(includedUseCase, flowStep));
	return stepSystemPart;
    }

    Step getStep() {
	return step;
    }

    FlowPart getFlowPart() {
	return flowPart;
    }

    UseCasePart getUseCasePart() {
	return getFlowPart().getUseCasePart();
    }

    ModelBuilder getModelBuilder() {
	return modelBuilder;
    }
}
