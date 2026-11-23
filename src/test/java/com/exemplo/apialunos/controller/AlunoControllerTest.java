package com.exemplo.apialunos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exemplo.apialunos.model.Aluno;
import com.exemplo.apialunos.service.AlunoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlunoController.class)
class AlunoControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AlunoService service;

    @Test
    void deveRetornar201AoCadastrar() throws Exception {
        when(service.salvar(any(Aluno.class))).thenReturn(
                new Aluno(1L, "Ana Souza", "ana@email.com", "Ciência da Computação", "20260001"));
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\",\"email\":\"ana@email.com\",\"curso\":\"Ciência da Computação\",\"matricula\":\"20260001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
    }
}
