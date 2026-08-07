package use_case.check_conditions;

// interface with one method, e.g. checkConditions(CheckConditionsInputData inputData).
// This is what the Controller calls. Defining it as an interface is what lets the
// Controller depend on an abstraction instead of the concrete interactor.

public interface CheckConditionsInputBoundary {

    /**
     * Checks observing conditions for the given location and time.
     *
     * @param inputData the observer location and observation time to check
     */
    void checkConditions(CheckConditionsInputData inputData);
}
