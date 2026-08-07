package use_case.custom_constellation;
import java.util.List;
import entity.CustomConstellation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConstellationOutputDataTest {

    @Test
    void exposesConstellationPassedToTheConstructor() {
        final CustomConstellation constellation =
                new CustomConstellation(
                        "My Pattern",
                        List.of()
                );
        final ConstellationOutputData outputData =
                new ConstellationOutputData(constellation);
        assertSame(constellation, outputData.getConstellation()
        );
    }
}