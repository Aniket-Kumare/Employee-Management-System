-- ============================================================
--  Employee Management System  —  Oracle DB Setup Script
--  Run this in SQL*Plus or SQL Developer before launching app
-- ============================================================

-- 1. DROP table if it already exists (clean slate)
DROP TABLE employee PURGE;

-- 2. CREATE the employee table
CREATE TABLE employee (
    eid     VARCHAR2(10)    PRIMARY KEY,
    efname  VARCHAR2(30)    NOT NULL,
    elname  VARCHAR2(30)    NOT NULL,
    esal    NUMBER(10)      NOT NULL,
    eaddr   VARCHAR2(50),
    edept   VARCHAR2(30),
    eemail  VARCHAR2(50)
);

-- 3. INSERT sample data
INSERT INTO employee VALUES ('E001', 'Ravi',    'Kumar',   55000, 'Hyderabad', 'Engineering',  'ravi.k@company.com');
INSERT INTO employee VALUES ('E002', 'Priya',   'Sharma',  72000, 'Bangalore', 'Engineering',  'priya.s@company.com');
INSERT INTO employee VALUES ('E003', 'Arjun',   'Mehta',   48000, 'Mumbai',    'HR',            'arjun.m@company.com');
INSERT INTO employee VALUES ('E004', 'Sneha',   'Reddy',   61000, 'Chennai',   'Finance',       'sneha.r@company.com');
INSERT INTO employee VALUES ('E005', 'Kiran',   'Das',     83000, 'Pune',      'Engineering',  'kiran.d@company.com');
INSERT INTO employee VALUES ('E006', 'Meena',   'Pillai',  45000, 'Hyderabad', 'HR',            'meena.p@company.com');
INSERT INTO employee VALUES ('E007', 'Rahul',   'Gupta',   95000, 'Delhi',     'Management',   'rahul.g@company.com');
INSERT INTO employee VALUES ('E008', 'Anjali',  'Singh',   67000, 'Kolkata',   'Finance',       'anjali.s@company.com');

-- 4. Confirm data
SELECT * FROM employee ORDER BY edept, efname;
COMMIT;
