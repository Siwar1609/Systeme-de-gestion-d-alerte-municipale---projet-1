package com.example.demo.config;
import com.example.demo.models.MunicipalService;
import com.example.demo.repositories.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServiceDataInitializer implements CommandLineRunner {

    private final ServiceRepository serviceRepository;

    public ServiceDataInitializer(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(String... args) {

        if (serviceRepository.count() == 0) {

            MunicipalService infra = new MunicipalService();
            infra.setNom("Infrastructure");
            infra.setZoneGeographique("Tous les quartiers");

            MunicipalService proprete = new MunicipalService();
            proprete.setNom(" Propreté");
            proprete.setZoneGeographique("Tous les quartiers");

            MunicipalService espace = new MunicipalService();
            espace.setNom("Espaces Verts");
            espace.setZoneGeographique("Tous les quartiers");

            MunicipalService  eclairage = new MunicipalService();
            eclairage.setNom("Éclairage Public");
            eclairage.setZoneGeographique("Tous les quartiers");
            serviceRepository.save(infra);
            serviceRepository.save(proprete);
            serviceRepository.save(eclairage);
            serviceRepository.save(espace);

            System.out.println("Services municipaux insérés !");
        }
    }
}
