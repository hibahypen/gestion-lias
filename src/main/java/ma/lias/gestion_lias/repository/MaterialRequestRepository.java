package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.MaterialRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    List<MaterialRequest> findAllByOrderByRequestDateDesc();
    List<MaterialRequest> findByMemberIdOrderByRequestDateDesc(Long memberId);
    long countByStatus(MaterialRequest.RequestStatus status);
}