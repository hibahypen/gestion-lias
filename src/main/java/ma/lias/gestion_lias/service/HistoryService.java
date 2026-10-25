package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.AffiliationHistory;
import ma.lias.gestion_lias.model.LaboratoryMember;
import ma.lias.gestion_lias.model.LaboratoryMember.MemberStatus;
import ma.lias.gestion_lias.model.MemberHistory;
import ma.lias.gestion_lias.repository.AffiliationHistoryRepository;
import ma.lias.gestion_lias.repository.MemberHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Cahier de charge §3, §4.3, §7, §21 : tout est historisé, rien n'est supprimé. */
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final MemberHistoryRepository historyRepository;
    private final AffiliationHistoryRepository affiliationRepository;

    // ---------- Journal des modifications ----------

    public String currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getName())) {
            return "système";
        }
        return a.getName();
    }

    public void log(LaboratoryMember member, MemberHistory.ChangeType type,
                    String field, Object oldValue, Object newValue) {
        MemberHistory h = new MemberHistory();
        h.setMember(member);
        h.setChangeType(type);
        h.setFieldName(field);
        h.setOldValue(clean(oldValue));
        h.setNewValue(clean(newValue));
        h.setChangedBy(currentUser());
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    /** N'enregistre que si la valeur a vraiment changé. */
    public boolean logIfChanged(LaboratoryMember member, MemberHistory.ChangeType type,
                                String field, Object oldValue, Object newValue) {
        String o = clean(oldValue) == null ? "" : clean(oldValue);
        String n = clean(newValue) == null ? "" : clean(newValue);
        if (o.equals(n)) return false;
        log(member, type, field, oldValue, newValue);
        return true;
    }

    public List<MemberHistory> getHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByChangedAtDesc(memberId);
    }

    private String clean(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty()) return null;
        return s.length() > 1900 ? s.substring(0, 1900) + "…" : s;
    }

    // ---------- Historique des affiliations (§4.3) ----------

    public List<AffiliationHistory> getAffiliations(Long memberId) {
        return affiliationRepository.findByMemberIdOrderByStartDateDesc(memberId);
    }

    public void openAffiliation(LaboratoryMember member, LocalDate start) {
        AffiliationHistory a = new AffiliationHistory();
        a.setMember(member);
        a.setStartDate(start != null ? start : LocalDate.now());
        a.setLabOrigin(member.getLaboratoireOrigine());
        affiliationRepository.save(a);
    }

    public void closeAffiliation(LaboratoryMember member, LocalDate end) {
        affiliationRepository
                .findFirstByMemberIdAndEndDateIsNullOrderByStartDateDesc(member.getId())
                .ifPresent(a -> {
                    a.setEndDate(end);
                    affiliationRepository.save(a);
                });
    }

    private boolean isOut(MemberStatus s) {
        return s == MemberStatus.RETRAITE || s == MemberStatus.ANCIEN;
    }

    /**
     * Un membre peut quitter et revenir (§4.3) :
     * passage en RETRAITE/ANCIEN => la période d'affiliation est fermée,
     * retour vers un statut actif => une nouvelle période s'ouvre.
     */
    public void onStatusChange(LaboratoryMember member, MemberStatus oldStatus, MemberStatus newStatus) {
        if (!isOut(oldStatus) && isOut(newStatus)) {
            closeAffiliation(member, LocalDate.now());
        } else if (isOut(oldStatus) && newStatus != null && !isOut(newStatus)) {
            openAffiliation(member, LocalDate.now());
        }
    }

    /** Rattrapage pour les membres créés avant cette fonctionnalité. */
    public void ensureAffiliation(LaboratoryMember member) {
        if (member.getStatus() == null || isOut(member.getStatus())) return;
        if (affiliationRepository.findByMemberIdOrderByStartDateDesc(member.getId()).isEmpty()) {
            openAffiliation(member, member.getDateAffiliation());
        }
    }
}