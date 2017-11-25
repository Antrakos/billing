DO $$
DECLARE
  command TEXT;
BEGIN
  FOR command IN (
    SELECT 'ALTER SEQUENCE ' ||
           quote_ident(PGT.schemaname) || '.' || quote_ident(S.relname) ||
           ' RESTART WITH 1;'
    FROM pg_class AS S,
      pg_depend AS D,
      pg_class AS T,
      pg_attribute AS C,
      pg_tables AS PGT
    WHERE S.relkind = 'S'
          AND S.oid = D.objid
          AND D.refobjid = T.oid
          AND D.refobjid = C.attrelid
          AND D.refobjsubid = C.attnum
          AND T.relname = PGT.tablename
    ORDER BY S.relname
  ) LOOP
    EXECUTE command;

  END LOOP;
END $$;
