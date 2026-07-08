/**
 * Infrastructure layer (adapters / concrete implementations).
 *
 * Where the external world touches the system:
 *   - Persistence (JPA, JDBC, Mongo, etc) — implements domain ports.
 *   - Spring configuration, beans, messaging, cache.
 *
 * RULE: the domain declares the INTERFACE (port); this layer provides the
 * IMPLEMENTATION (adapter). Swapping databases (SQL <-> NoSQL) only touches
 * this layer, without affecting domain or application.
 *
 * Database: NOT YET DEFINED (SQL vs NoSQL) — decision intentionally deferred.
 */
package br.com.creditcontract.infrastructure;
