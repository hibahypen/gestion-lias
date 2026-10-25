package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByPubYear(Integer year);
    List<Publication> findByAddedByIdOrderByPubYearDesc(Long memberId);
}