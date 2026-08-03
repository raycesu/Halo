package use_case.custom_constellation;

import java.util.List;
import entity.CustomConstellation;

public interface ConstellationDataAccessInterface {
    boolean existsByName(String name);
    void save(CustomConstellation constellation);

    List<CustomConstellation> findAll();
}
