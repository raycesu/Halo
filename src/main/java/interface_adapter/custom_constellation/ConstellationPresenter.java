package interface_adapter.custom_constellation;

import java.util.ArrayList;
import java.util.List;

import entity.ConstellationLine;
import entity.CustomConstellation;
import entity.Star;
import use_case.custom_constellation.ConstellationOutputBoundary;
import use_case.custom_constellation.ConstellationOutputData;

public class ConstellationPresenter implements ConstellationOutputBoundary {
    private final ConstellationViewModel viewModel;

    public ConstellationPresenter(final ConstellationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(final ConstellationOutputData outputData) {
        final ConstellationDisplayData displayData = toDisplayData(outputData.getConstellation());
        viewModel.addConstellation(displayData);
        viewModel.setErrorMessage("");
        viewModel.setSuccessMessage("Constellation saved: " + outputData.getConstellation().getName());
    }

    @Override
    public void prepareFailureView(final String errorMessage) {
        viewModel.setSuccessMessage("");
        viewModel.setErrorMessage(errorMessage);
    }

    private static ConstellationDisplayData toDisplayData(final CustomConstellation constellation) {
        final List<ConstellationLine> entityLines = constellation.getLines();
        final List<ConstellationDisplayData.Line> displayLines = new ArrayList<>(entityLines.size());
        for (final ConstellationLine line : entityLines) {
            final Star start = line.getStartStar();
            final Star end = line.getEndStar();
            displayLines.add(new ConstellationDisplayData.Line(
                    start.getAltitude(),
                    start.getAzimuth(),
                    start.isAboveHorizon(),
                    end.getAltitude(),
                    end.getAzimuth(),
                    end.isAboveHorizon()));
        }
        return new ConstellationDisplayData(constellation.getName(), displayLines);
    }
}
