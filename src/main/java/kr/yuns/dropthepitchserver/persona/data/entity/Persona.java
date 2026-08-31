package kr.yuns.dropthepitchserver.persona.data.entity;

import jakarta.persistence.*;
import kr.yuns.dropthepitchserver.common.jpa.BaseEntity;
import kr.yuns.dropthepitchserver.persona.data.enums.Gender;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persona")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Persona extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String imageUrl;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaBasic personaBasic;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaEconomy personaEconomy;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaPsychology personaPsychology;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaLifestyle personaLifestyle;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaDigital personaDigital;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonaDecision personaDecision;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PersonaTag> personaTags = new ArrayList<>();

    public void assignPersonaBasic(PersonaBasic personaBasic) {
        this.personaBasic = personaBasic;
    }

    public void assignPersonaEconomy(PersonaEconomy personaEconomy) {
        this.personaEconomy = personaEconomy;
    }

    public void assignPersonaPsychology(PersonaPsychology personaPsychology) {
        this.personaPsychology = personaPsychology;
    }

    public void assignPersonaLifestyle(PersonaLifestyle personaLifestyle) {
        this.personaLifestyle = personaLifestyle;
    }

    public void assignPersonaDigital(PersonaDigital personaDigital) {
        this.personaDigital = personaDigital;
    }

    public void assignPersonaDecision(PersonaDecision personaDecision) {
        this.personaDecision = personaDecision;
    }

    public void addPersonaTag(PersonaTag personaTag) {
        this.personaTags.add(personaTag);
    }
}
