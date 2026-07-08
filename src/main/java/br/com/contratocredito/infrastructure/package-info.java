/**
 * Camada de INFRAESTRUTURA (adapters / implementações concretas).
 *
 * É onde o mundo externo entra em contato com o sistema:
 *   - Persistência (JPA, JDBC, Mongo, etc) — implementa os ports do domínio.
 *   - Configurações do Spring, beans, mensageria, cache.
 *
 * REGRA: o domínio declara a INTERFACE (port); esta camada fornece a
 * IMPLEMENTAÇÃO (adapter). Trocar de banco (SQL <-> NoSQL) só toca aqui,
 * sem afetar domain nem application.
 *
 * Banco de dados: AINDA NÃO DEFINIDO (SQL vs NoSQL) — decisão adiada
 * propositalmente. Quando definirmos, o adapter concreto entra aqui.
 */
package br.com.contratocredito.infrastructure;
