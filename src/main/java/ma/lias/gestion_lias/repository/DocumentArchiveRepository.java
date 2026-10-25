package ma.lias.gestion_lias.repository;

import ma.lias.gestion_lias.model.DocumentArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentArchiveRepository extends JpaRepository<DocumentArchive, Long> {
    List<DocumentArchive> findAllByOrderByDocumentDateDesc();
    List<DocumentArchive> findByDocTypeOrderByDocumentDateDesc(DocumentArchive.DocumentType docType);
    List<DocumentArchive> findByEventIdOrderByDocumentDateDesc(Long eventId);
}