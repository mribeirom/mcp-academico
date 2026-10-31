package br.edu.ifpr.mcp_academico.server.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.domain.model.Situacao;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class RegrasAprovacaoResourceTest {
   private final SyncResourceSpecification specification =
                                 new RegrasAprovacaoResource().specification();

   private TextResourceContents read() {
      ReadResourceRequest request =
           ReadResourceRequest.builder(RegrasAprovacaoResource.URI).build();
      ReadResourceResult  result  = specification.readHandler()
                                                 .apply(null,request);

      return (TextResourceContents) result.contents().get(0);
   }

   @Test
   void deveDeclararURIETipoDoConteudo() {
      assertEquals(RegrasAprovacaoResource.URI,specification.resource().uri());
      assertEquals("text/markdown",specification.resource().mimeType());
   }

   @Test
   void deveRetornarOsCriteriosDaCalculadora() {
      TextResourceContents contents = read();

      assertEquals(RegrasAprovacaoResource.URI,contents.uri());
      assertEquals("text/markdown",contents.mimeType());
      assertTrue(contents.text().contains("6,0"),contents.text());
      assertTrue(contents.text().contains("75%"),contents.text());
   }

   @Test
   void deveDescreverTodasAsSituacoes() {
      String text = read().text();

      for (Situacao situacao : Situacao.values())
         assertTrue(text.contains(situacao.name()),situacao.name());
   }
}
