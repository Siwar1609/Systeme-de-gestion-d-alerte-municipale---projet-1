package com.example.demo.dto.agent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentUpdateDTO {
    private String nom;
    private Long serviceId;  // pour changer le MunicipalService
    private Long quartierId;  // pour changer le Quartier
}
