package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.Publication;
import ma.lias.gestion_lias.repository.MemberRepository;
import ma.lias.gestion_lias.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final MemberRepository memberRepository;

    public List<Publication> getAllPublications() {
        return publicationRepository.findAll();
    }

    public List<Publication> getByAuthor(Long memberId) {
        return publicationRepository.findByAddedByIdOrderByPubYearDesc(memberId);
    }

    public Publication getById(Long id) {
        return publicationRepository.findById(id).orElseThrow();
    }

    /** À la création, l'auteur = membre connecté. À la modification, l'auteur est conservé. */
    public Publication save(Publication form, String authorEmail) {
        Publication p = (form.getId() != null) ? getById(form.getId()) : new Publication();
        p.setTitle(form.getTitle());
        p.setPubType(form.getPubType());
        p.setPubYear(form.getPubYear());
        p.setJournal(form.getJournal());
        if (form.getId() == null) {
            p.setCreatedAt(LocalDate.now());
            memberRepository.findByEmail(authorEmail).ifPresent(p::setAddedBy);
        }
        return publicationRepository.save(p);
    }

    public void delete(Long id) {
        publicationRepository.deleteById(id);
    }
}