package use_case.custom_constellation;

public interface ConstellationOutputBoundary {

    void prepareSuccessView(ConstellationOutputData outputData);
    void prepareFailureView(String errorMessage);
}
