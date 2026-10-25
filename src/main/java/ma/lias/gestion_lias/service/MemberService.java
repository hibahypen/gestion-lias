package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.LaboratoryMember.MemberStatus;
import ma.lias.gestion_lias.model.MemberHistory.ChangeType;
import ma.lias.gestion_lias.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoryService historyService;

    public List<LaboratoryMember> getAllMembers() {
        return memberRepository.findAll();
    }

    public List<LaboratoryMember> getActiveMembers() {
        return memberRepository.findAll().stream()
                .filter(LaboratoryMember::isActive)
                .filter(m -> m.getStatus() != MemberStatus.ANCIEN && m.getStatus() != MemberStatus.RETRAITE)
                .toList();
    }

    public LaboratoryMember getById(Long id) {
        return memberRepository.findById(id).orElseThrow();
    }

    public LaboratoryMember getByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow();
    }

    @Transactional
    public LaboratoryMember updateOwnProfile(String email, LaboratoryMember formData, String newPassword) {
        LaboratoryMember m = getByEmail(email);

        track(m, "Prénom", m.getFirstName(), formData.getFirstName());
        track(m, "Nom", m.getLastName(), formData.getLastName());
        track(m, "Photo", m.getPhotoUrl(), formData.getPhotoUrl());
        track(m, "Téléphone", m.getTelephone(), formData.getTelephone());
        track(m, "Biographie", m.getBiography(), formData.getBiography());
        track(m, "Centres d'intérêt", m.getCentresInteret(), formData.getCentresInteret());
        track(m, "Spécialité", m.getSpecialite(), formData.getSpecialite());

        m.setFirstName(formData.getFirstName());
        m.setLastName(formData.getLastName());
        m.setPhotoUrl(formData.getPhotoUrl());
        m.setTelephone(formData.getTelephone());
        m.setBiography(formData.getBiography());
        m.setCentresInteret(formData.getCentresInteret());
        m.setSpecialite(formData.getSpecialite());

        if (newPassword != null && !newPassword.isBlank()) {
            m.setPassword(passwordEncoder.encode(newPassword));
            historyService.log(m, ChangeType.COMPTE, "Mot de passe", null, "modifié par le membre");
        }
        return memberRepository.save(m);
    }

    @Transactional
    public LaboratoryMember save(LaboratoryMember member, String rawPassword) {
        member.setPassword(passwordEncoder.encode(
                (rawPassword == null || rawPassword.isBlank()) ? "lias2026" : rawPassword
        ));
        if (member.getDateAffiliation() == null) {
            member.setDateAffiliation(LocalDate.now());
        }
        if (member.getRole() == null) {
            member.setRole(ma.lias.gestion_lias.model.Role.MEMBRE);
        }
        member.setActive(true);

        LaboratoryMember saved = memberRepository.save(member);
        historyService.log(saved, ChangeType.COMPTE, "Création du compte", null, "Statut : " + saved.getStatus());

        if (saved.getStatus() != MemberStatus.RETRAITE && saved.getStatus() != MemberStatus.ANCIEN) {
            historyService.openAffiliation(saved, saved.getDateAffiliation());
        }
        return saved;
    }

    @Transactional
    public LaboratoryMember updateByAdmin(LaboratoryMember form, String rawPassword) {
        LaboratoryMember m = getById(form.getId());
        MemberStatus oldStatus = m.getStatus();
        MemberStatus newStatus = form.getStatus() != null ? form.getStatus() : oldStatus;

        historyService.logIfChanged(m, ChangeType.STATUT, "Statut", oldStatus, newStatus);
        historyService.logIfChanged(m, ChangeType.AFFILIATION, "Date d'affiliation", m.getDateAffiliation(), form.getDateAffiliation());
        track(m, "Prénom", m.getFirstName(), form.getFirstName());
        track(m, "Nom", m.getLastName(), form.getLastName());
        track(m, "Email", m.getEmail(), form.getEmail());
        track(m, "Équipe", m.getTeam(), form.getTeam());
        track(m, "Établissement d'origine", m.getEstablishmentOrigin(), form.getEstablishmentOrigin());
        track(m, "Laboratoire d'origine", m.getLaboratoireOrigine(), form.getLaboratoireOrigine());
        track(m, "Date d'embauche", m.getDateEmbauche(), form.getDateEmbauche());
        track(m, "Biographie", m.getBiography(), form.getBiography());

        m.setFirstName(form.getFirstName());
        m.setLastName(form.getLastName());
        m.setEmail(form.getEmail());
        m.setStatus(newStatus);
        m.setTeam(form.getTeam());
        m.setEstablishmentOrigin(form.getEstablishmentOrigin());
        m.setLaboratoireOrigine(form.getLaboratoireOrigine());
        m.setDateAffiliation(form.getDateAffiliation() != null ? form.getDateAffiliation() : m.getDateAffiliation());
        m.setDateEmbauche(form.getDateEmbauche());
        m.setDateNaissance(form.getDateNaissance());
        m.setBiography(form.getBiography());

        if (rawPassword != null && !rawPassword.isBlank()) {
            m.setPassword(passwordEncoder.encode(rawPassword));
            historyService.log(m, ChangeType.COMPTE, "Mot de passe", null, "réinitialisé par l'administrateur");
        }

        LaboratoryMember saved = memberRepository.save(m);
        historyService.onStatusChange(saved, oldStatus, newStatus);
        return saved;
    }

    @Transactional
    public void archive(Long id) {
        LaboratoryMember m = getById(id);
        MemberStatus old = m.getStatus();
        if (old == MemberStatus.ANCIEN) return;
        m.setStatus(MemberStatus.ANCIEN);
        m.setActive(false);
        memberRepository.save(m);
        historyService.log(m, ChangeType.STATUT, "Statut", old, MemberStatus.ANCIEN);
        historyService.onStatusChange(m, old, MemberStatus.ANCIEN);
    }

    @Transactional
    public void toggleActive(Long id) {
        LaboratoryMember m = getById(id);
        boolean before = m.isActive();
        m.setActive(!before);
        memberRepository.save(m);
        historyService.log(m, ChangeType.COMPTE, "Compte actif", before ? "Oui" : "Non", !before ? "Oui" : "Non");
    }

    private void track(LaboratoryMember m, String label, Object oldV, Object newV) {
        historyService.logIfChanged(m, ChangeType.PROFIL, label, oldV, newV);
    }
}