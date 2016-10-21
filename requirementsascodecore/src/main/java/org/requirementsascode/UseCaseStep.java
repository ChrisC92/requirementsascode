package org.requirementsascode;

import static org.requirementsascode.UseCaseStepPredicate.afterStep;
import static org.requirementsascode.UseCaseStepPredicate.isRunnerAtStart;
import static org.requirementsascode.UseCaseStepPredicate.noOtherStepIsEnabledThan;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.requirementsascode.exception.ElementAlreadyInModelException;
import org.requirementsascode.exception.NoSuchElementInUseCaseException;

/**
 * A use case step, as part of a use case.
 * The use case steps define the behavior of the use case. 
 * 
 * A use case step is the core class of requirementsascode, providing all the necessary configuration 
 * information to the {@link UseCaseRunner} to cause the system to react to events.
 * A use case step has a predicate, which defines the complete condition that needs to be fulfilled
 * to enable the step, given a matching event occurs.  
 * 
 * @author b_muth
 *
 */
public class UseCaseStep extends UseCaseModelElement{
	private UseCaseFlow useCaseFlow;
	private Optional<UseCaseStep> previousStepInFlow;
	private Predicate<UseCaseRunner> predicate;
	
	private ActorPart actorPart;
	private EventPart<?> eventPart;
	private SystemPart<?> systemPart;
	
	/**
	 * Creates a use case step with the specified name that 
	 * belongs to the specified use case flow.
	 * 
	 * @param stepName the name of the step to be created
	 * @param useCaseFlow the use case flow that will contain the new use case
	 * @param previousStepInFlow the step created before the step in its flow, or else an empty optional if it is the first step in its flow
	 * @param predicate the complete predicate of the step, or else an empty optional which implicitly means: {@link #afterPreviousStepWhenNoOtherStepIsEnabled()} 
	 */
	UseCaseStep(String stepName, UseCaseFlow useCaseFlow, Optional<UseCaseStep> previousStepInFlow, Optional<Predicate<UseCaseRunner>> predicate) {
		super(stepName, useCaseFlow.getUseCaseModel());
		Objects.requireNonNull(previousStepInFlow);
		Objects.requireNonNull(predicate);
		
		this.useCaseFlow = useCaseFlow;
		this.previousStepInFlow = previousStepInFlow;
		this.predicate = predicate.orElse(afterPreviousStepWhenNoOtherStepIsEnabled());		
	}
	
	/**
	 * Defines which user group can cause the system to react to the event of this step.  
	 * 
	 * Note: in order for the system to react to the specified actor,
	 * {@link UseCaseRunner#as(Actor)} needs to be called
	 * before {@link UseCaseRunner#reactTo(Object)}.
	 * 
	 * @param actor the actor that defines the user group
	 * @return the created actor part of this step
	 */
	public UseCaseStep.ActorPart actor(Actor actor) {
		Objects.requireNonNull(actor);
		
		actorPart = new ActorPart(actor);
		return actorPart;
	}
	
	/**
	 * Defines an "autonomous system reaction",
	 * meaning the system will react when the step's predicate is true, without
	 * needing an event provided via {@link UseCaseRunner#reactTo(Object)}.
	 * 
	 * As an implicit side effect, the step is connected to the default system
	 * actor (see {@link UseCaseModel#getSystemActor()}).
	 * As another side effect, the step handles the default
	 * system event (see SystemEvent). Default system events are raised by the 
	 * use case runner itself, causing "autonomous system reactions".
	 * 
	 * @param systemReaction the autonomous system reaction
	 * @return the created system part of this step
	 */
	public UseCaseStep.SystemPart<?> system(Runnable systemReaction) {
		Objects.requireNonNull(systemReaction);
		
		Actor systemActor = getUseCaseModel().getSystemActor();
		
		UseCaseStep.SystemPart<?> systemPart =
			actor(systemActor).handle(SystemEvent.class).
				system(systemEvent -> systemReaction.run());
		
		return systemPart;
	}
	
	/**
	 * Defines the class of event objects or exception objects that this step accepts,
	 * so that they can cause a system reaction when the step's predicate is true.
	 * 
	 * Given that the step's predicate is true, the system reacts to objects that are
	 * instances of the specified class or instances of any direct or indirect subclass
	 * of the specified class.
	 * 
	 * As an implicit side effect, the step is connected to the default user
	 * actor (see {@link UseCaseModel#getUserActor()}).
	 * 
	 * Note: the event objects are provided at runtime by calling {@link UseCaseRunner#reactTo(Object)}
	 * 
	 * @param eventOrExceptionClass the class of events the system reacts to in this step
	 * @param <T> the type of the class
	 * @return the created event part of this step
	 */
	public <T> EventPart<T> handle(Class<T> eventOrExceptionClass) {
		Objects.requireNonNull(eventOrExceptionClass);

		Actor userActor = getUseCaseModel().getUserActor();
		EventPart<T> newEventPart = actor(userActor).handle(eventOrExceptionClass);
		
		return newEventPart;
	}
	
