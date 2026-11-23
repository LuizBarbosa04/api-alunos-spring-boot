package com.exemplo.apialunos.service;

import com.exemplo.apialunos.model.Aluno;
import com.exemplo.apialunos.repository.AlunoRepository;
import com.exemplo.apialunos.exception.RecursoNaoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AlunoService {
    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);
    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public Aluno salvar(Aluno aluno) {
        validar(aluno);
        Aluno salvo = alunoRepository.save(aluno);
        logger.info("Aluno cadastrado com id={}", salvo.getId());
        return salvo;
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado: " + id));
    }

    public Aluno atualizar(Long id, Aluno dados) {
        Aluno aluno = buscarPorId(id);
        validar(dados);
        aluno.setNome(dados.getNome());
        aluno.setEmail(dados.getEmail());
        aluno.setCurso(dados.getCurso());
        aluno.setMatricula(dados.getMatricula());
        Aluno atualizado = alunoRepository.save(aluno);
        logger.info("Aluno atualizado com id={}", id);
        return atualizado;
    }

    public void remover(Long id) {
        if (!alunoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Aluno não encontrado: " + id);
        }
        alunoRepository.deleteById(id);
        logger.info("Aluno removido com id={}", id);
    }

    private void validar(Aluno aluno) {
        if (aluno == null || aluno.getNome() == null || aluno.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }
        if (aluno.getEmail() == null || aluno.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail obrigatório");
        }
        if (aluno.getCurso() == null || aluno.getCurso().isBlank()) {
            throw new IllegalArgumentException("Curso obrigatório");
        }
        if (aluno.getMatricula() == null || aluno.getMatricula().isBlank()) {
            throw new IllegalArgumentException("Matrícula obrigatória");
        }
    }
}
