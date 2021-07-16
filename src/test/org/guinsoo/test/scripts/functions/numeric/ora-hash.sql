-- Copyright 2021 Gunsioo Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (https://github.com/ciusji/guinsoo/blob/master/LICENSE.txt).
-- Initial Developer: Gunsioo Group
--

SELECT ORA_HASH(NULL);
>> null

SELECT ORA_HASH(NULL, 0);
>> null

SELECT ORA_HASH(NULL, 0, 0);
>> null

SELECT ORA_HASH(1);
>> 3509391659

SELECT ORA_HASH(1, -1);
> exception INVALID_VALUE_2

SELECT ORA_HASH(1, 0);
>> 0

SELECT ORA_HASH(1, 4294967295);
>> 3509391659

SELECT ORA_HASH(1, 4294967296);
> exception INVALID_VALUE_2

SELECT ORA_HASH(1, 4294967295, -1);
> exception INVALID_VALUE_2

SELECT ORA_HASH(1, 4294967295, 0);
>> 3509391659

SELECT ORA_HASH(1, 4294967295, 10);
>> 2441322222

SELECT ORA_HASH(1, 4294967295, 4294967295);
>> 3501171530

SELECT ORA_HASH(1, 4294967295, 4294967296);
> exception INVALID_VALUE_2

CREATE TABLE TEST(I BINARY(3), B BLOB, S VARCHAR, C CLOB);
> ok

INSERT INTO TEST VALUES (X'010203', X'010203', 'abc', 'abc');
> update count: 1

SELECT ORA_HASH(I) FROM TEST;
>> 2562861693

SELECT ORA_HASH(B) FROM TEST;
>> 2562861693

SELECT ORA_HASH(S) FROM TEST;
>> 1191608682

SELECT ORA_HASH(C) FROM TEST;
>> 1191608682

DROP TABLE TEST;
> ok
