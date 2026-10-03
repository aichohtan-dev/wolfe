# Wolfe Dependency Security Baseline

- Spring Boot remains on **3.5.16**, the final OSS release of the 3.5.x generation. A migration to 4.x is a separate compatibility project and is intentionally not mixed into this security patch.
- Spring Security is inherited from the Spring Boot dependency management; no vulnerable SAML, X.509 client-certificate, Authorization Server, or CookieRequestCache feature is enabled by Wolfe.
- Apache PDFBox is pinned to **3.0.8**.
- Node is pinned to **22.23.2** in `.node-version`, `.nvmrc`, CI, and the frontend build image.
- Production Docker images use immutable digests.
- GitHub Actions use full commit SHAs.

Runtime dependency scanning should still run in CI (for example, Maven dependency scanning and an SBOM/vulnerability scanner) because transitive dependencies can change only when the lock/parent versions change.
