library "Correlated Query Test"

using QUICK

codesystem "ActCodes": 'http://hl7.org/fhir/v3/ActCode'

valueset "Inpatient VS": '77.66.55.44'
valueset "Outpatient VS": '88.77.66.55'

code "Inpatient Encounter": 'IMP' from "ActCodes"
code "Outpatient Encounter": 'AMB' from "ActCodes"

context Patient

define "Simple Case":
  [Encounter: "Inpatient VS"]

define "Filter By Code":
  [Encounter: "Inpatient Encounter"]