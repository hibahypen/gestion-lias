package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.Mandate;
import ma.lias.gestion_lias.model.MemberHistory.ChangeType;
import ma.lias.gestion_lias.model.Role;
import ma.lias.gestion_lias.repository.MandateRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MandateService {

    private final MandateRepository mandateRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;

    public List<Mandate> getActiveMandates() {
        return mandateRepository.findByActiveTrueOrderByStartDateDesc();
    }

    /** Cahier de charges §5.2 : Directeur en mandat actif uniquement */
    public Optional<LaboratoryMember> getDirecteurActuel() {
        return mandateRepository.findByRoleAndActiveTrue(Role.DIRECTEUR)
                .map(Mandate::getMember);
    }

    public String getDirecteurActuelNom() {
        return getDirecteurActuel()
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .orElse("Non défini");
    }

    @Transactional
    public void assignRole(Long memberId, Role role, String team) {
        LaboratoryMember member = memberRepository.findById(memberId).orElseThrow();
        Role oldRole = member.getRole();

        // Cahier de charges §5.2 : Un seul directeur en mandat à la fois.
        // L'ancien directeur redevient simple MEMBRE (son mandat passé reste en historique)
        if (role == Role.DIRECTEUR) {
            mandateRepository.findByRoleAndActiveTrue(Role.DIRECTEUR).ifPresent(old -> {
                LaboratoryMember former = old.getMember();
                if (!former.getId().equals(memberId)) {
                    closeMandate(old);
                    Role formerRole = former.getRole();
                    former.setRole(Role.MEMBRE); // Re-devient membre !
                    memberRepository.save(former);
                    historyService.log(former, ChangeType.ROLE, "Rôle", formerRole,
                            Role.MEMBRE + " (remplacé par nouveau directeur)");
                }
            });
        }

        // Clôturer les anciens mandats actifs du membre (sans jamais les supprimer)
        mandateRepository.findByMemberIdOrderByStartDateDesc(memberId).stream()
                .filter(Mandate::isActive)
                .forEach(this::closeMandate);

        Mandate mandate = new Mandate();
        mandate.setMember(member);
        mandate.setRole(role);
        mandate.setTeam(team);
        mandate.setStartDate(LocalDate.now());
        mandate.setActive(true);
        mandateRepository.save(mandate);

        member.setRole(role);
        memberRepository.save(member);

        String label = (team != null && !team.isBlank()) ? role + " (équipe : " + team + ")" : role.toString();
        historyService.log(member, ChangeType.ROLE, "Rôle", oldRole, label);
    }

    /**
     * Cahier de charges §7 & §21 : Fin de mandat.
     * Le membre redevient simple MEMBRE, son mandat et son historique restent intacts.
     */
    @Transactional
    public void endMandate(Long mandateId) {
        Mandate mandate = mandateRepository.findById(mandateId).orElseThrow();
        if (!mandate.isActive()) return;
        closeMandate(mandate);

        LaboratoryMember member = mandate.getMember();
        Role oldRole = member.getRole();

        // Le membre redevient MEMBRE (sauf si c'est l'ADMIN technique)
        if (member.getRole() != Role.ADMIN) {
            member.setRole(Role.MEMBRE);
            memberRepository.save(member);
            historyService.log(member, ChangeType.ROLE, "Rôle", oldRole, Role.MEMBRE + " (fin de mandat)");
        }
    }

    private void closeMandate(Mandate mandate) {
        mandate.setActive(false);
        mandate.setEndDate(LocalDate.now());
        mandateRepository.save(mandate);
    }
}
