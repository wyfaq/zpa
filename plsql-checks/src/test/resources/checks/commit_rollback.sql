create procedure test is

  procedure test2 is
    pragma autonomous_transaction;
  begin
    commit; -- no violation here, it is in an autonomous transaction
  end;
  
begin
 commit; -- Noncompliant {{Avoid COMMIT calls unless it is in an autonomous transaction.}}
 
 begin
   rollback; -- Noncompliant {{Avoid ROLLBACK calls unless it is in an autonomous transaction.}}

   -- should ignore rollback to savepoint
   rollback to savepoint s1;
   rollback to s1;
 end;
end;
/

begin
  commit; -- correct, it isn't a database object
end;
/
