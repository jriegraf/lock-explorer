CREATE USER lock_explorer IDENTIFIED BY "";

GRANT
    CREATE SESSION
TO lock_explorer;

ALTER USER lock_explorer
    QUOTA UNLIMITED ON users;

GRANT sysdba TO lock_explorer;