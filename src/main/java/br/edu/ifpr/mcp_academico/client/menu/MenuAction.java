// pacote (pasta) do menu do cliente
package br.edu.ifpr.mcp_academico.client.menu;

// padrão de projeto Command: cada opção do menu principal é um objeto, o que
// permite incluir uma opção sem alterar o menu
public interface MenuAction {
   // texto da opção mostrado no menu
   public String label();
   // o que acontece quando o usuário escolhe a opção
   public void execute();
}
