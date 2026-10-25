package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.LaboratoryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<LaboratoryMember, Long> {
    Optional<LaboratoryMember> findByEmail(String email);
}