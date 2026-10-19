package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    List<Usuario> findAllByOrderByAtivoDescNomeAsc();

    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);
}
