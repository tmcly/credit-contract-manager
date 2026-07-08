/**
 * Camada de APLICAÇÃO (use cases / casos de uso).
 *
 * Orquestra o domínio para atender uma necessidade do negócio.
 * Cada caso de uso é representado por uma classe (ex: CriarContratoUseCase),
 * seguindo o princípio da Responsabilidade Única (S - SOLID).
 *
 * Características:
 *   - Não contém regras de negócio complexas (isso é do domínio).
 *   - Depende apenas de ABSTRAÇÕES (ports) da camada de domain/infrastructure.
 *   - Não sabe detalhes de transporte (HTTP, fila) nem de persistência concreta.
 *
 * Exemplos futuros: CriarContratoUseCase, CancelarContratoUseCase,
 * BloquearContratoUseCase, ReanalisarContratoUseCase.
 */
package br.com.contratocredito.application;
