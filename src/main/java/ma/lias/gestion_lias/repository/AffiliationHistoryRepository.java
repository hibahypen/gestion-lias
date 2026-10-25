package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.AffiliationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AffiliationHistoryRepository extends JpaRepository<AffiliationHistory, Long> {
    List<AffiliationHistory> findByMemberIdOrderByStartDateDesc(Long memberId);
    Optional<AffiliationHistory> findFirstByMemberIdAndEndDateIsNullOrderByStartDateDesc(Long memberId);
}