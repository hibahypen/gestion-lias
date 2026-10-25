package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.Convention;
import ma.lias.gestion_lias.model.Partner;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.ConventionRepository;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConventionService {

    private final ConventionRepository conventionRepository;
    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;

    private final Path storagePath = Paths.get("uploads").toAbsolutePath().normalize();

    // --- PARTENAIRES ---
    public List<Partner> getAllPartners() {
        return partnerRepository.findAllByOrderByNameAsc();
    }

    public Partner getPartnerById(Long id) {
        return partnerRepository.findById(id).orElseThrow();
    }

    public Partner savePartner(Partner partner) {
        return partnerRepository.save(partner);
    }

    public void deletePartner(Long id) {
        partnerRepository.deleteById(id);
    }

    // --- CONVENTIONS ---
    public List<Convention> getAllConventions() {
        return conventionRepository.findAllByOrderByStartDateDesc();
    }

    public Convention getConventionById(Long id) {
        return conventionRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Convention saveConvention(Convention convention, Long partnerId, List<Long> eventIds, MultipartFile file) throws IOException {
        if (convention.getId() != null) {
            convention.setDocumentUrl(getConventionById(convention.getId()).getDocumentUrl());
        }
        convention.setPartner(getPartnerById(partnerId));

        if (eventIds != null && !eventIds.isEmpty()) {
            List<ScientificEvent> events = eventRepository.findAllById(eventIds);
            convention.setAssociatedEvents(new ArrayList<>(events));
        } else {
            convention.setAssociatedEvents(new ArrayList<>());
        }

        // Upload document si fourni
        if (file != null && !file.isEmpty()) {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
            String fileName = StoredFileNames.stored(file.getOriginalFilename());
            try (var input = file.getInputStream()) {
                Files.copy(input, storagePath.resolve(fileName));
            }
            convention.setDocumentUrl("/documents/download/" + fileName);
        }

        return conventionRepository.save(convention);
    }

    public void deleteConvention(Long id) {
        conventionRepository.deleteById(id);
    }

    public long countActiveConventions() {
        return conventionRepository.findByStatus(Convention.ConventionStatus.ACTIVE).size();
    }
}
