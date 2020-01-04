library "Correlated Query Test"

using QUICK

/* This isn't actually valid, but it's useful for testing */
codesystem "ICD9": 'ICD9CM'

code "Acute systolic heart failure": '428.21' from "ICD9"

context Patient

define _: true