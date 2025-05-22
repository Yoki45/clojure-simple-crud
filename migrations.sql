CREATE TABLE loans (
                       id SERIAL PRIMARY KEY,
                       user_id VARCHAR(50),
                       amount NUMERIC,
                       duration_months INTEGER,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
