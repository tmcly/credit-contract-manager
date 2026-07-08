/**
 * Camada de DOMÍNIO (DDD + regras de negócio).
 *
 * Aqui vive o coração do sistema: entidades, objetos de valor, agregados,
 * eventos de domínio e as regras que definem o que é válido ou não.
 *
 * REGRA DE OURO (Clean Architecture / DDD):
 *   - NÃO importa nada de Spring, JPA, Web, banco ou framework externo.
 *   - O domínio não sabe quem o consome nem onde os dados são persistidos.
 *   - Se você precisar de um adapter externo, declare uma INTERFACE (port)
 *     aqui, e a implementação fica na camada de infrastructure.
 *
 * Exemplos futuros: Contrato, Cliente, StatusContrato (enum),
 * regras de transição de estado, value objects (ValorMonetario, Cpf, etc).
 */
package br.com.contratocredito.domain;
