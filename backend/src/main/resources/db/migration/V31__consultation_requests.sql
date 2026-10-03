CREATE TABLE consultation_requests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    project VARCHAR(160),
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_consultation_status_created ON consultation_requests(status, created_at);
CREATE INDEX idx_consultation_email ON consultation_requests(email);
