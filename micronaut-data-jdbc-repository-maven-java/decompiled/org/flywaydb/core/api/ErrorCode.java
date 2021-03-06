package org.flywaydb.core.api;

public enum ErrorCode {
   FAULT,
   ERROR,
   JDBC_DRIVER,
   DB_CONNECTION,
   CONFIGURATION,
   DUPLICATE_VERSIONED_MIGRATION,
   DUPLICATE_REPEATABLE_MIGRATION,
   DUPLICATE_UNDO_MIGRATION,
   DUPLICATE_DELETED_MIGRATION,
   VALIDATE_ERROR,
   SCHEMA_DOES_NOT_EXIST,
   FAILED_REPEATABLE_MIGRATION,
   FAILED_VERSIONED_MIGRATION,
   APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED,
   APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED,
   RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED,
   RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED,
   OUTDATED_REPEATABLE_MIGRATION,
   TYPE_MISMATCH,
   CHECKSUM_MISMATCH,
   DESCRIPTION_MISMATCH;
}
