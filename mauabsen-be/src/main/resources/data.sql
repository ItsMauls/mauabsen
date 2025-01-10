INSERT INTO employees (username, email, password, role) 
SELECT 'admin', 'admin@example.com', 
'$2a$10$yourhashedpassword', 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM employees WHERE role = 'ROLE_ADMIN'
); 