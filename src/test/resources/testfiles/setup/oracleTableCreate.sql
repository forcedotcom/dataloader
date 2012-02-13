CREATE TABLE INT_ACCOUNTS
(
  SFDC_ACCOUNT_ID  VARCHAR2(64 BYTE),
  PARTY_ID         VARCHAR2(64 BYTE),
  ACCOUNT_SEQ      NUMBER(15)                   NOT NULL,
  ACCOUNT_NAME     VARCHAR2(60 BYTE)            NOT NULL,
  BUSINESS_PHONE   VARCHAR2(20 BYTE),
  ANNUAL_REVENUE   NUMBER                       DEFAULT 0,
  ACCOUNT_EXT_ID   VARCHAR2(20 BYTE)            NOT NULL,
  ACCOUNT_NUMBER   VARCHAR2(20 BYTE)            NOT NULL,
  SYSTEM_MODSTAMP  DATE                         DEFAULT SYSDATE               NOT NULL,
  LAST_UPDATED     DATE
)
TABLESPACE USERS
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          128K
            NEXT             128K
            MINEXTENTS       1
            MAXEXTENTS       2147483645
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOLOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
NOMONITORING;

CREATE SEQUENCE DATALOADER.INT_ACCOUNT_SEQ
START WITH 0
INCREMENT BY 1
MINVALUE 0
MAXVALUE 999999
NOCACHE
CYCLE
NOORDER;


CREATE UNIQUE INDEX INT_ACCOUNTS_EXT_ID_IDX ON INT_ACCOUNTS
(ACCOUNT_EXT_ID)
NOLOGGING
TABLESPACE USERS
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          128K
            NEXT             128K
            MINEXTENTS       1
            MAXEXTENTS       2147483645
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE OR REPLACE TRIGGER INT_ACCOUNT_INSERTED
BEFORE INSERT
ON INT_ACCOUNTS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
DECLARE
tmpVar NUMBER;
/******************************************************************************
   NAME:
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        7/10/2006             1. Created this trigger.

   NOTES:

   Automatically available Auto Replace Keywords:
      Object Name:
      Sysdate:         7/10/2006
      Date and Time:   7/10/2006, 1:06:03 PM, and 7/10/2006 1:06:03 PM
      Username:         (set in TOAD Options, Proc Templates)
      Table Name:       (set in the "New PL/SQL Object" dialog)
      Trigger Options:  (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
   tmpVar := 0;

   SELECT INT_ACCOUNT_SEQ.NEXTVAL INTO tmpVar FROM dual;
   :NEW.ACCOUNT_SEQ := tmpVar;
   IF(:NEW.ACCOUNT_EXT_ID is null) THEN
      :NEW.ACCOUNT_EXT_ID := CONCAT('1-', LPAD(tmpVar, 6, '0'));
   END IF;

   EXCEPTION
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END ;
/
SHOW ERRORS;



CREATE OR REPLACE TRIGGER INT_ACCOUNT_UPDATED
BEFORE INSERT OR UPDATE
ON INT_ACCOUNTS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
BEGIN
   IF(:NEW.Last_Updated is null) THEN
      :NEW.Last_Updated := SYSTIMESTAMP;
   END IF;

   EXCEPTION
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END ;
/
SHOW ERRORS;
