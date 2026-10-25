package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.MaterialAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialAssignmentRepository extends JpaRepository<MaterialAssignment, Long> {
    List<MaterialAssignment> findByActiveTrueOrderByAssignmentDateDesc();
    List<MaterialAssignment> findByMemberIdAndActiveTrue(Long memberId);
    List<MaterialAssignment> findAllByOrderByAssignmentDateDesc();
}