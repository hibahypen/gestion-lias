package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.AdhesionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdhesionRequestRepository extends JpaRepository<AdhesionRequest, Long> {
    List<AdhesionRequest> findByStatusOrderByDateSubmissionDesc(AdhesionRequest.AdhesionStatus status);
    List<AdhesionRequest> findAllByOrderByDateSubmissionDesc();
    long countByStatus(AdhesionRequest.AdhesionStatus status);
}