package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConventionRepository extends JpaRepository<Convention, Long> {
    List<Convention> findAllByOrderByStartDateDesc();
    List<Convention> findByStatus(Convention.ConventionStatus status);
}