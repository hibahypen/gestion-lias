package ma.lias.gestion_lias.service;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.*;
import ma.lias.gestion_lias.repository.MaterialAssignmentRepository;
import ma.lias.gestion_lias.repository.MaterialRepository;
import ma.lias.gestion_lias.repository.MaterialRequestRepository;
import ma.lias.gestion_lias.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialAssignmentRepository assignmentRepository;
    private final MaterialRequestRepository requestRepository;
    private final MemberRepository memberRepository;

    public List<Material> getAllMaterials() {
        return materialRepository.findAllByOrderByArrivalDateDesc();
    }

    public Material getMaterialById(Long id) {
        return materialRepository.findById(id).orElseThrow();
    }

    public Material saveMaterial(Material material) {
        if (material.getArrivalDate() == null) {
            material.setArrivalDate(LocalDate.now());
        }
        return materialRepository.save(material);
    }

    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    @Transactional
    public void assignMaterial(Long materialId, Long memberId, LocalDate returnDate, String notes) {
        Material material = getMaterialById(materialId);
        LaboratoryMember member = memberRepository.findById(memberId).orElseThrow();

        if (material.getQuantity() == null || material.getQuantity() <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Stock épuisé");
        }
        if (material.getQuantity() > 0) {
            material.setQuantity(material.getQuantity() - 1);
            materialRepository.save(material);
        }

        MaterialAssignment assignment = new MaterialAssignment();
        assignment.setMaterial(material);
        assignment.setMember(member);
        assignment.setAssignmentDate(LocalDate.now());
        assignment.setExpectedReturnDate(returnDate);
        assignment.setNotes(notes);
        assignment.setActive(true);

        assignmentRepository.save(assignment);
    }

    @Transactional
    public void returnMaterial(Long assignmentId) {
        MaterialAssignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        if (!assignment.isActive()) return;
        assignment.setActive(false);
        assignment.setActualReturnDate(LocalDate.now());
        assignmentRepository.save(assignment);

        Material material = assignment.getMaterial();
        material.setQuantity(material.getQuantity() == null ? 1 : material.getQuantity() + 1);
        materialRepository.save(material);
    }

    public List<MaterialAssignment> getActiveAssignments() {
        return assignmentRepository.findByActiveTrueOrderByAssignmentDateDesc();
    }

    public Map<String, Object> getDistributionStats() {
        List<LaboratoryMember> allActiveMembers = memberRepository.findAll().stream()
                .filter(LaboratoryMember::isActive)
                .filter(m -> m.getStatus() != LaboratoryMember.MemberStatus.ANCIEN && m.getStatus() != LaboratoryMember.MemberStatus.RETRAITE)
                .filter(m -> !"admin@lias.ma".equalsIgnoreCase(m.getEmail()))
                .toList();

        List<LaboratoryMember> membersWithMaterial = new ArrayList<>();
        List<LaboratoryMember> membersWithoutMaterial = new ArrayList<>();
        Map<Long, List<MaterialAssignment>> memberAssignments = new HashMap<>();

        for (LaboratoryMember m : allActiveMembers) {
            List<MaterialAssignment> list = assignmentRepository.findByMemberIdAndActiveTrue(m.getId());
            if (!list.isEmpty()) {
                membersWithMaterial.add(m);
                memberAssignments.put(m.getId(), list);
            } else {
                membersWithoutMaterial.add(m);
            }
        }

        membersWithoutMaterial.sort(Comparator.comparing(
                LaboratoryMember::getDateAffiliation,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        Map<String, Object> data = new HashMap<>();
        data.put("withMaterial", membersWithMaterial);
        data.put("withoutMaterial", membersWithoutMaterial);
        data.put("assignmentsMap", memberAssignments);
        data.put("totalActiveMembers", allActiveMembers.size());
        data.put("tauxCouverture", allActiveMembers.isEmpty() ? 0 :
                (membersWithMaterial.size() * 100) / allActiveMembers.size());

        return data;
    }

    public List<MaterialRequest> getAllRequests() {
        return requestRepository.findAllByOrderByRequestDateDesc();
    }

    public List<MaterialRequest> getRequestsByMember(Long memberId) {
        return requestRepository.findByMemberIdOrderByRequestDateDesc(memberId);
    }

    public void submitRequest(MaterialRequest req, String userEmail) {
        LaboratoryMember member = memberRepository.findByEmail(userEmail).orElseThrow();
        req.setId(null);
        req.setHandledBy(null);
        req.setDecisionDate(null);
        if (req.getQuantity() == null || req.getQuantity() < 1 || req.getMaterialName() == null || req.getMaterialName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Nom et quantité positive requis");
        }
        req.setMember(member);
        req.setRequestDate(LocalDate.now());
        req.setStatus(MaterialRequest.RequestStatus.EN_ATTENTE);
        requestRepository.save(req);
    }

    public void decideRequest(Long requestId, MaterialRequest.RequestStatus decision, String deciderEmail) {
        MaterialRequest req = requestRepository.findById(requestId).orElseThrow();
        LaboratoryMember decider = memberRepository.findByEmail(deciderEmail).orElseThrow();

        req.setStatus(decision);
        req.setDecisionDate(LocalDate.now());
        req.setHandledBy(decider);
        requestRepository.save(req);
    }

    public long countPendingRequests() {
        return requestRepository.countByStatus(MaterialRequest.RequestStatus.EN_ATTENTE);
    }
}