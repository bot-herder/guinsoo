-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

select length(null) en, length('This has 17 chars') e_17;
> EN   E_17
> ---- ----
> null 17
> rows: 1

SELECT LEN(NULL);
> exception FUNCTION_NOT_FOUND_1

SET MODE MSSQLServer;
> ok

select len(null) en, len('MSSQLServer uses the len keyword') e_32;
> EN   E_32
> ---- ----
> null 32
> rows: 1

SELECT LEN('A ');
>> 2

SELECT LEN(CAST('A ' AS CHAR(2)));
>> 1

SET MODE Regular;
> ok
