package br.edu.ifpr.mcp_academico.server.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_academico.server.ServerFixture;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class DisciplinasResourceTest {
   private final SyncResourceSpecification specification =
      new DisciplinasResource(new ServerFixture().disciplinas).specification();

   @Test
   void deveDeclararURIETipoDoConteudo() {
      assertEquals(DisciplinasResource.URI,specification.resource().uri());
      assertEquals("application/json",specification.resource().mimeType());
   }

   @Test
   void deveRetornarOCatalogoEmJson() {
      TextResourceContents contents = (TextResourceContents)
         specification.readHandler()
                      .apply(null,ReadResourceRequest.builder(
                                         DisciplinasResource.URI).build())
                      .contents().get(0);

      assertTrue(contents.text().contains("Serviços Web"));
      assertTrue(contents.text().contains("\"carga_horaria\" : 80"));
   }
}
