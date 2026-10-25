package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    List<Partner> findAllByOrderByNameAsc();
}