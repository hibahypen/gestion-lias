package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.ScientificEvent;
import ma.lias.gestion_lias.repository.EventRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    public List<ScientificEvent> getAllEvents() {
        return eventRepository.findByArchivedFalse();
    }

    public ScientificEvent getById(Long id) {
        return eventRepository.findById(id).orElseThrow();
    }

    /** Crée ou met à jour (sans perdre archived) et affecte les organisateurs. */
    public ScientificEvent save(ScientificEvent form, List<Long> organizerIds) {
        ScientificEvent event = (form.getId() != null) ? getById(form.getId()) : new ScientificEvent();
        event.setTitle(form.getTitle());
        event.setType(form.getType());
        event.setEventDate(form.getEventDate());
        event.setDescription(form.getDescription());
        event.setOrganizers(organizerIds == null
                ? new ArrayList<>()
                : new ArrayList<>(memberRepository.findAllById(organizerIds)));
        return eventRepository.save(event);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }
}