package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.Mandate;
import ma.lias.gestion_lias.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MandateRepository extends JpaRepository<Mandate, Long> {
    Optional<Mandate> findByRoleAndActiveTrue(Role role);
    Optional<Mandate> findByRoleAndTeamAndActiveTrue(Role role, String team);
    List<Mandate> findByActiveTrueOrderByStartDateDesc();
    List<Mandate> findByMemberIdOrderByStartDateDesc(Long memberId);
}