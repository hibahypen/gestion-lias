package ma.lias.gestion_lias.service;

import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final PublicationRepository publicationRepository;
    private final MandateService mandateService;

    public long countPermanentMembers() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getStatus() == LaboratoryMember.MemberStatus.PERMANENT)
                .count();
    }

    public long countDoctorants() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getStatus() == LaboratoryMember.MemberStatus.DOCTORANT)
                .count();
    }

    public long countEvents() {
        return eventRepository.findByArchivedFalse().size();
    }

    public long countPublications() {
        return publicationRepository.findAll().size();
    }

    public String getDirecteurActuel() {
        return mandateService.getDirecteurActuelNom();
    }

    // ===== Données pour les graphiques (charts) du dashboard =====

    public Map<String, Long> getMembersByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (LaboratoryMember.MemberStatus s : LaboratoryMember.MemberStatus.values()) {
            result.put(s.name(), 0L);
        }
        memberRepository.findAll().stream()
                .filter(m -> m.getStatus() != null)
                .collect(Collectors.groupingBy(m -> m.getStatus().name(), Collectors.counting()))
                .forEach(result::put);
        return result;
    }

    public Map<String, Long> getMembersByTeam() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getTeam() != null && !m.getTeam().isBlank())
                .collect(Collectors.groupingBy(LaboratoryMember::getTeam, Collectors.counting()));
    }

    public Map<String, Long> getEventsByType() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getType() != null)
                .collect(Collectors.groupingBy(e -> e.getType().name(), Collectors.counting()));
    }

    public Map<String, Long> getPublicationsByYear() {
        return publicationRepository.findAll().stream()
                .filter(p -> p.getPubYear() != null)
                .sorted((a, b) -> a.getPubYear().compareTo(b.getPubYear()))
                .collect(Collectors.groupingBy(p -> p.getPubYear().toString(), LinkedHashMap::new, Collectors.counting()));
    }
}