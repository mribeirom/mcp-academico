package br.edu.ifpr.mcp_academico.server.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.server.ServerFixture;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class HistoricoAlunoResourceTest {
   private final SyncResourceTemplateSpecification specification =
      new HistoricoAlunoResource(new ServerFixture().relatorios).specification();

   private ReadResourceResult read(String uri) {
      return specification.readHandler()
                          .apply(null,ReadResourceRequest.builder(uri).build());
   }

   @Test
   void deveDeclararOModeloDeURI() {
      assertEquals(HistoricoAlunoResource.URI_TEMPLATE,
                   specification.resourceTemplate().uriTemplate());
   }

   @Test
   void deveRetornarOHistoricoDoAlunoDaURI() {
      TextResourceContents contents = (TextResourceContents)
                read("academico://alunos/1/historico").contents().get(0);

      assertEquals("application/json",contents.mimeType());
      assertTrue(contents.text().contains("Ana Conceição"));
      assertTrue(contents.text().contains("APROVADO"));
   }

   @Test
   void deveFalharSeOIdDaURINaoForNumerico() {
      McpError error = assertThrows(McpError.class,
                             () -> read("academico://alunos/abc/historico"));

      assertTrue(error.getMessage().contains("ARGUMENTO_INVALIDO"));
   }

   @Test
   void deveFalharSeOAlunoNaoExistir() {
      McpError error = assertThrows(McpError.class,
                             () -> read("academico://alunos/99/historico"));

      assertTrue(error.getMessage().contains("ALUNO_NAO_ENCONTRADO"));
   }
}
