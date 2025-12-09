package com.example.demo.repositories;

import com.example.demo.models.Utilisateur;
import com.example.demo.models.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByTokenVerification(String tokenVerification);

    Optional<Utilisateur> findByEmailAndCompteActiveTrue(String email);

    // NOUVELLES MÉTHODES
    List<Utilisateur> findByRole(RoleEnum role);

    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.compteActive = :actif")
    List<Utilisateur> findByRoleAndCompteActive(@Param("role") RoleEnum role,
                                                @Param("actif") boolean actif);

    List<Utilisateur> findByCompteActive(boolean actif);
}