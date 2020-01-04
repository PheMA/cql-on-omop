library "Correlated Query Test"

using QUICK

codesystem "ActCodes": 'http://hl7.org/fhir/v3/ActCode'

valueset "HF Dx": '2.16.840.1.999999.2'

code "Inpatient Encounter": 'IMP' from "ActCodes"

context Patient

define "OP Encounter":
  [Encounter: "Outpatient Encounter"]

define "HF Dx":
  [Condition: "HF Dx"]