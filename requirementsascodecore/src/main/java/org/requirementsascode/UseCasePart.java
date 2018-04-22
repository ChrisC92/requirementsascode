package org.requirementsascode;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Part used by the {@link UseCaseModelBuilder} to build a {@link UseCaseModel}.
 *
 * @see UseCase
 * @author b_muth
 */
public class UseCasePart {
    private UseCase useCase;
    private UseCaseModelBuilder useCaseModelBuilder;

    UseCasePart(UseCase useCase, UseCaseModelBuilder useCaseModelBuilder) {
	this.useCase = useCase;
	this.useCaseModelBuilder = useCaseModelBuilder;
    }

    /**
     * Start the "happy day scenario" where all is fine and dandy.
     * 
     * @return the flow part to create the steps of the basic flow.
     */
    public FlowPart basicFlow() {
	Flow useCaseFlow = useCase().getBasicFlow();
	return new FlowPart(useCaseFlow, this);
    }

    /**
     * Start a flow with the specified name.
     * 
     * @param flowName
     *            the name of the flow.
     * 
     * @return the flow part to create the steps of the flow.
     */
    public FlowPart flow(String flowName) {
	Flow useCaseFlow = useCase().newFlow(flowName);
	return new FlowPart(useCaseFlow, this);
    }

    UseCase useCase() {
	return useCase;
    }

    UseCaseModelBuilder getUseCaseModelBuilder() {
	return useCaseModelBuilder;
    }

    /**
     * Returns the use case model that has been built.
     * 
     * @return the model
     */
    public UseCaseModel build() {
	return useCaseModelBuilder.build();
    }

    public <T> FlowlessUserPart<T> handles(Class<T> eventOrExceptionClass) {
	WhenPart whenPart = when(null);
	FlowlessUserPart<T> flowlessUserPart = whenPart.handles(eventOrExceptionClass);
	return flowlessUserPart;
    }

    public WhenPart when(Predicate<UseCaseModelRunner> whenCondition) {
	WhenPart whenPart = new WhenPart(whenCondition, 1);
	return whenPart;
    }

    class WhenPart {
	private long flowlessStepCounter;
	private Predicate<UseCaseModelRunner> optionalWhenCondition;

	public WhenPart(Predicate<UseCaseModelRunner> optionalWhenCondition, long flowlessStepCounter) {
	    this.optionalWhenCondition = optionalWhenCondition;
	    this.flowlessStepCounter = flowlessStepCounter;
	}

	public <T> FlowlessUserPart<T> handles(Class<T> eventOrExceptionClass) {
	    FlowlessUserPart<T> flowless = new FlowlessUserPart<>(optionalWhenCondition, eventOrExceptionClass, flowlessStepCounter);
	    return flowless;
	}
    }

    class FlowlessUserPart<T> {
	private StepUserPart<T> userPart;
	private long flowlessStepCounter;

	public FlowlessUserPart(Predicate<UseCaseModelRunner> optionalWhenCondition, Class<T> eventOrExceptionClass,
		long flowlessStepCounter) {
	    this.flowlessStepCounter = flowlessStepCounter;
	    StepPart stepPart = createStepPart(optionalWhenCondition, eventOrExceptionClass, "S" + flowlessStepCounter);
	    this.userPart = stepPart.handles(eventOrExceptionClass);
	}

	StepPart createStepPart(Predicate<UseCaseModelRunner> optionalWhenCondition, Class<T> eventOrExceptionClass,
		String stepName) {
	    FlowlessStep newStep = useCase.newFlowlessStep(stepName);
	    newStep.setWhen(optionalWhenCondition);
	    StepPart stepPart = new StepPart(newStep, UseCasePart.this, null);
	    return stepPart;
	}

	public FlowlessSystemPart<T> with(Consumer<T> systemReaction) {
	    userPart.system(systemReaction);
	    return new FlowlessSystemPart<>(flowlessStepCounter);
	}
    }

    class FlowlessSystemPart<T> {
	private long flowlessStepCounter;

	public FlowlessSystemPart(long flowlessStepCounter) {
	    this.flowlessStepCounter = flowlessStepCounter;
	}

	public WhenPart when(Predicate<UseCaseModelRunner> whenCondition) {
	    WhenPart whenPart = new WhenPart(whenCondition, ++flowlessStepCounter);
	    return whenPart;
	}

	public <U> FlowlessUserPart<U> handles(Class<U> eventOrExceptionClass) {
	    FlowlessUserPart<U> flowlessUserPart = when(null).handles(eventOrExceptionClass);
	    return flowlessUserPart;
	}

	public UseCaseModel build() {
	    return UseCasePart.this.build();
	}

    }
}