	/**
	 * Returns the use case flow this step is part of.
	 * 
	 * @return the containing use case flow
	 */
	public UseCaseFlow getFlow() {
		return useCaseFlow;
	}
	
	/**
	 * Returns the use case this step is part of.
	 * 
	 * @return the containing use case
	 */
	public UseCase getUseCase() {
		return getFlow().getUseCase();
	}
	
	/**
	 * Returns the step created before this step in its flow, 
	 * or else an empty optional if this step is the first step in its flow.
	 * 
	 * @return the previous step in this step's flow
	 */
	public Optional<UseCaseStep> getPreviousStepInFlow() {
		return previousStepInFlow;
	}
	
	/**
	 * Returns the actor part of this step.
	 * 
	 * @return the actor part
	 */
	public ActorPart getActorPart() {
		return actorPart;
	}
	
	/**
	 * Returns the event part of this step.
	 * 
	 * @return the event part
	 */
	public EventPart<?> getEventPart() {
		return eventPart;
	}
	
	/**
	 * Returns the system part of this step.
	 * 
	 * @return the system part
	 */
	public SystemPart<?> getSystemPart() {
		return systemPart;
	}
	
	/**
	 * Returns the predicate of this step
	 * 
	 * @return the predicate of this step
	 */
	public Predicate<UseCaseRunner> getPredicate() {
		return predicate;
	} 
	
	/**
	 * This predicate makes sure that use case steps following the first step
	 * in a flow are usually executed in sequence ("after previous step"),
	 * unless the first step of an alternative flow is enabled ("when no other step is enabled").
	 * This makes it possible to e.g. define the basic flow without knowing about
	 * alternative flows, allowing it to be interrupted by alternative flows if necessary.
	 * 
	 * @return the predicate for running steps in sequence
	 */
	private Predicate<UseCaseRunner> afterPreviousStepWhenNoOtherStepIsEnabled() {
		Predicate<UseCaseRunner> afterPreviousStepPredicate = 
			previousStepInFlow.map(s -> afterStep(s)).orElse(isRunnerAtStart());
		return afterPreviousStepPredicate.and(noOtherStepIsEnabledThan(this));
	}
	
	/**
	 * The part of the step that contains a reference to the actor
	 * that is allowed to trigger a system reaction for this step.
	 * 
	 * @author b_muth
	 *
	 */
	public class ActorPart{
		private Actor namedActor;
		
		private ActorPart(Actor actor) {
			this.namedActor = actor;
			connectActorToThisStep(namedActor);		
		}

		private void connectActorToThisStep(Actor actor) {
			actor.newStep(getUseCase(), UseCaseStep.this);
		}
		
		/**
		 * Defines the class of event objects or exception objects that this step accepts,
		 * so that they can cause a system reaction when the step's predicate is true.
		 * 
		 * Given that the step's predicate is true, the system reacts to objects that are
		 * instances of the specified class or instances of any direct or indirect subclass
		 * of the specified class.
		 * 
		 * Note: the event objects are provided at runtime by calling {@link UseCaseRunner#reactTo(Object)}
		 * 
		 * @param eventOrExceptionClass the class of events the system reacts to in this step
		 * @param <T> the type of the class
		 * @return the created event part of this step
		 */
		public <T> EventPart<T> handle(Class<T> eventOrExceptionClass) {
			EventPart<T> newEventPart = new EventPart<>(eventOrExceptionClass);
			UseCaseStep.this.eventPart = newEventPart;
			return newEventPart;
		}
		
		/**
		 * Returns the actor that determines which user group can 
		 * cause the system to react to the event of this step.  
		 * 
		 * @return the actor
		 */
		public Actor getActor() {
			return namedActor;
		}
	}
	
	/**
	 * The part of the step that contains a reference to the event
	 * that is allowed to trigger a system reaction for this step.
	 * 
	 * @author b_muth
	 *
	 */
	public class EventPart<T>{
		private Class<T> eventClass;
		
		private EventPart(Class<T> eventClass) {
			this.eventClass = eventClass;
		}
		
		/**
		 * Defines the system reaction,
		 * meaning the system will react as specified when the step's predicate
		 * is true and an appropriate event object is received
		 * via {@link UseCaseRunner#reactTo(Object)}.
		 * 
		 * @param systemReaction the specified system reaction
		 * @return the created system part of this step
		 */
		public SystemPart<T> system(Consumer<T> systemReaction) {
			Objects.requireNonNull(systemReaction);
			
			SystemPart<T> newSystemPart = new SystemPart<>(systemReaction);
			UseCaseStep.this.systemPart = newSystemPart;
			return newSystemPart;		
		}
		
		/**
		 * Returns the class of event or exception objects that the system reacts to in this step.
		 * The system reacts to objects that are instances of the returned class or 
		 * instances of any direct or indirect subclass of the returned class.
		 * 
		 * @return the class of events the system reacts to in this step
		 */
		public Class<T> getEventClass() {
			return eventClass;
		}
	}
	
