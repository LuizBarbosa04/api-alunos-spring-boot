package com.exemplo.apialunos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.exemplo.apialunos.exception.RecursoNaoEncontradoException;
import com.exemplo.apialunos.model.Aluno;
import com.exemplo.apialunos.repository.AlunoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {
    @Mock private AlunoRepository repository;
    @InjectMocks private AlunoService service;

    private Aluno alunoValido() {
        return new Aluno(null, "Ana Souza", "ana@email.com", "Ciência da Computação", "20260001");
    }

    @Test
    void deveCadastrarAlunoValido() {
        // Arrange: monta o objeto e programa o retorno do repositório simulado.
        Aluno aluno = alunoValido();
        Aluno salvo = new Aluno(1L, "Ana Souza", "ana@email.com", "Ciência da Computação", "20260001");
        when(repository.save(aluno)).thenReturn(salvo);

        // Act: executa o método que está sendo testado.
        Aluno resultado = service.salvar(aluno);

        // Assert: confere o resultado e a interação com o Repository.
        assertEquals(1L, resultado.getId());
        assertEquals("Ana Souza", resultado.getNome());
        verify(repository).save(aluno);
    }

    @Test
    void deveBuscarAlunoPorId() {
        Aluno aluno = new Aluno(1L, "Ana Souza", "ana@email.com", "Ciência da Computação", "20260001");
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));
        assertEquals("Ana Souza", service.buscarPorId(1L).getNome());
        verify(repository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoNomeForVazio() {
        Aluno aluno = alunoValido();
        aluno.setNome(" ");
        assertThrows(IllegalArgumentException.class, () -> service.salvar(aluno));
    }

    @Test
    void deveLancarErroQuandoAlunoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L));
        verify(repository).findById(99L);
    }
}
