package use_case.custom_constellation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.ConstellationLine;
import entity.CustomConstellation;
import entity.Star;

public class ConstellationInteractor
        implements ConstellationInputBoundary {

    private final ConstellationDataAccessInterface dataAccess;
    private final ConstellationOutputBoundary outputBoundary;

    public ConstellationInteractor(
            final ConstellationDataAccessInterface dataAccess,
            final ConstellationOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(final ConstellationInputData inputData) {
        if (inputData == null) {
            outputBoundary.prepareFailureView(
                    "Constellation information is required.");
            return;
        }

        final String name = inputData.getName() == null
                ? ""
                : inputData.getName().trim();
        final List<Star> selectedStars = inputData.getSelectedStars();

        if (name.isEmpty()) {
            outputBoundary.prepareFailureView(
                    "Enter a constellation name.");
            return;
        }

        if (selectedStars.size() < 2) {
            outputBoundary.prepareFailureView(
                    "Select at least two stars.");
            return;
        }

        if (containsDuplicateStars(selectedStars)) {
            outputBoundary.prepareFailureView(
                    "A star cannot be selected more than once.");
            return;
        }

        if (dataAccess.existsByName(name)) {
            outputBoundary.prepareFailureView(
                    "A constellation with this name already exists.");
            return;
        }

        final List<ConstellationLine> lines =
                createLines(selectedStars);
        final CustomConstellation constellation =
                new CustomConstellation(name, lines);

        dataAccess.save(constellation);
        outputBoundary.prepareSuccessView(
                new ConstellationOutputData(constellation));
    }

    private boolean containsDuplicateStars(
            final List<Star> stars) {
        final Set<Star> uniqueStars = new HashSet<>(stars);
        return uniqueStars.size() != stars.size();
    }

    private List<ConstellationLine> createLines(
            final List<Star> stars) {
        final List<ConstellationLine> lines =
                new ArrayList<>();

        for (int index = 0; index < stars.size() - 1; index++) {
            lines.add(new ConstellationLine(
                    stars.get(index),
                    stars.get(index + 1)));
        }

        return lines;
    }
}