package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.DocumentArchive;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.DocumentArchiveRepository;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentArchiveRepository documentRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public List<DocumentArchive> getAll() {
        return documentRepository.findAllByOrderByDocumentDateDesc();
    }

    public List<DocumentArchive> getPVs() {
        return documentRepository.findByDocTypeOrderByDocumentDateDesc(DocumentArchive.DocumentType.PV_REUNION);
    }

    public DocumentArchive getById(Long id) {
        return documentRepository.findById(id).orElseThrow();
    }

    public DocumentArchive saveFile(MultipartFile file,
                                    String title,
                                    DocumentArchive.DocumentType docType,
                                    LocalDate docDate,
                                    Long eventId,
                                    String ordreDuJour,
                                    String userEmail) throws IOException {

        if (!Files.exists(fileStorageLocation)) {
            Files.createDirectories(fileStorageLocation);
        }

        if (file.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Choisissez un fichier non vide");
        String originalFileName = StoredFileNames.original(file.getOriginalFilename());
        String storedFileName = StoredFileNames.stored(originalFileName);

        Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
        try (var input = file.getInputStream()) {
            Files.copy(input, targetLocation);
        }

        DocumentArchive doc = new DocumentArchive();
        doc.setTitle(title != null && !title.isBlank() ? title : originalFileName);
        doc.setDocType(docType);
        doc.setFileName(originalFileName);
        doc.setFileDownloadUri("/documents/download/" + storedFileName);
        doc.setFileType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setDocumentDate(docDate != null ? docDate : LocalDate.now());
        doc.setUploadedAt(LocalDate.now());
        doc.setReunionOrdreDuJour(ordreDuJour);

        if (eventId != null) {
            eventRepository.findById(eventId).ifPresent(doc::setEvent);
        }

        memberRepository.findByEmail(userEmail).ifPresent(doc::setUploadedBy);

        return documentRepository.save(doc);
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(fileStorageLocation) || !Files.isRegularFile(filePath)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Fichier introuvable");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Fichier non trouvé " + fileName, ex);
        }
    }

    public void delete(Long id) {
        documentRepository.deleteById(id);
    }
}
