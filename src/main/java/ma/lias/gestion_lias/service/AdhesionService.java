
package ma.lias.gestion_lias.service;

import ma.lias.gestion_lias.model.AdhesionRequest;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Role;
import ma.lias.gestion_lias.repository.AdhesionRequestRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdhesionService {

    private final AdhesionRequestRepository adhesionRequestRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // Ajout
    private final HistoryService historyService;

    @Transactional
    public void submit(AdhesionRequest request) {
        request.setId(null);
        request.setDateDecision(null);
        request.setTraitePar(null);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setStatus(AdhesionRequest.AdhesionStatus.EN_ATTENTE);
        request.setDateSubmission(LocalDate.now());
        adhesionRequestRepository.save(request);
    }

    public List<AdhesionRequest> getAll() {
        return adhesionRequestRepository.findAllByOrderByDateSubmissionDesc();
    }

    public long countPending() {
        return adhesionRequestRepository.countByStatus(AdhesionRequest.AdhesionStatus.EN_ATTENTE);
    }

    public AdhesionRequest getById(Long id) {
        return adhesionRequestRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void accept(Long id, LaboratoryMember directeur, LaboratoryMember.MemberStatus statutInitial) {
        AdhesionRequest request = getById(id);
        ensurePending(request);
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Un compte existe déjà pour cet email");
        }

        LaboratoryMember member = new LaboratoryMember();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        // Existing pending requests can still contain a legacy clear-text password.
        String password = request.getPassword();
        member.setPassword(password != null && password.matches("^\\$2[aby]\\$.*")
                ? password : passwordEncoder.encode(password));
        member.setStatus(statutInitial);
        member.setRole(Role.MEMBRE);
        member.setTeam(request.getTeamWanted());
        member.setEstablishmentOrigin(request.getEstablishmentOrigin());
        member.setPhotoUrl(request.getPhotoUrl());
        member.setDateAffiliation(LocalDate.now());
        member.setActive(true);

        // Modification demandée
        LaboratoryMember saved = memberRepository.save(member);

        historyService.log(
                saved,
                ma.lias.gestion_lias.model.MemberHistory.ChangeType.COMPTE,
                "Création du compte",
                null,
                "Adhésion acceptée, statut : " + statutInitial
        );

        historyService.openAffiliation(saved, saved.getDateAffiliation());

        request.setStatus(AdhesionRequest.AdhesionStatus.ACCEPTEE);
        request.setDateDecision(LocalDate.now());
        request.setTraitePar(directeur);
        adhesionRequestRepository.save(request);
    }

    @Transactional
    public void refuse(Long id, LaboratoryMember directeur) {
        AdhesionRequest request = getById(id);
        ensurePending(request);
        request.setStatus(AdhesionRequest.AdhesionStatus.REFUSEE);
        request.setDateDecision(LocalDate.now());
        request.setTraitePar(directeur);
        adhesionRequestRepository.save(request);
    }

    private void ensurePending(AdhesionRequest request) {
        if (request.getStatus() != AdhesionRequest.AdhesionStatus.EN_ATTENTE) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Cette demande a déjà été traitée");
        }
    }
}
