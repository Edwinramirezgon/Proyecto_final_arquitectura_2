SELECT 'CREATE DATABASE authdb' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'authdb')\gexec
SELECT 'CREATE DATABASE zonesdb' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'zonesdb')\gexec
SELECT 'CREATE DATABASE alertdb' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'alertdb')\gexec
