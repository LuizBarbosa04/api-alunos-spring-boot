package com.exemplo.apialunos.repository;

import com.exemplo.apialunos.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
