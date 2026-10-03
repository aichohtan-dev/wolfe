# Flyway migration history

`V14` is intentionally absent from the shipped migration set. It must not be recreated retroactively because existing databases may already have V15+ applied and Flyway is intentionally configured for forward-only migration order. The gap is documented by `V56__document_historical_migration_gap.sql`; no historical schema operation is invented.
