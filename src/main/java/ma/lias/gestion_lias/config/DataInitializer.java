package ma.lias.gestion_lias.config;

import lombok.RequiredArgsConstructor;
import ma.lias.gestion_lias.model.*;
import ma.lias.gestion_lias.repository.*;
import ma.lias.gestion_lias.service.HistoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final MandateRepository mandateRepository;
    private final MaterialRepository materialRepository;
    private final PartnerRepository partnerRepository;
    private final ConventionRepository conventionRepository;
    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoryService historyService;

    @Override
    public void run(String... args) {

        if (memberRepository.findByEmail("admin@lias.ma").isEmpty()) {
            LaboratoryMember admin = new LaboratoryMember();
            admin.setFirstName("Admin");
            admin.setLastName("Système");
            admin.setEmail("admin@lias.ma");
            admin.setPassword(passwordEncoder.encode(initialPassword()));
            admin.setStatus(LaboratoryMember.MemberStatus.PERMANENT);
            admin.setRole(Role.ADMIN);
            admin.setDateAffiliation(LocalDate.now());
            admin.setActive(true);
            memberRepository.save(admin);
        }

        if (memberRepository.findByEmail("directeur@lias.ma").isEmpty()) {
            LaboratoryMember directeur = new LaboratoryMember();
            directeur.setFirstName("Abderrahim");
            directeur.setLastName("Belaangour");
            directeur.setEmail("directeur@lias.ma");
            directeur.setPassword(passwordEncoder.encode(initialPassword()));
            directeur.setStatus(LaboratoryMember.MemberStatus.PERMANENT);
            directeur.setRole(Role.DIRECTEUR);
            directeur.setSpecialite("Génie Logiciel & Big Data");
            directeur.setDateAffiliation(LocalDate.of(2018, 9, 1));
            directeur.setActive(true);
            memberRepository.save(directeur);

            Mandate mandateDir = new Mandate();
            mandateDir.setMember(directeur);
            mandateDir.setRole(Role.DIRECTEUR);
            mandateDir.setStartDate(LocalDate.now().minusYears(1));
            mandateDir.setActive(true);
            mandateRepository.save(mandateDir);
        }

        seedTeamWithMembers(
                "Data Science & Intelligence Artificielle",
                "Karim", "El Amrani", "k.elamrani@lias.ma",
                "Intelligence Artificielle",
                new String[][]{
                        {"Sara", "Bennani", "s.bennani@lias.ma",
                                "PERMANENT", "Computer Vision"},
                        {"Omar", "Mansouri", "o.mansouri@lias.ma",
                                "DOCTORANT", "Deep Learning & NLP"},
                        {"Fatima Zahra", "Idrissi", "fz.idrissi@lias.ma",
                                "DOCTORANT", "Data Mining"}
                }
        );

        seedTeamWithMembers(
                "Big Data & Cloud Distributed Systems",
                "Mohamed Amine", "Chafiq", "ma.chafiq@lias.ma",
                "Systèmes Distribués",
                new String[][]{
                        {"Hicham", "Berrada", "h.berrada@lias.ma",
                                "PERMANENT", "Cloud Computing"},
                        {"Salma", "Tazi", "s.tazi@lias.ma",
                                "DOCTORANT", "Stream Processing (Spark)"}
                }
        );

        seedTeamWithMembers(
                "Sécurité des Systèmes & Blockchain",
                "Nadia", "Benkirane", "n.benkirane@lias.ma",
                "Cybersécurité & Crypto",
                new String[][]{
                        {"Adil", "Zerhouni", "a.zerhouni@lias.ma",
                                "ASSOCIE", "Smart Contracts"},
                        {"Ayoub", "Tahiri", "a.tahiri@lias.ma",
                                "DOCTORANT", "Blockchain & Traçabilité"}
                }
        );

        seedTeamWithMembers(
                "IoT, Systèmes Embarqués & Smart Cities",
                "Rachid", "Boukhriss", "r.boukhriss@lias.ma",
                "Systèmes Embarqués",
                new String[][]{
                        {"Mounir", "Bennani", "m.bennani@lias.ma",
                                "PERMANENT", "Edge AI & IoT"},
                        {"Zineb", "El Fassi", "z.elfassi@lias.ma",
                                "DOCTORANT", "Smart Cities"}
                }
        );

        seedTeamWithMembers(
                "Aide à la Décision & Recherche Opérationnelle",
                "Fatima Zahra", "Alami", "fz.alami@lias.ma",
                "Optimisation Combinatoire",
                new String[][]{
                        {"Tariq", "Naciri", "t.naciri@lias.ma",
                                "PERMANENT", "Recherche Opérationnelle"},
                        {"Houda", "Belhadj", "h.belhadj@lias.ma",
                                "DOCTORANT", "Process Mining"}
                }
        );

        memberRepository.findAll()
                .forEach(historyService::ensureAffiliation);
    }

    private void seedTeamWithMembers(
            String teamName,
            String chefFirst,
            String chefLast,
            String chefEmail,
            String chefSpec,
            String[][] members
    ) {
        Team team = teamRepository.findByName(teamName).orElseGet(() -> {
            Team t = new Team();
            t.setName(teamName);
            t.setDescription(
                    "Recherche scientifique et développement "
                            + "au sein du laboratoire LIAS."
            );
            return teamRepository.save(t);
        });

        LaboratoryMember chef = memberRepository.findByEmail(chefEmail)
                .orElseGet(() -> {
                    LaboratoryMember m = new LaboratoryMember();
                    m.setFirstName(chefFirst);
                    m.setLastName(chefLast);
                    m.setEmail(chefEmail);
                    m.setPassword(
                            passwordEncoder.encode(initialPassword())
                    );
                    m.setStatus(
                            LaboratoryMember.MemberStatus.PERMANENT
                    );
                    m.setRole(Role.CHEF_EQUIPE);
                    m.setTeam(team.getName());
                    m.setSpecialite(chefSpec);
                    m.setEstablishmentOrigin(
                            "Faculté des Sciences Ben M'Sik"
                    );
                    m.setDateAffiliation(
                            LocalDate.now().minusYears(2)
                    );
                    m.setActive(true);
                    return memberRepository.save(m);
                });

        if (mandateRepository.findByRoleAndTeamAndActiveTrue(
                Role.CHEF_EQUIPE, team.getName()
        ).isEmpty()) {
            Mandate mandate = new Mandate();
            mandate.setMember(chef);
            mandate.setRole(Role.CHEF_EQUIPE);
            mandate.setTeam(team.getName());
            mandate.setStartDate(LocalDate.now().minusYears(1));
            mandate.setActive(true);
            mandateRepository.save(mandate);
        }

        for (String[] memData : members) {
            String memberEmail = memData[2];

            if (memberRepository.findByEmail(memberEmail).isEmpty()) {
                LaboratoryMember m = new LaboratoryMember();
                m.setFirstName(memData[0]);
                m.setLastName(memData[1]);
                m.setEmail(memberEmail);
                m.setPassword(
                        passwordEncoder.encode(initialPassword())
                );
                m.setStatus(
                        LaboratoryMember.MemberStatus.valueOf(memData[3])
                );
                m.setRole(Role.MEMBRE);
                m.setTeam(team.getName());
                m.setSpecialite(memData[4]);
                m.setEstablishmentOrigin(
                        "Faculté des Sciences Ben M'Sik"
                );
                m.setDateAffiliation(LocalDate.now().minusMonths(8));
                m.setActive(true);
                memberRepository.save(m);
            }
        }
    }

    /**
     * Lit le mot de passe initial depuis l'environnement.
     * Appelée uniquement pour créer un compte absent de la base.
     */
    private String initialPassword() {
        String password = System.getenv("LIAS_INITIAL_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Définissez LIAS_INITIAL_PASSWORD dans les variables "
                            + "d'environnement IntelliJ pour créer "
                            + "les comptes initiaux."
            );
        }

        return password;
    }
}
