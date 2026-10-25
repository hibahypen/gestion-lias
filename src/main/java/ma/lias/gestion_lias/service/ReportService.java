package ma.lias.gestion_lias.service;

import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Publication;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EventRepository eventRepository;
    private final PublicationRepository publicationRepository;
    private final MemberRepository memberRepository;

    public List<ScientificEvent> getEvents(int year) {
        return eventRepository.findAll().stream()
                .filter(e -> e.getEventDate() != null && e.getEventDate().getYear() == year)
                .sorted(Comparator.comparing(ScientificEvent::getEventDate))
                .toList();
    }

    public List<Publication> getPublications(int year) {
        return publicationRepository.findAll().stream()
                .filter(p -> p.getPubYear() != null && p.getPubYear() == year)
                .toList();
    }

    public List<LaboratoryMember> getNewMembers(int year) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getDateAffiliation() != null && m.getDateAffiliation().getYear() == year)
                .toList();
    }

    public Map<String, Long> countEventsByType(List<ScientificEvent> events) {
        return events.stream().collect(Collectors.groupingBy(
                e -> e.getType() == null ? "NON_DEFINI" : e.getType().name(),
                TreeMap::new,
                Collectors.counting()));
    }

    public Map<String, Long> countPublicationsByType(List<Publication> publications) {
        return publications.stream().collect(Collectors.groupingBy(
                p -> (p.getPubType() == null || p.getPubType().isBlank()) ? "Non précisé" : p.getPubType(),
                TreeMap::new,
                Collectors.counting()));
    }

    public Map<String, Long> countMembersByStatus() {
        return memberRepository.findAll().stream().collect(Collectors.groupingBy(
                m -> m.getStatus() == null ? "NON_DEFINI" : m.getStatus().name(),
                TreeMap::new,
                Collectors.counting()));
    }

    public long countMembers() {
        return memberRepository.count();
    }

    /** Années proposées dans la liste déroulante : année courante + années ayant des données. */
    public List<Integer> getAvailableYears() {
        TreeSet<Integer> years = new TreeSet<>(Comparator.reverseOrder());
        years.add(LocalDate.now().getYear());
        eventRepository.findAll().forEach(e -> {
            if (e.getEventDate() != null) years.add(e.getEventDate().getYear());
        });
        publicationRepository.findAll().forEach(p -> {
            if (p.getPubYear() != null) years.add(p.getPubYear());
        });
        return new ArrayList<>(years);
    }
}