package org.requirementsascode;

import java.util.Optional;
import java.util.function.Predicate;

import org.requirementsascode.predicate.Anytime;

public class ConditionalStep extends Step {
    private static final long serialVersionUID = 7204738737376844201L;

    ConditionalStep(String stepName, UseCase useCase, Flow useCaseFlow) {
	super(stepName, useCase, useCaseFlow);
    }

    public Predicate<UseCaseModelRunner> getPredicate() {
	Predicate<UseCaseModelRunner> predicate;
	Predicate<UseCaseModelRunner> reactWhile = getReactWhile();

	if (reactWhile != null) {
	    predicate = reactWhile;
	} else {
	    predicate = getFlowPredicate().get();
	}

	return predicate;
    }

    public Optional<Predicate<UseCaseModelRunner>> getFlowPredicate() {
	Anytime anytime = new Anytime();

	Predicate<UseCaseModelRunner> flowPosition = getFlowPosition().orElse(anytime);
	Predicate<UseCaseModelRunner> when = getWhen().orElse(anytime);

	Optional<Predicate<UseCaseModelRunner>> flowPredicate = Optional
		.of(isRunnerInDifferentFlow().and(flowPosition).and(when));
	return flowPredicate;
    }

    private Predicate<UseCaseModelRunner> isRunnerInDifferentFlow() {
	Predicate<UseCaseModelRunner> isRunnerInDifferentFlow = runner -> runner.getLatestFlow()
		.map(runnerFlow -> !runnerFlow.equals(getFlow())).orElse(true);
	return isRunnerInDifferentFlow;
    }
}