USE online_exam;

-- 10 students. Replace the names with your real list before running.
-- username is UNIQUE, so INSERT IGNORE skips any username that already exists.
INSERT IGNORE INTO users (username, password, full_name, role) VALUES
 ('student01', 'pass01', 'Aarav Sharma',   'STUDENT'),
 ('student02', 'pass02', 'Diya Patel',     'STUDENT'),
 ('student03', 'pass03', 'Rohan Iyer',     'STUDENT'),
 ('student04', 'pass04', 'Ananya Reddy',   'STUDENT'),
 ('student05', 'pass05', 'Karthik Nair',   'STUDENT'),
 ('student06', 'pass06', 'Meera Singh',    'STUDENT'),
 ('student07', 'pass07', 'Vikram Rao',     'STUDENT'),
 ('student08', 'pass08', 'Sneha Menon',    'STUDENT'),
 ('student09', 'pass09', 'Arjun Das',      'STUDENT'),
 ('student10', 'pass10', 'Priya Krishnan', 'STUDENT');
