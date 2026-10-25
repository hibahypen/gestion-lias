package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.MemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {
    List<MemberHistory> findByMemberIdOrderByChangedAtDesc(Long memberId);
}