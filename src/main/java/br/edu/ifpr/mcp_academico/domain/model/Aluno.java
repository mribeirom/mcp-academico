// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// dados de um aluno. Um "record" é uma classe simples que só guarda valores:
// id (número interno), ra (registro acadêmico), nome e e-mail
public record Aluno(int id, String ra, String nome, String email) {}
