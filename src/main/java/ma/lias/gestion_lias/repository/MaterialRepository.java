package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findAllByOrderByArrivalDateDesc();
}