	/**
	 * The part of the step that contains a reference to the system reaction
	 * that can be triggered, given an approriate actor and event, and the 
	 * step's predicate being true.
	 * 
	 * @author b_muth
	 *
	 */
	public class SystemPart<T>{
		private Consumer<T> systemReaction;

		private SystemPart(Consumer<T> systemReaction) {
			this.systemReaction = systemReaction;
		}
		
		/**
		 * Returns the system reaction,
		 * meaning how the system will react when the step's predicate
		 * is true and an appropriate event object is received
		 * via {@link UseCaseRunner#reactTo(Object)}.
		 * 
		 * @return the system reaction
		 */
		public Consumer<T> getSystemReaction() {
			return systemReaction;
		}
		
		/**
		 * Creates a new use case in this model.
		 * 
		 * @param useCaseName the name of the use case to be created.
		 * @return the newly created use case
		 * @throws ElementAlreadyInModelException if a use case with the specified name already exists in the model
		 */
		public UseCase newUseCase(String useCaseName) {
			Objects.requireNonNull(useCaseName);

			UseCase newUseCase = getUseCaseModel().newUseCase(useCaseName);
			return newUseCase;
		}
		
		/**
		 * Creates a new flow in the use case that contains this step.
		 * 
		 * @param flowName the name of the flow to be created.
		 * @return the newly created flow
		 * @throws ElementAlreadyInModelException if a flow with the specified name already exists in the use case
		 */
		public UseCaseFlow newFlow(String flowName) {
			Objects.requireNonNull(flowName);

			UseCaseFlow newFlow = getUseCase().newFlow(flowName);
			return newFlow;
		}

		/**
		 * Creates a new step in this flow, with the specified name, that follows this step in sequence.
		 * 
		 * @param stepName the name of the step to be created
		 * @return the newly created step, to ease creation of further steps
		 * @throws ElementAlreadyInModelException if a step with the specified name already exists in the use case
		 */
		public UseCaseStep newStep(String stepName) {			
			Objects.requireNonNull(stepName);

			UseCaseStep newStep = 
				getUseCase().newStep(stepName, getFlow(), Optional.of(UseCaseStep.this), Optional.empty());
			
			return newStep; 
		}
		
		/**
		 * After triggering the system reaction, raise the specified event.
		 * Internally calls {@link UseCaseRunner#reactTo(Object)} for the specified event.
		 * 
		 * @param <U> the type of event to be raised
		 * @param eventSupplier the supplier proving the event
		 * @return the system part
		 */
		public <U> SystemPart<T> raise(Supplier<U> eventSupplier) {
			systemReaction = systemReaction.andThen(x -> getUseCaseModel().getUseCaseRunner().reactTo(eventSupplier.get()));
			return this;
		}
		
		/**
		 * Repeat this step while the condition is fulfilled.
		 * 
		 * Note that the specified condition is evaluated the first time after the
		 * step, so the step is "run" at least one time before checking the repeat condition.
		 * 
		 * @param condition the condition to check
		 * @return the system part
		 */
		public SystemPart<T> repeatWhile(Predicate<UseCaseRunner> condition) {
			Objects.requireNonNull(condition);
			
			String thisStepName = getName();
			String newRepeatStepName = uniqueRepeatStepName();
			
			UseCaseStep newRepeatStep = newFlow(newRepeatStepName)
				.after(thisStepName).when(condition).newStep(newRepeatStepName);
			
			makeRepeatStepBehaveLikeThisStep(newRepeatStep);
			
			getFlow().continueAfter(thisStepName, Optional.of(newRepeatStep), Optional.empty());
			
			return this;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void makeRepeatStepBehaveLikeThisStep(UseCaseStep newRepeatStep) {
			newRepeatStep
				.actor(getActorPart().getActor()).handle(getEventPart().getEventClass())
				.system((Consumer)getSystemPart().getSystemReaction());
		}

		/**
		 * Makes the use case runner start from the beginning, when no
		 * flow and step has been run.
		 * 
		 * @see UseCaseRunner#restart()
		 * @return the use case this step belongs to, to ease creation of further flows
		 */
		public UseCase restart() {
			return getFlow().restart(Optional.of(UseCaseStep.this), Optional.empty());
		}

		/**
		 * Makes the use case runner continue after the specified step.
		 * Only steps of the use case that this step is contained in are taken into account.
		 * 
		 * @param stepName name of the step to continue after.
		 * @return the use case this step belongs to, to ease creation of further flows
		 * @throws NoSuchElementInUseCaseException if no step with the specified stepName is found in the current use case
		 */
		public UseCase continueAfter(String stepName) {
			Objects.requireNonNull(stepName);
			
			return getFlow().continueAfter(stepName, Optional.of(UseCaseStep.this), Optional.empty());
		}
	}
	
	/**
	 * Returns a unique name for a "repeat" step, to avoid name
	 * collisions if multiple "repeat" steps exist in the model.
	 * 
	 * Overwrite this only if you are not happy with the "automatically created"
	 * step names in the model.
	 * 
	 * @return a unique step name
	 */
	protected String uniqueRepeatStepName() {
		return stepNameWithPostfix(getName(), "REPEAT");
	}
}
