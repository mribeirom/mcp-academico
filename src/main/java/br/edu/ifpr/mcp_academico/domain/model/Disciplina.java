// pacote (pasta) onde ficam as classes que representam os dados do sistema
package br.edu.ifpr.mcp_academico.domain.model;

// dados de uma disciplina: id (número interno), código (como "SWEB"), nome,
// carga horária em horas e se ela está ativa (aceitando matrículas)
public record Disciplina(int id, String codigo, String nome, int cargaHoraria,
                         boolean ativa) {}
