package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.ScientificEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<ScientificEvent, Long> {
    List<ScientificEvent> findByArchivedFalse();
}